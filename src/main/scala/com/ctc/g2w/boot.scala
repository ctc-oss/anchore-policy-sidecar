package com.ctc.g2w

import com.ctc.g2w.anchore.OAuthResponse
import spray.json._
import zio._
import zio.config.{ReadError, ZConfig, getConfig}
import zio.console._
import zio.system.System

object boot extends scala.App {
  val cfg: Layer[ReadError[String], anchore.HttpConfig] = System.live >>> ZConfig.fromSystemEnv(anchore.config.http)

  val app = for {
    c <- getConfig[anchore.config.Http]
    hc <- ZIO.effect(requests.get(s"${c.url()}/health").statusCode).catchAll(_ => IO.succeed(999))
    _ <- putStrLn(s"result: $hc")
    auth <- ZIO.effect(requests.post(s"${c.url()}/oauth/token", data = anchore.defaultAuth).text().parseJson.convertTo[OAuthResponse])
    _ <- putStrLn(s"result: $auth")
    p <- ZIO.effect(requests.get(url = s"${c.url()}/policies", headers = auth.headers))
    _ <- putStrLn(s"result: $p")
  } yield ()

  Runtime.unsafeFromLayer(cfg ++ Console.live).unsafeRun(app)
}
