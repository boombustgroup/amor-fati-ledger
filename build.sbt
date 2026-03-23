lazy val root = (project in file("."))
  .settings(
    name         := "amor-fati-ledger-poc",
    organization := "com.boombustgroup",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := "3.8.2",
    scalacOptions ++= Seq(
      "-Werror",
      "-Wunused:all",
      "-deprecation",
      "-feature",
      "-unchecked",
    ),
    libraryDependencies ++= Seq(
      "org.scalatest"  %% "scalatest"  % "3.2.19" % Test,
      "org.scalacheck" %% "scalacheck" % "1.18.1" % Test,
      "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test,
    ),
  )
