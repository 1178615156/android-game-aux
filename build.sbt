name := "android-game-aux"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= (Nil
  ++ Libs.akka.actors
  ++ Libs.akkaHttp.https
  ++ Libs.jackson.jacksons
  ++ Libs.play.json
  ++ Libs.logback
  ++ Libs.file
  ++ Libs.scalaTest.map(_ % Test)
  ++ Libs.jep
  )

enablePlugins(PackPlugin)
