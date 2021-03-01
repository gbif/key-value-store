#!/usr/bin/env bash
set -e
set -o pipefail

ENV=$1
TOKEN=$2

echo "Installing grscicoll cache refresh workflow for $ENV"

echo "Get latest grscicoll cache refresh job properties from GitHub"
curl -Ss -H "Authorization: token $TOKEN" -H 'Accept: application/vnd.github.v3.raw' -O -L https://api.github.com/repos/gbif/gbif-configuration/contents/grscicoll/cache-refresh-wf/$ENV/job.properties

OOZIE=$(grep '^oozie.url=' job.properties | cut -d= -f 2)
START=$(date +%Y-%m-%d)T$(grep '^startHour=' job.properties | cut -d= -f 2)Z
FREQUENCY=$(grep '^frequency=' job.properties | cut -d= -f 2)
METASTORE_URIS=$(grep '^hive.metastore.uris=' job.properties | cut -d= -f 2)
HIVE_HDFS_OUT=$(grep '^hive.hdfs.out=' job.properties | cut -d= -f 2)

# Gets the Oozie id of the current coordinator job if it exists
WID=$(oozie jobs -oozie $OOZIE -jobtype coordinator -filter name=Grscicoll-cache | awk 'NR==3 {print $1}')
if [ -n "$WID" ]; then
  echo "Killing current coordinator job" $WID
  sudo -u hdfs oozie job -oozie $OOZIE -kill $WID
fi

echo "Assembling jar for $ENV"
# Oozie uses timezone UTC
mvn -U -Dstart="$START" -Dfrequency=$FREQUENCY -Dhive.metastore.uris=$METASTORE_URIS -Dhive.hdfs.out=$HIVE_HDFS_OUT -DskipTests -Duser.timezone=UTC clean install package

echo "Copy to Hadoop"
sudo -u hdfs hdfs dfs -rm -r /grscicoll-cache-workflow/ || echo "No old workflow to remove"
sudo -u hdfs hdfs dfs -copyFromLocal target/grscicoll-cache-workflow/ /

echo "Start Oozie grscicoll cache job"
sudo -u hdfs oozie job --oozie $OOZIE -config job.properties -run