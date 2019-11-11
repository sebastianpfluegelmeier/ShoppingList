import sys.process._
import scala.language.postfixOps

name := """shoppinglist"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.akka" %% "akka-actor" % "2.6.0",
  "org.postgresql" % "postgresql" % "9.3-1100-jdbc4",
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % Test,
  "com.typesafe.play" %% "play-slick" % "4.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.2",
  "com.h2database" % "h2" % "1.4.200",
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  "com.typesafe.play" %% "play-json" % "2.7.3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "mysql" % "mysql-connector-java" % "5.1.23"
)


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
