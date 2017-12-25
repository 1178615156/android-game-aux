package nyhx.sequence

import scala.language.implicitConversions

import models._
import nyhx.Images
import org.slf4j.LoggerFactory
import utensil.{FindPicBuild, FindPicResult, IsFindPic, NoFindPic}

object FindAux {
  val returns           = FindAux(Images.returns.toGoal)
  val goToRoom          = FindAux(Images.returns_room.toGoal)
  val goToGakuen        = FindAux(Images.returns_gakuen.toGoal)
  val adventure         = FindAux(Images.Adventure.adventure.toGoal)
  val grouping          = FindAux(Images.Adventure.grouping.toGoal)
  val start             = FindAux(Images.start.toGoal)
  val totalTurn         = FindAux(Images.Adventure.totalTurn.toGoal)
  val mpEmpty           = FindAux(Images.Adventure.mpEmpty.toGoal)
  val navigateCondition = FindAux(Images.Adventure.navigateCondition.toGoal)
  val determine         = FindAux(Images.determine.toGoal)

  def find(image: GoalImage) = (clientRequest: ClientRequest) => FindPicBuild()
    .withGoal(image.toGoal)
    .withOriginal(clientRequest.image.toOriginal)

  def apply(image: Image) = find(image.toGoal)

  implicit def findPicBuilding2FindAux[X <: FindPicBuild.Request](f: ClientRequest => FindPicBuild[X]): FindAux = new FindAux(f)

  class FindAux(f: ClientRequest => FindPicBuild[FindPicBuild.Request]) {
    val logger = LoggerFactory.getLogger("find-aux")

    //常用的模式之一 if find then touch else ???
    def touch = RecAction { implicit c =>
      val findPicBuild = f(c)
      val result = findPicBuild.run()
      val name = findPicBuild.goal.get.simpleName
      logger.info(s"find $name : (${result.isFind})")
      result match {
        case IsFindPic(point) => Result.Success(Commands().tap(point))
        case NoFindPic()      => Result.Failure(NoFindPicException(name))
      }
    }

    //常用的模式之一 if no find then continue else goto next
    def waitFind = RecAction { implicit c =>
      val findPicBuild = f(c)
      val result = findPicBuild.run()
      val name = findPicBuild.goal.get.simpleName
      logger.info(s"wait find $name : (${result.isFind})")
      result match {
        case IsFindPic(point) => Result.Success(Commands().delay(100))
        case NoFindPic()      => Result.Execution(Commands())
      }
    }

  }

}

