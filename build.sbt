name := """activator-akka-sample"""

version := "2.3.4"

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.4"
)

site.settings

site.includeScaladoc()

