package com.ctc.g2w

import com.ctc.g2w.git.Git
import spray.json._
import zio.{IO, ZIO}

import java.nio.file.Files

object greylist {
  def readSingle(): ZIO[Git, Throwable, Greylist] =
    for {
      path <- Git.dir().map(_.resolve("redhat/ubi/ubi8/ubi8.greylist"))
      grey <- IO(Files.readAllBytes(path)).map(b => new String(b))
    } yield grey.parseJson.convertTo[Greylist]

  case class Vulnerability(vulnerability: String)
  object Vulnerability extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[Vulnerability] = jsonFormat1(Vulnerability.apply)
  }

  case class Greylist(image_name: String, image_tag: String, whitelisted_vulnerabilities: List[Vulnerability])
  object Greylist extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[Greylist] = jsonFormat3(Greylist.apply)
  }
}
