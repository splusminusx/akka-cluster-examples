package com.example

import com.typesafe.config._
import scala.collection.JavaConversions._

/**
 * This configuration is intended to run in a docker environment
 * It won't work
 */
case class NodeConfig(
    isSeed: Boolean = false,
    seedNodes: Seq[String] = Seq.empty,
    contactPoint: String = "172.17.0.2:9042") {

  import ConfigFactory._
  import NodeConfig._

  // Initialize the config once
  lazy val config = asConfig()

  // Name of the ActorSystem
  lazy val clusterName = config getString CLUSTER_NAME_PATH

  /**
   * @return config
   */
  private def asConfig(): Config = {
    val config = load(
      getClass.getClassLoader,
      ConfigResolveOptions.defaults.setAllowUnresolved(true)
    )
    val name = config getString CLUSTER_NAME_PATH

    // which config should be used
    val configPath = if (isSeed) SEED_NODE else CLUSTER_NODE

    // use configured ip or get host ip if available
    val ip = if (config hasPath "clustering.ip") config getString "clustering.ip" else HostIP.load getOrElse "127.0.0.1"
    val ipValue = ConfigValueFactory fromAnyRef ip

    // use configured ip or get host ip if available
    val webIp = if (config hasPath "http.ip") config getString "http.ip" else HostIP.load getOrElse "127.0.0.1"
    val webIpValue = ConfigValueFactory fromAnyRef webIp

    // add seed nodes to config
    val seedNodesString = seedNodes.map { node =>
      s"""akka.cluster.seed-nodes += "akka.tcp://$name@$node""""
    }.mkString("\n")

    // add cassandra contact points to config
    val journalContactPointsString =
      s"""messenger-cassandra-journal.contact-points = ["$contactPoint"]"""

    val storeContactPointsString =
      s"""messenger-cassandra-snapshot-store.contact-points = ["$contactPoint"]"""

    val configString = List(seedNodesString,
      journalContactPointsString,
      storeContactPointsString).mkString("\n")

    // build the final config and resolve it
    (ConfigFactory parseString configString)
      .withValue("http.ip", webIpValue)
      .withValue("clustering.ip", ipValue)
      .withFallback(ConfigFactory parseResources configPath)
      .withFallback(config)
      .resolve
  }

}

object NodeConfig {
  /** static configuration for seed nodes*/
  val SEED_NODE = "node.seed.conf"

  /** static configuration for normal cluster nodes */
  val CLUSTER_NODE = "node.cluster.conf"

  /** where to find the name of the ActorSystem */
  private val CLUSTER_NAME_PATH = "clustering.cluster.name"

  /**
   * @return NodeConfig
   * @throw IllegalStateException - if the cli parameters could not be parsed
   */
  def parse(args: Seq[String]): Option[NodeConfig] = {

    val parser = new scopt.OptionParser[NodeConfig]("akka-docker") {
      head("akka-docker", "2.3.4")
      opt[Unit]("seed") action { (_, c) =>
        c.copy(isSeed = true)
      } text "set this flag to start this system as a seed node"
      opt[String]('c', "cassandra-contact-point")
        .required() valueName "<host:port>" action { (x, c) =>
          println(x)
          c.copy(contactPoint = x) } text "out is a required file property"
      arg[String]("<seed-node>...") unbounded () optional () action { (n, c) =>
        c.copy(seedNodes = c.seedNodes :+ n)
      } text "give a list of seed nodes like this: <ip>:<port> <ip>:<port>"
      checkConfig {
        case NodeConfig(false, Seq(), "") => failure("ClusterNodes need at least one seed node")
        case _                        => success
      }
    }
    // parser.parse returns Option[C]
    parser.parse(args, NodeConfig())
  }

}