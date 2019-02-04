package org.gbif.kvs.hbase;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

/**
 *  Configuration settings for KV store based on a simple HBase table.
 */
public class HBaseKVStoreConfiguration implements Serializable {

    private final String hbaseZk;
    private final String tableName;
    private final String columnFamily;
    private final String qualifier;
    private final int numOfKeyBuckets;

    public HBaseKVStoreConfiguration(String hbaseZk, String tableName, String columnFamily, String qualifier, int numOfKeyBuckets) {
        this.hbaseZk = hbaseZk;
        this.tableName = tableName;
        this.columnFamily = columnFamily;
        this.qualifier = qualifier;
        this.numOfKeyBuckets = numOfKeyBuckets;
    }

    /**
     * HBase Zookeeper ensemble.
     * @return HBase ZK
     */
    public String getHBaseZk() {
        return hbaseZk;
    }

    /**
     * HBase table name to store key value elements.
     * @return HBase table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Column family where values are stored.
     * @return HBase column family
     */
    public String getColumnFamily() {
        return columnFamily;
    }

    /**
     *  Column qualifier where values are stored.
     * @return column qualifier
     */
    public String getQualifier() {
        return qualifier;
    }

    /**
     * Number of buckets
     * @return number of salted key buckets
     */
    public int getNumOfKeyBuckets() {
        return numOfKeyBuckets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)  {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HBaseKVStoreConfiguration that = (HBaseKVStoreConfiguration) o;
        return numOfKeyBuckets == that.numOfKeyBuckets &&
                Objects.equals(hbaseZk, that.hbaseZk) &&
                Objects.equals(tableName, that.tableName) &&
                Objects.equals(columnFamily, that.columnFamily) &&
                Objects.equals(qualifier, that.qualifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hbaseZk, tableName, columnFamily, qualifier, numOfKeyBuckets);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", HBaseKVStoreConfiguration.class.getSimpleName() + "[", "]")
                .add("hbaseZk='" + hbaseZk + "'")
                .add("tableName='" + tableName + "'")
                .add("columnFamily='" + columnFamily + "'")
                .add("qualifier='" + qualifier + "'")
                .add("numOfKeyBuckets=" + numOfKeyBuckets)
                .toString();
    }

    /**
     * Creates an instance of {@link Configuration} setting the 'hbase.zookeeper.quorum' to getHbaseZk.
     * @return a new Hadoop configuration
     */
    public Configuration hbaseConfig() {
        Configuration hbaseConfig = HBaseConfiguration.create();
        hbaseConfig.set("hbase.zookeeper.quorum", hbaseZk);
        return hbaseConfig;
    }


    /**
     * Builder for {@link HBaseKVStoreConfiguration} instances.
     */
    public static class Builder {
        private String hbaseZk;
        private String tableName;
        private String columnFamily;
        private String qualifier;
        private int numOfKeyBuckets;

        public Builder withHBaseZk(String hbaseZk) {
            this.hbaseZk = hbaseZk;
            return this;
        }

        public Builder withTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder withColumnFamily(String columnFamily) {
            this.columnFamily = columnFamily;
            return this;
        }

        public Builder withQualifier(String qualifier) {
            this.qualifier = qualifier;
            return this;
        }

        public Builder withNumOfKeyBuckets(int numOfKeyBuckets) {
            this.numOfKeyBuckets = numOfKeyBuckets;
            return this;
        }

        public HBaseKVStoreConfiguration build() {
            return new HBaseKVStoreConfiguration(hbaseZk, tableName, columnFamily, qualifier, numOfKeyBuckets);
        }
    }
}
