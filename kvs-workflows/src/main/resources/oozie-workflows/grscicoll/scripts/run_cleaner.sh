REPO=$1
EXECUTOR_MEMORY=$2
EXECUTOR_CORES=$3
NUM_EXECUTORS=$4
ZK=$5
DATABASE=$6
TABLE=$7
METASTORE_URIS=$8
TARGET_TABLE=$9
SALTED_KEY_BUCKETS=${10}
SPARK_OPTS=${11}

REPO_URL="https://repository.gbif.org/service/rest/v1/search/assets/download?repository="${REPO}
REPO_URL+="&group=org.gbif.kvs&name=kvs-indexing&sort=version&direction=desc&maven.classifier=shaded&maven.extension=jar"

echo "Downloading kvs-indexing.jar from " ${REPO_URL}
curl ${REPO_URL} -L -o kvs-indexing.jar

echo "Running the cleaner"
spark2-submit --class org.gbif.kvs.indexing.grscicoll.GrscicollLookupCleaner \
  --master yarn --executor-memory ${EXECUTOR_MEMORY} --executor-cores ${EXECUTOR_CORES} --num-executors ${NUM_EXECUTORS} \
  --conf spark.dynamicAllocation.enabled=false ${SPARK_OPTS} kvs-indexing.jar  \
  --runner=SparkRunner \
  --hbaseZk=${ZK} \
  --database=${DATABASE} \
  --table=${TABLE} \
  --metastoreUris=${METASTORE_URIS} \
  --targetTable=${TARGET_TABLE} \
  --saltedKeyBuckets=${SALTED_KEY_BUCKETS}