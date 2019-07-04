package com.howtographql.scala.sangria.db.mappings

import slick.jdbc.H2Profile.api._
import java.sql.Timestamp
import akka.http.scaladsl.model.DateTime

package object customcolumntypes {
  implicit val dateTimeColumnType = MappedColumnType.base[DateTime, Timestamp](
    dt => new Timestamp(dt.clicks),
    ts => DateTime(ts.getTime)
  )
}
