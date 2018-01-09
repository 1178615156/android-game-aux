package decoder

import java.io.PrintWriter

import scala.io.Source

import play.api.libs.json.{Json, Reads}

object ExploreDiscover {

  trait Tree {
    //    override def toString: String = this
  }


  case class Node(id: Int,
                  nexts: Seq[Tree],
                  subeventId: Seq[Int]) extends Tree

  case class Empty(id: Int, subeventId: Seq[Int]) extends Tree


  def readOf[T: Reads](f: String) = Source.fromFile(s"D:\\nyhx\\text-csv\\$f.json")
    .getLines()
    .map(s => Json.parse(s).as[T])

  val explore_mission  = readOf[ExploreMission]("explore_mission")
  val explore_event    = readOf[ExploreEvent]("explore_event")
  val explore_subevent = readOf[ExploreSubevent]("explore_subevent")
  val condition        = readOf[Condition]("condition")

  val explore_word_map     = readOf[ExploreWord]("explore_word").map(e => e.id -> e).toMap
  val explore_mission_map  = explore_mission.map(e => e.id -> e).toMap
  val explore_event_map    = explore_event.map(e => e.id -> e).toMap
  val explore_subevent_map = explore_subevent.map(e => e.id -> e).toMap
  val condition_map        = condition.map(e => e.id -> e).toMap

  def textIdsToString(seq: Seq[Int]) = {
    seq.map(explore_word_map.apply).map(_.text).map("  " + _).mkString("\n")
  }

  def subeventToString(subevent: ExploreSubevent) = {

    s"""${subevent.textId.map(explore_word_map.apply).map(_.text).mkString("\n")}
       |if (${condition_map(subevent.condition).desc})
       |${textIdsToString(subevent.textIdForT)}
       |else
       |${textIdsToString(subevent.textIdForF)}
     """.stripMargin
  }


//  def treeToString(tree: Tree): String = tree match {
//    case Empty(id, subeventId)       =>
//      s"""  $id
//         |${subeventId.map(explore_subevent_map.apply).map(subeventToString).flatMap(_.split("\n")).map("  " + _).mkString("\n")}""".stripMargin
//    case Node(id, nexts, subeventId) =>
//
//      val subevents = subeventId.map(explore_subevent_map.apply)
//      val (true_next,false_next)= {
//        nexts.toList match {
//          case a :: b :: Nil => a -> b
//          case a :: Nil => a -> a
//          case _ => ???
//        }
//      }
//
//      val before = subevents.init.map { subevent =>
//        s"""  sub-${subevent.id}
//           |  if (${subevent.condition} - ${condition_map(subevent.condition).desc})
//           |${textIdsToString(subevent.textIdForT).split("\n").map("  " + _).mkString("\n")}
//           |  else
//           |${textIdsToString(subevent.textIdForF).split("\n").map("  " + _).mkString("\n")}""".stripMargin
//      }.mkString("\n")
//      val subevent = explore_subevent_map(subeventId.last)
//      s"""  $id
//         |$before
//         |  sub-${subevent.id}
//         |${textIdsToString(subevent.textId)}
//         |  if ${subevent.condition} - ${condition_map(subevent.condition).desc}
//         |${textIdsToString(subevent.textIdForT).split("\n").map("  " + _).mkString("\n")}
//         |${treeToString(true_next).split("\n").map("  " + _).mkString("\n")}
//         |  else
//         |${textIdsToString(subevent.textIdForF).split("\n").map("  " + _).mkString("\n")}
//         |${treeToString(false_next).split("\n").map("  " + _).mkString("\n")}""".stripMargin
//  }
  def treeToString(tree: Tree): String = tree match {
    case Empty(id, subeventId)       =>
      s"""${subeventId.map(explore_subevent_map.apply).map(subeventToString).flatMap(_.split("\n")).map("  " + _).mkString("\n")}""".stripMargin
    case Node(id, nexts, subeventId) =>

      val subevents = subeventId.map(explore_subevent_map.apply)
      val (true_next,false_next)= {
        nexts.toList match {
          case a :: b :: Nil => a -> b
          case a :: Nil => a -> a
          case _ => ???
        }
      }

      val before = subevents.init.map { subevent =>
        s"""if (${condition_map(subevent.condition).desc})
           |${textIdsToString(subevent.textIdForT).split("\n").map("  " + _).mkString("\n")}
           |  else
           |${textIdsToString(subevent.textIdForF).split("\n").map("  " + _).mkString("\n")}""".stripMargin
      }.mkString("\n")
      val subevent = explore_subevent_map(subeventId.last)
      s"""$before
         |${textIdsToString(subevent.textId)}
         |  if (${condition_map(subevent.condition).desc})
         |${textIdsToString(subevent.textIdForT).split("\n").map("  " + _).mkString("\n")}
         |${treeToString(true_next).split("\n").map("  " + _).mkString("\n")}
         |  else
         |${textIdsToString(subevent.textIdForF).split("\n").map("  " + _).mkString("\n")}
         |${treeToString(false_next).split("\n").map("  " + _).mkString("\n")}""".stripMargin
  }
  def findEvent(id: Int): Tree = {
    val event = explore_event_map(id)
    if(event.nextId.isEmpty)
      Empty(id, event.subeventId)
    else
      Node(id, event.nextId.map(findEvent), event.subeventId)
  }

  def writeFile(f: String, s: String) = {
    val pw = new PrintWriter(s"D:/nyhx/explore/explore-$f")
    pw.append(s)
    //    s.foreach(s => pw.append(s).append("\n"))
    pw.close()
  }

  def main(args: Array[String]): Unit = {
    writeFile("1-1.txt", treeToString(findEvent(11001)))
    writeFile("1-2.txt", treeToString(findEvent(12001)))
    writeFile("1-3-1.txt", treeToString(findEvent(13001)))
    writeFile("1-3-2.txt", treeToString(findEvent(14001)))
    writeFile("1-3-3.txt", treeToString(findEvent(15001)))


  }
}
