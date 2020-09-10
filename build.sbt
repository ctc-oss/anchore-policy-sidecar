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

libraryDependencies := Seq(
  "dev.zio" %% "zio" % "1.0.0",
  "io.spray" %%  "spray-json" % "1.3.5",
  "org.scalatest" %% "scalatest" % "3.2.0" % Test
)

enablePlugins(GitVersioning, JavaServerAppPackaging)
