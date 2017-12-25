package nyhx.fsm

import akka.actor.ActorRef






trait TaskFinish

object TaskFinish extends TaskFinish

case class TaskFailure(exception: Exception) extends TaskFinish


trait BaseStatus {
  def statusToString(s: AnyRef) = s.getClass.getName.replace("$", ".")

  override def toString: String = statusToString(this)
}

object Finish extends BaseStatus

object Error extends BaseStatus


trait BaseData

object UnInit extends BaseData

object NoData extends BaseData

case class WorkActor(actorRef: ActorRef) extends BaseData
