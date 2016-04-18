package com.example

import akka.actor.{ActorLogging, Props, Actor}
import akka.cluster.sharding.ShardRegion

object Counter {

  case object Inc

  case object GetState

  case class State(id: String, counter: Int)

}

object CounterRegion {

  val nOfShards = 100
  val name = "CounterRegion"

  case class Inc(id: String)
  case class GetState(id: String)

  val idExtractor: ShardRegion.ExtractEntityId = {
    case Inc(id)      => (id, Counter.Inc)
    case GetState(id) => (id, Counter.GetState)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case Inc(id)      => (id.hashCode % nOfShards).toString
    case GetState(id) => (id.hashCode % nOfShards).toString
  }

  def props(): Props = Props(new Counter())

}

class Counter extends Actor with ActorLogging {
  import Counter._

  private var counter = 0

  override def receive: Receive = {
    case Inc =>
      counter += 1
      log.info(s"increment ${self.path.name} $counter")
    case GetState =>
      log.info(s"get state ${self.path.name} $counter")
      sender() ! State(self.path.name, counter)
  }

}
