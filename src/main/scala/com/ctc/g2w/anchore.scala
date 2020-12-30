package com.ctc.g2w

import com.ctc.g2w.api.anchore.{PolicyBundleRecord, Whitelist}
import com.ctc.g2w.api.auth
import com.ctc.g2w.api.auth.{OAuthResponse, OAuthToken}
import spray.json._
import zio._

object anchore {
  type HttpConfig = zio.config.ZConfig[cfg.anchore.Http]

  type AnchoreAPI = Has[AnchoreAPI.Service]
  object AnchoreAPI {
    trait Service {
      def health(): UIO[api.anchore.Health]
      def activePolicy(): ZIO[AnchoreAuth, Throwable, PolicyBundleRecord]
      def policies(): ZIO[AnchoreAuth, Throwable, List[PolicyBundleRecord]]
      def activate(whitelist: Whitelist): ZIO[AnchoreAuth, Throwable, PolicyBundleRecord]
    }

    def live(config: cfg.anchore.Http): Layer[AnchoreAuth, AnchoreAPI] = ZLayer.succeed(
      new AnchoreAPI.Service {
        def health(): UIO[api.anchore.Health] =
          ZIO
            .effect(requests.get(s"${config.url()}/health").statusCode)
            .map {
              case 200 => api.anchore.Health.Ok
            }
            .orElse(ZIO.succeed(api.anchore.Health.Fail))

        def activePolicy(): ZIO[AnchoreAuth, Throwable, PolicyBundleRecord] =
          policies().map(_.filter(_.active.getOrElse(false))).map(_.head)

        def policies(): ZIO[AnchoreAuth, Throwable, List[PolicyBundleRecord]] =
          for {
            tok <- AnchoreAuth.token()
            res = requests
              .get(s"${config.url()}/policies", headers = tok.headers, params = Map("detail" -> "true"))
              .text()
              .parseJson
              .convertTo[List[PolicyBundleRecord]]
          } yield res

        def activate(whitelist: Whitelist): ZIO[AnchoreAuth, Throwable, PolicyBundleRecord] = ZIO.succeed(
          defaults.anchore.DefaultBundle.copy(
            id = whitelist.id,
            mappings = List(defaults.anchore.DefaultMapping.copy(whitelist_ids = Some(List(whitelist.id)))),
            whitelists = Some(List(whitelist))
          )
        ).map(_.toJson.compactPrint)
          .flatMap(b =>
            for {
              h <- AnchoreAuth.token().map(_.headers ++ Seq("Content-Type" -> "application/json"))
              res = requests.post(s"${config.url()}/v1/policies", headers = h, data = b)
            } yield res.text().parseJson.convertTo[PolicyBundleRecord]
          )
      }
    )

    def health(): URIO[AnchoreAPI, api.anchore.Health] = ZIO.accessM(_.get.health())
    def activePolicy(): ZIO[AnchoreAPI with AnchoreAuth, Throwable, PolicyBundleRecord] =
      ZIO.accessM(_.get.activePolicy())
    def policies(): ZIO[AnchoreAPI with AnchoreAuth, Throwable, List[PolicyBundleRecord]] =
      ZIO.accessM(_.get.policies())
    def activate(whitelist: Whitelist): ZIO[AnchoreAPI with AnchoreAuth, Throwable, PolicyBundleRecord] =
      ZIO.accessM(_.get.activate(whitelist))
  }

  type AnchoreAuth = Has[AnchoreAuth.Service]
  object AnchoreAuth {
    trait Service {
      def token(): Task[OAuthToken]
    }

    def make(config: cfg.anchore.Http): Layer[Nothing, AnchoreAuth] =
      Ref
        .make(OAuthToken.Expired)
        .map { tokenRef =>
          new AnchoreAuth.Service {
            val tokenBuffer = 100 // todo;; move to config
            def token(): Task[OAuthToken] =
              tokenRef.get.flatMap { tok =>
                if (tok.expiresWithin(tokenBuffer)) {
                  ZIO
                    .effect(
                      requests
                        .post(s"${config.url()}/oauth/token", data = auth.Default)
                        .text()
                        .parseJson
                        .convertTo[OAuthResponse]
                    )
                    .flatMap(r => tokenRef.updateAndGet(_ => r.token))
                } else Task.succeed(tok)
              }
          }
        }
        .toLayer

    def token(): RIO[AnchoreAuth, OAuthToken] = ZIO.accessM(_.get.token())
  }
}
