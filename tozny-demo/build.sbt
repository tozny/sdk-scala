name := "tozny-demo"

version := "1.0"

scalaVersion := "2.11.4"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  "com.tozny"             %% "sdk-scala" % "1.0"
)