package com.ctc.g2w

import com.ctc.g2w.anchore.{AnchoreAPI, AnchoreAuth}
import com.ctc.g2w.git.Git
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.config.typesafe.TypesafeConfig
import zio.config.{ZConfig, getConfig}
import zio.console._
import zio.duration.durationInt
import zio.system.System

object boot extends scala.App {
  val HackConfigForNow = anchore.config.Http("localhost", 8228)

  val _auth = ZConfig.fromSystemEnv(anchore.config.http) >+> AnchoreAuth.make(HackConfigForNow)
  val _api = _auth >+> AnchoreAPI.live(HackConfigForNow)
  val _git = Blocking.live >+> TypesafeConfig.fromDefaultLoader(git.config.mode) >+> TypesafeConfig.fromDefaultLoader(git.config.repo) >+> Git.fromConfig()
  val deps = System.live >+> Console.live >+> Clock.live >+> _api >+> _git

  val app = for {
    mode <- getConfig[git.config.Mode]
    _ <- putStrLn(s"${mode}")
    repo <- getConfig[git.config.Repo]
    _ <- putStrLn(s"repo: ${repo.url}")
    hc <- AnchoreAPI.health()
    _ <- putStrLn(s"anchore health: $hc")
    token <- AnchoreAuth.token()
    _ <- putStrLn(s"anchore token: $token")
    p <- AnchoreAPI.policies()
    _ <- putStrLn(s"anchore policy: $p")
    sha <- Git.head()
    _ <- putStrLn(s"git sha: ${sha.value}")

    poll = for {
      frist <- Git.head().map(_.short)
      pulled <- Git.pull().map(_.short)
    } yield {
      println(s"$frist => $pulled")
    }
    _<- poll.repeat(Schedule.spaced(10.second))
  } yield ()

  Runtime.unsafeFromLayer(deps).unsafeRun(app)
}
