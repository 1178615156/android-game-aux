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

  def warEarlyEnd() = SeqenceActor(
    FindActor.waitIsFind(Find(Images.returns)),
    FindActor.touch(Find(Images.returns)),
    FindActor.waitIsFind(Find(Images.determine)),
    FindActor.touch(Find(Images.determine)),
    FindActor.waitIsFind(Find(Images.Adventure.grouping))

  )

  def checkMpEmpty() = Props(new MyAct {
    exec { c =>
      val result = Find(Images.Adventure.mpEmpty)(c).withThreshold(0.99).run()
      result match {
        case IsFindPic(point) =>
          logger.warn("mp empty in war;")
          sender() ! Commands()
          context.parent ! TaskFailure(MpEmptyException())
        case NoFindPic()      =>
          logger.info("check mp success;")
          sender() ! Commands()
          context.parent ! TaskFinish
      }
    }
  })

  def checkWarIsStart() = FindActor.keepTouch(Find(Images.start))

  def tapWarPoint(point: Point) = Props(new MyAct {
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
  })

  def waitWarEnd() = Props(new MyFsmAct {
    exec(c => Find(Images.Adventure.totalTurn).run(c) match {
      case IsFindPic(point) =>
        logger.info("war is end")
        Build.goto(Finish).replying(Commands().delay(0)).build()
      case NoFindPic()      =>
        logger.info("war no end")
        Build.stay().replying(Commands()).build()
    })
  })


  def sureWarReward() = Props(new MyAct {
    exec(c => Find(Images.returns).run(c) match {
      case IsFindPic(point) =>
        logger.info("get war reward ; go to next")
        replay(Commands().delay(0))
        finishTask()
      case NoFindPic()      =>
        logger.info("have not get war reward ; try again")
        replay(Commands().tap(Point(1, 1)))
    })
  })


}

































