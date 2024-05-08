set -x

cd `dirname $0`

mysqlcontainerId=$(docker ps --format '{{.ID}}  -  {{.Names}}'  |grep mysql | awk '{print $1}')

echo container id is ${mysqlcontainerId}

docker cp ~/testdump.sql ${mysqlcontainerId}:/usr
docker cp importDumpToLocal.sh ${mysqlcontainerId}:/usr
docker exec -it ${mysqlcontainerId} bash