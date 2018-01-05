package nyhx.fsm

import akka.actor.Props
import models.ClientRequest
import org.scalatest.{FunSuite, Matchers, WordSpec}
import sources.{AkkaTestSources, ImageTestSources}

class ConditionActorTest extends WordSpec with ImageTestSources with AkkaTestSources with Matchers {

  import testkit._

  val props = ConditionActor.of(Props(
    new MyAct {}
  ))
    .onSuccess(
      NameProps("success", Props(new MyAct {
        become {
          case e => sender() ! TaskFinish
        }
      }))
    )
    .onFailure(
      NameProps("failure", Props(new MyAct {
        become { case e => sender() ! TaskFailure }
      }))
    )
    .build()

  "run success" in {
    val actor = testProbe.childActorOf(props)
    actor ! TaskFinish
    actor ! ClientRequest(original.room)
    expectMsg(TaskFinish)
  }
}
