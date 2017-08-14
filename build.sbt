import sbt.Keys.sbtPlugin

lazy val driver = Project(
  id = "slick-postgres-driver",
  base = file("slick-postgres-driver")
)

lazy val plugin = Project(
  id = "sbt-slick-postgres-generator",
  base = file("sbt-slick-postgres-generator"),
  dependencies = Seq(driver % "compile->compile;test->test")
).settings(
  scalaVersion := "2.10.6",
  crossScalaVersions := Seq("2.10.6"),
  name := "sbt-slick-postgres-generator"
)

val root = Project(
  id = "slick-postgres-generator-driver",
  base = file("."),
  aggregate = Seq(
    driver,
    plugin
  ),
  settings = Defaults.coreDefaultSettings ++
    // APIDoc.settings ++
    Seq(
      publishLocal := {},
      publishM2 := {},
      publishArtifact := false
    )
)
