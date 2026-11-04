/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.kvs.indexing.geocode;

import org.gbif.kvs.SaltedKeyGenerator;
import org.gbif.kvs.geocode.GeocodeKVStoreFactory;
import org.gbif.kvs.geocode.GeocodeRequest;
import org.gbif.kvs.hbase.HBaseKVStoreConfiguration;
import org.gbif.rest.client.RestClientFactory;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;

import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkJobInfo;
import org.apache.spark.SparkStageInfo;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaSparkStatusTracker;
import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.lit;

/**
 * Spark job that indexes Geocode reverse lookup responses in a HBase KV table.
 *
 * <ol>
 *   <li>Reads the distinct coordinates from the occurrence Iceberg table using Spark SQL.
 *   <li>Discards invalid coordinates.
 *   <li>Calls the reverse geocode API twice for each coordinate and stores both responses in HBase
 *       using the salted key of {@link GeocodeRequest#getLogicalKey()}:
 *       <ul>
 *         <li>with coordinateUncertaintyInMeters ({@link #EMPTY_UNCERTAINTY} when it is null),
 *             keyed {@code lat|lng|uncertainty}
 *         <li>without uncertainty, once per distinct latitude/longitude, keyed {@code lat|lng}
 *       </ul>
 * </ol>
 *
 * The Iceberg catalog is configured through Spark, e.g. {@code --conf spark.sql.catalog...}.
 */
public class ReverseGeocodeIndexer {

  private static final Logger LOG = LoggerFactory.getLogger(ReverseGeocodeIndexer.class);

  /**
   * Default value to use when uncertainty is not provided to avoid serialisation issues.
   * This dummy value is the circumference of the earth in meters as a negative number.
   * Avoiding using 0.0 as it is a valid value, sort of.
   */
  public static final Double EMPTY_UNCERTAINTY = -40075000d;

  /** Name of the flag column added to tell apart lookups with and without uncertainty. */
  private static final String WITH_UNCERTAINTY = "withUncertainty";

  private static final String GEOCODE_JOB_GROUP = "geocode-and-write";

  private static final String QUERY =
      "SELECT DISTINCT decimalLatitude, decimalLongitude, coordinateUncertaintyInMeters FROM %s";

  public static void main(String[] args) {
    run(GeocodeIndexingOptions.fromArgs(args));
  }

  /** Runs the indexing job. */
  public static void run(GeocodeIndexingOptions options) {
    SparkSession spark =
        SparkSession.builder().appName("Reverse geocode KV indexing").getOrCreate();
    try {
      String query = String.format(QUERY, options.getSourceTable());
      LOG.info("Reading coordinates with query: {}", query);
      Dataset<Row> coordinates = spark.sql(query);

      spark
          .sparkContext()
          .setJobGroup(
              "count-coordinates",
              "Counting distinct coordinates in " + options.getSourceTable(),
              false);
      LOG.info("Read {} distinct coordinates from {}", coordinates.count(), options.getSourceTable());

      Dataset<Row> lookups = withAndWithoutUncertainty(coordinates);
      if (options.getNumPartitions() != null) {
        lookups = lookups.repartition(options.getNumPartitions());
      }

      LookupMetrics metrics = new LookupMetrics(spark.sparkContext());
      spark
          .sparkContext()
          .setJobGroup(
              GEOCODE_JOB_GROUP,
              "Reverse geocoding coordinates and writing to HBase table " + options.getTargetTable(),
              false);

      long start = System.currentTimeMillis();
      ScheduledExecutorService monitor = startProgressMonitor(spark, metrics, options, start);
      try {
        lookups.foreachPartition(new GeocodePartitionWriter(options, metrics));
      } finally {
        monitor.shutdownNow();
        spark.sparkContext().clearJobGroup();
      }
      LOG.info(
          "Finished indexing geocode responses into {}: {}",
          options.getTargetTable(),
          metrics.summary(System.currentTimeMillis() - start));
    } finally {
      spark.stop();
    }
  }

