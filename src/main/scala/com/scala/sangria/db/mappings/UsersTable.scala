package com.scala.sangria.db.mappings

import com.scala.sangria.datamodel.User
import slick.jdbc.H2Profile.api._
import com.scala.sangria.db.mappings.customcolumntypes.dateTimeColumnType
import akka.http.scaladsl.model.DateTime


class UsersTable(tag: Tag) extends Table[User](tag, "USERS"){
  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name: Rep[String] = column[String]("NAME")
  def email: Rep[String] = column[String]("EMAIL")
  def password: Rep[String] = column[String]("PASSWORD")
  def createdAt: Rep[DateTime] = column[DateTime]("CREATED_AT")

  def * = (id, name, email, password, createdAt).mapTo[User]
}