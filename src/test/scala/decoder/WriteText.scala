package decoder

import java.io.PrintWriter

import scala.io.Source

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import play.api.libs.json.{JsObject, JsString, Json}

object WriteText {


  def writeFile(_fn: String, body: Seq[String]) = {
    val fn = if(_fn.endsWith(".csv") || _fn.endsWith(".json")) _fn else _fn + ".csv"
    val pw = new PrintWriter(s"D:/nyhx/text-csv/$fn")
    body.foreach(e => pw.append(e).append("\n"))
    pw.close()
  }

  def readText(f: String) =
    Source.fromFile(s"D:/nyhx/text/$f")
      .getLines().mkString
      .replace(",}", "}")

  def achievement() = {
    val text = Source.fromFile("D:/nyhx/text/achievement")
      .getLines().mkString
      .replace(",}", "}")
    val json = Json.parse(text).as[JsObject].value.values.toList
    val body = json.map { js => s"${js.\("name").as[String]},${js.\("desc").as[String]}" }
    val head = "name,desc"
    writeFile("achievement", head +: body)
  }

  def vocabulary_entry() = {
    val text = readText("vocabulary_entry")
    val json = Json.parse(text).as[JsObject].value.values.toList
      .map(_.as[VocabularyEntry])
    val head = "tittle,id,type,content,res"
    val body = json.map(e =>s""""${e.tittle}","${e.id}","${e.`type`}","${e.content}","${e.res}" """)
    writeFile("vocabulary_entry", head +: body)
  }

  def handbook() = {
    val text = readText("handbook").replace("nil", "null")
    val json = Json.parse(text).as[JsObject].value.values.toList
      .map(_.as[JsObject])
      .map(js => (js - "ability") + ("ability" -> JsString(js("ability").toString().replace("\"", ""))))
      .map(_.as[HandBook])
    val head = "characteristic, grade, race, id, information, name, ability"
    val body = json.map(_.csvString)
    writeFile("handbook", head +: body)
  }

  def monster_handbook() = {
    val text = readText("monster_handbook").replace("nil", "null")
    val json = Json.parse(text).as[JsObject].value.values.toList
      .map(_.as[JsObject])
      .map(js => MonsterHandBook(
        characteristic = js.\("characteristic").as[String],
        ability = js("ability").as[String],
        race = js("race").as[String],
        information = js.apply("information").apply("3").as[String],
        name = js("name").apply("1").as[String]
      ))
    json.take(2).foreach(println)

    val head = "characteristic,ability,race,information,name"
    val body = json.map(_.csvString)
    writeFile("monster_handbook", head +: body)
  }

  def equipment() = {
    val text = readText("equipment")
    val json = Json.parse(text).as[JsObject].value.values.toList
      .map(_.as[JsObject])
      .map(_.as[Equipment])
    json.take(2).foreach(println)

    val head = "characteristic,type,name,desc"
    val body = json.map(_.csvString)
    writeFile("equipment", head +: body)
  }

  def personal_mission_word() = {
    val text = readText("personal_mission_word")
    val json = Json.parse(text).as[JsObject].value.values.toList
      .map(_.as[JsObject])
      .map(js => PersonalMissionWord(
        word = js.apply("word").apply("1").as[String],
        unitName = js.apply("unitName").as[String]
      ))
    json.take(2).foreach(println)
    val head = "word,unitName"
    val body = json.map(_.csvString)
    writeFile("personal_mission_word", head +: body)
  }

  def explore_word() = {
    val text = readText("explore_word")
    val json = Json.parse(text).as[JsObject].value.values.toList
      .map(_.as[JsObject])
      .map(_.as[ExploreWord])
    json.take(2).foreach(println)
    val body = json.map(e => Json.toJson(e).toString())
    writeFile("explore_word.json", body)
  }

  def explore_mission() = {
    val text = readText("explore_mission")
    val json = Json.parse(text).as[JsObject].value.values.toList
      .map(_.as[JsObject])
      .map(_.as[ExploreMission])
    json.take(2).foreach(println)
    val body = json.map(e => Json.toJson(e).toString())
    writeFile("explore_mission.json", body)
  }

  def explore_event() = {
    val text = readText("explore_event")
    val json = Json.parse(text).as[JsObject].value.values.toList
      .map(_.as[JsObject])
      .map(js => ExploreEvent(
        id = js.apply("id").as[Int],
        nextId = js.apply("nextId").as[JsObject].values.toList.map(_.as[Int]),
        subeventId = js.apply("subeventId").as[JsObject].values.toList.map(_.as[Int])
      ))
    writeFile("explore_event.json", json.map(s => Json.toJson(s).toString()))
  }

  def explore_subevent() = {
    val text = readText("explore_subevent")
    val json = Json.parse(text).as[JsObject].value.values.toList
      .map(_.as[JsObject])
      .map(js => ExploreSubevent(
        id = js.apply("id").as[Int],
        condition = js.apply("condition").as[Int],
        textId = js.apply("textId").as[JsObject].values.toList.map(_.as[Int]),
        textIdForF = js.apply("textIdForF").as[JsObject].values.toList.map(_.as[Int]),
        textIdForT = js.apply("textIdForT").as[JsObject].values.toList.map(_.as[Int])
      ))
    json.take(20).foreach(println)
    writeFile("explore_subevent.json", json.map(s => Json.toJson(s).toString()))
  }

  def condition() = {
    val text = readText("condition")
    val json = Json.parse(text).as[JsObject].value.values.toList
      .map(_.as[JsObject])
      .map(_.as[Condition])
    json.take(5).foreach(println)
    writeFile("condition.json", json.map(s => Json.toJson(s).toString()))
  }

  def main(args: Array[String]): Unit = {
    explore_word()
    explore_mission()
    explore_event()
    explore_subevent()
  }
}
















