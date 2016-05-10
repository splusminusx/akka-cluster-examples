package com.example

import akka.actor.{ReceiveTimeout, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.persistence.PersistentActor

import scala.concurrent.duration._


object Counter {

  case object Inc

  case object GetState

  case class State(id: String, counter: Int)

  case object Stop

  case object CounterIncremented

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

class Counter extends PersistentActor with ActorLogging {
  import Counter._
  import ShardRegion.Passivate

  private var counter = 0

  context.setReceiveTimeout(120.seconds)

  override def persistenceId: String = "counter-" + self.path.name

  override def receiveRecover: Receive = {
    case e @ CounterIncremented => updateState(e)
  }

  override def receiveCommand: Receive = {
    case Inc =>
      persist(CounterIncremented)(updateState)

    case GetState =>
      log.info(s"get state ${self.path.name} $counter")
      sender() ! State(self.path.name, counter)

    case ReceiveTimeout =>
      context.parent ! Passivate(stopMessage = Stop)

    case Stop =>
      context.stop(self)
  }

  private def updateState(event: CounterIncremented.type) = {
    counter += 1
    log.info(s"increment ${self.path.name} $counter")
  }
}
