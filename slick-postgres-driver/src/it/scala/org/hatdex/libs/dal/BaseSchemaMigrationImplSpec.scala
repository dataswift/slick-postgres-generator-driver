package org.hatdex.libs.dal

import java.sql.ResultSet

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.collection.JavaConverters._
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}

class BaseSchemaMigrationImplSpec extends AnyFlatSpec with ForAllTestContainer with Matchers {

  override val container: PostgreSQLContainer = PostgreSQLContainer()

  it should "apply each schema evolution in order" in {
    val schemaMigration = new SchemaMigrationImpl(container)

    Await.ready(
      schemaMigration.run(Seq("evolution_01.sql", "evolution_02.sql")),
      10.seconds
    )

    verifyTableCreated(schemaMigration.db)
  }

  private def verifyTableCreated(db: JdbcProfile#Backend#Database) {
    val session = db.createSession()
    try {
      val rs = session.conn.createStatement().executeQuery("SELECT column_name, data_type FROM information_schema.columns WHERE table_name='table_one' ORDER BY ordinal_position")
      rsToIterator(rs).toList shouldBe List(
        "id" -> "integer",
        "value" -> "jsonb",
        "type" -> "character varying"
      )
    } finally session.close()
  }

  private def rsToIterator(rs: ResultSet): Iterator[(String, String)] =
    new Iterator[(String, String)] {
      def hasNext: Boolean = rs.next()
      def next(): (String, String) = rs.getString(1) -> rs.getString(2)
    }

}

private class SchemaMigrationImpl(container: PostgreSQLContainer)
  extends BaseSchemaMigrationImpl {

  override protected implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)
  override protected val configuration: Config = ConfigFactory.empty()

  override val db: JdbcProfile#Backend#Database = {
    val dbConfigKey = "dockerpg"
    val cfg = ConfigFactory.parseMap(
      Map(
        dbConfigKey -> Map(
          "url" -> container.container.getJdbcUrl,
          "driver" -> container.container.getDriverClassName,
          "connectionPool" -> "disabled",
          "keepAliveConnection" -> true,
          "user" -> container.container.getUsername,
          "password" -> container.container.getPassword
        ).asJava
      ).asJava
    )
    Database.forConfig("dockerpg", cfg)
  }

}
