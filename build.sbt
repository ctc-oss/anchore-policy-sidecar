name := "anchore-g2w"
scalaVersion := "2.13.3"
git.useGitDescribe := true
scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-Wunused:imports",
  "-Xfatal-warnings",
  "-Xlint:_"
)

val zioVersion = "1.0.3"
libraryDependencies := Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-process" % "0.1.0",
  "dev.zio" %% "zio-config" % "1.0.0-RC27",
  "com.lihaoyi" %% "requests" % "0.6.5",
  "io.spray" %% "spray-json" % "1.3.5",
  "dev.zio" %% "zio-test" % zioVersion % Test,
  "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.0" % Test
)

enablePlugins(GitVersioning, JavaServerAppPackaging)
testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

dockerUpdateLatest := true
dockerExposedPorts := Seq(9000)
dockerBaseImage := "adoptopenjdk/openjdk11:debianslim-jre"
