/*
 * Copyright (C) $year HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 14/08/17 09:17
 */

package org.hatdex.libs.dal

import java.io.File
import java.sql.Connection

import com.typesafe.config.{ Config, ConfigFactory }
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.FileSystemResourceAccessor
import org.hatdex.libs.dal.HATPostgresProfile.api._
import org.slf4j.LoggerFactory
import sbt.Keys._
import sbt._
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext }

object SlickCodeGeneratorPlugin extends AutoPlugin {
  // This plugin is automatically enabled for projects
  //  override def trigger = allRequirements

  // by defining autoImport, the settings are automatically imported into user's `*.sbt`
  object autoImport {
    // configuration points, like the built-in `version`, `libraryDependencies`, or `compile`
    val gentables = taskKey[Seq[File]]("Generates tables.")
    val updateTestDb = taskKey[String]("Updates testing db.")

    val codegenBaseDir = settingKey[String]("Directory to output the generated DAL file")
    val codegenPackageName = settingKey[String]("Package for the generated DAL file")
    val codegenClassName = settingKey[String]("Class name for the generated DAL file")
    val codegenExcludedTables = settingKey[Seq[String]]("List of tables excluded from generating code for")
    val codegenDatabase = settingKey[String]("Live database from which structures are retrieved")
    val codegenConfig = settingKey[String]("Configuration to use for the code generator")
    val codegenEvolutions = settingKey[String]("Configuration to use for database evolutions")

    // default values for the tasks and settings
    lazy val codegenSettings: Seq[Def.Setting[_]] = Seq(
      gentables := {
        Generator(
          ConfigFactory.load(ConfigFactory.parseFile((resourceDirectory in Compile).value / (codegenConfig in gentables).value)), // puts reference.conf underneath,
          (codegenBaseDir in gentables).value,
          (codegenPackageName in gentables).value,
          (codegenClassName in gentables).value,
          (codegenDatabase in gentables).value,
          (codegenExcludedTables in gentables).value)
      },
      updateTestDb := {
        Updater(
          ConfigFactory.load(ConfigFactory.parseFile((resourceDirectory in Compile).value / (codegenConfig in gentables).value)), // puts reference.conf underneath,
          (resourceDirectory in Compile).value.getPath,
          (codegenDatabase in gentables).value,
          (codegenEvolutions in gentables).value)
      })
  }

  import autoImport._

  // a group of settings that are automatically added to projects.
  override lazy val projectSettings: Seq[Def.Setting[_]] = inConfig(Compile)(codegenSettings)

  object Updater {
    def apply(config: Config, codegenBaseDir: String, database: String, codegenEvolutions: String): String = {

      val schemaMigration = new BaseSchemaMigrationImpl {
        protected val configuration: Config = config
        val db: JdbcProfile#Backend#Database = Database.forConfig(database, config)
        implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
        protected val logger: org.slf4j.Logger = LoggerFactory.getLogger(this.getClass)

        override protected def createLiquibase(dbConnection: Connection, diffFilePath: String): Liquibase = {
          val resourceAccessor = new FileSystemResourceAccessor(codegenBaseDir)

          val database = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(new JdbcConnection(dbConnection))
          database.setDefaultSchemaName(defaultSchemaName)
          database.setLiquibaseSchemaName(liquibaseSchemaName)

          new Liquibase(diffFilePath, resourceAccessor, database)
        }
      }

      val eventuallyUpdated = schemaMigration.run(codegenEvolutions)

      Await.result(eventuallyUpdated, 5.minutes)
      s"Evolved $database"
    }
  }

  object Generator {
    def apply(config: Config, outputDir: String, packageName: String, className: String,
      database: String, excludedTables: Seq[String]): Seq[File] = {

      val eventuallyGenerated = new DatabaseCodeGenerator(config)
        .generate(outputDir, packageName, className, database, excludedTables)
      Await.result(eventuallyGenerated, 5.minutes)
      val fname = outputDir + "/" + packageName.replace('.', '/') + "/Tables.scala"
      Seq(file(fname))
    }
  }
}