#! /bin/sh

mvn clean package

docker ps -a | awk '{ print $1,$2 }' | grep projetoarcanjo/hperf:1.0 | awk '{print $1 }' | xargs -I {} docker rm -f {}
docker rmi projetoarcanjo/hperf:1.0

docker build --tag=projetoarcanjo/hperf:1.0 --rm=true .