  /**
   * Logs, on the driver, the task progress of the geocode job and the lookup counters at a fixed
   * interval. Counters only include completed tasks; executors log progress of running tasks.
   */
  private static ScheduledExecutorService startProgressMonitor(
      SparkSession spark, LookupMetrics metrics, GeocodeIndexingOptions options, long start) {
    JavaSparkStatusTracker tracker =
        JavaSparkContext.fromSparkContext(spark.sparkContext()).statusTracker();
    ScheduledExecutorService monitor =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread t = new Thread(r, "geocode-progress-monitor");
              t.setDaemon(true);
              return t;
            });
    long interval = options.getProgressIntervalSeconds();
    monitor.scheduleAtFixedRate(
        () -> {
          try {
            int tasks = 0;
            int completed = 0;
            int active = 0;
            for (int jobId : tracker.getJobIdsForGroup(GEOCODE_JOB_GROUP)) {
              SparkJobInfo job = tracker.getJobInfo(jobId);
              if (job == null) {
                continue;
              }
              for (int stageId : job.stageIds()) {
                SparkStageInfo stage = tracker.getStageInfo(stageId);
                if (stage != null) {
                  tasks += stage.numTasks();
                  completed += stage.numCompletedTasks();
                  active += stage.numActiveTasks();
                }
              }
            }
            double pct = tasks == 0 ? 0 : completed * 100d / tasks;
            LOG.info(
                "Geocode progress: tasks {}/{} ({}%) active={}, {}",
                completed,
                tasks,
                String.format("%.1f", pct),
                active,
                metrics.summary(System.currentTimeMillis() - start));
          } catch (Exception ex) {
            LOG.warn("Unable to report geocode progress", ex);
          }
        },
        interval,
        interval,
        TimeUnit.SECONDS);
    return monitor;
  }

  /**
   * Combines the distinct coordinates with uncertainty and the distinct latitude/longitude pairs
   * without it. Rows have the columns: latitude, longitude, uncertainty, {@link #WITH_UNCERTAINTY}.
   */
  static Dataset<Row> withAndWithoutUncertainty(Dataset<Row> coordinates) {
    StructField[] fields = coordinates.schema().fields();
    Column lat = col(fields[0].name());
    Column lng = col(fields[1].name());
    Column uncertainty = col(fields[2].name());

    Dataset<Row> withUncertainty =
        coordinates.select(lat, lng, uncertainty, lit(true).as(WITH_UNCERTAINTY));
    Dataset<Row> withoutUncertainty =
        coordinates
            .select(lat, lng)
            .distinct()
            .select(
                lat,
                lng,
                lit(null).cast(fields[2].dataType()).as(fields[2].name()),
                lit(false).as(WITH_UNCERTAINTY));
    return withUncertainty.unionByName(withoutUncertainty);
  }

  /** Converts a row of {@link #withAndWithoutUncertainty(Dataset)} into a geocode request. */
  static GeocodeRequest toRequest(Row row) {
    Double lat = toDouble(row.get(0));
    Double lng = toDouble(row.get(1));
    if (!row.getBoolean(3)) {
      return GeocodeRequest.create(lat, lng);
    }
    Double uncertainty = toDouble(row.get(2));
    return GeocodeRequest.create(lat, lng, uncertainty != null ? uncertainty : EMPTY_UNCERTAINTY);
  }

  /** Converts numeric or string column values to Double, null if absent or unparsable. */
  static Double toDouble(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    try {
      return Double.parseDouble(value.toString());
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Looks up each coordinate of a partition and writes the response to HBase. The REST client and
   * HBase connection are created once per partition on the executor.
   */
  static class GeocodePartitionWriter implements ForeachPartitionFunction<Row> {

    private final GeocodeIndexingOptions options;
    private final LookupMetrics metrics;

    GeocodePartitionWriter(GeocodeIndexingOptions options, LookupMetrics metrics) {
      this.options = options;
      this.metrics = metrics;
    }

    @Override
    public void call(Iterator<Row> rows) throws Exception {
      HBaseKVStoreConfiguration hbaseConfig = options.hbaseKVStoreConfiguration();
      SaltedKeyGenerator keyGenerator = new SaltedKeyGenerator(hbaseConfig.getNumOfKeyBuckets());
      BiFunction<byte[], GeocodeResponse, Put> valueMutator =
          GeocodeKVStoreFactory.valueMutator(
              Bytes.toBytes(hbaseConfig.getColumnFamily()),
              Bytes.toBytes(options.getJsonColumnQualifier()));
      GeocodeService geocodeService =
          RestClientFactory.createGeocodeService(options.clientConfiguration());

      try (Connection connection = ConnectionFactory.createConnection(hbaseConfig.hbaseConfig());
          BufferedMutator mutator =
              connection.getBufferedMutator(TableName.valueOf(hbaseConfig.getTableName()))) {
        PartitionProgress progress =
            new PartitionProgress(
                TaskContext.getPartitionId(), options.getProgressIntervalSeconds() * 1000L);
        while (rows.hasNext()) {
          GeocodeRequest request = toRequest(rows.next());
          if (!request.isValid()) {
            metrics.invalid.add(1);
            progress.invalid++;
            continue;
          }
          long lookupStart = System.currentTimeMillis();
          try {
            GeocodeResponse response = geocodeService.reverse(request);
            Put put =
                response == null
                    ? null
                    : valueMutator.apply(keyGenerator.computeKey(request.getLogicalKey()), response);
            if (put != null) {
              mutator.mutate(put);
              metrics.written.add(1);
              progress.written++;
            } else {
              metrics.empty.add(1);
              progress.empty++;
            }
          } catch (Exception ex) {
            metrics.failed.add(1);
            progress.failed++;
            LOG.error("Error performing Geocode lookup for {}", request, ex);
          } finally {
            long took = System.currentTimeMillis() - lookupStart;
            metrics.lookups.add(1);
            metrics.lookupTimeMs.add(took);
            progress.lookups++;
            progress.lookupTimeMs += took;
          }
          progress.logIfDue();
        }
        progress.log("finished");
      }
    }
  }

  /** Per-partition counters, logged on the executor while the partition is processed. */
  static class PartitionProgress {
    private final int partitionId;
    private final long intervalMs;
    private final long start = System.currentTimeMillis();
    private long lastLog = start;

    long lookups;
    long written;
    long empty;
    long failed;
    long invalid;
    long lookupTimeMs;

    PartitionProgress(int partitionId, long intervalMs) {
      this.partitionId = partitionId;
      this.intervalMs = intervalMs;
    }

    void logIfDue() {
      long now = System.currentTimeMillis();
      if (now - lastLog >= intervalMs) {
        lastLog = now;
        log("progress");
      }
    }

    void log(String state) {
      LOG.info(
          "Geocode partition {} {}: {}",
          partitionId,
          state,
          LookupMetrics.format(
              lookups, written, empty, failed, invalid, lookupTimeMs,
              System.currentTimeMillis() - start));
    }
  }
}
