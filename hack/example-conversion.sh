#!/usr/bin/env bash

cat anchore/cli/cli/cli.greylist | \
jq --arg wid $(git rev-parse --short HEAD) \
  '.whitelisted_vulnerabilities[] | {id: "dccscr", name: "DCCSCR Whitelist", comment: "Generated using commit ", version: $wid, items: [{id: .vulnerability, gate: "vulnerabilities", trigger_id: .vulnerability}]} | .comment += .id'


#{
#  "id": "dccscr",
#  "name": "DCCSCR Whitelist",
#  "comment": "Generated using commit dccscr",
#  "version": "bdb856d",
#  "items": [
#    {
#      "id": "VULNDB-229216",
#      "gate": "vulnerabilities",
#      "trigger_id": "VULNDB-229216"
#    }
#  ]
#}
