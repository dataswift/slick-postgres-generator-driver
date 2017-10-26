import Dependencies._

libraryDependencies ++= Seq(
  Library.Db.liquibase,
  Library.Slick.slick,
  Library.Slick.slickHikari,
  Library.Slick.slickCodegen,
  Library.Slick.slickPg,
  Library.Slick.slickPgCore,
  Library.Slick.slickPgJoda,
  Library.Slick.slickPgPlayJson
)

publishTo := {
  val prefix = if (isSnapshot.value) "snapshots" else "releases"
  Some(s3resolver.value("HAT Library Artifacts " + prefix, s3("library-artifacts-" + prefix + ".hubofallthings.com")) withMavenPatterns)
}
