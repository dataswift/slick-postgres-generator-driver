# Scala API wrappers for the HAT

Current Version: 0.0.2-SNAPSHOT

Slick PostgreSQL Code generator and Driver with useful extensions

## Usage

To use the sbt plugin in your project, add this to `plugins.sbt`:

```
    resolvers += "HAT Library Artifacts Snapshots" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-snapshots.hubofallthings.com"
    addSbtPlugin("org.hatdex" % "sbt-slick-postgres-generator" % "0.0.2-SNAPSHOT")
```

Similarly, for the driver library, add it to your `build.sbt`:

```
    libraryDependencies += "org.hatdex" %% "slick-postgres-driver" % "0.0.2-SNAPSHOT"
```

## Publishing

To publish sbt plugin:

    sbt "project sbt-slick-postgres-generator" "publish"

To publish the driver:

    sbt "project slick-postgres-driver" "+ publish"

