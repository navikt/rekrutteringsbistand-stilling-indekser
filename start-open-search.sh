#!/bin/sh

# Hentet fra https://opensearch.org/docs/latest/opensearch/install/docker/
docker run -p 9200:9200 -p 9600:9600 -e "discovery.type=single-node" opensearchproject/opensearch:2.3.0
