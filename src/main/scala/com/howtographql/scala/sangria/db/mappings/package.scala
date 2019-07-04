package com.howtographql.scala.sangria.db

import slick.lifted.TableQuery

package object mappings {
  val Links = TableQuery[LinksTable]
  val Users = TableQuery[UsersTable]
  val Votes = TableQuery[VotesTable]
  val AuthTokens = TableQuery[AuthTokensTable]
}
