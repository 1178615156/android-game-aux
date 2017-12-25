package models

import akka.actor.ActorRef
import nyhx.sequence.{Action, Result}

case class ClientRequest(image: Image)

