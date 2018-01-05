package nyhx.fsm

import akka.actor.{FSM, Props}
import models.{ClientRequest, Commands, Point}
import nyhx.{Find, Images, Points}
import org.slf4j.LoggerFactory
import utensil.{IsFindPic, NoFindPic}

case class ExportTask(group: Point, area: Point, map: Point, direct: Option[Point]) {
  override def toString: String = s"${area.name}-${map.name}_group-${group.name}"
}

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

  override def FinishStatus: BaseStatus = Finish

}


class ExportActor(task: ExportTask)
  extends FSM[BaseStatus, BaseData]
    with FsmHelper[BaseStatus, BaseData] {

  object GetReward extends BaseStatus

  object GotoMap extends BaseStatus

  object Check extends BaseStatus

  object CloseX extends BaseStatus

  object StartExport extends BaseStatus

  private val logger = LoggerFactory.getLogger("export")

  def getReward = Props(new ExportGetRewardActor(task.area))

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

  startWith(GetReward, of(getReward))
  when(GetReward)(work(nextStatus = goto(GotoMap).using(of(gotoMap))))
  when(GotoMap)(work(nextStatus = goto(Check).using(NoData)))
  when(Check) {
    case Event(c: ClientRequest, _) =>
      val x = Find(Images.x).run(c)
      val ea = Find(Images.Explore.exitAdventure).run(c)
      ea match {
        case IsFindPic(_) =>
          log.info("exitAdventure:find")
          Build.goto(Finish)
            .using(of(closeX))
            .replying(Commands().delay(1000).tap(x.point).delay(2000))
            .build()
        case NoFindPic()  =>
          log.info(s"exitAdventure:no find")
          Build
            .goto(StartExport)
            .using(of(startExport))
            .replying(Commands())
            .build()
      }
  }
  when(CloseX)(work(nextStatus = goto(Finish)))
  when(StartExport)(work(nextStatus = goto(Finish)))
  when(Finish)(finish)

  override def FinishStatus: BaseStatus = Finish

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

  def run() = {
    val x = NameProps("goToExport", ScenesActor.goToExport()) :: exportList.map(e => NameProps(e.toString, Props(new ExportActor(e))))

    SeqenceActor(x :+ FindActor.waitIsFind(Find(Images.returns)): _*)
  }

}




















