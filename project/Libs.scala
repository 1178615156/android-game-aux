import sbt._

object Libs {
  val akkaVersion     = "2.5.8"
  val akkaHttpVersion = "10.0.9"
  val sparkVersion    = "2.2.0"
  val jacksonVersion  = "2.9.0"

  object slf4j {
    val slf4j     = Seq("org.slf4j" % "slf4j-api" % "1.7.23")
    val log4jOver = Seq("org.slf4j" % "log4j-over-slf4j" % "1.7.25")
  }


  //db
  val slick     = Seq("com.typesafe.slick" %% "slick" % "3.2.0")
  val mysql     = Seq("mysql" % "mysql-connector-java" % "6.0.6").map(_.excludes.log4j)
  val quill     = Seq("io.getquill" %% "quill-jdbc" % "1.3.0")
  val mariaDB4j = Seq(("ch.vorburger.mariaDB4j" % "mariaDB4j" % "2.2.2").excludes.log4j)

  //http client
  val scalajHttp = Seq("org.scalaj" %% "scalaj-http" % "2.3.0")

  val groovy  = Seq("org.codehaus.groovy" % "groovy" % "2.4.10")
  val logback = slf4j.slf4j ++ groovy ++ Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "ch.qos.logback" % "logback-core" % "1.2.3"
  )

  //common utensil
  val config     = Seq("com.typesafe" % "config" % "1.3.1")
  val pureconfig = Seq("com.github.pureconfig" %% "pureconfig" % "0.8.0")
  val jodaTime   = Seq("joda-time" % "joda-time" % "2.9.9")
  val nscalatime = Seq("com.github.nscala-time" %% "nscala-time" % "2.16.0")
  val akkaQuartz = Seq("com.enragedginger" %% "akka-quartz-scheduler" % "1.6.1-akka-2.5.x")
  val common     = config ++ jodaTime

  //test
  val scalameter = Seq("com.storm-enroute" %% "scalameter-core" % "0.8.2")
  val scalaTest  = Seq("org.scalatest" %% "scalatest" % "3.0.1")
  val test       = (scalameter ++ scalajHttp ++ scalaTest).map(_ % Test)

  val smileScala      = Seq("com.github.haifengl" %% "smile-scala" % "1.4.0")
  val akkaStreamKafka = Seq("com.typesafe.akka" %% "akka-stream-kafka" % "0.17")

  // python
  val jep      = Seq("black.ninia" % "jep" % "3.7.1")
  //io
  val csv      = Seq("com.github.tototoshi" %% "scala-csv" % "1.3.4")
  val file     = Seq("com.github.pathikrit" %% "better-files" % "2.17.1")
  val excel    = Seq(
    "org.apache.poi" % "poi" % "3.14",
    "org.apache.poi" % "poi-ooxml" % "3.14"
  )
  //parser
  val jsoup    = Seq("org.jsoup" % "jsoup" % "1.10.3")
  val org_json = Seq("org.json" % "json" % "20170516")

  val akkaPersistence = Seq(
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
    "org.iq80.leveldb" % "leveldb" % "0.7",
    "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
  )

  //
  //
  //
  object jackson {
    val core        = Seq("com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion)
    val databind    = Seq("com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion)
    val annotations = Seq("com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion)
    val joda        = Seq("com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % jacksonVersion)
    val jsr310      = Seq("com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion)
    val scala       = Seq("com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion)

    val jacksons = core ++ databind ++ annotations ++ joda ++ jsr310 ++ scala
  }

  object akka {
    val actor   = Seq("com.typesafe.akka" %% "akka-actor" % akkaVersion)
    val remote  = Seq("com.typesafe.akka" %% "akka-remote" % akkaVersion)
    val slf4j   = Seq("com.typesafe.akka" %% "akka-slf4j" % akkaVersion)
    val testkit = Seq("com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test)
    val typed   = Seq("com.typesafe.akka" %% "akka-typed" % akkaVersion)

    val actors = actor ++ remote ++ slf4j ++ testkit ++ typed
  }

  object akkaHttp {
    val core    = Seq("com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion)
    val http    = Seq("com.typesafe.akka" %% "akka-http" % akkaHttpVersion)
    val testkit = Seq("com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test)
    val https   = core ++ http ++ testkit
  }

  object play {
    val json = Seq(("com.typesafe.play" %% "play-json" % "2.6.3").excludes.jackson) ++ jackson.jacksons
    val ws   = Seq("com.typesafe.play" %% "play-ws" % "2.6.3")
  }

  object spark {
    val sql       = List("org.apache.spark" %% "spark-sql" % sparkVersion)
    val core      = List("org.apache.spark" %% "spark-core" % sparkVersion)
    val streaming = List("org.apache.spark" %% "spark-streaming" % sparkVersion)
    val milib     = List("org.apache.spark" %% "spark-mllib" % sparkVersion)
    val sparks    = (slf4j.log4jOver ++ sql ++ core ++ streaming ++ milib).map(_.excludes.log4j)
  }

  object apacheCommons {
    val text         = Seq("org.apache.commons" % "commons-text" % "1.0")
    val collections4 = Seq("org.apache.commons" % "commons-collections4" % "4.1")

  }

  implicit class WithByModule(m: ModuleID) {

    object excludes {
      private val matchAll = "*"

      def log4j: ModuleID = m
        .exclude(org = "log4j", name = matchAll)
        .exclude(org = matchAll, name = "slf4j-log4j12")

      def jackson: ModuleID = m
        .exclude(org = "com.fasterxml.jackson.core", name = matchAll)
        .exclude(org = "com.fasterxml.jackson.datatype", name = matchAll)
        .exclude(org = "com.fasterxml.jackson.module", name = matchAll)
    }

  }

  def totals =
    (
      Nil
        ++ slick
        ++ mysql
        ++ quill
        ++ mariaDB4j
        ++ scalajHttp
        ++ config
        ++ pureconfig
        ++ jodaTime
        ++ nscalatime
        ++ common
        ++ groovy
        ++ logback
        ++ scalameter
        ++ scalaTest
        ++ test
        ++ smileScala
        ++ akkaStreamKafka
        ++ csv
        ++ file
        ++ excel
        ++ jsoup
        ++ org_json
        ++ akkaPersistence
        ++ play.json
        ++ play.ws
        ++ akka.actors
        ++ apacheCommons.text
        ++ apacheCommons.collections4
        ++ spark.sparks
        ++ akkaHttp.https
        ++ jackson.jacksons
      )

}
