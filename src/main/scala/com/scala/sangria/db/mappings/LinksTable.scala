package com.scala.sangria.db.mappings

import com.scala.sangria.datamodel.Link
import slick.jdbc.H2Profile.api._
import com.scala.sangria.db.mappings.customcolumntypes.dateTimeColumnType
import akka.http.scaladsl.model.DateTime


class LinksTable(tag: Tag) extends Table[Link](tag, "LINKS"){
  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def url: Rep[String] = column[String]("URL")
  def description: Rep[String] = column[String]("DESCRIPTION")
  def postedBy: Rep[Int] = column[Int]("USER_ID")
  def createdAt: Rep[DateTime] = column[DateTime]("CREATED_AT")

  def postedByFK = foreignKey("postedBy_FK", postedBy, Users)(_.id)

  def * = (id, url, description, postedBy, createdAt).mapTo[Link]
}

