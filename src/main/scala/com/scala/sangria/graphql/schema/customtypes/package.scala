package com.scala.sangria.graphql.schema

import akka.http.scaladsl.model.DateTime
import com.scala.sangria.datamodel.DateTimeCoerceViolation
import sangria.ast.StringValue
import sangria.schema.ScalarType

package object customtypes {
  implicit val GraphQLDateTime = ScalarType[DateTime](
    name = "DateTime",
    coerceOutput = (dt, _) => dt.toString,
    coerceInput = {
      case StringValue(dt, _, _ ) => DateTime.fromIsoDateTimeString(dt).toRight(DateTimeCoerceViolation)
      case _ => Left(DateTimeCoerceViolation)
    },
    coerceUserInput = { //5
      case s: String => DateTime.fromIsoDateTimeString(s).toRight(DateTimeCoerceViolation)
      case _ => Left(DateTimeCoerceViolation)
    }
  )
}
