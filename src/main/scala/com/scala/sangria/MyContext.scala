package com.scala.sangria

import com.scala.sangria.datamodel.{AuthToken, AuthenticationException, AuthorizationException, User}
import com.scala.sangria.db.DAO

import scala.concurrent._
import scala.concurrent.duration.Duration

case class MyContext(dao: DAO, currentUser: Option[User] = None){
  private def retrieveOrGenerateToken(userId: Int): AuthToken = {
    val validTokenOpt = Await.result(dao.getValidTokenByUserId(userId), Duration.Inf)
    validTokenOpt.getOrElse {
      val newToken = Await.result(dao.generateNewToken(userId), Duration.Inf)
      newToken
    }
  }

  def login(email: String, password: String): AuthToken = {
    val userOpt = Await.result(dao.login(email, password), Duration.Inf)
    userOpt.getOrElse(
      throw AuthenticationException("Email or password are incorrect!")
    )

    retrieveOrGenerateToken(userOpt.get.id)
  }

  def refreshToken(refreshToken: String): AuthToken = {
    val userOpt = Await.result(dao.getUserByRefreshToken(refreshToken), Duration.Inf)
    userOpt.getOrElse(
      throw AuthenticationException("The provided refresh token is not valid.")
    )

    retrieveOrGenerateToken(userOpt.get.id)
  }

  def authenticate(token: String): User = {
    println("heeeeeere!")
    val userOpt = Await.result(dao.getUserByToken(token), Duration.Inf)
    userOpt.getOrElse(
      throw AuthenticationException("Invalid token. Please refresh the token or login again!")
    )
  }


  def ensureAuthenticated(): Unit =
    if(currentUser.isEmpty)
      throw AuthorizationException("You do not have permission. Please sign in and query using the 'authenticate' action.")
}