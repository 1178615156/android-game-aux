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

def utensilSetting = Seq(
  libraryDependencies ++= Libs.akka.actors,
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

)


lazy val `utensil-macros` = (project in file("./utensil-macros"))
  .settings(utensilSetting)

lazy val root = (project in file("./"))
  .dependsOn(`utensil-macros`)
  .aggregate(`utensil-macros`)
  .enablePlugins(PackPlugin)
