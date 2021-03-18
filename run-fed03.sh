#! /bin/sh

docker run --name fed03 --hostname=fed03 --network arcanjo \
    -e FEDERATION_NAME=ArcanjoFederation \
    -e FEDERATE_NAME=fed03 \
	-v /etc/localtime:/etc/localtime:ro \
	-p 36023:8080 \
	-d projetoarcanjo/hperf:1.0	



