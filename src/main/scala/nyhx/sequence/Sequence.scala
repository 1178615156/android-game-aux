package nyhx.sequence

import scala.language.implicitConversions

import akka.actor.ActorRef
import models.ClientRequest

import models._
import org.slf4j.LoggerFactory

trait Action {
  def name = ""
}

object Action {
  implicit def rec2action(rec: RecAction): Rec = Rec(rec)

  implicit def sequ2action(sequence: Sequence): Sequ = Sequ(sequence)

  case class Rec(recAction: RecAction) extends Action

  case class Sequ(sequence: Sequence) extends Action {
    override def name = sequence.name
  }

}

trait Patten

object Patten {

  case class Next(action: Action) extends Patten

  case class Util(action: Action, num: Int) extends Patten

  case class Repeat(action: Action, currNum: Int, maxNum: Int) extends Patten

}

object Sequence {

  case class End(s: String) extends Result {
    override def commands: Commands = Commands()
  }

  import Patten._

  def run(sequence: Sequence)(clientRequest: ClientRequest, sender: ActorRef): Sequence = {
    def execRecAction(recAction: RecAction): Option[Action] = recAction(clientRequest) match {
      case Result.Failure(x)          => throw x
      case Result.Execution(x)        =>
        sender ! x
        Some(Action.Rec(recAction))
      case Result.Success(x)          =>
        sender ! x
        None
      case Result.Become(x, commands) =>
        sender ! commands
        Some(x)

      case Result.End() =>
        sender ! Commands()
        Some(RecAction { implicit c => Result.Execution(Commands()) })
    }

    def runByRec(action: RecAction) = {
      val result = execRecAction(action)
      result match {
        case Some(x) => Sequence(sequence.name, Patten.Next(x) +: sequence.tail)
        case None    => Sequence(sequence.name, sequence.tail)
      }
    }

    def runBySequence(sequ: Sequence) = {
      val result = run(sequ)(clientRequest, sender)
      if(result.isEnd)
        Sequence(sequence.name, sequence.tail)
      else
        Sequence(sequence.name, Patten.Next(result) +: sequence.tail)

    }

    val action = sequence.head
    action match {
      case Next(Action.Rec(action)) => runByRec(action)
      case Next(Action.Sequ(sequ))  => runBySequence(sequ)

      case Util(recAction, 0)               => throw new Exception("")
      case Util(Action.Rec(recAction), num) =>
        execRecAction(recAction) match {
          case Some(x) => Sequence(sequence.name, Patten.Util(x, num - 1) +: sequence.tail)
          case None    => Sequence(sequence.name, sequence.tail)
        }

      case Repeat(action, currNum, maxNum) if currNum == maxNum =>
        run(Sequence(sequence.name, sequence.tail))(clientRequest, sender)
      case Repeat(action, currNum, maxNum)                      =>
        logger.info(s"repeat [${action.name}] $currNum/$maxNum ")
        run(Sequence(sequence.name, Next(action) +: Repeat(action, currNum + 1, maxNum) +: sequence.tail))(clientRequest, sender)
    }
  }

  val logger = LoggerFactory.getLogger("sequence")
}

case class Sequence(name: String, actions: Seq[Patten] = Nil) {

  def next(recAction: Action) = Sequence(name, actions :+ Patten.Next(recAction))

  def util(recAction: RecAction, maxNum: Int) = Sequence(name, actions :+ Patten.Util(recAction, maxNum))

  def repeat(action: Action, num: Int) = Sequence(name, actions :+ Patten.Repeat(action, 0, num))

  val isEnd = actions.isEmpty

  def head = actions.head

  def tail = actions.tail

}