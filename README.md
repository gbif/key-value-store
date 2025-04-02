# Key-value store
This project contains a series of libraries and utilities to create KV stores backed by HBase (but possibly by other KV databases).
The main use of a KV store is to use it as fast lookup caches of pre-computed data.

## Overview
This project contains 5 modules:
  1. [kvs-common](/kvs-common/): request model objects which support a getKey method to support caching mechanisms.  
  2. [kvs-core](/kvs-core/): base model and a default implementation based on [Apache HBase](https://hbase.apache.org/).
  3. [kvs-indexing](/kvs-indexing/): [Apache Beam](https://beam.apache.org/) pipelines to index GBIF data in HBase tables.
  4. [kvs-rest-client](/kvs-rest-clients/):  [Feign](https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/)-based REST clients to access GBIF API services.
  5. [kvs-gbif](/kvs-gbif/): Implementation of GBIF KV stores/caches for commonly used data in the data ingestion process.

## Build
To build, install and run tests, execute the Maven command:

`mvn clean package install -U`
