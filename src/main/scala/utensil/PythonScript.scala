package utensil

import java.util.concurrent.Executors

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

import jep.Jep

object PythonScript {
  var libpath = System.getProperty("java.library.path")
  val userDir = System.getProperty("user.dir")
  libpath = s"${userDir}\\python-script;${libpath}"
  System.setProperty("java.library.path", libpath)
//  println(System.getProperty("java.library.path"))
  implicit private val _exec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))


  private val jep = Future {
    val jep = new Jep(false)
    jep.runScript("./python-script/find_pic.py")
    jep
  }

  def eval[T](f: Jep => T): Future[T] = jep.map(f)



}


