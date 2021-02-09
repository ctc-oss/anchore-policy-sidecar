package com.ctc.g2w

import zio.ZIO
import zio.test.Assertion._
import zio.test.{DefaultRunnableSpec, _}

import java.io.FileNotFoundException
import java.nio.file.Paths

object LoadGreylistSpec extends DefaultRunnableSpec {
  val repo = Option(getClass.getClassLoader.getResource("test-repo"))
    .map(_.getPath)
    .map(Paths.get(_))
    .getOrElse(throw new FileNotFoundException("repo-dir"))

  def spec =
    suite("greylist loading spec")(
      testM("load nested lists from repo") {
        for {
          count <- greylist
            .readAll(repo)
            .runCollect
            .map(_.length)
        } yield assert(count)(equalTo(4))
      },
      testM("resolve parent dependencies") {
        for {
          result <- greylist.full(repo)
          model = result.map(g => g.image -> g).toMap
          py36 <- ZIO.fromOption(model.get("redhat/python/python36:3.6"))
          pypimg <- ZIO.fromOption(py36.parent)
          pyparent <- ZIO.fromOption(model.get(pypimg))
        } yield assert(py36.whitelisted_vulnerabilities.containsSlice(pyparent.whitelisted_vulnerabilities))(isTrue)
      }
    )
}
