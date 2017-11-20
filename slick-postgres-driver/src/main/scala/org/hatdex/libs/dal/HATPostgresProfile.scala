/*
 * Copyright (C) $year HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 14/08/17 09:17
 */

package org.hatdex.libs.dal

import java.sql.Timestamp

import com.github.tminglei.slickpg._
import org.joda.time.DateTime
import play.api.libs.json.{ JsValue, Json }
import slick.jdbc.JdbcType

trait HATPostgresProfile extends ExPostgresProfile
  with PgArraySupport
  with PgDateSupportJoda
  with PgRangeSupport
  with PgHStoreSupport
  with PgSearchSupport
  with PgPlayJsonSupport {

  override val pgjson = "jsonb"
  override val api = new API {}
  override protected lazy val useTransactionForUpsert = false

  trait API extends super.API
    with ArrayImplicits
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
    def toJsonGeneric[T]: Rep[T] => Rep[JsValue] = SimpleFunction.unary[T, JsValue]("to_jsonb")
    def toJsonGenericOptional[T](c: Rep[T]) = SimpleFunction[Option[JsValue]]("to_jsonb").apply(Seq(c))
    val toTimestamp: Rep[Double] => Rep[Timestamp] = SimpleFunction.unary[Double, Timestamp]("to_timestamp")
    val datePart: (Rep[String], Rep[DateTime]) => Rep[String] = SimpleFunction.binary[String, DateTime, String]("date_part")
    val datePartTimestamp: (Rep[String], Rep[Timestamp]) => Rep[String] = SimpleFunction.binary[String, Timestamp, String]("date_part")
  }

}

object HATPostgresProfile extends HATPostgresProfile

trait SlickPostgresDriver extends HATPostgresProfile
object SlickPostgresDriver extends SlickPostgresDriver
