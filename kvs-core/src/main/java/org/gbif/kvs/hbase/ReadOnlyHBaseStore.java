package org.gbif.kvs.hbase;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.elastic.ElasticMeterRegistry;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.SaltedKeyGenerator;
import org.gbif.kvs.metrics.CacheMetrics;
import org.gbif.kvs.metrics.ElasticMetricsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Implementation of a read-only key-value based on HBase.
 * This implementation base its implementation on loader function that no necessarily produces values as the one provided by the KV store.*
 *   - A resultMapper function converts HBase {@link Result} into value V.
 *
 * The get method provides a getOrPut behaviour, if the key is not found in the store the loader function is used to
 * externally retrieve its value.
 *
 * @param <K> type of key elements
 * @param <V> type of values
 */
public class ReadOnlyHBaseStore<K extends Indexable, V> implements KeyValueStore<K, V>, Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(ReadOnlyHBaseStore.class);

  // HBase table name where KV pairs are stored
  private final TableName tableName;

  // Function to convert a byte[] into a V instance
  private final Function<Result, V> resultMapper;

  // Active HBase connection
  private final Connection connection;

  // Salted key generator for the specified number of buckets
  private final SaltedKeyGenerator saltedKeyGenerator;

  private final CacheMetrics metrics;

  private Command closeHandler;

  private ReadOnlyHBaseStore(HBaseKVStoreConfiguration config,
                             Function<Result, V> resultMapper,
                             MeterRegistry meterRegistry,
                             Command closeHandler) throws IOException {
    connection = ConnectionFactory.createConnection(config.hbaseConfig());
    saltedKeyGenerator = new SaltedKeyGenerator(config.getNumOfKeyBuckets());
    this.tableName = TableName.valueOf(config.getTableName());
    this.resultMapper = resultMapper;
    this.metrics = CacheMetrics.create(meterRegistry, config.getTableName());
    this.closeHandler = closeHandler;
  }

  /**
   * Wraps an exception into a {@link IllegalArgumentException}.
   * @param throwable to propagate
   * @param message to log and use for the exception wrapper
   * @return a new {@link IllegalArgumentException}
   */
  private static RuntimeException logAndThrow(Throwable throwable, String message) {
    LOG.error(message, throwable);
    return new IllegalStateException(throwable);
  }


  /**
   * Gets a V value associated with the K key. If the value is not found in the KV store, the loader
   * function is used to retrieve the value from an external source.
   *
   * @param key identifier of element to be retrieved
   * @return the value found, null otherwise
   */
  @Override
  public V get(K key) {
    try (Table table = connection.getTable(tableName)) {
      byte[] saltedKey = saltedKeyGenerator.computeKey(Bytes.toBytes(key.getLogicalKey()));
      Get get = new Get(saltedKey);
      Result result = table.get(get);
      if (result.isEmpty()) { // the key does not exists, create a new entry
        return null;
      }
      metrics.incHits();
      return resultMapper.apply(result);
    } catch (IOException ex) {
      throw logAndThrow(ex, "Error retrieving data");
    }
  }

  /**
   * Closes the underlying HBase resources.
   *
   * @throws IOException if HBase throws any error
   */
  @Override
  public void close() throws IOException {
    if (!connection.isClosed()) {
      connection.close();
    }
    Optional.ofNullable(closeHandler).ifPresent(Command::execute);
  }

  /**
   * Creates a new {@link ReadOnlyHBaseStore.Builder}.
   *
   * @param <K> type of key elements
   * @param <V> type of values to store
   * @return a new instance of a HBaseKVStore.Builder
   */
  public static <K extends Indexable, V> Builder<K, V> builder() {
    return new Builder<>();
  }

  /**
   * Builder of {@link ReadOnlyHBaseStore} instances.
   *
   * @param <K> type of key elements
   * @param <V> type of values to store
   */
  public static class Builder<K extends Indexable, V> {
    private HBaseKVStoreConfiguration configuration;
    private Function<Result, V> resultMapper;
    private ElasticMetricsConfig metricsConfig;
    private Command closeHandler;

    public Builder<K, V> withHBaseStoreConfiguration(HBaseKVStoreConfiguration configuration) {
      this.configuration = configuration;
      return this;
    }

    public Builder<K, V> withResultMapper(Function<Result, V> resultMapper) {
      this.resultMapper = resultMapper;
      return this;
    }

    public Builder<K, V> withElasticMetricsConfig(ElasticMetricsConfig metricsConfig) {
      this.metricsConfig = metricsConfig;
      return this;
    }

    public Builder<K, V> withCloseHandler(Command closeHandler) {
      this.closeHandler = closeHandler;
      return this;
    }

    public ReadOnlyHBaseStore<K, V> build() throws IOException {
      MeterRegistry metricsRegistry = Objects.nonNull(this.metricsConfig)? new ElasticMeterRegistry(this.metricsConfig, Clock.SYSTEM) : new SimpleMeterRegistry();
      return new ReadOnlyHBaseStore<>(configuration, resultMapper, metricsRegistry, closeHandler);
    }
  }
}
