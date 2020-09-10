#!/usr/bin/env bash

# from the initial state with single policy
anchore-cli policy list | tail -n1 | awk '{print $1}' | xargs anchore-cli policy get --detail

