# Scala API wrappers for the HAT

Current Version: 0.0.7

Slick PostgreSQL Code generator and Driver with useful extensions

## Usage

To use the sbt plugin in your project, add this to `plugins.sbt`:

```
    resolvers += "HAT Library Artifacts Snapshots" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com"
    addSbtPlugin("org.hatdex" % "sbt-slick-postgres-generator" % "0.0.7")
```

Similarly, for the driver library, add it to your `build.sbt`:

```
    libraryDependencies += "org.hatdex" %% "slick-postgres-driver" % "0.0.7"
```

## Publishing

To publish the library:

    sbt +publish


