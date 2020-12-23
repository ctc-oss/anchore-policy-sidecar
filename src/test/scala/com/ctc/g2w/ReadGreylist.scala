package com.ctc.g2w

import com.ctc.g2w.greylist.Greylist
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{OptionValues, matchers}
import spray.json._

import scala.io.Source

class ReadGreylist extends AnyWordSpec with OptionValues with matchers.should.Matchers {
  "greylist" should {
    "load from file" in {
      val json = Source.fromResource("example.greylist").mkString
      val b = json.parseJson.convertTo[Greylist]

      println(b.whitelisted_vulnerabilities)
    }
  }
}
