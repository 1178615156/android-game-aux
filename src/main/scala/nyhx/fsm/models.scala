package nyhx.fsm

import scala.language.implicitConversions

import akka.actor.{ActorRef, Props}


case class NameProps(name: Option[String], props: Props)

object NameProps {
  def apply(name: String, props: Props): NameProps = new NameProps(Some(name.replace(" ", "-")), props)

  implicit def props2nameProps(props: Props): NameProps = NameProps(None, props)
}

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
