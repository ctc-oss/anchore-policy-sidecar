#!/usr/bin/env bash

set -xe

# auth
readonly TOKEN=$("$(dirname "$0")/get-anchore-token.sh")

# post
r=$(curl -vs "http://localhost:8228/v1/policies" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "@${1}")
id=$(jq -r .policybundle.id <<< "$r")

# activate
curl -X PUT "http://localhost:8228/v1/policies/$id?active=true" -H "Authorization: Bearer $TOKEN" -H  "Content-Type: application/json" -d "$r"
