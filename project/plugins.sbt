logLevel := Level.Warn

resolvers += "FrugalMechanic Snapshots" at "s3://maven.frugalmechanic.com/snapshots"
addSbtPlugin("com.frugalmechanic" % "fm-sbt-s3-resolver" % "0.19.0")

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.2")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.1")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.1.1")
