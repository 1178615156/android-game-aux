import scala.reflect.runtime.universe._
val a = 1

a match {
  case e   => 122
  case 1 | 2 | 3 => 123
}