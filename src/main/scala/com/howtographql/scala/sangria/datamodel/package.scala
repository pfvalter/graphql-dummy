package com.howtographql.scala.sangria

import akka.http.scaladsl.model.DateTime
import sangria.execution.FieldTag
import sangria.validation.Violation

package object datamodel {
  //Others:
  //Token expiration time in millis:
  val tokenExpirationTime = 1 * 3600 * 1000

  //Data Model Objects (DTOs):
  case class Link(id: Int, url: String, description: String, postedBy: Int, createdAt: DateTime = DateTime.now)
  case class User(id: Int, name: String, email: String, password: String, createdAt: DateTime = DateTime.now)
  case class Vote(id: Int, userId: Int, linkId: Int, createdAt: DateTime = DateTime.now)

  case class AuthToken(token: String, refreshToken: String, userId: Int, expiryDate: DateTime = DateTime.now.+(tokenExpirationTime))

  //Input Case Classes:
  case class AuthProviderSignupData(email: AuthProviderEmail)
  case class AuthProviderEmail(email: String, password: String)

  //Parsing Violations:
  case object DateTimeCoerceViolation extends Violation {
    override def errorMessage: String = "Error during parsing DateTime"
  }

  //Exceptions:
  case class AuthenticationException(message: String) extends Exception(message)
  case class AuthorizationException(message: String) extends Exception(message)

  //Tagged Statuses:
  case object Authorized extends FieldTag
}
