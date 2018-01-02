package utensil.macros

import scala.language.experimental.macros

import akka.actor.{Actor, ActorRef, Props}

trait ActorOf {
  this: Actor =>
  val name_iter = new java.util.concurrent.atomic.AtomicInteger(0)

  def of(props: Props): ActorRef = macro ActorOf.impl


}

object ActorOf {
  def impl(c: scala.reflect.macros.blackbox.Context)(props: c.Expr[Props]) = {
    import c.universe._
    val newBody = props.tree match {
      case Select(_, name) => q"context.actorOf($props,${name.toString + "-"} + name_iter.getAndIncrement())"
      case _               => q"context.actorOf($props)"
    }
//    println(showRaw(props.tree))
//    println(newBody)
    newBody
  }
}