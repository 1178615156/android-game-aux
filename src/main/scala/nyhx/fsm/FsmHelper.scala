package nyhx.fsm

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.{ClientRequest, Commands, Point}
import utensil.macros.ActorOf


trait FsmHelper[S, D] extends FSM[S, D] with ActorOf {

  def of(props: Props, name: String): ActorRef = context.actorOf(props, name.replace(" ", "-"))

  def of(nameProps: NameProps): ActorRef = nameProps.name match {
    case Some(x) => context.actorOf(nameProps.props, x)
    case None    => context.actorOf(nameProps.props)
  }

  class Build[A](private val state: State) {
    def replying(commands: Commands) =
      new Build[A with Build.Reply](state.replying(commands))

    def using(d: D) =
      new Build[A](state.using(d))
  }

  object Build {

    implicit class WithBuild[A](b: Build[A])(implicit x: A <:< Reply) {
      def build(): State = b.state
    }

    def apply(state: State) = new Build[Nothing](state)

    def stay() = new Build[Nothing](self_.stay())

    def goto(s: S) = new Build[Nothing](self_.goto(s))

    trait Reply

    trait Nothing

  }

  private val self_ = this

  def work(nextStatus: => State): StateFunction = {
    case Event(c: ClientRequest, WorkActor(actorRef)) =>
      actorRef forward c
      stay()
    case Event(TaskFinish, x@WorkActor(actorRef))     =>
      context.stop(actorRef)
      log.info(s"work finish ${stateName}")
      nextStatus
  }

  implicit def actorRef2WorkActor(actorRef: ActorRef): WorkActor = WorkActor(actorRef)

  onTransition {
    case f -> t => log.debug(s"onTransition:$f -> $t")
  }

  def finish: StateFunction = {
    case _ =>
      log.warning("actor finish")
      stay()
  }

  @deprecated("", "")
  def onFinish(F: S): TransitionHandler = {
    case x -> F => context.parent ! TaskFinish
  }

  def FinishStatus: S

  onTransition {
    case x -> f if f == FinishStatus =>
      context.parent ! TaskFinish
    case x -> f if f == Error =>
      context.parent ! TaskFailure
  }

}

