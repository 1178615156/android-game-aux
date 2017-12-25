package nyhx.sequence

import scala.language.implicitConversions

import akka.actor.Actor
import models._
import nyhx.Images
import nyhx.sequence.FindAux.findPicBuilding2FindAux
import utensil.{IsFindPic, NoFindPic}

class DismissedActor extends Actor
  with ActorHelper
  with BaseHelper
  with ScenesHelper {
  val sequences = (Sequence("dismissed")
    next touchReturns
    next goToGruen
    next FindAux(Images.YuanZiWu.yuanZiWu.toGoal).touch
    next justDelay(1000)
    next FindAux(Images.YuanZiWu.dismissed.toGoal).touch
    repeat(execDismissed, 10)
    )

  def execDismissed: Sequence = (Sequence("exec dismissed")
    next FindAux(Images.YuanZiWu.selectStudent.toGoal).touch
    next FindAux(Images.Retrieve.retrieve.toGoal).touch
    next FindAux(Images.Retrieve.an.toGoal).touch
    next FindAux(Images.Retrieve.shui.toGoal).touch
    next justTap(Point(1, 1), 1000)
    next selectStudent
    next checkNeedDismissed
    next FindAux(Images.YuanZiWu.dismissedDetermine.toGoal).touch
    next justTap(Point(1, 1), 1000)
    )

  def selectStudent = RecAction { implicit c =>
    val points = 0 to 5 map (_ * 175 + 65) map (x => Point(x, 179))
    val commands = points.foldLeft(Commands())((l, r) =>
      l.tap(r).delay(200)
    )
    val result = FindAux(Images.lv1.toGoal)(c).run()
    if(result.noFind)
      Result.Success()
    else {
      Result.Success(commands)
    }
  }

  def checkNeedDismissed = RecAction { implicit c =>
    val result = FindAux(Images.YuanZiWu.dismissedSelectStudentDetermine.toGoal)(c).run()
    result match {
      case IsFindPic(point) =>
        Result.Success(Commands().tap(point))
      case NoFindPic()      =>
        Result.Become(end)
    }
  }


  def end = RecAction { implicit c =>
    context.parent ! DismissedTaskFinish(self)
    println("dismissed  end")
    Result.End()
  }
}
