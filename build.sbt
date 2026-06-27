ThisBuild / organization := "org.goldenport"
ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "3.3.7"
ThisBuild / publishMavenStyle := true

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

def ensurePublishAllowed(version: String): Unit = {
  if (version.endsWith("-SNAPSHOT"))
    sys.error(s"Refusing to publish SNAPSHOT goldenport-launcher-core version $version. Use publishLocal for development versions.")
}

def ensurePublishLocalAllowed(version: String): Unit = {
  if (!version.endsWith("-SNAPSHOT"))
    sys.error(s"Refusing to publishLocal release goldenport-launcher-core version $version. Use publish for public release versions.")
}

publish / skip := {
  ensurePublishAllowed(version.value)
  false
}

publishLocal / skip := {
  ensurePublishLocalAllowed(version.value)
  false
}

lazy val root = (project in file("."))
  .settings(
    name := "goldenport-launcher-core",
    publishTo := {
      val repo = baseDirectory.value / "maven-local"
      Some(Resolver.file("local-simplemodeling-maven", repo))
    },
    Compile / packageDoc / publishArtifact := false,
    Compile / doc / sources := Seq.empty
  )
