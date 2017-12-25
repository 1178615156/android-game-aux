package nyhx.sequence

import akka.actor.ActorRef
import models.{ClientRequest, Commands}

trait Result {
  def commands: Commands
}

object Result {

  trait Complete extends Result

  trait Continue extends Result

  case class Success(commands: Commands = Commands()) extends Complete

  case class Failure(exception: Exception) extends Complete {
    override def commands: Commands = throw exception
  }

  case class Execution(commands: Commands) extends Continue

  case class Become(f: RecAction, commands: Commands = Commands()) extends Continue

  case class End() extends Result {
    override def commands: Commands = Commands()
  }

}

trait RecAction extends (ClientRequest => Result)

object RecAction {
  def apply(f: ClientRequest => Result): RecAction = (v1: ClientRequest) => f(v1)
}

case class DismissedTaskFinish(actorRef: ActorRef)

case class WarTaskEnd(actorRef: ActorRef)