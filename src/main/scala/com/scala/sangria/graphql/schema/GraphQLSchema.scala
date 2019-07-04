package com.scala.sangria.graphql.schema

import com.scala.sangria.MyContext
import com.scala.sangria.datamodel._
import sangria.execution.deferred._
import sangria.schema.{Argument, Field, InputObjectType, IntType, ListInputType, ListType, ObjectType, OptionType, Schema, StringType, UpdateCtx, fields}
import com.scala.sangria.graphql.schema.customtypes.GraphQLDateTime
import sangria.macros.derive._
import sangria.marshalling.sprayJson._
import spray.json.DefaultJsonProtocol._


object GraphQLSchema {
  /*
   * Definition of the object types (to be returned).
   */
  lazy val UserType: ObjectType[Unit, User] = deriveObjectType[Unit, User](
    ReplaceField("createdAt", Field("createdAt", GraphQLDateTime, resolve = _.value.createdAt)),
    AddFields(
      Field("links", ListType(LinkType),
        resolve = c =>  linksFetcher.deferRelSeq(linkByUserRel, c.value.id)),
      Field("votes", ListType(VoteType),
        resolve = c =>  votesFetcher.deferRelSeq(votesByUserRel, c.value.id))
    )
  )

  lazy val LinkType: ObjectType[Unit, Link] = deriveObjectType[Unit, Link](
    ReplaceField("createdAt", Field("createdAt", GraphQLDateTime, resolve = _.value.createdAt)),
    ReplaceField("postedBy",
      Field("postedBy", UserType, resolve = c => usersFetcher.defer(c.value.postedBy))
    ),
    AddFields(
      Field("votes", ListType(VoteType), resolve = c => votesFetcher.deferRelSeq(votesByLinkRel, c.value.id))
    )
  )

  lazy val VoteType: ObjectType[Unit, Vote] = deriveObjectType[Unit, Vote](
    ReplaceField("createdAt", Field("createdAt", GraphQLDateTime, resolve = _.value.createdAt)),
    ReplaceField("userId",
      Field("user", UserType, resolve = c => usersFetcher.defer(c.value.userId))
    ),
    ReplaceField("linkId",
      Field("link", LinkType, resolve = c => linksFetcher.defer(c.value.linkId))
    )
  )

  lazy val AuthTokenType: ObjectType[Unit, AuthToken] = deriveObjectType[Unit, AuthToken](
    ExcludeFields("userId")
  )


  /*
   * Definition of objects to be received (for mutations):
   */
  implicit val authProviderEmailFormat = jsonFormat2(AuthProviderEmail)
  implicit val authProviderSignupDataFormat = jsonFormat1(AuthProviderSignupData)

  implicit val AuthProviderEmailInputType: InputObjectType[AuthProviderEmail] = deriveInputObjectType[AuthProviderEmail](
    InputObjectTypeName("AUTH_PROVIDER_EMAIL")
  )

  lazy val AuthProviderSignupDataInputType: InputObjectType[AuthProviderSignupData] = deriveInputObjectType[AuthProviderSignupData]()

  /*
   * Argument definition for mutation types:
   */
  val NameArg = Argument("name", StringType)
  val AuthProviderArg = Argument("authProvider", AuthProviderSignupDataInputType)

  val UrlArg = Argument("url", StringType)
  val DescArg = Argument("description", StringType)
  val PostedByArg = Argument("postedBy", IntType)

  val LinkIdArg = Argument("linkId", IntType)
  val UserIdArg = Argument("userId", IntType)

  val EmailArg = Argument("email", StringType)
  val PasswordArg = Argument("password", StringType)

  val AuthTokenArg = Argument("token", StringType)
  val RefreshTokenArg = Argument("refreshToken", StringType)


  /*
   * Query Data Relations and Fetchers
   */
  //Data Relations:
  val linkByUserRel: Relation[Link, Link, Int] = Relation[Link, Int]("byUser", l => Seq(l.postedBy))
  val votesByUserRel: Relation[Vote, Vote, Int] = Relation[Vote, Int]("byUser", v => Seq(v.userId))
  val votesByLinkRel: Relation[Vote, Vote, Int] = Relation[Vote, Int]("byLink", v => Seq(v.linkId))

