package nyhx.fsm

import akka.actor.{Actor, ActorRef, FSM, Props}
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
      sender() ! Commands()
      stay()
  }
  when(Finish)(finish)

  override def FinishStatus: BaseStatus = Finish


}

trait MyAct extends Actor {
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

  def apply(props: Props*): Props = Props(new MyAct {
    private var workSeq = props.map(context.actorOf)
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

  def justDelay(time: Int): Props = apply(Commands().delay(time))

  def justTap(point: Point) = apply(Commands().tap(point))

  def save() = NameProps("just-save", Props(new MyFsmAct {
    exec { c =>
      better.files.File(c.image.name).copyTo(
        better.files.File(s"D:\\nyhx\\${System.currentTimeMillis()}.png")
      )
      Build.goto(FinishStatus).replying(Commands().delay(0)).build()
    }
  }))
}

object ReplaceActor {

  case class WarNum(i: Int, actorRef: ActorRef)

  def apply(totalWarNum: Int, props: Props) = Props(new MyFsmAct {


    startWith(Run, WarNum(0, context.actorOf(props)))
    when(Run) {
      case Event(c: ClientRequest, WarNum(i, actorRef))               =>
        actorRef forward c
        stay()
      case Event(TaskFinish, WarNum(i, actorRef)) if i < totalWarNum  =>
        log.info(s"$i/$totalWarNum - end")
        context.stop(actorRef)
        goto(Run).using(WarNum(i + 1, context actorOf props))
      case Event(TaskFinish, WarNum(i, actorRef)) if i >= totalWarNum =>
        context.stop(actorRef)
        goto(Finish)
    }
  })
}