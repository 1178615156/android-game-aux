package nyhx.fsm

import akka.testkit.{TestActorRef, TestProbe}
import models.{ClientRequest, Commands, TapCommand}
import nyhx.{Find, Images}
import org.scalatest._
import sources.{AkkaTestSources, ImageTestSources}

class FindActorTest extends WordSpec with ImageTestSources with AkkaTestSources with Matchers {

  import testkit._


  "touch" in {
    val parent = TestProbe()
    val actor = parent.childActorOf(FindActor.touch(Find(Images.returns)))

    val c = ClientRequest(original.adventure)
    actor.tell(c, testActor)
    expectMsgPF() {
      case x: Commands =>
        assert(x.seq.length === 1)
        x.seq.head shouldBe a[TapCommand]
    }
    parent.expectMsg(TaskFinish)

  }
}
