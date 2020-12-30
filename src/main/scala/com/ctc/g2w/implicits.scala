package com.ctc.g2w

import com.ctc.g2w.api.anchore.{Whitelist, WhitelistItem}
import com.ctc.g2w.git.Sha
import com.ctc.g2w.greylist.Greylist

object implicits {
  implicit class RichGreylists(grey: Greylist) {
    def asWhitelist(version: Sha) = Whitelist(
      version.value,
      Some("Example whitelist"),
      "1_0",
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
