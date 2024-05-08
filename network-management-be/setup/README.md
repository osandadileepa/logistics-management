# Local Setup
Docker compose file is compose of setting up local MySQL database with version `5.7.30`

## Optional to replicate test database
- Setting local db is option, you can use TiDB to start MySQL cloud database for free. [TiDB](https://tidbcloud.com/)

## Local DB Setup steps
1. In terminal, navigate to setup/docker/database
2. run "sh start-docker.sh"

Note: This will not work if database is fresh as there is an issue on liquibase for the latest mysql version.
Use only for test db replication
