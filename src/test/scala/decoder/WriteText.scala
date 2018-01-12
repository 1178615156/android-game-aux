package decoder

import java.io.PrintWriter

import scala.io.Source

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.config.ConfigFactory
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

//trait BaseHelper {
//  val mapping = new ObjectMapper()
//
//  def readLua(fileName: String) = {
//    val spear = "[\\n|\\t|\\s| |\t|\n]+"
//    val s =
//      Source
//      .fromFile(s"F:\\software\\android\\res-decrypt\\codes\\shared\\conftable\\${fileName}.lua")
//      .getLines().mkString("\n")
//    """	{
//      |		text = "声音？似乎有什么声音传到了耳朵了？是错觉吗？",
//      |		word = "……",
//      |		id = 3854,
//      |		speaker = {
//      |			3009
//      |		},
//      |		speakerWord = {
//      |			"（或许…我的身世和图特大人也有千丝万缕的联系吧…）"
//      |		}
//      |	}
//      |
//    """.stripMargin
//    val text = s
//      .replace("\t", " ")
//      .replace("return", "")
//      .replaceAll(spear, "")
//      .replaceAll("\\{(\\d+)\\}", "$1")
//      .replaceAll("\\{([\\d|,]+)\\}", "[$1]")
//      .replaceAll("\\{{2}(\".+\")\\}{2}", "$1")
//      .replaceAll("\\{\"(.+)\"\\}", "$1")
//      .replaceAll("\\[(\\d+)\\]",""" "$1" """)
//      .replaceAll("([0-9|a-z|A-Z]+)=",""" "$1" = """)
//      .replace("=", ":")
//    if (text.startsWith("{{") && text.endsWith("}}")) s"[${text.tail.init}]"
//    else
//    text
//  }
//
//  def readLua2Json(fileName: String): JsValue = {
//    val text = readLua(fileName)
//    Json.parse(mapping.readTree(text).toString)
//  }
//
//  def writeFile(_fn: String, body: Seq[String]) = {
//    val fn = if(_fn.endsWith(".csv") || _fn.endsWith(".json")) _fn else _fn + ".csv"
//    val pw = new PrintWriter(s"D:/nyhx/text-csv/$fn")
//    body.foreach(e => pw.append(e).append("\n"))
//    pw.close()
//  }
//}
//
//object Read extends BaseHelper {
//  def main(args: Array[String]): Unit = {
////    readLua2Json("word")
//    println(readLua("explore_word"))
//    println(readLua2Json("explore_word"))
//  }
//}

object WriteText {




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
    val body = json.map(e => Json.toJson(e).toString())
    writeFile("handbook.json", body)
  }

  def target() = {
    val text = readText("target").replace("nil", "null")
    val json = Json.parse(text).as[JsObject].value.values.toList
      .map(_.as[JsObject])
      .map(js => Target(
        id = js("id").as[Int],
        filter = jsValue2values(js("filter")),
        desc = js("desc").as[String]
      ))
    val body = json.map(e => Json.toJson(e).toString())
    writeFile("target.json", body)
  }

  def school_mail() = {
    val text = readText("school_mail").replace("nil", "null")
    val json = Json.parse(text).as[JsObject].value.values.toList
      .map(_.as[JsObject])
      .map(js => SchoolMail(
        content = js("content").as[String],
        from = js("from").as[String],
        `type` = js("type").as[Int],
        title = js("title").as[String],
        link = js("link").as[String],
        id = js("id").as[Int],
        sender = js("sender").as[String],
        receive = js("receiver").as[String],
        targets = js("targetId").as[JsObject].values.toList.map(_.as[Int]),
        targetsCid = None ,
        targetsValue = None
      ))
    val body = json.map(e => Json.toJson(e).toString())
    writeFile("school_mail.json", body)
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
        handbook()
    //    explore_word()
    //    explore_mission()
    //    explore_event()
    //    explore_subevent()
    target()
  }
}
















