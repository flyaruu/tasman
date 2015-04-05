#export DOCKER_HOST=tcp://192.168.59.103:2376
docker run -d -p 8300:8300 -p 8301:8301/udp -p 54:53/udp -p 8101:8101 -p 8102:8102 -p 8102:8102/udp -p 8400:8400 -p 8500:8500 -h $HOSTNAME progrium/consul -server -bootstrap

