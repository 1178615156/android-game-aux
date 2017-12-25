package nyhx.fsm

import akka.actor.Props
import akka.testkit.{TestActorRef, TestProbe}
import models.{ClientRequest, Commands, Point, TapCommand}
import org.scalatest._
import sources.{AkkaTestSources, ImageTestSources}

class DismissedActorTest extends WordSpec
  with Matchers
  with AkkaTestSources
  with ImageTestSources {

  import testkit._

  lazy val actor     = TestActorRef[DismissedActor](Props(new DismissedActor))
  "pass move" in {
    actor ! TaskFinish
    assert(actor.underlyingActor.stateName === DismissedActor.SelectStudent)
  }
  "pass select student" in {
    actor ! TaskFinish
    assert(actor.underlyingActor.stateName === DismissedActor.DismissedSelectDetermine)
  }


  "dismissed SelectStudent Determine" must {
    "is find" in {
      actor ! ClientRequest(original.studentSelectDetermine)
      expectMsgPF() {
        case e: Commands =>
          e.seq.size shouldBe (1)
          e.seq.head match {
            case TapCommand(x, y, _) =>
              assert(x > 1)
              assert(y > 1)
          }
      }
    }
    "no find" in {
      actor ! ClientRequest(original.studentSelect)
      expectMsg(Commands())

    }
  }

}
