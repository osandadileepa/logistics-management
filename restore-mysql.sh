#!/usr/bin/env bash


# Define the MySQL password (replace 'your_password' with the actual password)
MYSQL_PASSWORD="root"

# Copy the SQL dump file into the Docker container
docker cp ./mysql-dump/SHPV2-mysql-backups/dump_20230922.sql shipmentv2-mysql:/tmp

# Execute commands inside the Docker container
docker exec -it shipmentv2-mysql bash -c "cd /tmp && \
mysql -u root -p$MYSQL_PASSWORD shipmentv2_local < dump_20230922.sql && \
sleep 5"

