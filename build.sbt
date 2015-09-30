name := """play-songkick"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

lazy val errorhandling = RootProject(uri(s"file:////Users/ekroth/Documents/git/errorhandling"))

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "org.scalaz" %% "scalaz-core" % "7.1.4",
  "org.typelevel" %% "scalaz-contrib-210" % "0.2"
)

lazy val root = (project in file("."))
  .aggregate(errorhandling)
  .dependsOn(errorhandling)
