package com.howtographql.scala.sangria.db.mappings

import com.howtographql.scala.sangria.datamodel.Vote
import slick.jdbc.H2Profile.api._
import com.howtographql.scala.sangria.db.mappings.customcolumntypes.dateTimeColumnType
import akka.http.scaladsl.model.DateTime

class VotesTable(tag: Tag) extends Table[Vote](tag, "VOTES"){
  def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def createdAt: Rep[DateTime] = column[DateTime]("CREATED_AT")
  def userId: Rep[Int] = column[Int]("USER_ID")
  def linkId: Rep[Int] = column[Int]("LINK_ID")

  def userFK = foreignKey("user_FK", userId, Users)(_.id)
  def linkFK = foreignKey("link_FK", linkId, Links)(_.id)

  def * = (id, userId, linkId, createdAt).mapTo[Vote]
}