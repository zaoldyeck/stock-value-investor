name := "stock-value-investor"

version := "1.0"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "4.1.0",
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.0.4",
  "com.typesafe.play" %% "play-ws-standalone-json" % "1.0.4",
  "net.ruippeixotog" %% "scala-scraper" % "2.0.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test"
)
