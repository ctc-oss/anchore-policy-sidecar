package com.ctc.g2w

import com.ctc.g2w.anchore.{AnchoreAPI, AnchoreAuth}
import com.ctc.g2w.api.exceptions.{ReadActivePolicyError, ReadActivePolicyVersionError}
import com.ctc.g2w.git.Git
import com.ctc.g2w.implicits._
import spray.json._
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.config.typesafe.TypesafeConfig
import zio.config.{ZConfig, getConfig}
import zio.console._
import zio.duration.durationInt
import zio.system.System

object boot extends scala.App {
  val HackConfigForNow = cfg.anchore.Http("localhost", 8228)

  val _auth = ZConfig.fromSystemEnv(cfg.anchore.http) >+> AnchoreAuth.make(HackConfigForNow)
  val _api = _auth >+> AnchoreAPI.live(HackConfigForNow)
  val _git = Blocking.live >+> TypesafeConfig.fromDefaultLoader(cfg.git.mode) >+> TypesafeConfig.fromDefaultLoader(
    cfg.git.repo
  ) >+> Git.fromConfig()

  val app = for {
    mode <- getConfig[cfg.git.Mode]
    _ <- putStrLn(s"${mode}")
    repo <- getConfig[cfg.git.Repo]
    _ <- putStrLn(s"repo: ${repo.url}")
    hc <- AnchoreAPI.health() // todo;; backoff
    _ <- putStrLn(s"anchore health: $hc")

    policy <- AnchoreAPI
      .activePolicy()
      .bimap(_ => ReadActivePolicyError(), Ref.make).flatten

    version <- policy.get.map(_.policybundle.map(_.version))
      .someOrFail(ReadActivePolicyVersionError())
      .flatMap(Ref.make)

    poll = for {
      head <- Git.pull()

      update = for {
        wl <- greylist.readSingle().map(_.asWhitelist(head))
        _ <- putStrLn(wl.toJson.prettyPrint)
        // todo;; send the whitelist to anchore...
      } yield ()

      h = head.value
      _ <- version.get.flatMap {
        case v if v == h =>
          putStrLn(s"existing policy is up to date @ $h")
        case v =>
          for {
            _ <- putStrLn(s"updating policy from $v to $h...")
            _ <- update.andThen(version.set(h))
          } yield ()
      }

    } yield ()

    _ <- poll.repeat(Schedule.spaced(10.second))
  } yield ()

  Runtime
    .unsafeFromLayer(
      System.live >+> Console.live >+> Clock.live >+> _api >+> _git
    )
    .unsafeRun(app)
}
