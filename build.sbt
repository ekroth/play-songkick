name := """play-songkick"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

lazy val errorhandling = RootProject(uri(s"file:////Users/ekroth/Documents/git/errorhandling"))

val akkaStreamVersion = "1.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %%  "akka-stream-experimental"            % akkaStreamVersion,
  "com.typesafe.akka" %%  "akka-http-core-experimental"         % akkaStreamVersion,
  "com.typesafe.akka" %%  "akka-http-experimental"              % akkaStreamVersion,
  "com.typesafe.akka" %%  "akka-http-spray-json-experimental"   % akkaStreamVersion,
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "org.scalaz" %% "scalaz-core" % "7.1.4",
  "org.typelevel" %% "scalaz-contrib-210" % "0.2"
)

lazy val root = (project in file("."))
  .aggregate(errorhandling)
  .dependsOn(errorhandling)
