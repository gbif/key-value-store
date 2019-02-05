#kvs-indexing

[Apache Beam](https://beam.apache.org/) pipelines to build HBase KV stores using GBIF data.
All pipeline must be run in an (Apache Spark)[https://spark.apache.org/] cluster.

## Build

To build, install and run tests, execute the Maven command:

`mvn clean package install -U`

## Reverse Geocode Indexing

[ReverseGeocodeIndexer](src/main/java/org/gbif/kvs/indexing/geocode/ReverseGeocodeIndexer.java) runs a Beam pipeline on Apache Spark.
In general, this pipeline executes this workflow:
  1. Read occurrence data from an HBase table.
  2. Collects the distinct values for each latitude and longitude.
  3. Performs a reverse Geocode lookup using each coordinate and stores the result in a column family in HBase.

By default uses the table definition:
  - Column family: `v`
  - Column qualifier for iso contry code: `c`
  - Column qualifier for the JSON response: `j`

### Run


### Pipeline arguments

  - runner: Apache Beam runner
  - hbaseZk: Zookeeper HBase ensemble
  - sourceTable: occurrence HBase table
  - targetTable: HBase table to store Geocode responses
  - baseApiUrl: GBIF base API url
  - saltedKeyBuckets: Number of buckets to use for the salted/primary key
  - apiTimeOut: connection time-out to the Geocode service
  - restClientCacheMaxSize: client file cache maximum size

### Example

After running the build command the produced shaded jar that can be used to execute the pipeline:

```
spark2-submit --class org.gbif.kvs.indexing.geocode.ReverseGeocodeIndexer
  --master yarn --executor-memory 2G --executor-cores 3 --num-executors 1 kvs-indexing-VERSION-shaded.jar
  --runner=SparkRunner
  --hbaseZk=zk1.dev.org,zk2.dev.org,zk3.dev.org
  --sourceTable=dev_occurrence
  --targetTable=geocode_kv
  --baseApiUrl=http://api.gbif-dev.org/v1/
  --saltedKeyBuckets=50
  --apiTimeOut=6000
  --restClientCacheMaxSize=64
```


