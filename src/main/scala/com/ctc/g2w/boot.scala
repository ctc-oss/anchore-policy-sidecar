package com.ctc.g2w

import zio._
import zio.config.{ReadError, ZConfig, getConfig}
import zio.console._
import zio.system.System

object boot extends scala.App {
  val cfg: Layer[ReadError[String], anchore.HttpConfig] = System.live >>> ZConfig.fromSystemEnv(anchore.config.http)

  val app = for {
    c <- getConfig[anchore.config.Http]
    r <- ZIO.effect(requests.get(s"${c.url()}/health").statusCode).catchAll(_ => IO.succeed(999))
    _ <- putStrLn(s"result: $r")
  } yield ()

  Runtime.unsafeFromLayer(cfg ++ Console.live).unsafeRun(app)
}
