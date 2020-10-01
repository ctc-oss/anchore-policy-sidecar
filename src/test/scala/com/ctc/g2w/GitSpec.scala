package com.ctc.g2w

import java.nio.file.Paths

import com.ctc.g2w.git.Git
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test._

object GitSpec extends DefaultRunnableSpec {
  import Git._

  val dep = Blocking.live >+> Git.from(Paths.get("/tmp")).mapError(TestFailure.fail)
  def spec =
    suite("GitSpec")(
      testM("version can be fetched") {
        for {
          v <- version()
        } yield assert(v)(startsWithString("git version 2"))
      }
    ).provideLayer(dep)
}
