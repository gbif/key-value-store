package org.gbif.kvs;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Utility class to generate salted keys based on a number of buckets/splits. Computed keys are
 * padded with an integer value.
 */
public class SaltedKeyGenerator implements Serializable {

  // Number of buckets/splits of the
  private final int numOfBuckets;

  // Cached variable that holds the left-padding format calculated from the number of bucket
  private final String paddingFormat;

  public SaltedKeyGenerator(int numOfBuckets) {
    this.numOfBuckets = numOfBuckets;
    // Calculated format is stored to avoid subsequent calculations of it
    paddingFormat = "%0" + Integer.toString(numOfBuckets).length() + 'd';
  }

  /**
   * Computes a salted key based on a expected number of buckets. The produced key is padded with
   * zeros to the left + logicalKey.hasCode*numOfBuckets + logicalKey.
   *
   * @param logicalKey logical identifier
   * @return a zeros left-padded string {0*}+bucketNumber+logicalKey
   */
  public byte[] computeKey(String logicalKey) {
    return (String.format(paddingFormat, logicalKey.hashCode() % numOfBuckets) + logicalKey)
        .getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Computes a salted key based on a expected number of buckets. Produces a key padded with zeros
   * to the left + new String(logicalKey).hasCode*numOfBuckets + new String(logicalKey).
   *
   * @param logicalKey logical identifier
   * @return a zeros left-padded string {0*}+bucketNumber+logicalKey
   */
  public byte[] computeKey(byte[] logicalKey) {
    return computeKey(new String(logicalKey, StandardCharsets.UTF_8));
  }

  /**
   * Extracts the bucket information of a salted key.
   *
   * @param saltedKey computed salted key
   * @return the bucket prefix
   */
  public byte[] bucketOf(String saltedKey) {
    return saltedKey.substring(0, Integer.toString(numOfBuckets).length()).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Extracts the bucket information of computed salted key.
   *
   * @param saltedKey computed salted key
   * @return the bucket prefix
   */
  public byte[] bucketOf(byte[] saltedKey) {
    return Arrays.copyOfRange(saltedKey, 0, Integer.toString(numOfBuckets).length());
  }
}
