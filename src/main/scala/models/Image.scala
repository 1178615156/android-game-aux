package models

import play.api.libs.json.Json

case class Point(x: Int, y: Int, name: String = "")

class Image(__name: String) {
  val name = {
    if(__name.endsWith(".png"))
      __name
    else
      __name
  }

  def simpleName = name.split("/|\\\\").last.split("\\.").head

  def toGoal = GoalImage(__name)

  def toOriginal = OriginalImage(__name)

  override def toString: String = __name
}

object Image {
  def apply(__name: String): Image = new Image(__name)
}

case class GoalImage(__name: String) extends Image(__name)

case class OriginalImage(__name: String) extends Image(__name)


