package com.example

import akka.pattern.ask
import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.FutureDirectives._
import akka.util.Timeout
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import scala.concurrent.duration._


import scala.concurrent.{ExecutionContextExecutor, Future}

case class NodeStatus(node: String, state: Int)

trait Protocol extends DefaultJsonProtocol{
  implicit val counterStateFormat = jsonFormat2(Counter.State.apply)
}

trait CounterService extends Protocol {

  implicit def executor: ExecutionContextExecutor
  implicit val timeout = Timeout(5 seconds)
  val counterRegion: ActorRef

  lazy val routes = {
    logRequestResult("messenger") {
      pathPrefix("state") {
        (get & path(Segment) & pathEndOrSingleSlash) { id =>
          complete { (counterRegion ? CounterRegion.GetState(id)).mapTo[Counter.State] }
        }
      } ~
      pathPrefix("inc") {
        (get & path(Segment) & pathEndOrSingleSlash) { id =>
          complete {
            counterRegion ! CounterRegion.Inc(id)
            OK
          }
        }
      }
    }
  }
}
