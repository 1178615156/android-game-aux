package utensil

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import models.{GoalImage, OriginalImage, Point}


object FindPicBuild {

  trait Image

  trait Original extends Image

  trait Goal extends Image

  trait Nothing extends Image

  type Request = Goal with Original

  def apply() = new FindPicBuild[Nothing](None, None)

  def findPic(originalName: String, goalName: String, patten: String) = {
    val result = PythonScript.eval { jep =>
      val regex = "\\(([0-9|.]+), ?([0-9]+), ?([0-9]+)\\)".r

      jep.getValue(s"jvm_find_pic('$originalName','$goalName','$patten')") match {
        case regex(sim, x, y) => (sim.toDouble, x.toInt, y.toInt)
      }
    }

    val (max, x, y) = Await.result(result, Duration.Inf)
    max -> Point(x, y)
  }

  implicit class WithRun[Arr <: Image](findPicBuild: FindPicBuild[Arr])(implicit x: Arr <:< Original with Goal) {
    def run() = {
      val original = findPicBuild.original.get
      val goal = findPicBuild.goal.get
      val patten = findPicBuild.patten
      val threshold = findPicBuild.threshold
      val originalName = original.name.replaceAll("\\\\", "/")
      val goalName = goal.name.replaceAll("\\\\", "/")

      val (similarity, topLeftPoint) = findPic(originalName, goalName, patten.toString)

      if(similarity > threshold)
        new IsFindPic(topLeftPoint, similarity)
      else
        new NoFindPic(topLeftPoint, similarity)
    }
  }

  object Patten extends Enumeration {
    val Default = Value("default")
    val Edge    = Value("edge")
  }

}

trait FindPicResult {
  def isFind: Boolean = this match {
    case IsFindPic(_) => true
    case NoFindPic()  => false
  }

  def similarity = this match {
    case x@IsFindPic(_) => x.sim
    case x@NoFindPic()  => x.sim
  }

  def noFind = !isFind

  def point = this match {
    case IsFindPic(x) => x
    case _            => ???
  }
}

class IsFindPic(val topLeftPoint: Point, val sim: Double) extends FindPicResult

object IsFindPic {
  def unapply(arg: IsFindPic): Option[Point] = Some(arg.topLeftPoint)
}

class NoFindPic(val topLeftPoint: Point, val sim: Double) extends FindPicResult

object NoFindPic {
  def unapply(arg: NoFindPic) = true
}

class FindPicBuild[+Arr] private(val original: Option[OriginalImage],
                                 val goal: Option[GoalImage],
                                 val threshold: Double = 0.95,
                                 val patten: FindPicBuild.Patten.Value = FindPicBuild.Patten.Default) {

  def withGoal(goal: GoalImage) =
    new FindPicBuild[Arr with FindPicBuild.Goal](goal = Some(goal), original = original, threshold = threshold, patten = patten)

  def withOriginal(original: OriginalImage) =
    new FindPicBuild[Arr with FindPicBuild.Original](goal = goal, original = Some(original), threshold = threshold, patten = patten)

  def withThreshold(threshold: Double) =
    new FindPicBuild[Arr](goal = goal, original = original, threshold = threshold, patten = patten)

  def withPatten(threshold: Double) =
    new FindPicBuild[Arr](goal = goal, original = original, threshold = threshold, patten = patten)

}


//case class FindPic(original: OriginalImage,
//                   goal: GoalImage,
//                   threshold: Double = 0.95,
//                   patten: FindPic.Patten.Value = FindPic.Patten.Default) {
//  lazy val (similarity, topLeftPoint) = {
//    val originalName = original.name.replaceAll("\\\\", "/")
//    val goalName = goal.name.replaceAll("\\\\", "/")
//
//    val result = PythonScript.eval { jep =>
//      val regex = "\\(([0-9|.]+), ?([0-9]+), ?([0-9]+)\\)".r
//
//      jep.getValue(s"jvm_find_pic('$originalName','$goalName','$patten')") match {
//        case regex(sim, x, y) => (sim.toDouble, x.toInt, y.toInt)
//      }
//    }
//
//    val (max, x, y) = Await.result(result, Duration.Inf)
//    max -> Point(x, y)
//  }
//
//  def point: Option[Point] = if(isFind) Some(topLeftPoint) else None
//
//  def isFind: Boolean = similarity >= threshold
//
//  def noFind: Boolean = !isFind
//}

