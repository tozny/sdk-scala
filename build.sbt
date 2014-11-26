lazy val root = (project in file(".")).
  settings(
    organization := "com.tozny",
    name         := "sdk-scala",
    version      := "1.0",
    scalaVersion := "2.11.4",
    resolvers ++= Seq(
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
      ),
    libraryDependencies ++= Seq(
      "commons-codec"             % "commons-codec" % "1.6",
      "org.apache.httpcomponents" % "httpclient"    % "4.3.+",
      "com.typesafe.play"        %% "play-json"     % "2.3.+"
      )
    )

lazy val demo = (project in file("tozny-demo")).
  settings(
    name         := "tozny-demo",
    version      := "1.0",
    scalaVersion := "2.11.4"
  ).
  dependsOn(root).
  enablePlugins(PlayScala)


net.virtualvoid.sbt.graph.Plugin.graphSettings
