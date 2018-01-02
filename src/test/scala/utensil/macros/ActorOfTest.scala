package utensil.macros

import akka.actor.{Actor, Props}
import nyhx.fsm.MyAct


trait ActorOfTest extends ActorOf with Actor{
  val aaa  = Props(new MyAct {})

  val b = of(aaa)
}

