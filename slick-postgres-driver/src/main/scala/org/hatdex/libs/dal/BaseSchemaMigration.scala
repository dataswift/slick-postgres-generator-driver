package org.hatdex.libs.dal

import com.typesafe.config.Config
import slick.jdbc.JdbcProfile
import org.slf4j.{ Logger => Slf4jLogger }

import scala.concurrent.ExecutionContext

trait BaseSchemaMigration extends SchemaMigration {

  protected val configuration: Config

  protected def db: JdbcProfile#Backend#Database

  protected val logger: Slf4jLogger
  implicit protected val ec: ExecutionContext
  protected val changeContexts = "structuresonly,data"
  protected val defaultSchemaName = "hat"
  protected val liquibaseSchemaName = "public"

}
