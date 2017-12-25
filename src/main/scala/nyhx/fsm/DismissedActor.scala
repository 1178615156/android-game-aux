package nyhx.fsm

import akka.actor.{FSM, Props}
import models.{ClientRequest, Commands, Point}
import nyhx.fsm.FindActor.IsFind
import nyhx.{Find, Images}
import Find.FindPicBuildingWithRun
import utensil.{IsFindPic, NoFindPic}

object DismissedActor {

  type Status = BaseStatus

  object SelectStudent extends Status

  object TouchSelect extends Status

  object TouchRetrieve extends Status

  object SureRetrieve extends Status

  object TapStudent extends Status


  object Move extends Status

  object DismissedSelectDetermine extends Status

  object Determine extends Status

  type Data = BaseData
}

import DismissedActor._

/**
  * goto gruen
  * touch yzw
  * touch dismissed
  * touch select student
  * select student
  * find determine
  * if is find then
  * touch it
  * touch determine sure
  * goto select student
  * else
  * return
  */
class DismissedActor extends FSM[Status, Data] with FsmHelper[Status, Data] {

  def moveActors() = context actorOf SeqenceActor(
    ScenesActor.returns,
    ScenesActor.goToGruen,
    FindActor.touch(Find(Images.YuanZiWu.yuanZiWu)),

    JustActor.justDelay(3000),

    FindActor.waitOf(FindActor.IsFind, Find(Images.YuanZiWu.dismissed)),
    FindActor.touch(Find(Images.YuanZiWu.dismissed)),

  )

  def dismissedSelectActor() =
    context actorOf Props(new DismissedSelectActor)


  def dismissedDetermineActor() = context actorOf SeqenceActor(
    FindActor.touch(Find(Images.determine), FindActor.IfFind),
    FindActor.touch(Find(Images.YuanZiWu.dismissedDetermine)),
    JustActor.justTap(Point(1, 1)),
    JustActor.justTap(Point(1, 1)),
  )

  startWith(Move, moveActors())
  when(Move)(work(nextStatus = goto(SelectStudent).using(dismissedSelectActor())))
  when(SelectStudent)(work(goto(DismissedSelectDetermine).using(NoData)))
  when(DismissedSelectDetermine) {
    case Event(c: ClientRequest, _) =>
      val result = Find(Images.YuanZiWu.dismissedSelectStudentDetermine).run(c)
      log.info(s"exist need dismissed (${result.isFind})")
      if(result.isFind)
        Build
          .goto(Determine).using(dismissedDetermineActor())
          .replying(Commands().tap(result.point))
          .build()
      else
        Build
          .goto(Finish).using(NoData)
          .replying(Commands())
          .build()
  }
  when(Determine)(work(nextStatus = goto(SelectStudent).using(dismissedSelectActor())))
  when(Finish)(finish)
  onTransition (onFinish(Finish))
}

class DismissedSelectActor extends FSM[Status, Data] with FsmHelper[Status, Data] {

  def touchSelect() = context actorOf SeqenceActor(
    FindActor.waitOf(IsFind, Find(Images.YuanZiWu.selectStudent)),
    FindActor.touch(Find(Images.YuanZiWu.selectStudent))
  )

  def touchRetrieve() = context actorOf SeqenceActor(
    FindActor.waitOf(IsFind, Find(Images.Retrieve.retrieve)),
    FindActor.touch(Find(Images.Retrieve.retrieve)),

    FindActor.waitOf(IsFind, Find(Images.Retrieve.an)),
    FindActor.touch(Find(Images.Retrieve.an)),

    FindActor.waitOf(IsFind, Find(Images.Retrieve.shui)),
    FindActor.touch(Find(Images.Retrieve.shui))
  )

  startWith(TouchSelect, touchSelect())
  when(TouchSelect)(work(nextStatus = goto(TouchRetrieve).using(touchRetrieve())))
  when(TouchRetrieve)(work(nextStatus = goto(SureRetrieve).using(NoData)))
  when(SureRetrieve) {
    case Event(c: ClientRequest, _) =>
      Find(Images.Retrieve.attributes).run(c) match {
        case IsFindPic(point) => Build.stay().replying(Commands().tap(Point(1, 1))).build()
        case NoFindPic()      => Build.goto(TapStudent).replying(Commands().delay(0)).build()
      }
  }
  when(TapStudent) {
    case Event(c: ClientRequest, _) =>
      val points = 0 to 4 map (_ * 150 + 65) map (x => Point(x, 179))
      val commands = points.foldLeft(Commands())((l, r) =>
        l.tap(r).delay(500)
      )
      Build.goto(Finish).replying(commands).build()
  }

  when(Finish)(finish)
  onTransition (onFinish(Finish))
}



































