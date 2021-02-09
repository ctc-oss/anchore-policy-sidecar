anchore policy sidecar (psc)
===

[Docs](doc/README.md)

### policy automation through gitops

A sidecar operator that schedules policy generation from git managed metadata and applies as an anchore policy.

### modes

- Ref: run once against a specified ref and then terminate
- Branch: run once against a branch ref and then continue to poll for additional commits to the branch

### versioning

- Policy Bundle id is set using the git commit id
- Whitelist id is set using the git commit id
- Version fields on objects are API version and not related to git commit id

### dev

see the [hack](hack) directory for helpers

### deploy to Microk8s

`uK8s=1 sbt docker:publish`

`k apply -f k8s.yml`

### clean up

`k delete all -l app=anchore-w-sidecar`

### anchore api models

the anchore api models were generated with [sbt-swagger-codegen](https://github.com/unicredit/sbt-swagger-codegen) with some hand tuning / pruning

### libraries
- https://github.com/zio/zio
- https://github.com/zio/zio-config
- https://github.com/lihaoyi/requests-scala
- https://github.com/unicredit/sbt-swagger-codegen

### reference
- https://docs.anchore.com/current/docs/engine/general/concepts/policy/bundles/
- https://github.com/anchore/anchore-engine/blob/master/anchore_engine/services/apiext/swagger/swagger.yaml
- https://github.com/anchore/anchore-engine/blob/master/docs/content/docs/general/concepts/policy/bundles/_index.md#whitelists
