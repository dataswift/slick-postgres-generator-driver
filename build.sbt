import Dependencies.Library

val scala212 = "2.12.15"
val scala213 = "2.13.4"

inThisBuild(
  List(
    scalaVersion := scala212
  )
)

lazy val commonSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    val prefix = if (isSnapshot.value) "snapshots" else "releases"
    Some(s"HAT Library Artifacts $prefix" at s"s3://library-artifacts-$prefix.hubofallthings.com")
  },
  resolvers += "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com",
  resolvers += "HAT Library Artifacts Snapshots" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-snapshots.hubofallthings.com"
)

lazy val driver = project
  .in(file("slick-postgres-driver"))
  .enablePlugins(BasicSettings)
  .settings(
    name := "slick-postgres-driver",
    commonSettings,
    crossScalaVersions := Seq(scala212, scala213),
    scalaVersion := scala212
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
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    fork in IntegrationTest := true,
    envVars in IntegrationTest := Map("TESTCONTAINERS_RYUK_DISABLED" -> "true")
  )

lazy val plugin = project
  .in(file("sbt-slick-postgres-generator"))
  .enablePlugins(BasicSettings)
  .settings(
    name := "sbt-slick-postgres-generator",
    sbtPlugin := true,
    commonSettings
  )
  .settings(
    libraryDependencies ++= Seq(
          Library.Db.liquibase,
          Library.Slick.slickCodegen,
          Library.Slick.slickHikari,
          Library.Slick.slickPg,
          Library.Slick.slickPgCore,
          Library.Slick.slickPgJoda,
          Library.Slick.slickPgPlayJson
        )
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
  .aggregate(driver, plugin)
