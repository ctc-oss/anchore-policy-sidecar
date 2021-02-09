package com.ctc.g2w

import com.ctc.g2w.greylist.Greylist
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{matchers, OptionValues}
import spray.json._

import scala.io.Source
import scala.util.Try

class ReadGreylist extends AnyWordSpec with OptionValues with matchers.should.Matchers {
  "greylist" should {
    "load from file" in {
      val json = Source.fromResource("example.greylist").mkString
      val b = json.parseJson.convertTo[Greylist]

      println(b.whitelisted_vulnerabilities)
    }
  }

  "greylists" should {
    "load from file" in {
      var lists = Map.empty[String, Greylist]
      loadgl("example").foreach(gl => lists += gl.image -> gl)
      loadgl("example-w-child").foreach(gl => lists += gl.image -> gl)

    }
  }

  def loadgl(r: String): Option[Greylist] =
    Try {
      val json = Source.fromResource(s"$r.greylist").mkString
      json.parseJson.convertTo[Greylist]
    }.toOption
}
