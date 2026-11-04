# Key-Value Store: Indexing

[Apache Beam](https://beam.apache.org/) pipelines to build HBase KV stores using GBIF data.
All pipelines must be run in an [Apache Spark](https://spark.apache.org/) cluster.

## Build

To build, install and run tests, execute the Maven command:

`mvn clean package install -U`

## Reverse Geocode Indexing

[ReverseGeocodeIndexer](src/main/java/org/gbif/kvs/indexing/geocode/ReverseGeocodeIndexer.java) is a Spark SQL job.
It executes this workflow:
  1. Read the distinct coordinates from the occurrence Iceberg table:
     `SELECT DISTINCT decimalLatitude, decimalLongitude, coordinateUncertaintyInMeters FROM occurrence`
  2. Discard invalid coordinates (missing, or outside ±90 latitude / ±180 longitude).
  3. Perform two reverse Geocode lookups and store each JSON response in a column family in HBase:
     - with `coordinateUncertaintyInMeters`, keyed `lat|lng|uncertainty` (a placeholder of `-4.0075E7` is used when it is null)
     - without uncertainty, once per distinct latitude/longitude, keyed `lat|lng`

By default uses the table definition:
  - Column family: `v`
  - Column qualifier for the JSON response: `j`

# Prepare HBase tables

Using `hbase shell`, run the following:

```bash

create 'name_usage_kv', {NAME => 'v', BLOOMFILTER => 'ROW', VERSIONS => '1', IN_MEMORY => 'false', KEEP_DELETED_CELLS => 'FALSE', DATA_BLOCK_ENCODING => 'FAST_DIFF', COMPRESSION => 'SNAPPY', MIN_VERSIONS => '0', BLOCKCACHE => 'true', BLOCKSIZE => '65536', REPLICATION_SCOPE => '0'}
create 'geocode_kv', {NAME => 'v', BLOOMFILTER => 'ROW', VERSIONS => '1', IN_MEMORY => 'false', KEEP_DELETED_CELLS => 'FALSE', DATA_BLOCK_ENCODING => 'FAST_DIFF', COMPRESSION => 'SNAPPY', MIN_VERSIONS => '0', BLOCKCACHE => 'true', BLOCKSIZE => '65536', REPLICATION_SCOPE => '0'}
create 'grscicoll_lookup_kv', {NAME => 'v', BLOOMFILTER => 'ROW', VERSIONS => '1', IN_MEMORY => 'false', KEEP_DELETED_CELLS => 'FALSE', DATA_BLOCK_ENCODING => 'FAST_DIFF', COMPRESSION => 'SNAPPY', MIN_VERSIONS => '0', BLOCKCACHE => 'true', BLOCKSIZE => '65536', REPLICATION_SCOPE => '0'}
```

### Run

The Iceberg catalog is configured through Spark (`--conf spark.sql.catalog.*` or `spark-defaults.conf`) and the
Iceberg Spark runtime must be on the classpath, e.g. `--packages org.apache.iceberg:iceberg-spark-runtime-3.5_2.12:<version>`.

### Job arguments

  - sourceTable: table to read coordinates from (default `occurrence`), may be qualified, e.g. `iceberg.uat.occurrence`
  - numPartitions: optional, repartition the distinct coordinates; controls how many tasks call the API concurrently
  - hbaseZk: Zookeeper HBase ensemble (required)
  - hbaseZkNode: Zookeeper HBase znode parent
  - targetTable: HBase table to store Geocode responses (required)
  - kvColumnFamily: HBase column family (default `v`)
  - jsonColumnQualifier: HBase column qualifier for the JSON response (default `j`)
  - saltedKeyBuckets: Number of buckets to use for the salted/primary key (required)
  - baseApiUrl: GBIF base API url (required)
  - apiTimeOut: connection time-out to the Geocode service in seconds (default 60)
  - restClientCacheMaxSize: client file cache maximum size in MB (default 64)
  - progressIntervalSeconds: how often the driver and each partition log lookup progress (default 60)

### Monitoring progress

- The Spark UI groups the jobs as `count-coordinates` and `geocode-and-write`, and lists the
  `geocode.*` accumulators (lookups, written, emptyResponses, failedLookups, invalidCoordinates, lookupTimeMs)
  on the stage page.
- The driver logs `Geocode progress: tasks <completed>/<total> ...` with the accumulator totals. These only include
  completed tasks.
- Each executor logs `Geocode partition <id> progress: ...` for its running partition, and a `finished` line at the end.

### Example

On k8s

```bash
/stackable/spark/bin/spark-submit \
--master k8s://https://130.225.43.216:6443 \
--deploy-mode client \
--conf spark.kubernetes.authenticate.serviceAccountName="dev-spark-client" \
--conf spark.kubernetes.namespace="uat" \
--conf spark.executor.instances=6 \
--conf spark.executor.memory="8g" \
--conf spark.executor.cores="4" \
--conf spark.driver.host="spark-shell-gateway" \
--conf spark.driver.memory="2g" \
--conf spark.driver.cores="2" \
--conf spark.driver.port="7078" \
--conf spark.blockManager.port="7089" \
--conf spark.driver.bindAddress="0.0.0.0" \
--conf spark.driver.extraClassPath="/etc/hadoop/conf/:/etc/gbif/:/stackable/spark/extra-jars/*" \
--conf spark.executor.extraClassPath="/etc/hadoop/conf/:/etc/gbif/:/stackable/spark/extra-jars/*" \
--conf spark.jars.ivy="/tmp" \
--conf spark.kubernetes.container.image="docker.stackable.tech/stackable/spark-k8s:3.5.0-stackable23.11.0" \
--conf spark.sql.extensions=org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions \
--conf spark.sql.catalog.iceberg=org.apache.iceberg.spark.SparkCatalog \
--conf spark.sql.catalog.iceberg.type=hive \
--class org.gbif.kvs.indexing.geocode.ReverseGeocodeIndexer \
/tmp/kvs-indexing.jar \
--hbaseZk=gbif-zookeeper-server-default-0.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-1.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-2.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-3.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-4.gbif-zookeeper-server-default.uat.svc.cluster.local:2282  \
--sourceTable=iceberg.uat.occurrence \
--targetTable=test_geocode_kv \
--saltedKeyBuckets=5 \
--baseApiUrl=http://api.gbif-uat.org/v1/
```


## Taxonomic Name Match Indexing

[NameUsageMatchIndexer](src/main/java/org/gbif/kvs/indexing/species/NameUsageMatchIndexer.java) runs a Beam pipeline on Apache Spark.
In general, this pipeline executes this workflow:
  1. Read occurrence data from an Avro files.
  2. Collect the distinct values for each taxonomic name usage.
  3. Perform a name match lookup using the taxonomic data and stores the result in a column family in HBase.

By default uses the table definition:
  - Column family: `v`
  - Column qualifier for the JSON response: `j`

### Run


### Pipeline arguments

  - runner: Apache Beam runner
  - hbaseZk: Zookeeper HBase ensemble
  - sourceGlob: glob (wildcard pattern) for Avro occurrence files
  - targetTable: HBase table to store species match responses  
  - saltedKeyBuckets: Number of buckets to use for the salted/primary key
  - nameUsageBaseApiUrl: Checklistbank NameUsage GBIF base API URL    
  - nameUsageApiTimeOut: Checklistbank NameUsage  API connection time-out to the match service
  - nameUSageRestClientCacheMaxSize: Checklistbank NameUsage  client file cache maximum size

### Example

After running the build command the produced shaded JAR that can be used to execute the pipeline:

```bash
./spark-submit \
  --class org.gbif.kvs.indexing.species.NameUsageMatchIndexer \
  --master yarn \
  --executor-memory 2G \
  --executor-cores 2 \
  --num-executors 10 \
  --conf spark.dynamicAllocation.enabled=false \
  /tmp/kvs-indexing.jar \
  --runner=SparkRunner \
  --hbaseZk=gbif-zookeeper-server-default-0.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-1.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-2.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-3.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-4.gbif-zookeeper-server-default.uat.svc.cluster.local:2282 \
  --sourceGlob=hdfs://ha-nn/data/hdfsview/occurrence/view_occurrence_* \
  --targetTable=test_name_usage_kv \
  --saltedKeyBuckets=5 \
  --nameUsageBaseApiUrl=http://backbonebuild-vh.gbif.org:9100 \ 
  --nameUsageApiTimeOut=6000 \
  --nameUsageRestClientCacheMaxSize=64
```

On k8s

```bash
/stackable/spark/bin/spark-submit \
--master k8s://https://130.225.43.216:6443 \
--deploy-mode client \
--conf spark.kubernetes.authenticate.serviceAccountName="dev-spark-client" \
--conf spark.kubernetes.namespace="uat" \
--conf spark.executor.instances=6 \
--conf spark.executor.memory="8g" \
--conf spark.executor.cores="4" \
--conf spark.driver.host="spark-shell-gateway" \
--conf spark.driver.memory="2g" \
--conf spark.driver.cores="2" \
--conf spark.driver.port="7078" \
--conf spark.blockManager.port="7089" \
--conf spark.driver.bindAddress="0.0.0.0" \
--conf spark.driver.extraClassPath="/etc/hadoop/conf/:/etc/gbif/:/stackable/spark/extra-jars/*" \
--conf spark.executor.extraClassPath="/etc/hadoop/conf/:/etc/gbif/:/stackable/spark/extra-jars/*" \
--conf spark.jars.ivy="/tmp" \
--conf spark.kubernetes.container.image="docker.stackable.tech/stackable/spark-k8s:3.5.0-stackable23.11.0" \
--packages com.coxautodata:spark-distcp_2.12:0.2.5 \
--class org.gbif.kvs.indexing.species.NameUsageMatchIndexer \
/tmp/kvs-indexing.jar \
--runner=SparkRunner \
--hbaseZk=gbif-zookeeper-server-default-0.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-1.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-2.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-3.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-4.gbif-zookeeper-server-default.uat.svc.cluster.local:2282  \
--sourceGlob=hdfs://gbif-hdfs/data/hdfsview/occurrence/occurrence/*.avro \
--targetTable=test_name_usage_kv \
--saltedKeyBuckets=5 \
--nameUsageBaseApiUrl=http://backbonebuild-vh.gbif.org:9100
```


## GrSciColl lookup indexing
[GrscicollLookupServiceIndexer](src/main/java/org/gbif/kvs/indexing/grscicoll/GrscicollLookupServiceIndexer.java) runs a Beam pipeline on Apache Spark.
In general, this pipeline executes this workflow:
  1. Read occurrence data from Avro files.
  2. Collect the distinct values for each grscicoll lookup request.
  3. Perform a reverse Grscicoll lookup call using each request and stores the result in a column family in HBase.

By default uses the table definition:
  - Column family: `v`
  - Column qualifier for the JSON response: `j`

### Run


### Pipeline arguments

  - runner: Apache Beam runner
  - hbaseZk: Zookeeper HBase ensemble
  - sourceGlob: glob (wildcard pattern) for Avro occurrence files
  - targetTable: HBase table to store Geocode responses
  - baseApiUrl: GBIF base API url
  - saltedKeyBuckets: Number of buckets to use for the salted/primary key
  - apiTimeOut: connection time-out to the Geocode service
  - restClientCacheMaxSize: client file cache maximum size

### Example

After running the build command the produced shaded jar that can be used to execute the pipeline:

```bash
spark2-submit --class org.gbif.kvs.indexing.grscicoll.GrscicollLookupServiceIndexer \
    --master yarn --executor-memory 4G --executor-cores 2 --num-executors 10 \
    --conf spark.dynamicAllocation.enabled=false kvs-indexing-1.9-SNAPSHOT-shaded.jar  \
    --runner=SparkRunner \
    --hbaseZk=c3zk1.gbif-dev.org,c3zk2.gbif-dev.org,c3zk3.gbif-dev.org \
    --sourceGlob=hdfs://ha-nn/data/hdfsview/occurrence/view_occurrence_* \
    --targetTable=grscicoll_lookup_kv \
    --baseApiUrl=http://api.gbif-dev.org/v1/ \
    --saltedKeyBuckets=3 \
    --apiTimeOut=6000 \
    --restClientCacheMaxSize=64 &> /dev/null &
```

On k8s

```bash
/stackable/spark/bin/spark-submit \
--master k8s://https://130.225.43.216:6443 \
--deploy-mode client \
--conf spark.kubernetes.authenticate.serviceAccountName="dev-spark-client" \
--conf spark.kubernetes.namespace="uat" \
--conf spark.executor.instances=6 \
--conf spark.executor.memory="8g" \
--conf spark.executor.cores="4" \
--conf spark.driver.host="spark-shell-gateway" \
--conf spark.driver.memory="2g" \
--conf spark.driver.cores="2" \
--conf spark.driver.port="7078" \
--conf spark.blockManager.port="7089" \
--conf spark.driver.bindAddress="0.0.0.0" \
--conf spark.driver.extraClassPath="/etc/hadoop/conf/:/etc/gbif/:/stackable/spark/extra-jars/*" \
--conf spark.executor.extraClassPath="/etc/hadoop/conf/:/etc/gbif/:/stackable/spark/extra-jars/*" \
--conf spark.jars.ivy="/tmp" \
--conf spark.kubernetes.container.image="docker.stackable.tech/stackable/spark-k8s:3.5.0-stackable23.11.0" \
--packages com.coxautodata:spark-distcp_2.12:0.2.5 \
--class org.gbif.kvs.indexing.grscicoll.GrscicollLookupServiceIndexer \
/tmp/kvs-indexing.jar \
--runner=SparkRunner \
--hbaseZk=gbif-zookeeper-server-default-0.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-1.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-2.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-3.gbif-zookeeper-server-default.uat.svc.cluster.local:2282,gbif-zookeeper-server-default-4.gbif-zookeeper-server-default.uat.svc.cluster.local:2282  \
--sourceGlob=hdfs://gbif-hdfs/data/hdfsview/occurrence/occurrence/*.avro \
--targetTable=test_grscicoll_lookup_kv \
--saltedKeyBuckets=5 \
--baseApiUrl=http://api.gbif.org/v1/ 
```


### Alternative that reads from a hive table instead of from the avro files

1. First, we need to create a hive table with all the distinct combinations found in the occurrence data:
```sql
CREATE TABLE occurrence_collections STORED AS parquet AS 
SELECT DISTINCT ownerInstitutionCode, institutionId, institutionCode, collectionCode, collectionId, datasetKey, 
CASE WHEN datasetKey = '4fa7b334-ce0d-4e88-aaae-2e0c138d049e' THEN 'US' ELSE publishingCountry END AS country
FROM prod_h.occurrence;
```

2. Then run the [GrscicollLookupServiceIndexerFromHiveTable](src/main/java/org/gbif/kvs/indexing/grscicoll/GrscicollLookupServiceIndexerFromHiveTable.java) Beam pipeline on Apache Spark.

Example:
```bash
spark2-submit --class org.gbif.kvs.indexing.grscicoll.GrscicollLookupServiceIndexerFromHiveTable \
    --master yarn --executor-memory 8G --executor-cores 4 --num-executors 10 \
    --conf spark.dynamicAllocation.enabled=false kvs-indexing-1.13-SNAPSHOT-shaded.jar  \
    --runner=SparkRunner \
    --hbaseZk=c4zk1.gbif-uat.org,c4zk2.gbif-uat.org,c4zk3.gbif-uat.org \
    --database=uat \
    --table=occurrence_collections \
    --metastoreUris=thrift://c4hivemetastore.gbif-uat.org:9083 \
    --targetTable=grscicoll_lookup_kv \
    --baseApiUrl=http://api.gbif-uat.org/v1/ \
    --saltedKeyBuckets=9 \
    --apiTimeOut=6000 \
    --restClientCacheMaxSize=64 &> /dev/null &
```


