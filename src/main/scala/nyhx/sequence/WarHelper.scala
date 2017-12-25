package nyhx.sequence

import java.util.UUID

import akka.actor.ActorContext
import models._
import nyhx.sequence.FindAux.findPicBuilding2FindAux
import nyhx.{Images, Points}
import utensil.{IsFindPic, NoFindPic}

trait WarHelper {
  this: ScenesHelper with BaseHelper =>
  /**
    * tap result
    * go to room
    * find adventure in room
    * tap adventure
    * -- end
    */
  def goToAdventure = (Sequence("goToAdventure")
    next touchReturns
    next goToRoom
    next mustFind(FindAux.adventure(_))
    next FindAux.adventure.touch
    util(FindAux.grouping.waitFind, 10)
    )

  // tap grouping
  // check mp
  // tap start
  def warReady(implicit context: ActorContext) = (Sequence("warReady")
    next FindAux.grouping.touch
    next checkMpEmpty
    next FindAux.start.touch
    next checkWarIsStart
    )

  def checkWarIsStart(implicit context: ActorContext) = RecAction { implicit c =>
    FindAux.start(c).run() match {
      case IsFindPic(point) =>
        println("start war failure")
        context.parent ! WarTaskEnd(context.self)
        Result.End()
      case NoFindPic()      =>
        Result.Success()
    }
  }


  // tap point
  // tap start
  // wait war end
  // sure reward
  def warPoint(point: Point) = (Sequence("warPoint")
    next FindAux.navigateCondition.waitFind
    next justTap(point, 2000)
    next FindAux.start.waitFind
    next FindAux.start.touch
    next waitWarEnd
    next sureWarReward
    )

  def randomPoint(point: Point) = (Sequence(s"random point :${point.name}")
    next FindAux.navigateCondition.waitFind
    next justTap(point, 2000)
    next screenSave
    next FindAux(Images.Adventure.selectA.toGoal).waitFind
    next randomSelect
    next justTap(Point(1, 1), 500)
    next justTap(Point(1, 1), 500)
    )

  def randomSelect = RecAction { implicit c =>
    val a = FindAux(Images.Adventure.needSurvey.toGoal).andThen(_.withThreshold(0.93))(c).run()
    println(a.isFind)
    val backup = FindAux(Images.Adventure.selectA.toGoal).touch
    if(a.isFind) Result.Success(Commands().tap(a.point))
    else backup(c)
  }

  def screenSave = RecAction { implicit x =>
    better.files.File(x.image.name).copyTo(
      better.files.File(s"D:\\random\\${UUID.randomUUID().toString}.png")
    )
    Result.Success(Commands().delay(10))

  }

  def warEnd = (Sequence("warEnd")
    next FindAux.returns.waitFind
    next FindAux.returns.touch
    next FindAux.determine.waitFind
    next FindAux.determine.touch
    util(FindAux.grouping.waitFind, 10)
    )

  def waitWarEnd = FindAux.totalTurn.waitFind

  def goToWarArea(area: Point, zone: Int) = RecAction { implicit c =>
    val toArea = Commands()
      .tap(Points.Area.one).delay(1000)
      .tap(area).delay(1000)
    Result.Success(
      (1 until zone).foldLeft(toArea)((l, r) => l.tap(Points.Adventure.next).delay(1000))
    )
  }

  def checkMpEmpty = RecAction { implicit c =>
    val result = FindAux.mpEmpty(c).withThreshold(0.99).run()
    result match {
      case IsFindPic(point) =>
        logger.warn("mp empty in war;")
        Result.Failure(MpEmptyException())
      case NoFindPic()      =>
        logger.warn("check mp success;")
        Result.Success(Commands())
    }
  }

  def sureWarReward = RecAction { implicit c =>
    val result = FindAux.returns(c).run()

    result match {
      case IsFindPic(point) =>
        logger.info("get war reward ; go to next")
        Result.Success(Commands().delay(100))

      case NoFindPic() =>
        logger.info("have not get war reward ; try again")
        Result.Execution(Commands().tap(Point(0, 0)))
    }
  }

}
