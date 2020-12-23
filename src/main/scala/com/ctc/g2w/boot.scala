package com.ctc.g2w

import com.ctc.g2w.anchore.{AnchoreAPI, AnchoreAuth}
import com.ctc.g2w.api.anchore.{PolicyBundle, PolicyBundleRecord}
import com.ctc.g2w.api.exceptions.InitError
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
      .bimap({ e =>
         InitError("no active policy found", e)
      }, Ref.make)

    poll = for {
      head <- Git.head() // todo;; Ref, created outside of poll
      pull <- Git.pull()

      update = for {
        single <- greylist.readSingle()
        _ <- putStrLn(s"$head => $pull")
        white = single.asWhitelist(pull)
        _ <- putStrLn(white.toJson.prettyPrint)
        // todo;; send the whitelist to anchore...
      } yield ()

      _ <- policy.flatMap(_.get).flatMap {
        case PolicyBundleRecord(_, _, _, _, Some(PolicyBundle(_, _, _, _, Some(wl), _, _, _, _)))
            if wl.exists(_.version == pull.value) =>
          putStrLn("existing policy is up to date")
        case _ =>
          for {
            _ <- putStrLn("updating policy...")
            _ <- update
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
