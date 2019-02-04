package org.gbif.kvs.hbase;

import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.SaltedKeyGenerator;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Implementation of a key-value based on HBase.
 *
 * @param <K> type of key elements
 * @param <V> type of values
 */
public class HBaseStore<K extends LogicalKey,V> implements KeyValueStore<K,V>, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(HBaseStore.class);

    //HBase table name where KV pairs are stored
    private final String tableName;

    //Byte[] version of column family
    private final byte[] columnFamily;

    //Byte[] of the HBase column qualifier
    private final byte[] qualifier;

    //Function to convert a value V into byte[], as expected by HBase
    private final Function<V,byte[]> valueSerializer;

    //Function to convert a byte[] into a V instance
    private final Function<byte[],V> valueDeSerializer;

    //Function that loads data from external sources when the value is not in the KV store.
    private final Function<K,V> loader;

    //Active HBase connection
    private final Connection connection;

    //Salted key generator for the specified number of buckets
    private final SaltedKeyGenerator saltedKeyGenerator;


    private HBaseStore(HBaseKVStoreConfiguration config,
                       Function<V, byte[]> valueSerializer,
                       Function<byte[], V> valueDeSerializer,
                       Function<K,V> loader) throws IOException {
        connection = ConnectionFactory.createConnection(config.hbaseConfig());
        saltedKeyGenerator = new SaltedKeyGenerator(config.getNumOfKeyBuckets());
        this.tableName = config.getTableName();
        this.columnFamily = config.getColumnFamily().getBytes();
        this.qualifier = config.getQualifier().getBytes();
        this.valueSerializer = valueSerializer;
        this.valueDeSerializer = valueDeSerializer;
        this.loader = loader;
    }

    /**
     * Stores a KV pair in the store.
     * @param key element identifier
     * @param value data or payload
     */
    @Override
    public void put(K key, V value) {
        try(Table table = connection.getTable(TableName.valueOf(tableName))) {
            Put put = new Put(saltedKeyGenerator.bucketOf(key.getLogicalKey()));
            put.addColumn(columnFamily, qualifier, valueSerializer.apply(value));
            table.put(put);
        } catch (IOException ex) {
            LOG.error("Appending data to store failed",ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Gets a V value associated with the K key.
     * If the value is not found in the KV store, the loader function is used to retrieve the value from
     * an external source.
     * @param key identifier of element to be retrieved
     * @return the value found, null otherwise
     */
    @Override
    public V get(K key) {
        try(Table table = connection.getTable(TableName.valueOf(tableName))) {
            byte[] saltedKey = saltedKeyGenerator.computeKey(key.getLogicalKey());
            Get get = new Get(saltedKey);
            Result result = table.get(get);
            if (result.isEmpty()) {
                V newValue = loader.apply(key);
                if (Objects.nonNull(newValue)) {
                    put(key, newValue);
                }
                return newValue;
            }
            return valueDeSerializer.apply(result.getValue(columnFamily, qualifier));
        } catch (IOException ex) {
            LOG.error("Error retrieving data",ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Closes the underlying HBase resources.
     * @throws IOException if HBase throws any error
     */
    @Override
    public void close() throws IOException {
        if (!connection.isClosed()) {
        connection.close();
        }
    }

    /**
     * Creates a new {@link HBaseStore.Builder}.
     * @param <K> type of key elements
     * @param <V> type of values to store
     * @return a new instance of a HBaseKVStore.Builder
     */
    public static <K extends LogicalKey,V> Builder<K,V> builder() {
        return new Builder<>();
    }


    /**
     * Builder of {@link HBaseStore} instances.
     * @param <K> type of key elements
     * @param <V> type of values to store
     */
    public static class Builder<K extends LogicalKey, V> {
        private HBaseKVStoreConfiguration configuration;
        private Function<V, byte[]> valueSerializer;
        private Function<byte[], V> valueDeSerializer;
        private Function<K, V> loader;


        public Builder<K, V>  withHBaseStoreConfiguration(HBaseKVStoreConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }


        public Builder<K, V>  withValueSerializer(Function<V, byte[]> valueSerializer) {
            this.valueSerializer = valueSerializer;
            return this;
        }

        public Builder<K, V>  withValueDeSerializer(Function<byte[], V> valueDeSerializer) {
            this.valueDeSerializer = valueDeSerializer;
            return this;
        }

        public Builder<K, V>  withLoader(Function<K, V> loader) {
            this.loader = loader;
            return this;
        }


        public HBaseStore<K, V> build() throws IOException {
            return new HBaseStore<K, V>(configuration, valueSerializer, valueDeSerializer, loader);
        }
    }
}
