package nyhx

import akka.actor.{FSM, Props}
import nyhx.fsm._
import nyhx.sequence._
import org.slf4j.LoggerFactory

object ClientActor {

  type Status = BaseStatus

  object War extends Status

  object Wdj extends Status

  object Dismissed extends Status

  object Tx extends Status

  object Export extends Status

  object Run extends Status

  type Data = BaseData


}

import nyhx.ClientActor._

class ClientActor(args: Seq[String]) extends FSM[Status, Data] with FsmHelper[Status, Data] {

  import context.actorOf

  val logger = LoggerFactory.getLogger("client-actor")
  val warNum = 20


  val map = {
    //default value
    val map: Map[Status, Props] = Map(
      //      War -> nyhx.fsm.WarTowActor.tow_b(warNum),
      //      War -> nyhx.fsm.WarOneActor.four_boss(warNum),
      War -> nyhx.fsm.WarOneActor.five_boos(warNum).props,
      //      War -> WarSixActor.four_b(warNum),
      Dismissed -> Props(new fsm.DismissedActor),
      Export -> ExportActor.run(),
      Tx -> Props(new TeXunActor()),
      Wdj -> Props(new WdjActor(warNum))
    )

    args.foldLeft(map) {
      case (acc, "war-2-6") => acc + (War -> Props(new WarAreaTwoSix(warNum)))
      case (acc, "war-6-4") => acc + (War -> Props(new WarAreaSixActor(warNum)))
      case (acc, "war-3-1") => acc + (War -> fsm.WarSixActor.four_b(warNum))
      case (acc, "wdj")     => acc + (Wdj -> Props(new WdjActor(warNum)))
      case (acc, "dismiss") => acc + (Dismissed -> Props(new fsm.DismissedActor))
      case (acc, "tx")      => acc + (Tx -> Props(new TeXunActor()))
      case (acc, _)         => acc
    }
  }

  def contains(s: String) = args.contains(s.trim)

  def run = ReplaceActor.apply(10, SeqenceActor(
    //    map(Tx) -> "tx",
    NameProps("export", map(Export)),
    NameProps("war", map(War)),
    NameProps("dismissed", map(Dismissed))
  ))

  startWith(Run, of(run, "run"))
  when(Run)(work(nextStatus = goto(Finish)))

//  when(Export)(work(nextStatus = goto(War).using(actorOf(map(War)))))
//  when(War)(work(nextStatus = goto(Dismissed).using(actorOf(map(Dismissed)))))
//  when(Dismissed)(work(nextStatus = goto(War).using(actorOf(map(War)))))
//  when(Tx)(work(nextStatus = goto(War).using(actorOf(map(War)))))
//  when(Wdj)(work(nextStatus = goto(War).using(actorOf(map(War)))))
  when(Finish)(finish)

  override def FinishStatus: BaseStatus = Finish
}
