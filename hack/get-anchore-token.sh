#!/usr/bin/env bash

if ! command -v jq &> /dev/null
then
    echo "jq was not found"
    exit
fi

readonly port="${1:-8228}"

readonly grant_string="grant_type=password&client_id=anonymous&username=admin&password=foobar"
curl -s -v -d "$grant_string" -X POST http://localhost:$port/v1/oauth/token 2> /dev/null | jq -r .access_token
