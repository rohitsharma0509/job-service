#!/usr/bin/env bash

set -o errexit
set -o pipefail

java -XX:+UseContainerSupport -Djdk.tls.client.protocols=TLSv1.2 -jar /usr/src/app/service.jar