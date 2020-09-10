package com.ctc.g2w

import zio._

object boot extends scala.App {
  val app = for {
    _ <- ZIO.effectTotal(println("boom"))
  } yield ()

  Runtime.default.unsafeRun(app)
}
