package nyhx.sequence

import models.ClientRequest
import nyhx.Images
import org.scalatest.{FunSuite, WordSpec}
import org.slf4j.{Logger, LoggerFactory}
import sources.ImageTestSources
import utensil.FindPicBuild

class WarHelperTest extends WordSpec with ImageTestSources {
  val x = new WarHelper with ScenesHelper with BaseHelper {
    override def logger: Logger = LoggerFactory.getLogger("xxx")
  }
  "find" in {
    val c = ClientRequest(readOriginal("random-survey.png"))
    assert(FindAux(Images.Adventure.needSurvey.toGoal)(c).run().isFind)
  }
  "find 2" in {
    //    val c = ClientRequest(readOriginal("random-survey-2.png"))
    //    val result = Find(Images.Adventure.needSurvey.toGoal)(c).run()
    //
    //    println(result.similarity)
    //    println(result.point)
    //    println(result)
    val result = FindPicBuild.findPic(
      readOriginal("random-survey.png").name,
      Images.Adventure.needSurvey.name,
      "default"
    )

    println(result)
  }
}
