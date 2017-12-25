package nyhx.fsm

import akka.actor.Props
import akka.testkit.TestActorRef
import models.{ClientRequest, Commands, DelayCommand, TapCommand}
import nyhx.fsm.DismissedActor._
import org.scalatest.{FunSuite, Matchers, WordSpec}
import sources.{AkkaTestSources, ImageTestSources}

class DismissedSelectActorTest extends WordSpec
  with Matchers
  with AkkaTestSources
  with ImageTestSources {

  import testkit._

  lazy val actor = TestActorRef[DismissedSelectActor](new DismissedSelectActor)

  s"pass $TouchSelect" in {
    actor ! TaskFinish
  }
  s"pass $TouchRetrieve" in {
    actor ! TaskFinish
  }
  "sure retrieve" must {
    "find" in {

      actor ! ClientRequest(original.dismissedRetrieve)
      expectMsgPF() {
        case x: Commands =>
          x.seq.head shouldBe a[TapCommand]
      }
    }
    "no find" in {
      actor ! ClientRequest(original.studentSelectDetermine)
      expectMsg(Commands().delay(0))
    }
  }
  "tap student" in {
    actor ! ClientRequest(original.studentSelect)
    expectMsgPF() { case e: Commands =>
      assert(e.seq.length > 1)
      assert(e.seq.exists(_.isInstanceOf[TapCommand]))
      assert(e.seq.exists(_.isInstanceOf[DelayCommand]))
    }
  }

  "finish" in {
    assert(actor.underlyingActor.stateName === Finish)
  }
}
