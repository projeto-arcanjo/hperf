#! /bin/sh

docker run --name fed01 --hostname=fed01 --network arcanjo \
    -e FEDERATION_NAME=ArcanjoFederation \
    -e FEDERATE_NAME=fed01 \
	-v /etc/localtime:/etc/localtime:ro \
	-p 36021:8080 \
	-d projetoarcanjo/hperf:1.0	



