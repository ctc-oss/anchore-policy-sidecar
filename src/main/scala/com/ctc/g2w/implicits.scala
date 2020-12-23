package com.ctc.g2w

import com.ctc.g2w.anchore.api.{Whitelist, WhitelistItem}
import com.ctc.g2w.git.Sha
import com.ctc.g2w.greylist.Greylist

object implicits {
  implicit class RichGreylists(grey: Greylist) {
    def asWhitelist(version: Sha) = Whitelist(
      "SingleExample",
      Some("Single vulnerability example whitelist"),
      version.value,
      Some(version).map(_.short.value).map(sha => s"Generated using commit $sha"),
      Some(
        grey.whitelisted_vulnerabilities.map(v =>
          WhitelistItem(
            Some(v.vulnerability),
            "vulnerabilities",
            trigger_id = v.vulnerability
          )
        )
      )
    )
  }
}
