package com.ctc.g2w

import com.ctc.g2w.anchore.{AnchoreAPI, AnchoreAuth}
import zio._
import zio.config.{ReadError, ZConfig, getConfig}
import zio.console._
import zio.system.System

object boot extends scala.App {
  val HackConfigForNow = anchore.config.Http("localhost", 8228)

  val cfg: Layer[ReadError[String], anchore.HttpConfig] = System.live >>> ZConfig.fromSystemEnv(anchore.config.http)
  val api = AnchoreAPI.live(HackConfigForNow)
  val auth = AnchoreAuth.make(HackConfigForNow)
  val deps = cfg ++ auth ++ api ++ Console.live

  val app = for {
    c <- getConfig[anchore.config.Http]
    hc <- AnchoreAPI.health()
    _ <- putStrLn(s"result: $hc")
    token <- AnchoreAuth.token()
    _ <- putStrLn(s"result: $token")
    p <- ZIO.effect(requests.get(url = s"${c.url()}/policies", headers = Map("Authorization"->s"Bearer ${token.value}")))
    _ <- putStrLn(s"result: $p")
  } yield ()

  Runtime.unsafeFromLayer(deps).unsafeRun(app)
}
