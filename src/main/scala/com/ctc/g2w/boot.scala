package com.ctc.g2w

import com.ctc.g2w.anchore.{AnchoreAPI, AnchoreAuth}
import com.ctc.g2w.git.Git
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.config.{ReadError, ZConfig, getConfig}
import zio.console._
import zio.duration.durationInt
import zio.system.System

import java.nio.file.Paths

object boot extends scala.App {
  val HackConfigForNow = anchore.config.Http("localhost", 8228)

  val cfg: Layer[ReadError[String], anchore.HttpConfig] = System.live >>> ZConfig.fromSystemEnv(anchore.config.http)
  val api = AnchoreAPI.live(HackConfigForNow)
  val auth = AnchoreAuth.make(HackConfigForNow)
  val git = Git.from(Paths.get(sys.env("PWD")))
  val deps = Blocking.live >+> Console.live >+> Clock.live >+> cfg >+> auth >+> api >+>  git

  val app = for {
    c <- getConfig[anchore.config.Http]
    hc <- AnchoreAPI.health()
    _ <- putStrLn(s"result: $hc")
    token <- AnchoreAuth.token()
    _ <- putStrLn(s"result: $token")
    p <- AnchoreAPI.policies()
    _ <- putStrLn(s"result: $p")
    sha <- Git.head()
    _ <- putStrLn(s"result: ${sha.value}")

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
