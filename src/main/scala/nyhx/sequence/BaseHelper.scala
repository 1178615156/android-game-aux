package nyhx.sequence

import models._
import utensil.{FindPicBuild, IsFindPic, NoFindPic}

trait BaseHelper{
  def justTap(point: Point, delay: Int) =
    RecAction(e => Result.Success(Commands().tap(point).delay(delay)))

  def justDelay(delay: Int) =
    RecAction(e => Result.Success(Commands().delay(delay)))

  def mustFind[T <: FindPicBuild.Request](f: ClientRequest => FindPicBuild[T]) = RecAction { implicit clientRequest =>
    val findPicBuild = f(clientRequest)
    findPicBuild.run() match {
      case IsFindPic(_) => Result.Success()
      case NoFindPic()  => Result.Failure(NoFindPicException(findPicBuild.goal.get.simpleName))
    }
  }

}
