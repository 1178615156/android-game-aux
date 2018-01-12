import java.io.PrintWriter

import scala.io.Source

import play.api.libs.json._

package object decoder {
  def jsValue2values(jsValue: JsValue): String = jsValue match {
    case x: JsObject => x.values.toList.map(jsValue2values).mkString("{", ",", "}")
    case x: JsString => x.as[String]
    case x           => x.toString()
  }

  def readOf[T: Reads](f: String) = Source.fromFile(s"D:/nyhx/text-csv/$f.json")
    .getLines()
    .toList
    .map(s => Json.parse(s).as[T])
  def writeFile(_fn: String, body: Seq[String]) = {
    val fn = if(_fn.endsWith(".csv") || _fn.endsWith(".json")) _fn else _fn + ".csv"
    val pw = new PrintWriter(s"D:/nyhx/text-csv/$fn")
    body.foreach(e => pw.append(e).append("\n"))
    pw.close()
  }
  case class VocabularyEntry(tittle: String, id: Int, `type`: Int, content: String, res: String)

  case class HandBook(characteristic: String, grade: Int, race: String, id: Int, information: String, name: String, ability: String)


  case class Target(id: Int, desc: String, filter: String) {
    private val r = "cid,eq,(\\d+)".r

    def cids = r.findAllIn(filter).toList.map(_.split(",").last.toInt)

  }

  case class SchoolMail(id: Int, content: String, `type`: Int, sender: String,
                        title: String, link: String, from: String, receive: String,
                        targets: Seq[Int],
                        targetsCid: Option[Seq[Int]],
                        targetsCidName: Option[Seq[String]] = None,
                        targetsValue: Option[Seq[String]])

  case class ExploreWord(id: Int, text: String, word: String) {
    def csvString = s"""$id,"$text","$word" """
  }

  case class ExploreMission(id: Int, name: String, eventId: Int)


  case class ExploreEvent(id: Int, nextId: Seq[Int], subeventId: Seq[Int])

  case class ExploreSubevent(id: Int, condition: Int,
                             textId: Seq[Int], textIdForF: Seq[Int], textIdForT: Seq[Int])

  case class Condition(id: Int, desc: String)

  case class MonsterHandBook(
                              characteristic: String,
                              ability: String,
                              race: String,
                              information: String,
                              name: String
                            ) {
    def csvString = s"""$characteristic,$ability,$race,"$information",$name """
  }

  case class Equipment(
                        characteristic: Int,
                        `type`: Int,
                        name: String,
                        desc: String
                      ) {
    def csvString = s"""$characteristic,${`type`},$name,"$desc" """
  }

  case class PersonalMissionWord(word: String, unitName: String) {
    val csvString = s""" "$word",$unitName """.trim
  }

  implicit val VocabularyEntryJson = Json.format[VocabularyEntry]
  implicit val HandBookJson        = Json.format[HandBook]
  implicit val EquipmentJson       = Json.format[Equipment]
  implicit val ExploreWordJson     = Json.format[ExploreWord]
  implicit val ExploreMissionJson  = Json.format[ExploreMission]
  implicit val ExporeEventJson     = Json.format[ExploreEvent]
  implicit val ExploreSubeventJson = Json.format[ExploreSubevent]
  implicit val ConditionJson       = Json.format[Condition]
  implicit val TargetJson          = Json.format[Target]
  implicit val SchoolMailJson      = Json.format[SchoolMail]

}
