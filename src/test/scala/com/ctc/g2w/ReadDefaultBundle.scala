package com.ctc.g2w

import com.ctc.g2w.api.anchore.PolicyBundle
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{OptionValues, matchers}
import spray.json._

import scala.io.Source

class ReadDefaultBundle extends AnyWordSpec with OptionValues with matchers.should.Matchers {
  "default bundle" should {
    "load from file" in {
      val json = Source.fromResource("default-bundle.json").mkString
      val b = json.parseJson.convertTo[PolicyBundle]

      b.whitelists.value.length shouldBe 1
      b.policies.head.rules.value.length shouldBe 6
    }
  }
}
