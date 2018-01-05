package nyhx.fsm

import akka.actor.{Actor, ActorLogging, ActorRef, FSM, Props}
import models.{ClientRequest, Commands, Point}


trait MyFsmAct extends FSM[BaseStatus, Any] with FsmHelper[BaseStatus, Any] {

  object Run extends BaseStatus

  def exec(f: ClientRequest => State): Unit = when(Run) {
    case Event(c: ClientRequest, _) => f(c)
  }

  startWith(Run, NoData)

  when(Error) {
    case Event(_, d) =>
      log.error(s"exist error : ${d}")
      stay()
  }
  when(Finish)(finish)

  override def FinishStatus: BaseStatus = Finish


}

trait MyAct extends Actor with ActorLogging {
  def finishTask() = context.parent ! TaskFinish

  def replay(commands: Commands) = sender() ! commands

  def exec(f: ClientRequest => Any): Unit = become {
    case c: ClientRequest => f(c)
  }

  def become(behavior: Actor.Receive): Unit = context.become(behavior)

  override def receive: Receive = Actor.emptyBehavior
}


object SeqenceActor {

  def of(nps: NameProps*) = Props(new MyAct {
    private var workSeq = nps.map(e => e.name.map(s => context.actorOf(e.props, s)).getOrElse(context.actorOf(e.props)))
    context.become {
      case c: ClientRequest =>
        workSeq.head forward c

      case TaskFinish =>
        if(workSeq.tail.nonEmpty) {
          workSeq = workSeq.tail
        } else {
          context.parent ! TaskFinish
          context.become(Actor.emptyBehavior)
        }
    }
  })

  //  def of(props: (Props, String)*) = Props(new MyAct {
  //    private var workSeq = props.map { case (a, n) => context.actorOf(a, n) }
  //    context.become {
  //      case c: ClientRequest =>
  //        workSeq.head forward c
  //
  //      case TaskFinish =>
  //        if(workSeq.tail.nonEmpty) {
  //          workSeq = workSeq.tail
  //        } else {
  //          context.parent ! TaskFinish
  //          context.become(Actor.emptyBehavior)
  //        }
  //    }
  //  })

  def apply(props: NameProps*) = of(props: _*)

  //    Props(new MyAct {
  //    private var workSeq = props.map(context.actorOf)
  //    context.become {
  //      case c: ClientRequest =>
  //        workSeq.head forward c
  //
  //      case TaskFinish =>
  //        if(workSeq.tail.nonEmpty) {
  //          workSeq = workSeq.tail
  //        } else {
  //          context.parent ! TaskFinish
  //          context.become(Actor.emptyBehavior)
  //        }
  //    }
  //  })

}

object JustActor {
  def apply(commands: Commands) = Props(new MyAct {
    context.become {
      case c: ClientRequest =>
        sender() ! commands
        context.parent ! TaskFinish
        context.become(Actor.emptyBehavior)
    }
  })

  def justDelay(time: Int) = NameProps("just delay", apply(Commands().delay(time)))

  def justTap(point: Point) = NameProps("just tap", apply(Commands().tap(point)))

  def save() = NameProps("just-save", Props(new MyFsmAct {
    exec { c =>
      better.files.File(c.image.name).copyTo(
        better.files.File(s"D:\\nyhx\\${System.currentTimeMillis()}.png")
      )
      Build.goto(FinishStatus).replying(Commands().delay(0)).build()
    }
  }))
}


object ConditionActor {

  case class Build(c: NameProps, success: Option[NameProps], failure: Option[NameProps]) {
    def onSuccess(success: NameProps) = this.copy(success = Some(success))

    def onFailure(failure: NameProps) = this.copy(failure = Some(failure))

    def build() = Props(new MyFsmAct {

      object Success extends BaseStatus

      object Failure extends BaseStatus

      startWith(Run, of(c))
      when(Run) {
        case Event(c: ClientRequest, workActor: ActorRef) =>
          workActor forward c
          stay()
        case Event(TaskFinish, workActor: ActorRef)       =>
          context.stop(workActor)
          log.info("condition success ")
          goto(Success).using(WorkActor(of(success.get)))
        case Event(TaskFailure, workActor: ActorRef)      =>
          context.stop(workActor)
          log.info("condition failure ")
          goto(Failure).using(WorkActor(of(failure.get)))
      }
      when(Success)(work(nextStatus = goto(FinishStatus)))
      when(Failure)(work(nextStatus = goto(FinishStatus)))
    })
  }

  def of(nameProps: NameProps) = Build(nameProps, None, None)
}

object ReplaceActor {

  case class WarNum(i: Int, actorRef: ActorRef)

  def apply(totalWarNum: Int, props: NameProps) = Props(new MyFsmAct {


    startWith(Run, WarNum(0, of(props)))
    when(Run) {
      case Event(c: ClientRequest, WarNum(i, actorRef))               =>
        actorRef forward c
        stay()
      case Event(TaskFinish, WarNum(i, actorRef)) if i < totalWarNum  =>
        log.info(s"$i/$totalWarNum - end")
        context.stop(actorRef)
        goto(Run).using(WarNum(i + 1, of(props)))
      case Event(TaskFinish, WarNum(i, actorRef)) if i >= totalWarNum =>
        context.stop(actorRef)
        goto(Finish)
    }
  })
}

