import Dependencies._

libraryDependencies ++= Seq(
  Library.Db.liquibase,
  Library.Slick.slickCodegen,
  Library.Slick.slickHikari,
  Library.Slick.slickPg,
  Library.Slick.slickPgCore,
  Library.Slick.slickPgJoda,
  Library.Slick.slickPgPlayJson
)

sbtPlugin := true

publishTo := {
  val prefix = if (isSnapshot.value) "snapshots" else "releases"
  Some(s3resolver.value("HAT Library Artifacts " + prefix, s3("library-artifacts-" + prefix + ".hubofallthings.com")) withMavenPatterns)
}
