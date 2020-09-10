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
  "dev.zio" %% "zio" % "1.0.0"
)

enablePlugins(GitVersioning, JavaServerAppPackaging)
