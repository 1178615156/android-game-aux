package models

import play.api.libs.json.Json

trait Command {
  def toJsonString: String
}

case class TapCommand(x: Int, y: Int, action: String = "tap") extends Command {

  override def toJsonString = Json.toJson(this)(TapCommand.json).toString()
}

case class DelayCommand(time: Int, action: String = "delay") extends Command {

  override def toJsonString = Json.toJson(this)(DelayCommand.json).toString()
}

case class Commands(seq: Command*) {
  def add(action: Command) = Commands((seq :+ action): _*)

  def tap(point: Point) = add(TapCommand(point.x, point.y))

  def delay(time: Int) = add(DelayCommand(time))

  def toJsonString = seq.map(_.toJsonString).mkString("[",",","]")
}


object DelayCommand{
  implicit val json = Json.format[DelayCommand]
}

object TapCommand{
  implicit val json = Json.format[TapCommand]
}