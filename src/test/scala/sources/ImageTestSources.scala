package sources

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import models.{GoalImage, OriginalImage}
import org.scalatest.{BeforeAndAfterAll, Suite}

trait AkkaTestSources extends BeforeAndAfterAll {
  this: Suite =>
  lazy val actorSystem = ActorSystem("test")
  lazy val testkit     = new TestKit(actorSystem) with akka.testkit.ImplicitSender
  lazy val testProbe   = TestProbe()(actorSystem)

  implicit lazy val exec = actorSystem.dispatcher

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    actorSystem
    testkit
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    actorSystem.terminate()
  }

}

trait ImageTestSources {
  val userDir = System.getProperty("user.dir").replaceAll("\\\\", "/")

  def readOriginal(name: String) = OriginalImage(s"$userDir/images-original/$name")

  def readGoal(name: String) = GoalImage(s"$userDir/images-goal/$name")

  object original {
    val adventure              = readOriginal("adventure.png")
    val room                   = readOriginal("room.png")
    val studentSelect          = readOriginal("student-select.png")
    val dismissedRetrieve      = readOriginal("dismissed-retrieve.png")
    val studentSelectDetermine = readOriginal("student-select-determine.png")
  }

}
