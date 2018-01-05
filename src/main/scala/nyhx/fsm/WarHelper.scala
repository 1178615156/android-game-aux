package nyhx.fsm

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.{ClientRequest, Commands, MpEmptyException, Point}
import nyhx.{Find, Images, Points}
import Find.FindPicBuildingWithRun
import org.slf4j.LoggerFactory
import utensil.{IsFindPic, NoFindPic}

object WarHelper {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def goToAdventure() = SeqenceActor(
    ScenesActor.returns,
    ScenesActor.goToRoom,
    FindActor.touch(Find(Images.Adventure.adventure)),
    FindActor.waitOf(FindActor.IsFind, Find(Images.Adventure.grouping)),
  )

  def goToWarArea(area: Point, zone: Int): Props = {
    val toArea = Commands()
      .tap(Points.Area.one).delay(1000)
      .tap(area).delay(1000)
    val result =
      (1 until zone).foldLeft(toArea)((l, r) => l.tap(Points.Adventure.next).delay(1000))
    JustActor(result)
  }

  def warReady() = SeqenceActor(
    FindActor.waitIsFind(Find(Images.Adventure.grouping)),
    FindActor.touch(Find(Images.Adventure.grouping)),
    checkMpEmpty(),
    FindActor.keepTouch(Find(Images.start))
  )


  def warPoint(point: Point) = SeqenceActor(
    FindActor.waitOf(FindActor.IsFind, Find(Images.Adventure.navigateCondition)),
    tapWarPoint(point),
    FindActor.waitIsFind(Find(Images.start)),
    FindActor.keepTouch(Find(Images.start)),
    waitWarEnd(),
    sureWarReward()
  )

  def randomPoint(point: Point) =
    NameProps("random point", SeqenceActor(
      FindActor.waitOf(FindActor.IsFind, Find(Images.Adventure.navigateCondition)),
      tapWarPoint(point),
      FindActor.waitIsFind(Find(Images.Adventure.selectA)),
      JustActor.save(),
      FindActor.keepTouch(Find(Images.Adventure.selectA)),
      Props(new MyFsmAct {
        exec(c => (Find(Images.Adventure.navigateCondition) or Find(Images.returns) or Find(Images.start)).run(c) match {
          case IsFindPic(point) => goto(Finish).replying(Commands().delay(0))
          case NoFindPic()      => stay().replying(Commands().tap(Point(1, 1)))
        })
      })
    ))

  def randomPointCheck() =
    ConditionActor.of(
      NameProps("check status", Props(new MyFsmAct {
        exec(c => Find(Images.Adventure.navigateCondition).run(c) -> Find(Images.start).run(c) match {
          case (IsFindPic(_), NoFindPic()) => Build.goto(Finish).using(NoData).replying(Commands()).build()
          case (NoFindPic(), IsFindPic(_)) => Build.goto(Error).using(NoData).replying(Commands()).build()
          case (NoFindPic(), NoFindPic())  => Build.stay().replying(Commands()).build()
        })
      }))
    )


  def warEarlyEnd() = NameProps("war early end", SeqenceActor(
    FindActor.waitIsFind(Find(Images.returns)),
    FindActor.touch(Find(Images.returns)),
    FindActor.waitIsFind(Find(Images.determine)),
    FindActor.touch(Find(Images.determine)),
    FindActor.waitIsFind(Find(Images.Adventure.grouping))
  ))

  def checkMpEmpty() = Props(new MyAct {
    exec { c =>
      val result = Find(Images.Adventure.mpEmpty).map(_.withThreshold(0.99)).run(c)
      result match {
        case IsFindPic(point) =>
          log.warning("mp empty in war;")
          sender() ! Commands()
          context.parent ! TaskFailure(MpEmptyException())
        case NoFindPic()      =>
          log.info("check mp success;")
          sender() ! Commands()
          context.parent ! TaskFinish
      }
    }
  })

  def checkWarIsStart() = FindActor.keepTouch(Find(Images.start))

  def tapWarPoint(point: Point) = NameProps("tap war point", Props(new MyAct {
    exec(c =>
      Find(Images.start).run(c) match {
        case NoFindPic()  =>
          sender() ! Commands().tap(point).delay(2000)
          context.parent ! TaskFinish
        case IsFindPic(_) =>
          sender() ! Commands()
          context.parent ! TaskFinish
      }
    )
  }))

  def waitWarEnd() = Props(new MyFsmAct {
    exec(c => (Find(Images.Adventure.totalTurn) or Find(Images.determine)).run(c) match {
      case IsFindPic(point) =>
        log.info("war is end")
        Build.goto(Finish).replying(Commands().tap(point).delay(0)).build()
      case NoFindPic()      =>
        log.info("war no end")
        Build.stay().replying(Commands()).build()
    })
  })


  def sureWarReward() = Props(new MyAct {
    exec(c => Find(Images.returns).run(c) match {
      case IsFindPic(point) =>
        log.info("get war reward ; go to next")
        replay(Commands().delay(0))
        finishTask()
      case NoFindPic()      =>
        log.info("have not get war reward ; try again")
        replay(Commands().tap(Point(1, 1)))
    })
  })


}

































