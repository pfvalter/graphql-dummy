package com.howtographql.scala.sangria.db

import akka.http.scaladsl.model.DateTime
import com.howtographql.scala.sangria.datamodel.{Link, User, Vote}
import com.howtographql.scala.sangria.db.mappings._
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await

object DBSchema {
  /**
    * Load schema and populate sample data withing this Sequence od DBActions
    */
  val databaseSetup = DBIO.seq(
    Users.schema.create,
    Links.schema.create,
    Votes.schema.create,
    AuthTokens.schema.create,

    Users forceInsertAll Seq(
      User(1, "mario", "mario@example.com", "s3cr3t"),
      User(2, "Fred", "fred@flinstones.com", "wilmalove")
    ),
    Links forceInsertAll Seq(
      Link(1, "http://howtographql.com", "Awesome community driven GraphQL tutorial", 1, DateTime(2017,9,12)),
      Link(2, "http://graphql.org", "Official GraphQL web page", 1, DateTime(2017,10,1)),
      Link(3, "https://facebook.github.io/graphql/", "GraphQL specification", 2, DateTime(2017,10,2))
    ),
    Votes forceInsertAll Seq(
      Vote(id = 1, userId = 1, linkId = 1),
      Vote(id = 2, userId = 1, linkId = 2),
      Vote(id = 3, userId = 1, linkId = 3),
      Vote(id = 4, userId = 2, linkId = 2),
    )
  )


  def createDatabase: DAO = {
    val db = Database.forConfig("h2mem")

    Await.result(db.run(databaseSetup), scala.concurrent.duration.Duration.Inf)

    new DAO(db)
  }
}
