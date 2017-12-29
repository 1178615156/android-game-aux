package nyhx.fsm

import akka.actor.{FSM, Props}
import models.{ClientRequest, Commands, Point}
import nyhx.{Find, Images, Points}
import org.slf4j.LoggerFactory
import utensil.{IsFindPic, NoFindPic}

case class ExportTask(group: Point, area: Point, map: Point, direct: Option[Point])

class ExportGetRewardActor(area: Point) extends FSM[BaseStatus, BaseData]
  with FsmHelper[BaseStatus, BaseData] {

  object GotoArea extends BaseStatus

  object Settlement extends BaseStatus

  object GetPrize extends BaseStatus

  object SureReward extends BaseStatus

  val logger = LoggerFactory.getLogger("export-get-reward")

  startWith(GotoArea, context.actorOf(SeqenceActor(JustActor.justTap(area))))
  when(GotoArea)(work(nextStatus = goto(Settlement).using(NoData)))
  when(Settlement) {
    case Event(c: ClientRequest, _) =>
      Find(Images.Explore.settlement).run(c) match {
        case NoFindPic()      =>
          logger.info("no exit finish task")
          Build.goto(Finish).replying(Commands()).build()
        case IsFindPic(point) =>
          logger.info("exist finish task try to get it ")
          Build
            .goto(GetPrize)
            .replying(Commands().tap(point).delay(2000))
            .using(context.actorOf(FindActor.touch(Find(Images.Explore.getPrize).map(_.withThreshold(0.8)))))
            .build()
      }
  }
  when(GetPrize)(work(nextStatus = goto(SureReward).using(NoData)))
  when(SureReward) {
    case Event(c: ClientRequest, _) =>
      Find(Images.Explore.earnReward).run(c) match {
        case IsFindPic(point) =>
          logger.info("try sure reward")
          Build.stay().replying(Commands().tap(Point(1, 1))).build()
        case NoFindPic()      => Build.goto(Settlement).replying(Commands()).build()
      }
  }
  when(Finish)(finish)
  onTransition(onFinish(Finish))
}

object ExportActor {


  val exportList = List(
    ExportTask(
      Points.Group.b,
      Points.Area.one,
      Points.Explore.Map.three, Some(Points.Explore.OneThreeDirect.one)),
    ExportTask(
      Points.Group.c,
      Points.Area.three,
      Points.Explore.Map.three, Some(Points.Explore.ThreeThreeDirect.two)),
    ExportTask(
      Points.Group.d,
      Points.Area.four,
      Points.Explore.Map.three, Some(Points.Explore.FourThreeDirect.two)),
    ExportTask(
      Points.Group.e,
      Points.Area.five,
      Points.Explore.Map.three, Some(Points.Explore.FiveThreeDirect.two)),
  )

  def run() = SeqenceActor(
    ScenesActor.goToExport() +: exportList.map(e => Props(new ExportActor(e))): _*)

}

class ExportActor(task: ExportTask)
  extends FSM[BaseStatus, BaseData]
    with FsmHelper[BaseStatus, BaseData] {

  object GetReward extends BaseStatus

  object Check extends BaseStatus

  object CloseX extends BaseStatus

  object StartExport extends BaseStatus

  val logger = LoggerFactory.getLogger("export")

  def getReward = SeqenceActor(
    Props(new ExportGetRewardActor(task.area)),
    JustActor.justTap(task.map)
  )

  def closeX = FindActor.keepTouch(Find(Images.x))

  def startExport = SeqenceActor(
    task.direct
      .map(JustActor.justTap)
      .getOrElse(JustActor.justDelay(0)),
    FindActor.waitIsFind(Find(Images.start)),
    JustActor.justTap(task.group),
    FindActor.touch(Find(Images.start))
  )

  startWith(GetReward, context.actorOf(getReward))
  when(GetReward)(work(nextStatus = goto(Check).using(NoData)))
  when(Check) {
    case Event(c: ClientRequest, _) =>
      Find(Images.Explore.exitAdventure).run(c) match {
        case IsFindPic(_) =>
          logger.info("task no finish")
          val x = Find(Images.x).run(c).point
          Build.goto(CloseX)
            .using(context.actorOf(closeX))
            .replying(Commands())
            .build()
        case NoFindPic()  =>
          Build
            .goto(StartExport)
            .using(context.actorOf(startExport))
            .replying(Commands())
            .build()
      }
  }
  when(CloseX)(work(nextStatus = goto(Finish)))
  when(StartExport)(work(nextStatus = goto(Finish)))
  when(Finish)(finish)
  onTransition(onFinish(Finish))
}



























