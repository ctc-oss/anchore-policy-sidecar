#!/usr/bin/env bash

cat anchore/cli/cli/cli.greylist | \
jq --arg wid $(git rev-parse --short HEAD) \
  '.whitelisted_vulnerabilities[] | {id: "example", name: "Example Whitelist", comment: "Generated using commit ", version: $wid, items: [{id: .vulnerability, gate: "vulnerabilities", trigger_id: .vulnerability}]} | .comment += $wid'


#{
#  "id": "example",
#  "name": "Example Whitelist",
#  "comment": "Generated using commit bdb856d",
#  "version": "bdb856d",
#  "items": [
#    {
#      "id": "VULNDB-229216",
#      "gate": "vulnerabilities",
#      "trigger_id": "VULNDB-229216"
#    }
#  ]
#}
