import ReleaseTransformations._

lazy val catsCheckSettings = Seq(
  organization := "org.typelevel",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  homepage := Some(url("http://github.com/non/catscheck")),

  scalaVersion := "2.12.0",
  crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0"),

  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked"
  ),

  libraryDependencies ++= Seq(
    "org.scalacheck" %%% "scalacheck" % "1.13.4",
    "org.typelevel" %%% "cats" % "0.8.1",
    "org.typelevel" %%% "cats-laws" % "0.8.1" % "test",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test",
    "org.typelevel" %%% "discipline" % "0.7.2"      % "test"
  ),

  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := Function.const(false),

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("Snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("Releases" at nexus + "service/local/staging/deploy/maven2")
  },

  pomExtra := (
    <scm>
      <url>git@github.com:non/jawn.git</url>
      <connection>scm:git:git@github.com:non/jawn.git</connection>
    </scm>
    <developers>
      <developer>
        <id>d_m</id>
        <name>Erik Osheim</name>
        <url>http://github.com/non/</url>
      </developer>
    </developers>
  ),

  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges))

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false)

lazy val root = project
  .in(file("."))
  .aggregate(catsCheckJS, catsCheckJVM)
  .settings(name := "catsCheck-root")
  .settings(catsCheckSettings: _*)
  .settings(noPublish: _*)

lazy val catsCheck = crossProject
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(name := "catsCheck")
  .settings(catsCheckSettings: _*)

lazy val catsCheckJVM = catsCheck.jvm

lazy val catsCheckJS = catsCheck.js
