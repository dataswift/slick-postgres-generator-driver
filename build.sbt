import sbt.Keys.sbtPlugin

lazy val driver = Project(
  id = "slick-postgres-driver",
  base = file("slick-postgres-driver")
).settings(
  name := "slick-postgres-driver",
  crossScalaVersions := Seq("2.12.4", "2.11.8")
)

lazy val plugin = Project(
  id = "sbt-slick-postgres-generator",
  base = file("sbt-slick-postgres-generator")
).settings(
  name := "sbt-slick-postgres-generator"
).dependsOn(driver)

val root = Project(
  id = "slick-postgres-generator-driver",
  base = file(".")
)
.settings(
  Defaults.coreDefaultSettings,
  publishLocal := {},
  publishM2 := {},
  publishArtifact := false
)
.aggregate(driver, plugin)
