package com.ctc.g2w

import java.nio.file.Path

import zio.blocking.Blocking
import zio.process.{Command, CommandError}
import zio.{Has, Layer, ZIO, ZLayer}

object git {
  case class Sha(value: String) {
    def short = Sha(value.take(8))
  }

  type Git = Has[Git.Service]
  object Git {
    trait Service {
      def head(): ZIO[Blocking, CommandError, Sha]
    }

    def make(path: Path): ZLayer[Blocking, CommandError, Git] =
      ZLayer
        .fromEffect(Command("git", "clone").run.map { _ =>
          def cmd(processName: String, args: String*): Command =
            Command(processName, args: _*).workingDirectory(path.toFile)

          new Git.Service {
            def head(): ZIO[Blocking, CommandError, Sha] =
              cmd("git", "rev-parse", "HEAD").run.flatMap(r => r.stdout.string.map(Sha))
          }
        })

    def live(path: Path): Layer[Blocking, Git] =
      ZLayer.succeed(
        new Git.Service {
          def cmd(processName: String, args: String*): Command =
            Command(processName, args: _*).workingDirectory(path.toFile)

          def head(): ZIO[Blocking, CommandError, Sha] =
            cmd("git", "rev-parse", "HEAD").run.flatMap(r => r.stdout.string.map(Sha))
        }
      )

    def head(): ZIO[Git with Blocking, CommandError, Sha] = ZIO.accessM(_.get.head())
  }
}
