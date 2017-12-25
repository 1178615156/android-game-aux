package nyhx.fsm

import akka.actor.Props
import akka.testkit.TestActorRef
import models.{ClientRequest, Commands, TapCommand}
import org.scalatest._
import sources.{AkkaTestSources, ImageTestSources}

class ScenesActorTest extends WordSpec
  with Matchers
  with AkkaTestSources
  with ImageTestSources {


  //  def mkActor(status: ScenesActor.ScenesStatus) = TestActorRef[ScenesActor](
  //    Props(new ScenesActor(status, testkit.testActor)))
  //
  //
  //  import testkit._
  //
  //  val adventureImage = readOriginal("adventure.png")
  //  val randomSurvey   = readOriginal("random-survey.png")
  //
  //
  //  "returns" must {
  //    "find" in {
  //      val actor = mkActor(ScenesActor.Returns)
  //      actor.tell(ClientRequest(adventureImage), testActor)
  //      expectMsgPF() {
  //        case x: Commands =>
  //          assert(x.seq.length === 1)
  //          x.seq.head shouldBe a[TapCommand]
  //      }
  //
  //    }
  //
  //    "no find" in {
  //      val actor = mkActor(ScenesActor.Returns)
  //      actor.tell(ClientRequest(randomSurvey), testActor)
  //      expectMsg(Commands())
  //      expectMsg(TaskFinish)
  //
  //    }
  //
  //  }
}
