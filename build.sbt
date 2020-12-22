import com.typesafe.sbt.packager.docker._

enablePlugins(GitVersioning, JavaServerAppPackaging, DockerPlugin)

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
  "dev.zio" %% "zio-config-typesafe" % "1.0.0-RC27",
  "com.lihaoyi" %% "requests" % "0.6.5",
  "io.spray" %% "spray-json" % "1.3.5",
  "dev.zio" %% "zio-test" % zioVersion % Test,
  "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.0" % Test
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

dockerUpdateLatest := true
dockerExposedPorts := Seq(9000)
dockerBaseImage := "adoptopenjdk/openjdk11:debianslim-jre"
dockerCommands ++= Seq(
  Cmd("USER", "root"),
  ExecCmd("RUN", "apt", "update"),
  ExecCmd("RUN", "apt", "install", "-y", "git"),
  ExecCmd("RUN", "apt", "clean"),
  Cmd("USER", "1001")
)
dockerUsername := Some("jwiii")
dockerRepository := {
  if (sys.env.exists {
        case ("uK8s", "1") => true
        case ("uK8s", "0") => false
        case ("uK8s", b)   => b.toBoolean
        case _                 => false
      }) Some("localhost:32000")
  else None
}
