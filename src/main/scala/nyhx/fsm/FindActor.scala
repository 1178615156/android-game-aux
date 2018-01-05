package nyhx.fsm

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.{ClientRequest, Commands, NoFindPicException}
import nyhx.Find
import org.slf4j.{Logger, LoggerFactory}
import utensil.{FindPicBuild, IsFindPic, NoFindPic}
import nyhx.Find._

object FindActor {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)


  trait Status extends BaseStatus

  object Touch extends Status

  object KeepTouch extends Status

  object WaitFind extends Status

  object FailureNoFind extends Status

  object FailureNoSupper extends Status

  object Success extends Status

  trait Condition extends BaseStatus

  object IsFind extends Condition

  object NoFind extends Condition

  object MustFind extends Condition

  object IfFind extends Condition

  object Nothing extends Condition

  def touch[T <: FindPicBuild.Goal](f: Find[T], con: Condition = MustFind) = Props(new FindActor(Touch, con, f))

  def keepTouch[T <: FindPicBuild.Goal](f: Find[T]) = NameProps("keep touch", Props(new FindActor(KeepTouch, Nothing, f)))

  def waitIsFind[T <: FindPicBuild.Goal](f: Find[T]) = NameProps("wait is find", Props(new FindActor(WaitFind, IsFind, f)))

  def waitNoFind[T <: FindPicBuild.Goal](f: Find[T]) = NameProps("wait no find", Props(new FindActor(WaitFind, NoFind, f)))

  def waitOf[T <: FindPicBuild.Goal](condition: Condition, f: Find[T]) = NameProps("wait of", Props(new FindActor(WaitFind, condition, f)))
}

import FindActor._

class FindActor[T <: FindPicBuild.Goal](status: FindActor.Status,
                                        condition: Condition,
                                        findPicBuild: Find[T])
  extends Actor
    with FSM[FindActor.Status, FindActor.Condition] {
  startWith(status, condition)

  when(Touch) {
    case Event(c: ClientRequest, MustFind) =>
      val goal = findPicBuild.values.map(_.goal.get.simpleName)
      findPicBuild.run(c) match {
        case x@NoFindPic()      =>
          log.info(s"($goal) no find;  sim:${x.sim}")
          goto(FailureNoFind).replying(Commands())
        case x@IsFindPic(point) =>
          log.info(s"($goal) is find; touch : sim:${x.sim}")
          goto(Success).replying(Commands().tap(point))
      }
    case Event(c: ClientRequest, IfFind)   =>
      val goal = findPicBuild.values.map(_.goal.get.simpleName)
      findPicBuild.run(c) match {
        case x@NoFindPic()      =>
          log.info(s"($goal) no find;  sim:${x.sim}")
          goto(Success).replying(Commands())
        case x@IsFindPic(point) =>
          log.info(s"($goal) is find; touch : sim:${x.sim}")
          goto(Success).replying(Commands().tap(point))
      }
    case Event(c: ClientRequest, _)        =>
      log.error("no supper")
      goto(FailureNoFind)
  }
  when(KeepTouch) {
    case Event(c: ClientRequest, Nothing) =>
      val goal = findPicBuild.values.map(_.goal.get.simpleName)
      findPicBuild.run(c) match {
        case NoFindPic()      => goto(Success).replying(Commands())
        case IsFindPic(point) =>
          log.info(s"($goal) is find; keep touch")
          stay().replying(Commands().tap(point))
      }
  }

  when(WaitFind) {
    case Event(c: ClientRequest, IsFind) =>
      val goal = findPicBuild.values.map(_.goal.get.simpleName)
      findPicBuild.run(c) match {
        case x@NoFindPic()    =>

          log.info(s"wait find ($goal) no find , re try : sim:${x.sim}")
          stay().replying(Commands())
        case IsFindPic(point) =>
          log.info(s"wait find ($goal) is find , success")
          goto(Success).replying(Commands())
      }
    case Event(c: ClientRequest, NoFind) =>
      findPicBuild.run(c) match {
        case IsFindPic(_) => stay().replying(Commands())
        case NoFindPic()  => goto(Success).replying(Commands())
      }
  }

  def regulator: ActorRef = context.parent

  when(FailureNoFind) {
    case Event(_, _) =>
      log.error("scenes move is failure")
      stay()
  }
  when(FailureNoSupper) {
    case Event(_, _) =>
      log.error("failure no supper")
      stay()
  }
  when(Success) {
    case _ =>
      log.error("scenes move is finish")
      stay()
  }

  def statusToString(s: Status) = s.getClass.getName.replace("$", ".")

  onTransition {
    case x -> FailureNoFind   =>
      log.warning(s"from $x failure to $FailureNoFind")
      regulator ! TaskFailure(NoFindPicException(statusToString(x)))
    case x -> FailureNoSupper => regulator ! TaskFailure(new Exception(x.toString + s" with(${stateData.getClass.getName}) is no supper"))
    case x -> Success         => regulator ! TaskFinish
      log.debug("[finish] : " + (x))
  }
  onTransition {
    case f -> t =>
      log.debug(s"status:${(f)} -> ${(t)}")
  }
}