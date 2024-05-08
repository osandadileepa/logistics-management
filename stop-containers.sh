#!/usr/bin/env bash

docker-compose -f /home/osanda/Documents/IdeaProjects/quincus/shipment-nextgen-be/setup/docker/database/docker-compose-local.yml down
sleep 5

docker-compose -f /home/osanda/Documents/IdeaProjects/quincus/shipment-nextgen-be/setup/docker/kafka/docker-compose-local.yml down
sleep 5

docker-compose -f /home/osanda/Documents/IdeaProjects/quincus/shipment-nextgen-be/karate-s3-mock/docker-compose.yml down

#sleep 5
#docker-compose -f /home/osanda/Documents/IdeaProjects/quincus/network-management-be/setup/docker/database/docker-compose-local.yml down








