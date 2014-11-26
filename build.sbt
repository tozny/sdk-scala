lazy val root = (project in file(".")).
  settings(
	name         := "sdk-scala",
	version      := "1.0",
	scalaVersion := "2.11.4",
	libraryDependencies ++= Seq(
	  "commons-codec"             % "commons-codec" % "1.6",
	  "org.apache.httpcomponents" % "httpclient"    % "4.3.+"
	)
  )

net.virtualvoid.sbt.graph.Plugin.graphSettings