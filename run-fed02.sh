#! /bin/sh

docker run --name fed02 --hostname=fed02 --network arcanjo \
    -e FEDERATION_NAME=ArcanjoFederation \
    -e FEDERATE_NAME=fed02 \
	-v /etc/localtime:/etc/localtime:ro \
	-p 36022:8080 \
	-d projetoarcanjo/hperf:1.0	



