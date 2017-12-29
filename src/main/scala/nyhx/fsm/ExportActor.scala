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

  startWith(GotoArea, context.actorOf(SeqenceActor(
//    ScenesActor.goToExport(),
    JustActor.justTap(area))))
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

class ExportActor {
  val exportList = List(
    //    ExportTask(Points.Group.b,Points.Area.one,)
  )
  ScenesActor.goToExport()

}
