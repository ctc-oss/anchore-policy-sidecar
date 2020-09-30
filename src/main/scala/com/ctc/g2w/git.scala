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
      def head(): ZIO[Blocking, CommandError, Sha]
    }

    private def cmd(gitdir: Path, processName: String, args: String*): Command =
      Command(processName, args: _*).workingDirectory(gitdir.toFile)

    type Head = Ref[String]

    private def svc(path: Path, head: Head): Git.Service =
      new Git.Service {
        def head(): ZIO[Blocking, CommandError, Sha] =
          cmd(path, "git", "rev-parse", "HEAD").run.flatMap(r => r.stdout.string.map(Sha))
      }

    def from(url: String, path: Path): ZLayer[Blocking, CommandError, Git] = {
      for {
        _ <- Command("git", "clone", url, path.toString).run
        sha <- cmd(path, "git", "rev-parse", "HEAD").string
        ref <- Ref.make(sha)
      } yield svc(path, ref)
    }.toLayer

    def from(path: Path): ZLayer[Blocking, CommandError, Git] = {
      for {
        sha <- cmd(path, "git", "rev-parse", "HEAD").string
        ref <- Ref.make(sha)
      } yield svc(path, ref)
    }.toLayer

    def head(): ZIO[Git with Blocking, CommandError, Sha] = ZIO.accessM(_.get.head())
  }
}
