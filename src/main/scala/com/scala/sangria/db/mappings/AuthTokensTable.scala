package com.scala.sangria.db.mappings

import slick.jdbc.H2Profile.api._
import com.scala.sangria.db.mappings.customcolumntypes.dateTimeColumnType
import akka.http.scaladsl.model.DateTime
import com.scala.sangria.datamodel.AuthToken

class AuthTokensTable(tag: Tag) extends Table[AuthToken](tag, "AUTH_TOKENS"){
  def token: Rep[String] = column[String]("TOKEN", O.PrimaryKey)
  def refreshToken: Rep[String] = column[String]("REFRESH_TOKEN")
  def userId: Rep[Int] = column[Int]("USER_ID")
  def expiryDate: Rep[DateTime] = column[DateTime]("EXPIRY_DATE")

  def userIdFK = foreignKey("userId_FK", userId, Users)(_.id)
  def idx = index("idx_user", userId, unique = true)

  def * = (token, refreshToken, userId, expiryDate).mapTo[AuthToken]
}
