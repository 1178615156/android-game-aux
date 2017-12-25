package utensil

import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf

import org.scalatest.WordSpec
import utensil.PythonScript._

class PythonScriptTest extends WordSpec {
  "jep" in {
    assert(Await.result(PythonScript.eval(_.getValue("1+1").asInstanceOf[java.lang.Long]), Inf) == 2)
  }

}
