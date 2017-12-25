package nyhx

import akka.actor.{ActorRef, FSM, Props}
import nyhx.fsm.{DismissedActor => _, _}
import nyhx.sequence._
import org.slf4j.LoggerFactory

object ClientActor {

  type Status = BaseStatus

  object War extends Status

  object Wdj extends Status

  object Dismissed extends Status

  object Tx extends Status

  type Data = BaseData


}

import nyhx.ClientActor._

class ClientActor(args: Seq[String]) extends FSM[Status, Data] with FsmHelper[Status, Data] {

  import context.actorOf

  val logger = LoggerFactory.getLogger("client-actor")
  val warNum = 100

  def contains(s: String) = args.contains(s.trim)

  val map = {
    val map: Map[Status, Props] = Map(
      //default value
      War -> nyhx.fsm.WarSixActor.four_b(warNum),
      War -> nyhx.fsm.WarTowActor.tow_b(warNum),
      Dismissed -> Props(new nyhx.fsm.DismissedActor),
      Tx -> Props(new TeXunActor()),
      Wdj -> Props(new WdjActor(warNum))
    )

    args.foldLeft(map) {
      case (acc, "war-2-6") => acc + (War -> Props(new WarAreaTwoSix(warNum)))
      case (acc, "war-6-4") => acc + (War -> Props(new WarAreaSixActor(warNum)))
      case (acc, "war-3-1") => acc + (War -> nyhx.fsm.WarSixActor.four_b(warNum))
      case (acc, "wdj")     => acc + (Wdj -> Props(new WdjActor(warNum)))
      case (acc, "dismiss") => acc + (Dismissed -> Props(new nyhx.fsm.DismissedActor))
      case (acc, "tx")      => acc + (Tx -> Props(new TeXunActor()))
      case (acc, _)         => acc
    }
  }

  if(contains("tx"))
    startWith(Tx, actorOf(map(Tx)))
  else if(contains("wdj"))
    startWith(Wdj, actorOf(map(Wdj)))
  else
    startWith(War, actorOf(map(War)))

//  startWith(War, actorOf(map(War)))
  //  startWith(Wdj, actorOf(map(Wdj)))
    startWith(Tx, actorOf(map(Tx)))
  //  startWith(Dismissed, map(Dismissed)())
  when(War)(work(nextStatus = goto(Dismissed).using(actorOf(map(Dismissed)))))
  when(Dismissed)(work(nextStatus = goto(War).using(actorOf(map(War)))))
  when(Tx)(work(nextStatus = goto(War).using(actorOf(map(War)))))
  when(Wdj)(work(nextStatus = goto(War).using(actorOf(map(War)))))
}
