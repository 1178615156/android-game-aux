package models


case class MpEmptyException() extends Exception("em empty")

case class NoFindPicException(s: String) extends Exception(s)

case class WarStartFailure() extends Exception("war start failure")

case class NoStudentDismissed() extends Exception("no student need Dismissed")