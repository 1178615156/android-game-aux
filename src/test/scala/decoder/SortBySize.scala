package decoder

import better.files._

object SortBySize {
  def main(args: Array[String]): Unit = {
    val dir = "C:\\Users\\yujieshui\\Desktop\\res-decrypt\\codes"

    File(dir)
        .listRecursively
      .filterNot(_.isDirectory)
      .toList
      .sortBy(_.size)
      .map(e => e.name -> (e.size / 1024))
      .foreach(println)
  }
}
