package nyhx.sequence


import models._
import nyhx.Images
import org.slf4j.Logger
import utensil.{IsFindPic, NoFindPic}


trait ScenesHelper {
  def logger: Logger


  def goToRoom = RecAction { implicit clientRequest =>
    if(FindAux.adventure(clientRequest).run().isFind)
      Result.Success()
    else
      FindAux.goToRoom(clientRequest).run() match {
        case IsFindPic(point) => Result.Execution(Commands().tap(point))
        case NoFindPic()      => Result.Success()
      }
  }

  def goToGruen = RecAction { implicit clientRequest =>
    if(FindAux.goToRoom(clientRequest).run().isFind)
      Result.Success()
    else
      FindAux.goToGakuen(clientRequest).run() match {
        case IsFindPic(point) => Result.Execution(Commands().tap(point))
        case NoFindPic()      => Result.Success()
      }
  }

  def touchReturns = RecAction { implicit clientRequest =>
    val result = FindAux(Images.returns.toGoal)(clientRequest).run()
    logger.info(s"find return :${result.isFind}")
    result match {
      case IsFindPic(point) => Result.Execution(Commands().tap(point))
      case NoFindPic()      => Result.Success()
    }
  }


}
