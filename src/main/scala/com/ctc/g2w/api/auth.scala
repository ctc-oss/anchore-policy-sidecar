package com.ctc.g2w.api

import requests.RequestBlob
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import java.time.Instant

object auth {
  // grant_type=password&client_id=anonymous&username=admin&password=foobar
  val Default = OAuthCredentials("anonymous", "admin", "foobar")

  case class OAuthCredentials(client_id: String, username: String, password: String)
  object OAuthCredentials {
    implicit def oauthRequestBlob(oa: OAuthCredentials): RequestBlob = RequestBlob.FormEncodedRequestBlob(
      Seq("client_id" -> oa.client_id, "username" -> oa.username, "password" -> oa.password, "grant_type" -> "password")
    )
  }

  case class OAuthToken(value: String, expires: Instant)
  object OAuthToken {
    val Expired = OAuthToken("_", Instant.ofEpochSecond(0))

    implicit class exOauthToken(t: OAuthToken) {
      def expiresWithin(sec: Int): Boolean = t.expires.plusSeconds(sec).isAfter(t.expires)
      def isExpired: Boolean = Instant.now().isAfter(t.expires)
      def headers: Iterable[(String, String)] = Map("Authorization" -> s"Bearer ${t.value}")
    }
  }
  case class OAuthResponse(access_token: String, expires_in: Int)
  object OAuthResponse extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[OAuthResponse] = jsonFormat2(OAuthResponse.apply)

    implicit class RichResponse(r: OAuthResponse) {
      def token: OAuthToken = OAuthToken(r.access_token, Instant.now().plusSeconds(r.expires_in))
    }
  }
}
