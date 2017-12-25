package nyhx.sequence

import akka.actor.Actor
import models.ClientRequest
import org.slf4j.LoggerFactory

trait ActorHelper {
  self: Actor =>
  val logger = LoggerFactory.getLogger("war")

  def rec(action: Sequence): PartialFunction[Any, Sequence] = PartialFunction { case c: ClientRequest =>
    Sequence.run(action)(c, sender())
  }

  def onRec(action: Sequence): Receive =
    rec(action).andThen(action => context.become(onRec(action)))

  def receive: Receive = onRec(sequences)

  def sequences: Sequence


  def become(sequence: Sequence): Receive = onRec(sequence)
}
