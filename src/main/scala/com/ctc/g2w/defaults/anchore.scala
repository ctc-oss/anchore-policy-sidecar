package com.ctc.g2w.defaults

import com.ctc.g2w.api.anchore._

object anchore {
  val DefaultPolicy = Policy(
    id = "default-psc-policy",
    name = Some("Default Policy (psc"),
    comment = None,
    version = "1_0",
    rules = Some(
      List(
        rules.dockerfileBlacklistSSH,
        rules.dockerfileMissingHealthcheck,
        rules.vuledataStale,
        rules.vulndataUnavailable,
        rules.vulnWarnSeverity,
        rules.vulnStopSeverity
      )
    )
  )

  val DefaultGlobalWhitelist = Whitelist(
    id = "default-psc-whitelist",
    name = Some("Global Whitelist (psc)"),
    comment = None,
    version = "1_0",
    items = Some(List.empty)
  )

  val DefaultMapping = MappingRule(
    id = Some("default-psc-mapping"),
    name = "default (psc)",
    whitelist_ids = Some(List(DefaultGlobalWhitelist.id)),
    policy_ids = Some(List(DefaultPolicy.id)),
    registry = "*",
    repository = "*",
    image = ImageRef("tag", "*")
  )

  val DefaultBundle = PolicyBundle(
    id = "default-psc-bundle",
    name = Some("Default Bundle (psc)"),
    comment = None,
    version = "1_0",
    whitelists = Some(List(DefaultGlobalWhitelist)),
    policies = List(DefaultPolicy),
    mappings = List(DefaultMapping),
    whitelisted_images = None,
    blacklisted_images = None
  )

  object rules {
    val dockerfileBlacklistSSH = PolicyRule(
      None,
      gate = "dockerfile",
      action = "STOP",
      trigger = "exposed_ports",
      params = List(
        RuleParam("ports", "22"),
        RuleParam("type", "blacklist")
      )
    )

    val dockerfileMissingHealthcheck = PolicyRule(
      None,
      gate = "dockerfile",
      action = "WARN",
      trigger = "instruction",
      params = List(
        RuleParam("instruction", "HEALTHCHECK"),
        RuleParam("check", "not_exists")
      )
    )

    val vuledataStale = PolicyRule(
      None,
      gate = "vulnerabilities",
      action = "WARN",
      trigger = "stale_feed_data",
      params = List(
        RuleParam("max_days_since_sync", "2")
      )
    )

    val vulndataUnavailable = PolicyRule(
      None,
      gate = "vulnerabilities",
      action = "WARN",
      trigger = "vulnerability_data_unavailable",
      params = List.empty
    )

    val vulnWarnSeverity = PolicyRule(
      None,
      gate = "vulnerabilities",
      action = "WARN",
      trigger = "package",
      params = List(
        RuleParam("package_type", "all"),
        RuleParam("severity_comparison", "="),
        RuleParam("severity", "medium")
      )
    )

    val vulnStopSeverity = PolicyRule(
      None,
      gate = "vulnerabilities",
      action = "STOP",
      trigger = "package",
      params = List(
        RuleParam("package_type", "all"),
        RuleParam("severity_comparison", ">"),
        RuleParam("severity", "medium")
      )
    )
  }
}
