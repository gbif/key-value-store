package org.gbif.kvs.hbase;

import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.SaltedKeyGenerator;
import org.gbif.kvs.metrics.CacheMetrics;
import org.gbif.kvs.metrics.ElasticMetricsConfig;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.elastic.ElasticMeterRegistry;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a key-value based on HBase.
 * This implementation base its implementation on loader function that no necessarily produces values as the one provided by the KV store.
 *   - A loader function provides instances of data (L) from {@link Indexable} key elements.
 *   - A valueMutator is used to convert values of L data into HBase Put mutation.
 *   - A resultMapper function converts HBase {@link Result} into value V.
 *
 * The get method provides a getOrPut behaviour, if the key is not found in the store the loader function is used to
 * externally retrieve its value.
 *
 * @param <K> type of key elements
 * @param <V> type of values
 * @param <L> type of values produced by the loader
 */
public class HBaseStore<K extends Indexable, V, L> implements KeyValueStore<K, V>, Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(HBaseStore.class);

  // HBase table name where KV pairs are stored
  private final TableName tableName;

  // Function to convert a value V into byte[], as expected by HBase
  private final BiFunction<byte[], L, Put> valueMutator;

  // Function to convert a byte[] into a V instance
  private final Function<Result, V> resultMapper;

  // Function to convert a L instance into a V instance
  private final Function<L, V> valueMapper;

  // Function that loads data from external sources when the value is not in the KV store.
  private final Function<K, L> loader;

  // Active HBase connection
  private final Connection connection;

  // Salted key generator for the specified number of buckets
  private final SaltedKeyGenerator saltedKeyGenerator;

  private Command closeHandler;

  private final CacheMetrics metrics;

  private HBaseStore(HBaseKVStoreConfiguration config, BiFunction<byte[], L, Put> valueMutator,
                     Function<Result, V> resultMapper, Function<L, V> valueMapper,
                     Function<K, L> loader,
                     MeterRegistry meterRegistry,
                     Command closeHandler) throws IOException {
    connection = ConnectionFactory.createConnection(config.hbaseConfig());
    saltedKeyGenerator = new SaltedKeyGenerator(config.getNumOfKeyBuckets());
    this.tableName = TableName.valueOf(config.getTableName());
    this.valueMutator = valueMutator;
    this.resultMapper = resultMapper;
    this.valueMapper = valueMapper;
    this.loader = loader;
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
   * Stores in HBase a value for using the key element.
   *
   * @param key HBase row key
   * @param value value to transform
   */
  private V store(byte[] key, L value) {
    try (Table table = connection.getTable(tableName)) {
      Put put = valueMutator.apply(key, value);
      if(Objects.nonNull(put)) {
        table.put(valueMutator.apply(key, value));
        metrics.incInserts();
        return Optional.ofNullable(valueMapper.apply(value)).orElse(null);
      }
      return null;
    } catch (IOException ex) {
      throw logAndThrow(ex, "Appending data to store failed");
    }
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
        L newValue = loader.apply(key);
        if (Objects.nonNull(newValue)) {
          return store(saltedKey, newValue);
        }
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
   * Creates a new {@link HBaseStore.Builder}.
   *
   * @param <K> type of key elements
   * @param <V> type of values to store
   * @return a new instance of a HBaseKVStore.Builder
   */
  public static <K extends Indexable, V, L> Builder<K, V, L> builder() {
    return new Builder<>();
  }

  /**
   * Builder of {@link HBaseStore} instances.
   *
   * @param <K> type of key elements
   * @param <V> type of values to store
   */
  public static class Builder<K extends Indexable, V, L> {
    private HBaseKVStoreConfiguration configuration;
    private BiFunction<byte[], L, Put> valueMutator;
    private Function<Result, V> resultMapper;
    private Function<L, V> valueMapper;
    private Function<K, L> loader;
    private ElasticMetricsConfig metricsConfig;
    private Command closeHandler;

    public Builder<K, V, L> withHBaseStoreConfiguration(HBaseKVStoreConfiguration configuration) {
      this.configuration = configuration;
      return this;
    }

    public Builder<K, V, L> withValueMutator(BiFunction<byte[], L, Put> valueMutator) {
      this.valueMutator = valueMutator;
      return this;
    }

    public Builder<K, V, L> withResultMapper(Function<Result, V> resultMapper) {
      this.resultMapper = resultMapper;
      return this;
    }

    public Builder<K, V, L> withValueMapper(Function<L, V> valueMapper) {
      this.valueMapper = valueMapper;
      return this;
    }

    public Builder<K, V, L> withLoader(Function<K, L> loader) {
      this.loader = loader;
      return this;
    }

    public Builder<K, V, L> withCloseHandler(Command closeHandler) {
      this.closeHandler = closeHandler;
      return this;
    }


    public Builder<K, V, L> withElasticMetricsConfig(ElasticMetricsConfig metricsConfig) {
      this.metricsConfig = metricsConfig;
      return this;
    }

    public HBaseStore<K, V, L> build() throws IOException {
      MeterRegistry metricsRegistry = Objects.nonNull(this.metricsConfig)? new ElasticMeterRegistry(this.metricsConfig, Clock.SYSTEM) : new SimpleMeterRegistry();
      return new HBaseStore<>(configuration, valueMutator, resultMapper, valueMapper, loader, metricsRegistry, closeHandler);
    }
  }
}
