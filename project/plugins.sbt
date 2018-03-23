logLevel := Level.Warn

resolvers += Resolver.typesafeRepo("releases")

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.2")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.1")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

// S3 based SBT resolver
resolvers += Resolver.jcenterRepo
addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.18.0")
