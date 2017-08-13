/*
 * Copyright (C) HAT Data Exchange Ltd - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 8 2017
 */

package org.hatdex.libs.dal

import java.sql.Timestamp
import java.time.ZonedDateTime

import com.github.tminglei.slickpg._
import play.api.libs.json.{ JsValue, Json }
import slick.jdbc.JdbcType

trait SlickPostgresDriver extends ExPostgresDriver
    with PgArraySupport
    with PgDateSupportJoda
    with PgRangeSupport
    with PgHStoreSupport
    with PgSearchSupport
    with PgPlayJsonSupport
    with PgPostGISSupport {

  override val pgjson = "jsonb"
  override val api = MyAPI
  override protected lazy val useTransactionForUpsert = false

  object MyAPI extends API with ArrayImplicits
      with DateTimeImplicits
      with RangeImplicits
      with HStoreImplicits
      with SearchImplicits
      with PlayJsonImplicits
      with SearchAssistants {

    implicit val playJsonArrayTypeMapper: DriverJdbcType[List[JsValue]] =
      new AdvancedArrayJdbcType[JsValue](
        pgjson,
        (s) => utils.SimpleArrayUtils.fromString[JsValue](Json.parse(_))(s).orNull,
        (v) => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)).to(_.toList)

    import scala.language.implicitConversions

    override implicit val playJsonTypeMapper: JdbcType[JsValue] =
      new GenericJdbcType[JsValue](
        pgjson,
        (v) => Json.parse(v),
        (v) => Json.stringify(v).replace("\\u0000", ""),
        hasLiteralForm = false)

    override implicit def playJsonColumnExtensionMethods(c: Rep[JsValue]): FixedJsonColumnExtensionMethods[JsValue, JsValue] = {
      new FixedJsonColumnExtensionMethods[JsValue, JsValue](c)
    }
    override implicit def playJsonOptionColumnExtensionMethods(c: Rep[Option[JsValue]]): FixedJsonColumnExtensionMethods[JsValue, Option[JsValue]] = {
      new FixedJsonColumnExtensionMethods[JsValue, Option[JsValue]](c)
    }

    class FixedJsonColumnExtensionMethods[JSONType, P1](override val c: Rep[P1])(
        implicit
        tm: JdbcType[JSONType]) extends JsonColumnExtensionMethods[JSONType, P1](c) {
      override def <@:[P2, R](c2: Rep[P2])(implicit om: o#arg[JSONType, P2]#to[Boolean, R]) = {
        om.column(jsonLib.ContainsBy, n, c2.toNode)
      }
    }

    val toJson: Rep[String] => Rep[JsValue] = SimpleFunction.unary[String, JsValue]("to_jsonb")
    val toTimestamp: Rep[Double] => Rep[Timestamp] = SimpleFunction.unary[Double, Timestamp]("to_timestamp")
    val datePart: (Rep[String], Rep[ZonedDateTime]) => Rep[String] = SimpleFunction.binary[String, ZonedDateTime, String]("date_part")
    val datePartTimestamp: (Rep[String], Rep[Timestamp]) => Rep[String] = SimpleFunction.binary[String, Timestamp, String]("date_part")
  }

}

object SlickPostgresDriver extends SlickPostgresDriver
