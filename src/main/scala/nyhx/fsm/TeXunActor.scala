package nyhx.fsm

import akka.actor.{FSM, Props}
import models.{ClientRequest, Commands, Point}
import nyhx.Find.FindPicBuildingWithRun
import nyhx.{Find, Images, Points}
import org.slf4j.LoggerFactory
import utensil.{FindPicBuild, IsFindPic, NoFindPic}

object TeXunActor {
  type Status = BaseStatus
  type Data = BaseData

  object StartTx extends Status

  object StartCheck extends Status

  object WarPoint extends Status

  object War extends Status

  object TakeResult extends Status

}

import nyhx.fsm.TeXunActor._

class TeXunActor() extends FSM[BaseStatus, BaseData] with FsmHelper[BaseStatus, BaseData] {
  val logger    = LoggerFactory.getLogger(this.getClass)
  val findStart =
    (Find.build(Images.start.toGoal) or Find.build(Images.start2.toGoal)).map(_.withThreshold(0.8))


  startWith(StartTx, context.actorOf(startTx()))

  when(StartTx)(work(nextStatus = goto(StartCheck).using(NoData)))
  when(StartCheck) {
    case Event(c: ClientRequest, _) =>
      findStart.run(c) match {
        case IsFindPic(point) =>
          logger.info("find start -> do war point")
          Build.goto(WarPoint)
            .using(context.actorOf(warPoint()))
            .replying(Commands().delay(0))
            .build()
        case x@NoFindPic()    =>
          logger.info(s"no find start -> do war ${x.sim}")
          Build.goto(TeXunActor.War)
            .using(NoData)
            .replying(Commands().delay(0))
            .build()
      }
  }
  when(WarPoint)(work(nextStatus = goto(TeXunActor.War).using(NoData)))
  when(TeXunActor.War) {
    case Event(c: ClientRequest, _) if findStart.run(c).isFind =>
      Build.goto(WarPoint)
        .using(context.actorOf(warPoint()))
        .replying(Commands())
        .build()
    case Event(c: ClientRequest, _)                            =>
      val waitWarPoint = {
        import Images.Tx._
        val l = List(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11, _12, _13, _14, _15)

        val ll = l.map(e => Find.build(e).map(_.withThreshold(0.7)).run(c))
        logger.info(ll.zipWithIndex.map { case (v, i) => (i + 1) -> v.similarity }.toString())

        val min = ll.minBy(_.similarity).similarity
        val minIndex = ll.indexWhere(_.similarity == min)
        if(minIndex == ll.length - 1)
          None
        else {
          logger.info(s"war point : ${minIndex + 2} -> ${ll(minIndex+1).similarity}")
          Some(ll(minIndex + 1))
        }
      }

      waitWarPoint match {
        case Some(IsFindPic(point)) =>
          Build.stay()
            .replying(Commands().tap(point).delay(3000))
            .build()
        case None                   =>
          logger.info("war end go to take result")
          Build.goto(TakeResult)
            .replying(Commands())
            .build()
      }
  }
  when(TakeResult) {
    case Event(c: ClientRequest, _) =>
      val reward = Find(Images.Tx.reward).run(c)
      val result = Find(Images.Tx.result).map(_.withThreshold(0.8)).run(c)
      (reward, result) match {
        case (IsFindPic(point), _)          =>
          logger.info(" sure reward")
          Build.stay().replying(Commands().tap(point)).build()
        case (_, IsFindPic(point))          =>
          logger.info(" take result")
          Build.stay().replying(Commands().tap(point)).build()
        case (a@NoFindPic(), b@NoFindPic()) =>
          logger.info(s"[finish] take result a:${a.sim} b:${b.sim}")
          Build.goto(Finish).replying(Commands()).build()
      }
  }
  when(Finish)(finish)
  onTransition(onFinish(Finish))

  def startTx() = SeqenceActor(
    goToTeXun(),
    startTeXun(),
    JustActor.justDelay(3000)
  )

  def goToTeXun() = SeqenceActor(
    ScenesActor.returns,
    JustActor.justDelay(1000),
    ScenesActor.goToGruen,
    JustActor.justTap(Points.Grean.teXun)
  )


  def startTeXun() = {
    val group = Props(new MyFsmAct {
      exec(c => Find(Images.Tx.group).run(c) match {
        case IsFindPic(point) => Build.goto(Finish).replying(Commands().tap(point)).build()
        case NoFindPic()      => Build.goto(Finish).replying(Commands()).build()
      })
    })

    val start = Props(new MyFsmAct {
      exec(c => findStart.run(c) match {
        case IsFindPic(point) => Build.goto(Finish).replying(Commands().tap(point)).build()
        case x@NoFindPic()    =>
          logger.info(s"in start TeXun no find `start` max sim : ${x.sim}")
          Build.stay().replying(Commands()).build()
      })
    })

    SeqenceActor(group, start)
  }


  def warPoint() = SeqenceActor(
    warStart(),
    waitWarEnd(),
    touchNext(),
    FindActor.waitIsFind(Find(Images.Tx.reward)),
    sureWarReward()
  )

  def sureWarReward() = Props(new MyFsmAct {
    exec(c => Find(Images.Tx.reward).run(c) match {
      case IsFindPic(_) =>
        logger.info("sure war reward")
        Build.stay().replying(Commands().tap(Point(1, 1))).build()
      case NoFindPic()  => Build.goto(Finish).replying(Commands()).build()
    })
  })

  def warStart() = Props(new MyFsmAct {
    exec(c => findStart.run(c) match {
      case IsFindPic(point) => Build.goto(Finish).replying(Commands().tap(point)).build()
      case x@NoFindPic()    =>
        logger.info(x.sim.toString)
        Build.stay().replying(Commands()).build()
    })
  })

  def waitWarEnd() = FindActor.waitIsFind(Find(Images.Tx.fightResult))

  def touchNext() = FindActor.touch(Find(Images.Tx.next))

}
