package nyhx.sequence

import models.{ClientRequest, Commands}
import org.scalatest._
import sources.ImageTestSources
import FindAux.findPicBuilding2FindAux

class FindAuxTest extends WordSpec with Matchers with ImageTestSources {
  "returns" in {
    val result = FindAux.returns(ClientRequest(readOriginal("adventure.png"))).run()
    assert(result.isFind)
  }
  "touch" must {
    "is find" in {

      val result = FindAux.returns.touch(ClientRequest(readOriginal("adventure.png")))
      result shouldBe a[Result.Success]
    }
    "no find" in {

      val result = FindAux.returns.touch(ClientRequest(readOriginal("room.png")))

      result shouldBe a[Result.Failure]
    }
  }
}
