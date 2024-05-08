#!/usr/bin/env bash
set -x

cd `dirname $0`
sh ./stop-docker.sh
docker-compose -f docker-compose-local.yml down --remove-orphans
sh ./start-docker.sh