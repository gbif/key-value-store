package org.gbif.kvs;

import java.io.Serializable;
import java.nio.charset.Charset;
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
  
  //Charset encoding, only the name is store because th Charset class is not serializable
  private final String charset;

  /**
   * Creates a new instance using UTF_8 as default Charset.
   * @param numOfBuckets salted key buckets
   */
  public SaltedKeyGenerator(int numOfBuckets) {
    this(numOfBuckets, StandardCharsets.UTF_8);
  }

  /**
   * Creates a new instance using a defined number of buckets and a charset.
   * @param numOfBuckets salted key buckets
   * @param charset encoding charset
   */
  public SaltedKeyGenerator(int numOfBuckets, Charset charset) {
    this.numOfBuckets = numOfBuckets;
    this.charset = charset.name();
    // Calculated format is stored to avoid subsequent calculations of it
    paddingFormat = "%0" + Integer.toString(numOfBuckets).length() + 'd';
  }

  /**
   *
   * @return the charset used by this key generator
   */
  public Charset getCharset() {
    return Charset.forName(charset);
  }

  /**
   * Computes a salted key based on a expected number of buckets. The produced key is padded with
   * zeros to the left + logicalKey.hasCode*numOfBuckets + logicalKey.
   *
   * @param logicalKey logical identifier
   * @return a zeros left-padded string {0*}+bucketNumber+logicalKey
   */
  public byte[] computeKey(String logicalKey) {
    return (String.format(paddingFormat, Math.abs(logicalKey.hashCode() % numOfBuckets)) + logicalKey)
            .getBytes(getCharset());
  }

  /**
   * Computes a salted key based on a expected number of buckets. Produces a key padded with zeros
   * to the left + new String(logicalKey).hasCode*numOfBuckets + new String(logicalKey).
   *
   * @param logicalKey logical identifier
   * @return a zeros left-padded string {0*}+bucketNumber+logicalKey
   */
  public byte[] computeKey(byte[] logicalKey) {
    return computeKey(new String(logicalKey, getCharset()));
  }

  /**
   * Extracts the bucket information of a salted key.
   *
   * @param saltedKey computed salted key
   * @return the bucket prefix
   */
  public byte[] bucketOf(String saltedKey) {
    return saltedKey.substring(0, Integer.toString(numOfBuckets).length()).getBytes(getCharset());
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
