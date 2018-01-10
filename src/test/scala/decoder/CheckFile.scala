package decoder

object CheckFile {

  def main(args: Array[String]): Unit = {
    import better.files._
    File("D:\\nyhx\\text").list
      .filterNot(_.isDirectory)
      .map(_.lines)
      .foreach(e=>println(e.head))
  }
}
