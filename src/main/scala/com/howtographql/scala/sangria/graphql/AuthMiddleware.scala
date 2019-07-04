package com.howtographql.scala.sangria.graphql

import com.howtographql.scala.sangria.MyContext
import com.howtographql.scala.sangria.datamodel.Authorized
import sangria.execution.{Middleware, MiddlewareBeforeField, MiddlewareQueryContext}
import sangria.schema.{Action, Context}

object AuthMiddleware extends Middleware[MyContext] with MiddlewareBeforeField[MyContext] {
  override type QueryVal = Unit
  override type FieldVal = Unit

  override def beforeQuery(context: MiddlewareQueryContext[MyContext, _, _]): Unit = ()

  override def afterQuery(queryVal: QueryVal, context: MiddlewareQueryContext[MyContext, _, _]): Unit = ()

  override def beforeField(
    queryVal: QueryVal,
    mctx: MiddlewareQueryContext[MyContext, _, _],
    context: Context[MyContext, _]
  ): (Unit, Option[Action[MyContext, _]]) = {
    val requireAuth = context.field.tags.contains(Authorized)

    if(requireAuth) context.ctx.ensureAuthenticated()

    continue
  }
}