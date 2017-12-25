package models

import play.api.libs.json.Json

trait Command {
  def toJsonString: String
}

case class TapCommand(x: Int, y: Int, action: String = "tap") extends Command {
  implicit val json = Json.format[TapCommand]

  override def toJsonString = Json.toJson(this).toString()
}

case class DelayCommand(time: Int, action: String = "delay") extends Command {
  implicit val json = Json.format[DelayCommand]

  override def toJsonString = Json.toJson(this).toString()
}

case class Commands(seq: Command*) {
  def add(action: Command) = Commands((seq :+ action): _*)

  def tap(point: Point) = add(TapCommand(point.x, point.y))

  def delay(time: Int) = add(DelayCommand(time))
}
