/*
 * Copyright (C) HAT Data Exchange Ltd - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 8 2017
 */

package org.hatdex.libs.dal

import java.sql.Connection

import com.typesafe.config.Config
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.{ Contexts, LabelExpression, Liquibase }
import org.hatdex.libs.dal.SlickPostgresDriver.api.Database
import org.slf4j.{ Logger => Slf4jLogger }
import slick.driver.JdbcProfile

import scala.collection.JavaConverters._
import scala.concurrent.{ ExecutionContext, Future, blocking }
import scala.util.Try

/**
 * Runs Liquibase based database schema and data migrations. This is the only place for all related
 * modules to run updates from.
 *
 * Liquibase finds its files on the classpath and applies them to DB. If migration fails
 * this class will throw an exception and by default your application should not continue to run.
 *
 * It does not matter which module runs this migration first.
 */
trait BaseSchemaMigrationImpl extends SchemaMigration {

  protected val configuration: Config
  protected def db: JdbcProfile#Backend#Database
  protected val logger: Slf4jLogger
  protected implicit val ec: ExecutionContext

  def run(evolutionsConfig: String = "db.default.evolutions"): Future[Unit] = {
    Option(configuration.getStringList(evolutionsConfig)).map(_.asScala).map { migrations =>
      logger.info(s"Running database schema migrations on $migrations")
      run(migrations)
    } getOrElse {
      logger.warn("No evolutions configured")
      Future.successful(())
    }
  }

  /**
   * Invoke this method to apply all DB migrations.
   */
  def run(changeLogFiles: Seq[String]): Future[Unit] = {
    logger.info(s"Running schema migrations: ${changeLogFiles.mkString(", ")}")
    changeLogFiles.foldLeft(Future.successful(())) { (execution, evolution) => execution.flatMap { _ => updateDb(evolution) } }
  }

  def resetDatabase(): Future[Unit] = {
    val eventuallyEvolved = Future {
      val dbConnection = db.createSession().conn

      val liquibase = blocking {
        createLiquibase(dbConnection, "")
      }
      liquibase.getLog.setLogLevel("severe")
      blocking {
        Try(liquibase.dropAll())
          .recover {
            case e =>
              liquibase.forceReleaseLocks()
              logger.error(s"Error dropping all database information")
              throw e
          }
        liquibase.forceReleaseLocks()
      }
    }

    eventuallyEvolved onFailure {
      case e =>
        logger.error(s"Error updating database: ${e.getMessage}")
    }

    eventuallyEvolved
  }

  def rollback(changeLogFiles: Seq[String])(implicit db: Database): Future[Unit] = {
    logger.info(s"Rolling back schema migrations: ${changeLogFiles.mkString(", ")}")
    changeLogFiles.foldLeft(Future.successful(())) { (execution, evolution) => execution.flatMap { _ => updateDb(evolution) } }
  }

  private def updateDb(diffFilePath: String): Future[Unit] = {
    val eventuallyEvolved = Future {

      val dbConnection = db.createSession().conn

      logger.info(s"Liquibase running evolutions $diffFilePath on db: [${dbConnection.getMetaData.getURL}]")
      val changesets = "structuresonly,data"
      val liquibase = blocking {
        createLiquibase(dbConnection, diffFilePath)
      }
      liquibase.getLog.setLogLevel("severe")
      blocking {
        listChangesets(liquibase, new Contexts(changesets))
        Try(liquibase.update(changesets))
          .recover {
            case e =>
              liquibase.forceReleaseLocks()
              logger.error(s"Error executing schema evolutions: ${e.getMessage}")
              throw e
          }
        liquibase.forceReleaseLocks()
      }
    }

    eventuallyEvolved onFailure {
      case e =>
        logger.error(s"Error updating database: ${e.getMessage}")
    }

    eventuallyEvolved
  }

  private def rollbackDb(diffFilePath: String): Future[Unit] = {
    val eventuallyEvolved = Future {

      val dbConnection = db.createSession().conn

      logger.info(s"Liquibase rolling back evolutions $diffFilePath on db: [${dbConnection.getMetaData.getURL}]")
      val changesets = "structuresonly,data"
      val liquibase = blocking {
        createLiquibase(dbConnection, diffFilePath)
      }
      blocking {
        val contexts = new Contexts(changesets)
        val changesetsExecuted = liquibase.getChangeSetStatuses(contexts, new LabelExpression()).asScala.filterNot(_.getWillRun)
        Try(liquibase.rollback(changesetsExecuted.length, contexts, new LabelExpression()))
          .recover {
            case e =>
              liquibase.forceReleaseLocks()
              logger.error(s"Error executing schema evolutions: ${e.getMessage}")
              throw e
          }
        liquibase.forceReleaseLocks()
      }
    }

    eventuallyEvolved onFailure {
      case e =>
        logger.error(s"Error updating database: ${e.getMessage}")
    }

    eventuallyEvolved
  }

  private def listChangesets(liquibase: Liquibase, contexts: Contexts): Unit = {
    val changesetStatuses = liquibase.getChangeSetStatuses(contexts, new LabelExpression()).asScala

    logger.info("Existing changesets:")
    changesetStatuses.foreach { cs =>
      if (cs.getWillRun) {
        logger.info(s"${cs.getChangeSet.toString} will run")
      }
      else {
        logger.info(s"${cs.getChangeSet.toString} will not run - previously executed on ${cs.getDateLastExecuted}")
      }
    }
  }

  private def createLiquibase(dbConnection: Connection, diffFilePath: String): Liquibase = {
    val classLoader = configuration.getClass.getClassLoader
    val resourceAccessor = new ClassLoaderResourceAccessor(classLoader)

    val database = DatabaseFactory.getInstance()
      .findCorrectDatabaseImplementation(new JdbcConnection(dbConnection))
    database.setDefaultSchemaName("hat")
    database.setLiquibaseSchemaName("public")
    new Liquibase(diffFilePath, resourceAccessor, database)
  }
}
