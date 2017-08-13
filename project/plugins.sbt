logLevel := Level.Warn

resolvers += Resolver.typesafeRepo("releases")

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.0")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

// S3 based SBT resolver
resolvers += "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com"
addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.15.0")
