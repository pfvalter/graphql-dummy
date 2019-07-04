package com.scala.sangria.db

import java.util.UUID

import akka.http.scaladsl.model.DateTime
import com.scala.sangria.datamodel._
import com.scala.sangria.db.mappings._
import sangria.execution.deferred.{RelationIds, SimpleRelation}
import com.scala.sangria.db.mappings.customcolumntypes.dateTimeColumnType
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}

class DAO(db: Database) {
  /**
    * @return all links in the DB
    */
  def getAllLinks: Future[Seq[Link]] = db.run(Links.result)

  /**
    * @param ids a Seq of ids of Links to be retrieved
    * @return all the Link objects matching ids present in the Seq of ids given
    */
  def getLinksByIds(ids: Seq[Int]): Future[Seq[Link]] = db.run(
    Links.filter(_.id inSet ids).result
  )

  /**
    * @param userIds a Seq of userIds of Links to be retrieved
    * @return all the Link objects matching ids present in the Seq of userIds given
    */
  def getLinksByUserIds(userIds: Seq[Int]): Future[Seq[Link]] = db.run(
    Links.filter(_.postedBy inSet userIds).result
  )

  /**
    *
    * @param ids a Seq of ids of Users to be retrieved
    * @return all the User objects matching ids present in the Seq of ids given
    */
  def getUsersByIds(ids: Seq[Int]): Future[Seq[User]] = db.run(
    Users.filter(_.id inSet ids).result
  )

  /**
    *
    * @param ids a Seq of ids of Votes to be retrieved
    * @return all the Vote objects matching ids present in the Seq of the ids given
    */
  def getVotesByIds(ids: Seq[Int]): Future[Seq[Vote]] = db.run(
    Votes.filter(_.id inSet ids).result
  )

  /**
    *
    * @param relationIds a Seq of userIds/linkIds of Votes to be retrieved
    * @return all the Vote objects matching ids present in the Seq of userIds/linkIds given
    */
  def getVotesByRelationIds(relationIds: RelationIds[Vote]): Future[Seq[Vote]] = db.run(
    Votes.filter { vote =>
      relationIds.rawIds.collect({
        case (SimpleRelation("byUser"), ids) => vote.userId inSet ids.asInstanceOf[Seq[Int]]
        case (SimpleRelation("byLink"), ids) => vote.linkId inSet ids.asInstanceOf[Seq[Int]]
      }).foldLeft(true: Rep[Boolean])(_ || _)

    }.result
  )


  /**
    *
    * @param name name of the User to be created
    * @param authProvider authProvider data input object of the User to be created
    * @return The Created User
    */
  def createUser(name: String, authProvider: AuthProviderSignupData): Future[User] = {
    val newUser = User(0, name, authProvider.email.email, authProvider.email.password)

    val insertAndReturnUserQuery = (Users returning Users.map(_.id)) into {
      (user, id) => user.copy(id = id)
    }

    db.run {
      insertAndReturnUserQuery += newUser
    }
  }


  /**
    *
    * @param url url of the Link to be created
    * @param description description of the Link
    * @param postedBy id of the user who created the Link
    * @return
    */
  def createLink(url: String, description: String, postedBy: Int): Future[Link] = {

    val insertAndReturnLinkQuery = (Links returning Links.map(_.id)) into {
      (link, id) => link.copy(id = id)
    }
    db.run {
      insertAndReturnLinkQuery += Link(0, url, description, postedBy)
    }
  }


  /**
    *
    * @param linkId
    * @param userId
    * @return
    */
  def createVote(userId: Int, linkId: Int): Future[Vote] = {
    val insertAndReturnVoteQuery = (Votes returning Votes.map(_.id)) into {
      (vote, id) => vote.copy(id = id)
    }
    db.run {
      insertAndReturnVoteQuery += Vote(0, userId, linkId)
    }
  }


  /**
    *
    * @param email email of the User
    * @param password password of the User
    * @return The user, if found in the DB.
    */
  def login(email: String, password: String): Future[Option[User]] = db.run {
    Users.filter(u => u.email === email && u.password === password).result.headOption
  }


  /**
    *
    * @param userId
    * @return
    */
  def getValidTokenByUserId(userId: Int): Future[Option[AuthToken]] = db.run {
    AuthTokens.filter(at => at.userId === userId && at.expiryDate > DateTime.now).result.headOption
  }


  /**
    *
    * @param userId
    * @return
    */
  def generateNewToken(userId: Int): Future[AuthToken] = {
    implicit val ec: ExecutionContext = ExecutionContext.global

    val newToken = UUID.randomUUID()
    val refreshToken = UUID.randomUUID()

    val authToken: AuthToken = AuthToken(newToken.toString, refreshToken.toString, userId)

    db.run {
      AuthTokens += authToken
    }.map(_ => authToken)
  }

  /**
    * Method used to retrieve the user of a token, when valid
    * @param token
    * @return
    */
  def getUserByToken(token: String): Future[Option[User]] = {
    db.run{
      (
        for {
          userId <- AuthTokens.filter(entry => entry.token === token && entry.expiryDate > DateTime.now).map(_.userId)
          user <- Users.filter(_.id === userId)
        } yield user
      ).result.headOption
    }
  }


  /**
    * Method used to retrieve the user of a token, when valid
    * @param token
    * @return
    */
  def getUserByRefreshToken(token: String): Future[Option[User]] = {
    import com.scala.sangria.db.mappings.customcolumntypes.dateTimeColumnType

    db.run{
      (
        for {
          userId <- AuthTokens.filter(entry => entry.refreshToken === token && entry.expiryDate > DateTime.now).map(_.userId)
          user <- Users.filter(_.id === userId)
        } yield user
        ).result.headOption
    }
  }
}
