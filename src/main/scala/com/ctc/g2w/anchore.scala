package com.ctc.g2w

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object anchore {
  type HttpConfig = zio.config.ZConfig[anchore.config.Http]

  object config {
    import zio.config._
    import ConfigDescriptor._

    case class Http(addr: String, port: Int) {
      def url(scheme: String = "http"): String = s"$scheme://$addr:$port"
    }
    val http: ConfigDescriptor[Http] =
      (
        string("ANCHORE_ADDR").default("localhost") |@|
          int("ANCHORE_PORT").default(8080)
      )(Http.apply, Http.unapply)
  }

  object api {
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

    case class PolicyRule(id: Option[String], gate: String, trigger: String, action: String)
    object PolicyRule extends DefaultJsonProtocol {
      implicit val format: RootJsonFormat[PolicyRule] = jsonFormat4(PolicyRule.apply)
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
        policy_id: Option[String],
        policy_ids: Option[List[String]],
        registry: String,
        repository: String,
        image: ImageRef
    )
    object MappingRule extends DefaultJsonProtocol {
      implicit val format: RootJsonFormat[MappingRule] = jsonFormat8(MappingRule.apply)
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
}
