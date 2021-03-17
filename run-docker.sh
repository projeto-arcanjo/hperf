#! /bin/sh

docker run --name hperf --hostname=hperf --network arcanjo \
    -e FEDERATION_NAME=ArcanjoFederation \
    -e FEDERATE_NAME=HPerf \
	-v /etc/localtime:/etc/localtime:ro \
	-p 36009:8080 \
	-d projetoarcanjo/hperf:1.0	



