package com.ctc.g2w

import zio._
import zio.blocking.Blocking
import zio.config.getConfig
import zio.process.{Command, CommandError}

import java.nio.file.{Files, Path}

object git {
  type RepoConfig = zio.config.ZConfig[cfg.git.Repo]

  case class Sha(value: String) {
    def short = Sha(value.take(8))
  }

  type Git = Has[Git.Service]
  object Git {
    trait Service {
      def dir(): UIO[Path]
      def version(): ZIO[Blocking, CommandError, String]
      def head(): ZIO[Blocking, CommandError, Sha]
      def pull(): ZIO[Blocking, CommandError, Sha] // updated sha
    }

    private type Head = Ref[String]
    private def svc(path: Path, head: Head): Git.Service =
      new Git.Service {
        def dir(): UIO[Path] = IO.succeed(path)

        def version(): ZIO[Blocking, CommandError, String] =
          git(path, "version").string

        def head(): ZIO[Blocking, CommandError, Sha] =
          git(path, "rev-parse", "HEAD").run.flatMap(_.stdout.string.map(_.trim).map(Sha))

        def pull(): ZIO[Blocking, CommandError, Sha] =
          git(path, "pull").run *> head()
      }

    def fromConfig(): ZLayer[RepoConfig with Blocking, Throwable, Git] =
      ZLayer.fromAcquireRelease {
        for {
          repo <- getConfig[cfg.git.Repo].map(_.url)
          path <- IO.effect(Files.createTempDirectory(repo.split("/").last))
          ___ <- Command("git", "clone", repo, path.toString).run.tap(_.exitCode)
          sha <- git(path, "rev-parse", "--short", "5", "HEAD").run.flatMap(_.stdout.string)
          ref <- Ref.make(sha)
        } yield svc(path, ref)
      }(_.dir().map(println))

    def from(path: Path): ZLayer[Blocking, CommandError, Git] = {
      for {
        sha <- git(path, "rev-parse", "HEAD").string
        ref <- Ref.make(sha)
      } yield svc(path, ref)
    }.toLayer

    def dir(): ZIO[Git, Nothing, Path] = ZIO.accessM(_.get.dir())
    def version(): ZIO[Git with Blocking, CommandError, String] = ZIO.accessM(_.get.version())
    def head(): ZIO[Git with Blocking, CommandError, Sha] = ZIO.accessM(_.get.head())
    def pull(): ZIO[Git with Blocking, CommandError, Sha] = ZIO.accessM(_.get.pull())

    private def git(gitdir: Path, args: String*): Command =
      Command("git", args: _*).workingDirectory(gitdir.toFile)
  }

}
