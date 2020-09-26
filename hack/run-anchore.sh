#!/usr/bin/env bash

# https://stackoverflow.com/a/246128
script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

docker run -d --rm --name anchore-inline-scan -p 8228:8228 -v "$script_dir/config.yaml:/config/config.yaml"  anchore/inline-scan:latest
