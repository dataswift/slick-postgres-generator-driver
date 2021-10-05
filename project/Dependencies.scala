/*
 * Copyright (C) $year HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 14/08/17 09:17
 */

import sbt._

object Dependencies {

  val resolvers = Seq(
    "Atlassian Releases" at "https://maven.atlassian.com/public/",
    Resolver.bintrayRepo("scalaz", "releases"),
    Resolver.sonatypeRepo("snapshots")
  )

  object Library {

    object Db {
      val liquibase = "org.liquibase" % "liquibase-maven-plugin" % "3.6.0"
    }

    object Slick {
      private val slickVersion = "3.3.3"
      val slick = "com.typesafe.slick" %% "slick" % slickVersion
      val slickHikari = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
      val slickCodegen = "com.typesafe.slick" %% "slick-codegen" % slickVersion
      val slick_pgV = "0.19.4"
      val slickPgCore = "com.github.tminglei" %% "slick-pg_core" % slick_pgV
      val slickPg = "com.github.tminglei" %% "slick-pg" % slick_pgV
      val slickPgJoda = "com.github.tminglei" %% "slick-pg_joda-time" % slick_pgV
      val slickPgJts = "com.github.tminglei" %% "slick-pg_jts" % slick_pgV
      val slickPgSprayJson = "com.github.tminglei" %% "slick-pg_spray-json" % slick_pgV
      val slickPgPlayJson = "com.github.tminglei" %% "slick-pg_play-json" % slick_pgV
      val slickPgDate2 = "com.github.tminglei" %% "slick-pg_date2" % slick_pgV
    }

    object Utils {
      val jodaTime = "joda-time" % "joda-time" % "2.9.9"
      val slf4j = "org.slf4j" % "slf4j-api" % "1.7.18"
    }

    object ScalaTest {
      private val version = "3.2.2"
      val test = "org.scalatest" %% "scalatest" % version % IntegrationTest
    }

    object TestContainers {
      private val version = "0.38.9"
      val scalaTest = "com.dimafeng" %% "testcontainers-scala-scalatest" % version % IntegrationTest
      val postgresql = "com.dimafeng" %% "testcontainers-scala-postgresql" % version % IntegrationTest
    }

  }

}
