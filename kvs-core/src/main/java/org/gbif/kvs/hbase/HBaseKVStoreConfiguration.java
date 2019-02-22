package org.gbif.kvs.hbase;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

/** Configuration settings for KV store based on a simple HBase table. */
public class HBaseKVStoreConfiguration implements Serializable {

  private final String hbaseZk;
  private final String tableName;
  private final String columnFamily;
  private final int numOfKeyBuckets;

  public HBaseKVStoreConfiguration(String hbaseZk, String tableName, String columnFamily, int numOfKeyBuckets) {
    this.hbaseZk = hbaseZk;
    this.tableName = tableName;
    this.columnFamily = columnFamily;
    this.numOfKeyBuckets = numOfKeyBuckets;
  }

  /**
   * HBase Zookeeper ensemble.
   *
   * @return HBase ZK
   */
  public String getHBaseZk() {
    return hbaseZk;
  }

  /**
   * HBase table name to store key value elements.
   *
   * @return HBase table name
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Column family in which the values are stored.
   *
   * @return HBase column family
   */
  public String getColumnFamily() {
    return columnFamily;
  }

  /**
   * Number of buckets
   *
   * @return number of salted key buckets
   */
  public int getNumOfKeyBuckets() {
    return numOfKeyBuckets;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HBaseKVStoreConfiguration that = (HBaseKVStoreConfiguration) o;
    return numOfKeyBuckets == that.numOfKeyBuckets
        && Objects.equals(hbaseZk, that.hbaseZk)
        && Objects.equals(tableName, that.tableName)
        && Objects.equals(columnFamily, that.columnFamily);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hbaseZk, tableName, columnFamily, numOfKeyBuckets);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", HBaseKVStoreConfiguration.class.getSimpleName() + "[", "]")
        .add("hbaseZk='" + hbaseZk + "'")
        .add("tableName='" + tableName + "'")
        .add("columnFamily='" + columnFamily + "'")
        .add("numOfKeyBuckets=" + numOfKeyBuckets)
        .toString();
  }

  /**
   * Creates an instance of {@link Configuration} setting the 'hbase.zookeeper.quorum' to
   * getHbaseZk.
   *
   * @return a new Hadoop configuration
   */
  public Configuration hbaseConfig() {
    Configuration hbaseConfig = HBaseConfiguration.create();
    hbaseConfig.set("hbase.zookeeper.quorum", hbaseZk);
    return hbaseConfig;
  }

  /**
   * Creates a new {@link Builder} instance.
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for {@link HBaseKVStoreConfiguration} instances. */
  public static class Builder {

    private String hbaseZk;
    private String tableName;
    private String columnFamily;
    private int numOfKeyBuckets;

    /**
     * Hidden constructor to force use the containing class builder() method.
     */
    private Builder() {
      //DO NOTHING
    }

    public Builder withHBaseZk(String hbaseZk) {
      this.hbaseZk = hbaseZk;
      return this;
    }

    public Builder withTableName(String tableName) {
      this.tableName = tableName;
      return this;
    }

    public Builder withNumOfKeyBuckets(int numOfKeyBuckets) {
      this.numOfKeyBuckets = numOfKeyBuckets;
      return this;
    }


    public Builder withColumnFamily(String columnFamily) {
      this.columnFamily = columnFamily;
      return this;
    }

    public HBaseKVStoreConfiguration build() {
      return new HBaseKVStoreConfiguration(hbaseZk, tableName, columnFamily, numOfKeyBuckets);
    }
  }
}
