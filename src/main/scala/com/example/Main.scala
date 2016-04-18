package com.example

import akka.actor._
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object Main extends App with CounterService {
  val nodeConfig: NodeConfig = NodeConfig.parse(args).get
  implicit val system: ActorSystem = ActorSystem(nodeConfig.clusterName, nodeConfig.config)
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val counterRegion = ClusterSharding(system).start(
    typeName = CounterRegion.name,
    entityProps = CounterRegion.props(),
    settings = ClusterShardingSettings(system),
    extractEntityId = CounterRegion.idExtractor,
    extractShardId = CounterRegion.shardResolver)

  system.actorOf(Props[MonitorActor], "cluster-monitor")
  Http().bindAndHandle(routes,
    nodeConfig.config.getString("http.ip"),
    nodeConfig.config.getInt("http.port"))

  system.log.info(s"ActorSystem ${system.name} started successfully")

}