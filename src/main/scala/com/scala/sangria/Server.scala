package com.scala.sangria

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import com.scala.sangria.graphql.GraphQLServer
import spray.json._

import scala.concurrent.Await
import scala.language.postfixOps

object Server extends App {

  val PORT = 8080

  implicit val actorSystem = ActorSystem("graphql-server")
  implicit val materializer = ActorMaterializer()

  import actorSystem.dispatcher

  scala.sys.addShutdownHook(() -> shutdown())

  //This route config allows the API served through POST /graphql (to be used by any client) or it serves the
  //graphiql.html file to any other Verb or route (i.e. GET / in a normal browser)
  val route: Route =
    (post & path("graphql")) {
      entity(as[JsValue]) { requestJson =>
        GraphQLServer.endpoint(requestJson)
      }
    } ~ {
      getFromResource("graphiql.html")
    }

  Http().bindAndHandle(route, "0.0.0.0", PORT)
  println(s"open a browser with URL: http://localhost:$PORT")


  def shutdown(): Unit = {
    actorSystem.terminate()
    Await.result(actorSystem.whenTerminated, scala.concurrent.duration.Duration.Inf)
  }
}
