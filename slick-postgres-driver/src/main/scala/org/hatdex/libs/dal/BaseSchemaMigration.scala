package org.hatdex.libs.dal

import com.typesafe.config.Config
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.{ Contexts, LabelExpression, Liquibase }
import slick.jdbc.JdbcProfile
import org.slf4j.{ Logger => Slf4jLogger }

import java.sql.Connection
import scala.concurrent.{ blocking, ExecutionContext, Future }
import scala.util.control.NonFatal

trait BaseSchemaMigration extends SchemaMigration {

  protected val configuration: Config

  protected def db: JdbcProfile#Backend#Database

  protected val logger: Slf4jLogger
  implicit protected val ec: ExecutionContext
  protected val changeContexts = "structuresonly,data"
  protected val defaultSchemaName = "hat"
  protected val liquibaseSchemaName = "public"

  def convertJavaList[T](list: java.util.List[T]): List[T]

  def run(evolutionsConfig: String = "db.default.evolutions"): Future[Unit] =
    Option(configuration.getStringList(evolutionsConfig))
      .map { migrations =>
        logger.info(s"Running database schema migrations on $migrations")
        run(convertJavaList(migrations))
      } getOrElse {
        logger.warn("No evolutions configured")
        Future.successful(())
      }

  /**
   * Invoke this method to apply all DB migrations.
   */
  def run(changeLogFiles: Seq[String]): Future[Unit] = {
    logger.info(s"Running schema migrations: ${changeLogFiles.mkString(", ")}")
    Future(db.createSession().conn).flatMap { dbConnection =>
      val sequencedEvolutions: Future[Unit] = changeLogFiles.foldLeft(Future.successful(())) { (execution, evolution) =>
        execution.flatMap(_ => updateDb(evolution, dbConnection))
      }
      sequencedEvolutions.onComplete(_ => dbConnection.close())
      sequencedEvolutions
    } recover {
      case e => logger.error("Running database evolutions failed", e)
    }
  }

  def resetDatabase(): Future[Unit] =
    Future {
      blocking {
        val dbConnection = db.createSession().conn
        val liquibase = createLiquibase(dbConnection, "")
        try liquibase.dropAll()
        catch {
          case NonFatal(th) =>
            logger.error("Error dropping all database information", th)
            throw th
        } finally liquibase.forceReleaseLocks()
      }
    }

  def rollback(changeLogFiles: Seq[String]): Future[Unit] = {
    logger.info(s"Rolling back schema migrations: ${changeLogFiles.mkString(", ")}")
    changeLogFiles.foldLeft(Future.successful(())) { (execution, evolution) =>
      execution.flatMap(_ => rollbackDb(evolution))
    }
  }

  private def updateDb(
    diffFilePath: String,
    dbConnection: Connection): Future[Unit] =
    Future {
      blocking {
        logger.info(s"Liquibase running evolutions $diffFilePath on db: [${dbConnection.getMetaData.getURL}]")
        val liquibase = createLiquibase(dbConnection, diffFilePath)
        listChangesets(liquibase, new Contexts(changeContexts))
        try liquibase.update(changeContexts)
        catch {
          case NonFatal(th) =>
            logger.error(s"Error executing schema evolutions: ${th.getMessage}")
            throw th
        } finally liquibase.forceReleaseLocks()
      }
    }

  private def rollbackDb(diffFilePath: String): Future[Unit] =
    Future {
      blocking {
        val dbConnection = db.createSession().conn
        logger.info(s"Liquibase rolling back evolutions $diffFilePath on db: [${dbConnection.getMetaData.getURL}]")
        val liquibase = createLiquibase(dbConnection, diffFilePath)
        val contexts = new Contexts(changeContexts)
        val changesetsExecuted =
          convertJavaList(liquibase.getChangeSetStatuses(contexts, new LabelExpression())).filterNot(_.getWillRun)
        try liquibase.rollback(changesetsExecuted.length, contexts, new LabelExpression())
        catch {
          case NonFatal(th) =>
            logger.error(s"Error rolling back schema evolutions: ${th.getMessage}")
            throw th
        } finally liquibase.forceReleaseLocks()
      }
    }

  private def listChangesets(
    liquibase: Liquibase,
    contexts: Contexts): Unit = {
    val changesetStatuses = convertJavaList(liquibase.getChangeSetStatuses(contexts, new LabelExpression()))
    logger.info("Existing changesets:")
    changesetStatuses.foreach { cs =>
      if (cs.getWillRun)
        logger.info(s"${cs.getChangeSet.toString} will run")
      else
        logger.info(s"${cs.getChangeSet.toString} will not run - previously executed on ${cs.getDateLastExecuted}")
    }
  }

  protected def createLiquibase(
    dbConnection: Connection,
    diffFilePath: String): Liquibase = {
    val classLoader = configuration.getClass.getClassLoader
    val resourceAccessor = new ClassLoaderResourceAccessor(classLoader)
    val database = DatabaseFactory
      .getInstance()
      .findCorrectDatabaseImplementation(new JdbcConnection(dbConnection))
    database.setDefaultSchemaName(defaultSchemaName)
    database.setLiquibaseSchemaName(liquibaseSchemaName)
    new Liquibase(diffFilePath, resourceAccessor, database)
  }

}
