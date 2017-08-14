/*
 * Copyright (C) $year HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 14/08/17 09:17
 */

package org.hatdex.libs.dal

import org.slf4j.Logger

import scala.concurrent.Future

/**
 * Runs Liquibase based database schema and data migrations. This is the only place for all related
 * modules to run updates from.
 *
 * Liquibase finds its files on the classpath and applies them to DB. If migration fails
 * this class will throw an exception and by default your application should not continue to run.
 *
 * It does not matter which module runs this migration first.
 */
trait SchemaMigration {
  protected val logger: Logger

  /**
   * Invoke this method to apply all DB migrations.
   */
  def run(evolutionsConfig: String = "db.default.evolutions"): Future[Unit]

  /**
   * Invoke this method to apply selected DB migrations.
   */
  def run(changeLogFiles: Seq[String]): Future[Unit]

  /**
   * Invoke this method to reset the database by removing all tables and data from it
   */
  def resetDatabase(): Future[Unit]
}
