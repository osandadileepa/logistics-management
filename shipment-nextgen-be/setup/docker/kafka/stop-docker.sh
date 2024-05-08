#!/usr/bin/env bash
set -x

cd `dirname $0`
docker-compose -f docker-compose-local.yml stop