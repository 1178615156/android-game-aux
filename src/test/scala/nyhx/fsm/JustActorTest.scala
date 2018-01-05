package nyhx.fsm

import models.{ClientRequest, Commands, Point}
import org.scalatest._
import sources.{AkkaTestSources, ImageTestSources}

class JustActorTest extends WordSpec with AkkaTestSources with ImageTestSources {

  import testkit._


  "tap" in {
    val actor = testProbe.childActorOf(JustActor.justTap(Point(1, 1)).props)
    actor ! ClientRequest(original.room)
    expectMsg(Commands().tap(Point(1, 1)))
    testProbe.expectMsg(TaskFinish)
  }
}
