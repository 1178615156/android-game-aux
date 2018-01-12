package decoder

import play.api.libs.json.Json

object SchoolMainDiscover {
  val targetMap  = readOf[Target]("target").map(e => e.id -> e).toMap
  val schoolMail = readOf[SchoolMail]("school_mail")
  val handBook   = readOf[HandBook]("handbook").map(e => e.id -> e).toMap

  def main(args: Array[String]): Unit = {
    targetMap.values.take(100).foreach(e => println(e.cids))
    val result = schoolMail.map(sm => {
      val targets = sm.targets
      val cids = targets.map(targetMap.get).collect { case Some(x) => x }.flatMap(_.cids)
      val names = cids.map(handBook.get).collect { case Some(x) => x }.map(_.name)
      val values = targets.map(targetMap.get).map(e => if(e.nonEmpty) e.get.toString else "null")
      sm.copy(
        targetsCid = Some(cids),
        targetsValue = Some(values),
        targetsCidName = Some(names)
      )
    })

    writeFile("school_mail_total.json", result.map(e => Json.toJson(e).toString()))
  }
}
