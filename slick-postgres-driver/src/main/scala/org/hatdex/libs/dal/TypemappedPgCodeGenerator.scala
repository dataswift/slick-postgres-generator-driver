/*
 * Copyright (C) $year HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 14/08/17 09:17
 */

package org.hatdex.libs.dal

import slick.codegen.SourceCodeGenerator
import slick.model.Model
import slick.sql.SqlProfile.ColumnOption

class TypemappedPgCodeGenerator(model: Model) extends SourceCodeGenerator(model) {
  override def Table = new Table(_) { table =>
    override def Column = new Column(_) { column =>
      // customize db type -> scala type mapping, pls adjust it according to your environment
      override def rawType: String = model.tpe match {
        case "java.sql.Date" => "LocalDate"
        case "java.sql.Time" => "LocalTime"
        case "java.sql.Timestamp" => model.options.find(_.isInstanceOf[ColumnOption.SqlType]).map(_.asInstanceOf[ColumnOption.SqlType].typeName).map({
          case "timestamp" => "LocalDateTime"
          case "timestamptz" => "DateTime"
          case _ => "LocalDateTime"
        }).getOrElse("LocalDateTime")
        // currently, all types that's not built-in support were mapped to `String`
        case "String" => model.options.find(_.isInstanceOf[ColumnOption.SqlType]).map(_.asInstanceOf[ColumnOption.SqlType].typeName).map({
          case "hstore" => "Map[String, String]"
          //          case "geometry" => "com.vividsolutions.jts.geom.Geometry"
          case "int8[]" => "List[Long]"
          case "int4[]" => "List[Int]"
          case "text[]" => "List[String]"
          case "varchar[]" => "List[String]"
          case "varchar" => "String"
          case "_int4" => "List[Int]"
          case "_varchar" => "List[String]"
          case "_text" => "List[String]"
          case "jsonb" => "JsValue"
          case "_jsonb" => "List[JsValue]"
          case _ => "String"
        }).getOrElse("String")
        case "scala.collection.Seq" => model.options.find(_.isInstanceOf[ColumnOption.SqlType]).map(_.asInstanceOf[ColumnOption.SqlType].typeName).map({
          case "_text" => "List[String]"
          case "_varchar" => "List[String]"
          case "_int4" => "List[Int]"
          case "_int8" => "List[Long]"
          //          case "_jsonb" => "List[JsValue]"
          case _ => "String"
        }).getOrElse("String")
        case _ => super.rawType
      }
    }
  }

  // ensure to use our customized postgres driver at `import profile.simple._`
  override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]): String = {
    s"""
    |package $pkg
    |// AUTO-GENERATED Slick data model
    |/** Stand-alone Slick data model for immediate use */
    |object $container extends {
    |  val profile = $profile
    |} with $container
    |/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
    |trait ${container}${parentType.map(t => s" extends $t").getOrElse("")} {
    |  val profile: $profile
    |  import profile.api._
    |${indent(code)}
    |}
      """.stripMargin.trim()
  }
}