  //Fetcher's are a specialized version of Deferred Resolver. They provide a high-level API easier to use and serve the
  // purpose of optimizing the resolution of fetched entities based on IDs or relations. They also, deduplicate entities
  // and caches the results and much more.
  val linksFetcher: Fetcher[MyContext, Link, Link, Int] = Fetcher.rel(
    (ctx: MyContext, ids: Seq[Int]) => ctx.dao.getLinksByIds(ids),
    (ctx: MyContext, ids: RelationIds[Link]) => ctx.dao.getLinksByUserIds(ids(linkByUserRel))
  )(HasId(_.id))

  val usersFetcher: Fetcher[MyContext, User, User, Int] = Fetcher(
    (ctx: MyContext, ids: Seq[Int]) => ctx.dao.getUsersByIds(ids)
  )(HasId(_.id))

  val votesFetcher: Fetcher[MyContext, Vote, Vote, Int] = Fetcher.rel(
    (ctx: MyContext, ids: Seq[Int]) => ctx.dao.getVotesByIds(ids),
    (ctx: MyContext, ids: RelationIds[Vote]) => ctx.dao.getVotesByRelationIds(ids)
  )(HasId(_.id))

  val Resolver: DeferredResolver[MyContext] = DeferredResolver.fetchers(linksFetcher, usersFetcher, votesFetcher)


  /*
   * Query Definition:
   */
  val QueryType = ObjectType(
    "Query",
    fields[MyContext, Unit](
      Field("allLinks", ListType(LinkType), resolve = context => context.ctx.dao.getAllLinks),
      Field("link", //name of the field in the DSL
        OptionType(LinkType), //GraphQL return type
        arguments = List(Argument("id", IntType)), //list of expected arguments defined by 'name' and 'type'
        resolve = context => linksFetcher.deferOpt(context.arg[Int]("id")) //resolver function to fetch the result of the query
      ),
      Field("links",
        ListType(LinkType),
        arguments = List(Argument("ids", ListInputType(IntType))),
        tags = Authorized :: Nil,
        resolve = context =>
          //linksFetcher.deferSeq(context.arg[Seq[Int]]("ids"))
          context.ctx.dao.getLinksByIds(context.arg[Seq[Int]]("ids"))
      ),
      Field("users",
        ListType(UserType),
        arguments = List(Argument("ids", ListInputType(IntType))),
        resolve = context => usersFetcher.deferSeq(context.arg[Seq[Int]]("ids"))
      ),
      Field("votes",
        ListType(VoteType),
        arguments = List(Argument("ids", ListInputType(IntType))),
        resolve = context => votesFetcher.deferSeq(context.arg[Seq[Int]]("ids"))
      ),
      Field("authenticate",
        UserType,
        arguments = List(Argument("token", StringType)),
        resolve = context => UpdateCtx(
          context.ctx.authenticate(context.arg[String]("token"))){ user =>
          context.ctx.copy(currentUser = Some(user))
        }
      )
    )
  )


  /*
   * Mutation definition:
   */
  val Mutation = ObjectType(
    "Mutation",
    fields[MyContext, Unit](
      Field("createUser",
        UserType,
        arguments = NameArg :: AuthProviderArg :: Nil,
        resolve = context => context.ctx.dao.createUser(context.arg(NameArg), context.arg(AuthProviderArg))
      ),
      Field("createLink",
        LinkType,
        arguments = UrlArg :: DescArg :: PostedByArg :: Nil,
        tags = Authorized :: Nil,
        resolve = context => context.ctx.dao.createLink(context.arg(UrlArg), context.arg(DescArg), context.arg(PostedByArg))
      ),
      Field("createVote",
        VoteType,
        arguments = UserIdArg :: LinkIdArg :: Nil,
        tags = Authorized :: Nil,
        resolve = context => context.ctx.dao.createVote(context.arg(UserIdArg), context.arg(LinkIdArg))
      ),
      Field("login",
        AuthTokenType,
        arguments = EmailArg :: PasswordArg :: Nil,
        resolve = context => context.ctx.login(context.arg(EmailArg), context.arg(PasswordArg))
      ),
      Field("authenticate",
        UserType,
        arguments = AuthTokenArg :: Nil,
        resolve = context => UpdateCtx(
          context.ctx.authenticate(context.arg(AuthTokenArg))){ user =>
          context.ctx.copy(currentUser = Some(user))
        }
      ),
      Field("refreshToken",
        AuthTokenType,
        arguments = RefreshTokenArg :: Nil,
        resolve = context => context.ctx.refreshToken(context.arg(RefreshTokenArg))
      )
    )
  )

  val SchemaDefinition = Schema(QueryType, Some(Mutation))
}
