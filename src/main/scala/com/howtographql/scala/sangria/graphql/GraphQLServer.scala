package com.howtographql.scala.sangria.graphql

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import sangria.marshalling.sprayJson._
import com.howtographql.scala.sangria.MyContext
import com.howtographql.scala.sangria.db.DBSchema
import com.howtographql.scala.sangria.graphql.schema.GraphQLSchema
import sangria.ast.Document
import sangria.execution._
import sangria.parser.QueryParser
import spray.json.{JsObject, JsString, JsValue}

import com.howtographql.scala.sangria.datamodel.{AuthenticationException, AuthorizationException}
import sangria.execution.{ExceptionHandler => EHandler, _}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object GraphQLServer {

  //DB Connection Creation:
  private val dao = DBSchema.createDatabase


  def endpoint(requestJSON: JsValue)(implicit ec: ExecutionContext): Route = {
    val JsObject(fields) = requestJSON
    val JsString(query) = fields("query")

    //The query parser is implemented by Sangria itself.
    QueryParser.parse(query) match {
      case Success(queryAst) =>
        val operation = fields.get("operationName") collect {
          case JsString(op) => op
        }

        val variables = fields.get("variables") match {
          case Some(obj: JsObject) => obj
          case _ => JsObject.empty
        }

        complete(executeGraphQLQuery(queryAst, operation, variables))
      case Failure(error) =>
        complete(BadRequest, JsObject("error" -> JsString(error.getMessage)))
    }

  }

  private def executeGraphQLQuery(query: Document, operation: Option[String], vars: JsObject)(implicit ec: ExecutionContext) = {
    Executor.execute(
      GraphQLSchema.SchemaDefinition,
      query,
      MyContext(dao),
      variables = vars,
      operationName = operation,
      exceptionHandler = ErrorHandler,
      //deferredResolver = GraphQLSchema.Resolver, //line that points at a deferred resolver for optimization of queries.
      middleware = AuthMiddleware :: Nil
    ).map(OK -> _)
      .recover {
        case error: QueryAnalysisError => BadRequest -> error.resolveError
        case error: ErrorWithResolver => InternalServerError -> error.resolveError
      }
  }

  val ErrorHandler = EHandler {
    case (_, AuthenticationException(message)) ⇒ HandledException(message)
    case (_, AuthorizationException(message)) ⇒ HandledException(message)
  }
}
