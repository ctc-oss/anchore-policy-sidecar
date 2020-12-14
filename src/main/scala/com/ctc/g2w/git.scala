package com.ctc.g2w

import java.nio.file.Path

import zio._
import zio.blocking.Blocking
import zio.process.{Command, CommandError}

object git {
  case class Sha(value: String) {
    def short = Sha(value.take(8))
  }

  type Git = Has[Git.Service]
  object Git {
    trait Service {
      def version(): ZIO[Blocking, CommandError, String]
      def head(): ZIO[Blocking, CommandError, Sha]
      def pull(): ZIO[Blocking, CommandError, Sha] // updated sha
    }

    private type Head = Ref[String]
    private def svc(path: Path, head: Head): Git.Service =
      new Git.Service {
        def version(): ZIO[Blocking, CommandError, String] =
          git(path, "version").string

        def head(): ZIO[Blocking, CommandError, Sha] =
          git(path, "rev-parse", "HEAD").run.flatMap(_.stdout.string.map(_.trim).map(Sha))

        def pull(): ZIO[Blocking, CommandError, Sha] =
          git(path, "pull").run *> head()
      }

    def from(url: String, path: Path): ZLayer[Blocking, CommandError, Git] = {
      for {
        _ <- Command("git", "clone", url, path.toString).run
        sha <- git(path, "rev-parse", "HEAD").string
        ref <- Ref.make(sha)
      } yield svc(path, ref)
    }.toLayer

    def from(path: Path): ZLayer[Blocking, CommandError, Git] = {
      for {
        sha <- git(path, "rev-parse", "HEAD").string
        ref <- Ref.make(sha)
      } yield svc(path, ref)
    }.toLayer

    def version(): ZIO[Git with Blocking, CommandError, String] = ZIO.accessM(_.get.version())
    def head(): ZIO[Git with Blocking, CommandError, Sha] = ZIO.accessM(_.get.head())
    def pull(): ZIO[Git with Blocking, CommandError, Sha] = ZIO.accessM(_.get.pull())

    private def git(gitdir: Path, args: String*): Command =
      Command("git", args: _*).workingDirectory(gitdir.toFile)
  }
}
