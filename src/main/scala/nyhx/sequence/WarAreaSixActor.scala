package nyhx.sequence

import scala.language.implicitConversions

import akka.actor.Actor
import models._
import nyhx._
import FindAux.findPicBuilding2FindAux
import nyhx.fsm.TaskFinish
import utensil.{IsFindPic, NoFindPic}

class WarAreaSixActor(warNum: Int = 100)
  extends Actor
    with ActorHelper
    with BaseHelper
    with ScenesHelper
    with WarHelper {
  logger.info("create war ara six actor")

  val sequences: Sequence = (Sequence("war")
    next goToAdventure
    next goToWarArea(Points.Area.six, 4)
    repeat(warPoint_B, warNum)
    next end
    )


  def warPoint_B = {
    // goto adventure
    // war point(B)
    // exit war
    (Sequence("warPoint_B")
      next warReady
      next warPoint(Points.Adventure.Six.Four.b)
      next warEnd
      )
  }

  def end = RecAction { implicit c => context.parent ! TaskFinish; Result.Success(Commands()) }

}

class WarAreaThreeOneActor(warNum: Int = 100) extends Actor
  with ActorHelper
  with BaseHelper
  with ScenesHelper
  with WarHelper {


  val sequences: Sequence = (Sequence("war")
    next goToAdventure
    next goToWarArea(Points.Area.three, 1)
    repeat(warPoint_b_e, warNum)
    next end
    )

  def warPoint_b_e = (Sequence("")
    next warReady
    next warPoint(Points.Adventure.AreaThree.One.b)
    next warPoint(Points.Adventure.AreaThree.One.e)
    next warEnd
    )

  def end = RecAction { implicit c =>
    println("WarAreaThreeOneActor end")
    context.parent ! TaskFinish
    Result.End()
  }


}

class WarAreaFiveOneActor(warNum: Int) extends Actor
  with ActorHelper
  with BaseHelper
  with ScenesHelper
  with WarHelper {
  val sequences: Sequence =
  //    Sequence("") next randomPoint(Point(275, 235))
    (Sequence("war")
      next goToAdventure
      next goToWarArea(Points.Area.five, 1)
      repeat(warPoint_f, warNum)
      next end
      )

  def warPoint_f = (Sequence("warPoint_f")
    next warReady
    next warPoint(Points.Adventure.Five.One.b)
    next warPoint(Points.Adventure.Five.One.c)
    next warPoint(Points.Adventure.Five.One.d)
    next randomPoint(Points.Adventure.Five.One.e)
    next warPoint(Points.Adventure.Five.One.f)
    next justDelay(3000)
    next warEnd
    )

  def end = RecAction { implicit c =>
    println("WarAreaThreeOneActor end")
    context.parent ! TaskFinish
    Result.End()
  }
}


class WarAreaThreeSixActor(warNum: Int) extends Actor
  with ActorHelper
  with BaseHelper
  with ScenesHelper
  with WarHelper {
  val sequences: Sequence =
  //    Sequence("") next randomPoint(Point(275, 235))
    (Sequence("war")
      next goToAdventure
      next goToWarArea(Points.Area.three, 6)
      repeat(warPoint_h, warNum)
      next end
      )

  def warPoint_h = (Sequence("warPoint_h")
    next warReady
    next warPoint(Points.Adventure.Three.Six.b)
    next warPoint(Points.Adventure.Three.Six.c)
    next randomPoint(Points.Adventure.Three.Six.g)
    next warPoint(Points.Adventure.Three.Six.h)
    next warEnd
    )

  def end = RecAction { implicit c =>
    println("WarAreaThreeOneActor end")
    context.parent ! WarTaskEnd(self)
    Result.End()
  }
}

class WarAreaTwoSix(warNum: Int) extends Actor
  with ActorHelper
  with BaseHelper
  with ScenesHelper
  with WarHelper {
  val points              = Points.Adventure.Two
  val sequences: Sequence =
  //    Sequence("") next randomPoint(Point(275, 235))
    (Sequence("war")
      next goToAdventure
      next goToWarArea(Points.Area.two, 6)
      repeat(warPoint_f, warNum)
      next end
      )

  def warPoint_f = {
    (Sequence("warPoint_f")
      next warReady
      next warPoint(points.Six.b)
      next warPoint(points.Six.c)
      next randomPoint(points.Six.e)
      next warPoint(points.Six.f)
      next justDelay(3000)
      )
  }

  def end = RecAction { implicit c =>
    println("WarAreaTwoSix end")
    context.parent ! WarTaskEnd(self)
    Result.End()
  }

}

class WarAreaOneThree(warNum: Int) extends Actor
  with ActorHelper
  with BaseHelper
  with ScenesHelper
  with WarHelper {
  val points              = Points.Adventure.One
  val sequences: Sequence =
    (Sequence("war")
      next goToAdventure
      next goToWarArea(Points.Area.one, 3)
      repeat(warPoint_b, warNum)
      next end
      )

  def warPoint_b = {
    (Sequence("warPoint_b")
      next warReady
      next warPoint(points.Three.b)
      next warEnd
      )
  }

  def end = RecAction { implicit c =>
    println("WarAreaOneThree end")
    context.parent ! WarTaskEnd(self)
    Result.End()
  }

}
class WarAreaOneFour(warNum: Int) extends Actor
  with ActorHelper
  with BaseHelper
  with ScenesHelper
  with WarHelper {
  val points              = Points.Adventure.One
  val sequences: Sequence =
    (Sequence("war")
      next goToAdventure
      next goToWarArea(Points.Area.one, 4)
      repeat(warPoint_boos, warNum)
      next end
      )

  def warPoint_boos = {
    (Sequence("warPoint_boos")
      next warReady
      next warPoint(points.Four.b)
      next warPoint(points.Four.c)
      next randomPoint(points.Four.f)
      next warPoint(points.Four.g)
      next warPoint(points.Four.boss)
      next justDelay(2000)
      )
  }

  def end = RecAction { implicit c =>
    println("WarAreaOneFour end")
    context.parent ! WarTaskEnd(self)
    Result.End()
  }

}