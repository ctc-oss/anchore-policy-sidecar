package com.ctc.g2w.cfg

import zio.config._
import ConfigDescriptor._

object anchore {
  case class Http(addr: String, port: Int) {
    def url(scheme: String = "http"): String = s"$scheme://$addr:$port"
  }
  val http: ConfigDescriptor[Http] =
    (
      string("ANCHORE_ADDR").default("localhost") |@|
        int("ANCHORE_PORT").default(8228)
    )(Http.apply, Http.unapply)
}
