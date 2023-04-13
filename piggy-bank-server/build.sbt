val scala3Version = "3.2.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "piggy-bank-server",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies += "org.postgresql" % "postgresql" % "42.3.0",
    libraryDependencies += "io.circe" %% "circe-core" % "0.14.1",
    libraryDependencies += "io.circe" %% "circe-generic" % "0.14.1"

  )
