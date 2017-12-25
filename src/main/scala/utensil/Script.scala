package utensil

import scala.sys.process.Process


object Script {
  def asWin: Boolean = System.getProperty("os.name").toLowerCase.startsWith("win")

  def bash(s: String) = {
    if(asWin)
      Process(Seq("cmd.exe", "/c", s))
    else
      Process(Seq("bash", "-c", s))
  }

  def python(s: String) = {
    if(asWin)
      Process(Seq("python", s))
    else
      Process(Seq("python", s))
  }
}