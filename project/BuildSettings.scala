/*
 * Copyright (C) $year HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 14/08/17 09:17
 */

import com.typesafe.sbt.SbtScalariform
import sbt.Keys._
import sbt._

////*******************************
//// Basic settings
////*******************************
object BasicSettings extends AutoPlugin {
  override def trigger = allRequirements

  override def projectSettings = Seq(
    organization := "org.hatdex",
    version := "0.0.5-SNAPSHOT",
    resolvers ++= Dependencies.resolvers,
    scalaVersion := Dependencies.Versions.scalaVersion,
    crossScalaVersions := Dependencies.Versions.crossScala,
    name := "slick-postgres-generator-driver",
    description := "Slick PostgreSQL Code generator and Driver with useful extensions",
    licenses += ("Mozilla Public License 2.0", url("https://www.mozilla.org/en-US/MPL/2.0")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/Hub-of-all-Things/slick-postgres-generator-driver"),
        "scm:git@github.com:Hub-of-all-Things/slick-postgres-generator-driver.git"
      )
    ),
    homepage := Some(url("https://hubofallthings.com")),
    developers := List(
      Developer(
        id    = "AndriusA",
        name  = "Andrius Aucinas",
        email = "andrius@smart-e.org",
        url   = url("http://smart-e.org")
      )
    ),
    scalacOptions ++= Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.
      "-Xfatal-warnings", // Fail the compilation if there are any warnings.
      "-Xlint", // Enable recommended additional warnings.
      "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
      "-Ywarn-dead-code", // Warn when dead code is identified.
      "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
      "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
      "-language:postfixOps", // Allow postfix operators
      "-Ywarn-numeric-widen" // Warn when numerics are widened.
      ),
    scalacOptions in Test ~= { (options: Seq[String]) =>
      options filterNot (_ == "-Ywarn-dead-code") // Allow dead code in tests (to support using mockito).
    },
    parallelExecution in Test := false,
    fork in Test := true,
    // Needed to avoid https://github.com/travis-ci/travis-ci/issues/3775 in forked tests
    // in Travis with `sudo: false`.
    // See https://github.com/sbt/sbt/issues/653
    // and https://github.com/travis-ci/travis-ci/issues/3775
    javaOptions += "-Xmx1G")
}

//*******************************
// Scalariform settings
//*******************************
object CodeFormatter extends AutoPlugin {

   import com.typesafe.sbt.SbtScalariform._
   import scalariform.formatter.preferences._

  lazy val prefs = Seq(
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
     .setPreference(FormatXml, false)
     .setPreference(DoubleIndentConstructorArguments, true)
     .setPreference(AlignSingleLineCaseStatements, true)
     .setPreference(CompactControlReadability, true)
     .setPreference(DanglingCloseParenthesis, Prevent))

   override def projectSettings = scalariformSettings ++ prefs
 }

//*******************************
// ScalaDoc settings
//*******************************
object Doc extends AutoPlugin {

  override def projectSettings = Seq(
    autoAPIMappings := true,
    apiURL := Some(url(s"http://hub-of-all-things.github.io/doc/${version.value}/")),
    apiMappings ++= {
      implicit val cp = (fullClasspath in Compile).value
      Map(
        scalaInstance.value.libraryJar -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
    })

  /**
   * Gets the JAR file for a package.
   *
   * @param organization The organization name.
   * @param name The name of the package.
   * @param cp The class path.
   * @return The file which points to the JAR.
   * @see http://stackoverflow.com/a/20919304/2153190
   */
  private def jarFor(organization: String, name: String)(implicit cp: Seq[Attributed[File]]): File = {
    (for {
      entry <- cp
      module <- entry.get(moduleID.key)
      if module.organization == organization
      if module.name.startsWith(name)
      jarFile = entry.data
    } yield jarFile).head
  }
}
