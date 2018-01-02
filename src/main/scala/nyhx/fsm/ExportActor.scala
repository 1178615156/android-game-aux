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
            .using(context.actorOf(SeqenceActor(
              FindActor.keepTouch(Find(Images.Explore.settlement)),
              FindActor.touch(Find(Images.Explore.getPrize).map(_.withThreshold(0.5))),
              FindActor.waitIsFind(Find(Images.Explore.earnReward))
            )))
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


class ExportActor(task: ExportTask)
  extends FSM[BaseStatus, BaseData]
    with FsmHelper[BaseStatus, BaseData] {

  object GetReward extends BaseStatus

  object GotoMap extends BaseStatus

  object Check extends BaseStatus

  object CloseX extends BaseStatus

  object StartExport extends BaseStatus

  val logger = LoggerFactory.getLogger("export")

  def getReward = SeqenceActor(
    Props(new ExportGetRewardActor(task.area)),
    //    JustActor.justTap(task.map),
    //    JustActor.justDelay(3000)
  )

  def closeX = SeqenceActor(
    FindActor.keepTouch(Find(Images.x)),
    FindActor.waitIsFind(Find(Images.returns))
  )

  def startExport = SeqenceActor(
    task.direct
      .map(JustActor.justTap)
      .getOrElse(JustActor.justDelay(0)),
    FindActor.waitIsFind(Find(Images.start)),
    JustActor.justTap(task.group),
    FindActor.touch(Find(Images.start))
  )

  def gotoMap = SeqenceActor(
    JustActor.justTap(task.map),
    FindActor.waitIsFind(Find(Images.Explore.goalPoint).map(_.withThreshold(0.85)) or Find(Images.x))
  )

  startWith(GetReward, of(getReward, "get reward"))
  when(GetReward)(work(nextStatus = goto(GotoMap).using(of(gotoMap, "go to map"))))
  when(GotoMap)(work(nextStatus = goto(Check).using(NoData)))
  when(Check) {
    case Event(c: ClientRequest, _) =>
      val x = Find(Images.x).run(c)
      val ea = Find(Images.Explore.exitAdventure).run(c)
      ea match {
        case IsFindPic(_) =>
          logger.info("exitAdventure:find")
          Build.goto(Finish)
            .using(context.actorOf(closeX))
            .replying(Commands().delay(1000).tap(x.point).delay(2000))
            .build()
        case NoFindPic()  =>
          logger.info(s"exitAdventure:no find")
          Build
            .goto(StartExport)
            .using(context.actorOf(startExport))
            .replying(Commands())
            .build()
      }
    //      (r -> ea) match {
    //        case (x@IsFindPic(_), x1@NoFindPic()) =>
    //          logger.info(s"exitAdventure:no find(${x1.sim}) return: is find (${x.sim})")
    //          Build
    //            .goto(StartExport)
    //            .using(context.actorOf(startExport))
    //            .replying(Commands())
    //            .build()
    //        case (NoFindPic(), IsFindPic(_))      =>
    //          logger.info("exitAdventure:find")
    //          Build.goto(Finish)
    //            .using(context.actorOf(closeX))
    //            .replying(Commands().delay(1000).tap(x.point).delay(2000))
    //            .build()
    //        case (NoFindPic(), NoFindPic())       =>
    //          logger.info("exitAdventure:no find ; return :no find")
    //          Build.stay().replying(Commands()).build()
    //      }
  }
  when(CloseX)(work(nextStatus = goto(Finish)))
  when(StartExport)(work(nextStatus = goto(Finish)))
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




















