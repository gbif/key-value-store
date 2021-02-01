#!/usr/bin/env bash
set -e
set -o pipefail

ENV=$1
TOKEN=$2

echo "Resuming grscicoll cache workflow for $ENV"

echo "Get latest grscicoll cache refresh config profiles from GitHub"
curl -Ss -H "Authorization: token $TOKEN" -H 'Accept: application/vnd.github.v3.raw' -O -L https://api.github.com/repos/gbif/gbif-configuration/contents/grscicoll-cache-refresh/$ENV/job.properties

OOZIE=$(grep '^oozie.url=' job.properties | cut -d= -f 2)

# Gets the Oozie id of the current coordinator job if it exists
WID=$(oozie jobs -oozie $OOZIE -jobtype coordinator -filter name=Grscicoll-cache | awk 'NR==3 {print $1}')
if [ -n "$WID" ]; then
  echo "Resuming current coordinator job" $WID
  sudo -u hdfs oozie job -oozie $OOZIE -resume $WID
fi
