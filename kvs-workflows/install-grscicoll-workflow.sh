#!/usr/bin/env bash
set -e
set -o pipefail

ENV=$1
TOKEN=$2

echo "Installing grscicoll cache refresh workflow for $ENV"

echo "Get latest grscicoll cache refresh config profiles from GitHub"
curl -Ss -H "Authorization: token $TOKEN" -H 'Accept: application/vnd.github.v3.raw' -O -L https://api.github.com/repos/gbif/gbif-configuration/contents/grscicoll-cache-refresh/$ENV/job.properties

echo "Get latest maven profiles from github"
curl -s -H "Authorization: token $TOKEN" -H 'Accept: application/vnd.github.v3.raw' -O -L https://api.github.com/repos/gbif/gbif-configuration/contents/grscicoll-cache-refresh/profiles.xml

OOZIE=$(grep '^oozie.url=' job.properties | cut -d= -f 2)

# Gets the Oozie id of the current coordinator job if it exists
WID=$(oozie jobs -oozie $OOZIE -jobtype coordinator -filter name=grscicoll-cache-refresh-coord | awk 'NR==3 {print $1}')
if [ -n "$WID" ]; then
  echo "Killing current coordinator job" $WID
  sudo -u hdfs oozie job -oozie $OOZIE -kill $WID
fi

echo "Assembling jar for $ENV"
# Oozie uses timezone UTC
mvn --settings profiles.xml -U -P$ENV -DskipTests -Duser.timezone=UTC clean install package

echo "Copy to Hadoop"
sudo -u hdfs hdfs dfs -rm -r /grscicoll-cache-workflow/
sudo -u hdfs hdfs dfs -copyFromLocal target/grscicoll-cache-workflow /

echo "Start Oozie gridded datasets job"
sudo -u hdfs oozie job --oozie $OOZIE -config gridded.properties -run