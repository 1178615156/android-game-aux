package nyhx.fsm

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.{ClientRequest, Commands, NoFindPicException}
import org.slf4j.{Logger, LoggerFactory}
import utensil.{FindPicBuild, IsFindPic, NoFindPic}
import nyhx.Find._

object FindActor {


  trait Status extends BaseStatus

  object Touch extends Status

  object KeepTouch extends Status

  object WaitFind extends Status

  object FailureNoFind extends Status

  object FailureNoSupper extends Status

  object Success extends Status

  trait Condition

  object IsFind extends Condition

  object NoFind extends Condition

  object MustFind extends Condition

  object IfFind extends Condition

  object Nothing extends Condition

  type Func = ClientRequest => FindPicBuild[FindPicBuild.Request]

  def touch(f: Func, condition: Condition = MustFind) = Props(new FindActor(Touch, condition, f))

  def keepTouch(f: Func) = Props(new FindActor(KeepTouch, Nothing, f))

  def waitIsFind(f:Func) =  Props(new FindActor(WaitFind, IsFind, f))
  def waitNoFind(f:Func) =  Props(new FindActor(WaitFind, NoFind, f))
  def waitOf(condition: Condition, f: Func) = Props(new FindActor(WaitFind, condition, f))
}

import FindActor._

class FindActor(status: FindActor.Status,
                condition: Condition,
                findPicBuild: ClientRequest => FindPicBuild[FindPicBuild.Request])
  extends Actor
    with FSM[FindActor.Status, FindActor.Condition] {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  startWith(status, condition)

  when(Touch) {
    case Event(c: ClientRequest, MustFind) =>
      val goal = findPicBuild(c).goal.get.simpleName
      findPicBuild.run(c) match {
        case NoFindPic()      => goto(FailureNoFind).replying(Commands())
        case IsFindPic(point) =>
          logger.info(s"($goal) is find; touch")
          goto(Success).replying(Commands().tap(point))
      }
    case Event(c: ClientRequest, IfFind)   =>
      val goal = findPicBuild(c).goal.get.simpleName
      findPicBuild.run(c) match {
        case NoFindPic()      => goto(Success).replying(Commands())
        case IsFindPic(point) =>
          logger.info(s"($goal) is find; touch")
          goto(Success).replying(Commands().tap(point))
      }
    case Event(c: ClientRequest, _)        =>
      logger.error("no supper")
      goto(FailureNoFind)
  }
  when(KeepTouch) {
    case Event(c: ClientRequest, Nothing) =>
      val goal = findPicBuild(c).goal.get.simpleName
      findPicBuild.run(c) match {
        case NoFindPic()      => goto(Success).replying(Commands())
        case IsFindPic(point) =>
          logger.info(s"($goal) is find; keep touch")
          stay().replying(Commands().tap(point))
      }
  }

  when(WaitFind) {
    case Event(c: ClientRequest, IsFind) =>
      val goal = findPicBuild(c).goal.get.simpleName
      findPicBuild.run(c) match {
        case x@NoFindPic()      =>

          logger.info(s"wait find ($goal) no find , re try : sim:${x.sim }")
          stay().replying(Commands())
        case IsFindPic(point) =>
          logger.info(s"wait find ($goal) is find , success")
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
      logger.error("scenes move is failure")
      stay()
  }
  when(FailureNoSupper) {
    case Event(_, _) =>
      logger.error("failure no supper")
      stay()
  }
  when(Success) {
    case _ =>
      logger.error("scenes move is finish")
      stay()
  }

  def statusToString(s: Status) = s.getClass.getName.replace("$", ".")

  onTransition {
    case x -> FailureNoFind   =>
      logger.warn(s"from $x failure to $FailureNoFind")
      regulator ! TaskFailure(NoFindPicException(statusToString(x)))
    case x -> FailureNoSupper => regulator ! TaskFailure(new Exception(x.toString + s" with(${stateData.getClass.getName}) is no supper"))
    case x -> Success         => regulator ! TaskFinish
      logger.debug("[finish] : " + (x))
  }
  onTransition {
    case f -> t =>
      logger.debug(s"status:${(f)} -> ${(t)}")
  }
}