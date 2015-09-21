name := """play-songkick"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
)
