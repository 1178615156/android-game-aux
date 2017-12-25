package nyhx

import akka.actor.{Actor, Props}
import akka.testkit.TestActorRef
import nyhx.sequence.{DismissedTaskFinish, WarTaskEnd}
import org.scalatest.WordSpec
import sources.{AkkaTestSources, ImageTestSources}

class ClientActorTest extends WordSpec with AkkaTestSources with ImageTestSources {
  import testkit._
  lazy val actor = TestActorRef[ClientActor](Props(new ClientActor(Nil)))

  def nothingActor() = actorSystem.actorOf(Props(new Actor {
    override def receive: Receive = {
      case e =>
    }
  }))

  "war" in {
    assert(actor.underlyingActor.stateName === ClientActor.War)
    actor ! WarTaskEnd(nothingActor())
    assert(actor.underlyingActor.stateName === ClientActor.Dismissed)
  }
  "dismissed" in {

    actor ! DismissedTaskFinish(nothingActor())
    assert(actor.underlyingActor.stateName === ClientActor.War)
  }

}
