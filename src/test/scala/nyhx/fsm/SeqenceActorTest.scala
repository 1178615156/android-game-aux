package nyhx.fsm

import models.{ClientRequest, Commands, Point}
import org.scalatest._
import sources.{AkkaTestSources, ImageTestSources}

class SeqenceActorTest extends WordSpec with AkkaTestSources with ImageTestSources {
  lazy val a = JustActor.justTap(Point(1, 1))
  lazy val b = JustActor.justTap(Point(2, 2))

  import testkit._
  "run" in {
    val actor = testProbe.childActorOf(SeqenceActor(a,b))
    actor ! ClientRequest(original.room)
    expectMsg(Commands().tap(Point(1,1)))

    actor ! ClientRequest(original.room)
    expectMsg(Commands().tap(Point(2,2)))

    testProbe.expectMsg(TaskFinish)
  }
}
