package nyhx.fsm

import akka.actor.{ActorRef, FSM, Props}
import models.{ClientRequest, Commands, Point}
import nyhx.{Find, Images}
import org.slf4j.LoggerFactory
import utensil.{IsFindPic, NoFindPic}


object WdjActor {

  type Status = BaseStatus

  object Move extends Status

  object War extends Status

  object StartWar extends Status

  object WaitWarEnd extends Status

  object SureWarResult extends Status

  type Data = BaseData

  case class WarNum(num: Int, actorRef: ActorRef) extends Data

}

import nyhx.fsm.WdjActor._

class WdjWarActor extends FSM[Status, Data]
  with FsmHelper[Status, Data] {

  import context.actorOf


  val logger = LoggerFactory.getLogger("wdj-war")

  def startWar() = WorkActor(actorOf(FindActor.keepTouch(Find(Images.Wdj.matchBattle))))

  def waitWarEnd() = WorkActor(actorOf(FindActor.waitOf(FindActor.IsFind, Find(Images.Wdj.fightResult))))

  def sureWarResult(): StateFunction = {
    case Event(c: ClientRequest, _) =>
      Find(Images.Wdj.fightResult)(c).run() match {
        case IsFindPic(point) =>
          logger.info("sure war result")
          Build.stay().replying(Commands().tap(Point(1, 1))).build()
        case NoFindPic()      =>
          logger.info("war end")
          context.parent ! TaskFinish
          Build.goto(Finish).replying(Commands()).build()
      }
  }

  startWith(StartWar, startWar())
  when(StartWar)(work(nextStatus = goto(WaitWarEnd).using(waitWarEnd())))
  when(WaitWarEnd)(work(nextStatus = goto(SureWarResult).using(NoData)))
  when(SureWarResult)(sureWarResult())
  when(Finish) { case _ => logger.info("finish"); stay() }

}

class WdjActor(totalWarNum: Int = 10) extends FSM[Status, Data] with FsmHelper[Status, Data] {

  import context.actorOf

  def moveActors() = context.actorOf(SeqenceActor(
    (ScenesActor.returns),
    (ScenesActor.goToRoom),
    (FindActor.touch(Find(Images.Wdj.wuDouJi))),
    (FindActor.waitOf(FindActor.IsFind, Find(Images.returns))),
    (FindActor.touch(Find(Images.Wdj.shenShen))),
  ))

  def warActor() = actorOf(Props(new WdjWarActor))


  startWith(Move, moveActors())
  when(Move)(work(nextStatus = goto(War).using(WarNum(0, warActor()))))
  when(War) {
    case Event(c: ClientRequest, WarNum(i, actorRef))               =>
      actorRef forward c
      stay()
    case Event(TaskFinish, WarNum(i, actorRef)) if i < totalWarNum  =>
      logger.info(s"$i/$totalWarNum - end")
      context.stop(actorRef)
      goto(War).using(WarNum(i + 1, warActor()))
    case Event(TaskFinish, WarNum(i, actorRef)) if i >= totalWarNum =>
      context.stop(actorRef)
      goto(Finish)
  }

  when(Finish) { case _ =>
    logger.info("finish")
    stay()
  }


  val logger = LoggerFactory.getLogger("wdj")
}
