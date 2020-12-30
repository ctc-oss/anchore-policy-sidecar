package com.ctc.g2w.api

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object anchore {
  sealed trait Health
  object Health {
    object Ok extends Health {
      override def toString: String = "Ok"
    }
    object Fail extends Health {
      override def toString: String = "Fail"
    }
  }
  case class PolicyBundle(
      id: String,
      name: Option[String],
      comment: Option[String],
      version: String,
      whitelists: Option[List[Whitelist]],
      policies: List[Policy],
      mappings: List[MappingRule],
      whitelisted_images: Option[List[ImageSelectionRule]],
      blacklisted_images: Option[List[ImageSelectionRule]]
  )
  object PolicyBundle extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[PolicyBundle] = jsonFormat9(PolicyBundle.apply)
  }

  case class PolicyBundleRecord(
      policyId: Option[String],
      active: Option[Boolean],
      userId: Option[String],
      policy_source: Option[String],
      policybundle: Option[PolicyBundle]
  )
  object PolicyBundleRecord extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[PolicyBundleRecord] = jsonFormat5(PolicyBundleRecord.apply)
  }

  case class Policy(
      id: String,
      name: Option[String],
      comment: Option[String],
      version: String,
      rules: Option[List[PolicyRule]]
  )
  object Policy extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[Policy] = jsonFormat5(Policy.apply)
  }

  case class PolicyRule(id: Option[String], gate: String, trigger: String, action: String, params: List[RuleParam])
  object PolicyRule extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[PolicyRule] = jsonFormat5(PolicyRule.apply)
  }

  case class RuleParam(name: String, value: String)
  object RuleParam extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[RuleParam] = jsonFormat2(RuleParam.apply)
  }

  case class Whitelist(
      id: String,
      name: Option[String],
      version: String,
      comment: Option[String],
      items: Option[List[WhitelistItem]]
  )
  object Whitelist extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[Whitelist] = jsonFormat5(Whitelist.apply)
  }

  case class WhitelistItem(id: Option[String], gate: String, trigger_id: String)
  object WhitelistItem extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[WhitelistItem] = jsonFormat3(WhitelistItem.apply)
  }

  case class MappingRule(
      id: Option[String],
      name: String,
      whitelist_ids: Option[List[String]],
      policy_ids: Option[List[String]],
      registry: String,
      repository: String,
      image: ImageRef
  )
  object MappingRule extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[MappingRule] = jsonFormat7(MappingRule.apply)
  }

  case class ImageSelectionRule(
      id: Option[String],
      name: String,
      registry: String,
      repository: String,
      image: ImageRef
  )
  object ImageSelectionRule extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[ImageSelectionRule] = jsonFormat5(ImageSelectionRule.apply)
  }

  case class ImageRef(`type`: String, value: String)
  object ImageRef extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[ImageRef] = jsonFormat2(ImageRef.apply)
  }
}
