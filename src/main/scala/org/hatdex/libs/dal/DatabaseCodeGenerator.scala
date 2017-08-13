package org.hatdex.libs.dal

import com.typesafe.config.Config
import org.hatdex.libs.dal.SlickPostgresDriver.api._
import slick.jdbc.meta.MTable
import slick.model.Model

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DatabaseCodeGenerator(config: Config) {
  protected def modelFuture(database: String, excludedTables: Seq[String]): Future[Model] = {
    Database.forConfig(database, config).run {
      MTable.getTables(None, None, None, Some(Seq("TABLE", "VIEW"))) //TABLE, and VIEW represent metadata, i.e. get database objects which are tables and views
        .map(_.filterNot(t => excludedTables contains t.name.name))
        .flatMap(SlickPostgresDriver.createModelBuilder(_, ignoreInvalidDefaults = false).buildModel)
    }
  }

  def generate(outputDir: String, packageName: String, className: String = "Tables",
    database: String = "devdb", excludedTables: Seq[String] = Seq("databasechangelog", "databasechangeloglock")): Future[Unit] = {

    modelFuture(database, excludedTables)
      .map(model => new TypemappedPgCodeGenerator(model))
      .map(_.writeToFile("org.hatdex.libs.dal.SlickPostgresDriver", outputDir, packageName, className, s"$className.scala"))
  }
}
