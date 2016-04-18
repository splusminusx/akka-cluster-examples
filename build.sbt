name := "messenger"
version := "0.0.1"
scalaVersion := "2.11.8"

val akkaV = "2.4.4"

enablePlugins(JavaAppPackaging)

maintainer := "Roman Malygin"
packageSummary := s"Akka ${version.value} Server"

libraryDependencies ++= Seq(
  // Akka
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-cluster" % akkaV,
  "com.typesafe.akka" %% "akka-remote" % akkaV,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaV,
  // Akka Streams
  "com.typesafe.akka" %% "akka-stream" % akkaV,
  "com.typesafe.akka" %% "akka-http-core" % akkaV,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
  // Other
  "com.github.scopt" %% "scopt" % "3.2.0",
  // Cassandra Persistence
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.12"
)

// Create custom run tasks to start a seed and a cluster node
// http://www.scala-sbt.org/0.13.0/docs/faq.html#how-can-i-create-a-custom-run-task-in-addition-to-run
lazy val runSeed = taskKey[Unit]("Start the seed node on 127.0.0.1:2551")
fullRunTask(runSeed, Compile, "com.example.Main", "--seed")
fork in runSeed := true

javaOptions in runSeed ++= Seq(
    "-Dclustering.ip=127.0.0.1",
    "-Dclustering.port=2551"
)

lazy val runNode = taskKey[Unit]("Start a node on 127.0.0.1:2552")
fullRunTask(runNode, Compile, "com.example.Main", "127.0.0.1:2551")
fork in runNode := true

javaOptions in runNode ++= Seq(
    "-Dclustering.ip=127.0.0.1",
    "-Dclustering.port=2552"
)
