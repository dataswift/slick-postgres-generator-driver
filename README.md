# Scala API wrappers for the HAT

Slick PostgreSQL Code generator and Driver with useful extensions

## Usage

To use the sbt plugin in your project, add this to `plugins.sbt`:

```Scala
resolvers += "HAT Library Artifacts Snapshots" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com"
addSbtPlugin("org.hatdex" % "sbt-slick-postgres-generator" % "X.Y.Z")
```

Similarly, for the driver library, add it to your `build.sbt`:

```Scala
libraryDependencies += "org.hatdex" %% "slick-postgres-driver" % "X.Y.Z"
```

## Publishing

To publish the library:

```Bash
sbt +publish
```
