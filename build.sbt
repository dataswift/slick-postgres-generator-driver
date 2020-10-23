import Dependencies.Library

lazy val driver = project.in(file("slick-postgres-driver"))
  .enablePlugins(BasicSettings)
  .settings(
    name := "slick-postgres-driver",
    crossScalaVersions := Seq("2.12.12", "2.11.12")
  )
  .settings(
    libraryDependencies ++= Seq(
      Library.Db.liquibase,
      Library.Slick.slick,
      Library.Slick.slickHikari,
      Library.Slick.slickCodegen,
      Library.Slick.slickPg,
      Library.Slick.slickPgCore,
      Library.Slick.slickPgJoda,
      Library.Slick.slickPgPlayJson,
      Library.ScalaTest.test,
      Library.TestContainers.scalaTest,
      Library.TestContainers.postgresql
    )
  )
  .settings(
    publishTo := {
      val prefix = if (isSnapshot.value) "snapshots" else "releases"
      Some(s3resolver.value("HAT Library Artifacts " + prefix, s3("library-artifacts-" + prefix + ".hubofallthings.com")) withMavenPatterns)
    }
  )
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    fork in IntegrationTest := true,
    envVars in IntegrationTest := Map("TESTCONTAINERS_RYUK_DISABLED" -> "true")
  )


lazy val plugin = project.in(file("sbt-slick-postgres-generator"))
  .enablePlugins(BasicSettings)
  .settings(
    name := "sbt-slick-postgres-generator",
    sbtPlugin := true
  )
  .settings(
    libraryDependencies ++= Seq(
      Library.Db.liquibase,
      Library.Slick.slickCodegen,
      Library.Slick.slickHikari,
      Library.Slick.slickPg,
      Library.Slick.slickPgCore,
      Library.Slick.slickPgJoda,
      Library.Slick.slickPgPlayJson)
  )
  .settings(
    publishTo := {
      val prefix = if (isSnapshot.value) "snapshots" else "releases"
      Some(s3resolver.value("HAT Library Artifacts " + prefix, s3("library-artifacts-" + prefix + ".hubofallthings.com")) withMavenPatterns)
    }
  )
  .dependsOn(driver)

lazy val genDriver = project
  .in(file("."))
  .settings(
    publishLocal := {},
    publishM2 := {},
    publishArtifact := false,
    skip in publish := true
  )
  .settings(
    name := "slick-postgres-generator-driver"
  )
  .aggregate(
    driver,
    plugin)
