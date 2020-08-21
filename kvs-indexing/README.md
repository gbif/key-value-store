# Key-Value Store: Indexing

[Apache Beam](https://beam.apache.org/) pipelines to build HBase KV stores using GBIF data.
All pipeline must be run in an (Apache Spark)[https://spark.apache.org/] cluster.

## Build

To build, install and run tests, execute the Maven command:

`mvn clean package install -U`

## Reverse Geocode Indexing

[ReverseGeocodeIndexer](src/main/java/org/gbif/kvs/indexing/geocode/ReverseGeocodeIndexer.java) runs a Beam pipeline on Apache Spark.
In general, this pipeline executes this workflow:
  1. Read occurrence data from Avro files.
  2. Collect the distinct values for each latitude and longitude.
  3. Perform a reverse Geocode lookup using each coordinate and stores the result in a column family in HBase.

By default uses the table definition:
  - Column family: `v`
  - Column qualifier for iso contry code: `c`
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

```
spark2-submit --class org.gbif.kvs.indexing.geocode.ReverseGeocodeIndexer \
  --master yarn --executor-memory 8G --executor-cores 2 --num-executors 10 \
  --conf spark.dynamicAllocation.enabled=false kvs-indexing-1.9-SNAPSHOT-shaded.jar \
  --runner=SparkRunner \
  --hbaseZk=c4zk1.gbif-uat.org,c4zk2.gbif-uat.org,c4zk3.gbif-uat.org \
  --sourceGlob=hdfs://ha-nn/data/hdfsview/occurrence/view_occurrence_* \
  --targetTable=geocode_new_kv \
  --baseApiUrl=http://api.gbif-uat.org/v1/ \
  --saltedKeyBuckets=5 \
  --apiTimeOut=6000 \
  --restClientCacheMaxSize=64
```

## Taxonomic Name Match Indexing

[NameUsageMatchIndexer](src/main/java/org/gbif/kvs/indexing/species/NameUsageMatchIndexer.java) runs a Beam pipeline on Apache Spark.
In general, this pipeline executes this workflow:
  1. Read occurrence data from an HBase table.
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
  - baseApiUrl: GBIF base API URL
  - saltedKeyBuckets: Number of buckets to use for the salted/primary key
  - apiTimeOut: connection time-out to the match service
  - restClientCacheMaxSize: client file cache maximum size

### Example

After running the build command the produced shaded JAR that can be used to execute the pipeline:

```
spark2-submit --class org.gbif.kvs.indexing.species.NameUsageMatchIndexer \
  --master yarn --executor-memory 2G --executor-cores 2 --num-executors 10 \
  --conf spark.dynamicAllocation.enabled=false kvs-indexing-1.9-SNAPSHOT-shaded.jar \
  --runner=SparkRunner \
  --hbaseZk=c4zk1.gbif-uat.org,c4zk2.gbif-uat.org,c4zk3.gbif-uat.org \
  --sourceGlob=hdfs://ha-nn/data/hdfsview/occurrence/view_occurrence_* \
  --targetTable=name_usage_new_kv \
  --baseApiUrl=http://api.gbif-uat.org/v1/ \
  --saltedKeyBuckets=5 \
  --apiTimeOut=6000 \
  --restClientCacheMaxSize=64
```
