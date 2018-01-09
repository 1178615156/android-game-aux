import play.api.libs.json.Json

package object decoder {

  case class VocabularyEntry(tittle: String, id: Int, `type`: Int, content: String, res: String)

  case class HandBook(characteristic: String, grade: Int, race: String, id: Int, information: String, name: String, ability: String) {
    def csvString =
      s"""$characteristic,$grade,$race,$id,"$information",$name,"$ability" """
  }

  case class ExploreWord(id: Int, text: String, word: String) {
    def csvString = s"""$id,"$text","$word" """
  }

  case class ExploreMission(id: Int, name: String, eventId: Int) {
    def csvString = s"""$id,"$name",$eventId """
  }

  val ExploreMissionHead = "id,name,eventId"

  case class ExploreEvent(id: Int, nextId: Seq[Int], subeventId: Seq[Int])

  case class ExploreSubevent(id: Int, condition: Int,
                             textId: Seq[Int], textIdForF: Seq[Int], textIdForT: Seq[Int])

  case class Condition(id:Int,desc:String)
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
  implicit val ConditionJson = Json.format[Condition]

}
