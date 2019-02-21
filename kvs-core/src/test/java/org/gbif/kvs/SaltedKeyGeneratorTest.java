package org.gbif.kvs;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

/** Test class for {@link SaltedKeyGenerator}. */
public class SaltedKeyGeneratorTest {

  // Number of buckets/splits
  private static final int NUM_OF_BUCKETS = 10;

  // Salted key generator for 10 splits/buckets
  private static final SaltedKeyGenerator SALT_KEY_GENERATOR = new SaltedKeyGenerator(NUM_OF_BUCKETS);

  // Logical test key
  private static final String TEST_LOGICAL_KEY = "abcd";

  /** Asserts that a generated salted contains the expected prefix and suffix. */
  @Test
  public void saltedKeyGenerationTest() {

    String saltedKey = new String(SALT_KEY_GENERATOR.computeKey(TEST_LOGICAL_KEY), SALT_KEY_GENERATOR.getCharset());

    Assert.assertTrue(
        "Salted key must end with provided logical key", saltedKey.endsWith(TEST_LOGICAL_KEY));

    String bucket = new String(SALT_KEY_GENERATOR.bucketOf(saltedKey), SALT_KEY_GENERATOR.getCharset());

    Assert.assertTrue(
        "Salted key must end with provided logical key", saltedKey.startsWith(bucket));
  }

  /** Tests that salted key are evenly distributed in buckets. */
  @Test
  public void evenlyDistributedKeys() {
    int numOfRecords = 20;
    Map<String, Long> counts =
        IntStream.rangeClosed(1, numOfRecords)
            .mapToObj(key -> SALT_KEY_GENERATOR.computeKey(Integer.toString(key)))
            .collect(
                Collectors.groupingBy(
                    key -> new String(SALT_KEY_GENERATOR.bucketOf(key), SALT_KEY_GENERATOR.getCharset()), Collectors.counting()));

    // The elements must be allocated in all the buckets
    Assert.assertEquals("Wrong number of expected buckets", NUM_OF_BUCKETS, counts.size());

    // The expected average is at least 4 elements per bucket
    counts.values().stream()
        .mapToDouble(count -> count)
        .average()
        .ifPresent(average -> Assert.assertEquals(numOfRecords/NUM_OF_BUCKETS, average, 0.0001));
  }

  /**
   * Tests that the prefix generated for the salted key corresponds to the expected value.
   */
  @Test
  public void prefixLengthTest() {
    Stream.of(1, 10, 10, 100, 127, 542, 1000).forEach(bucket -> {
      SaltedKeyGenerator saltedKeyGenerator = new SaltedKeyGenerator(bucket);
      Assert.assertEquals("Bucket prefix is not of expected size", Integer.toString(bucket - 1).length(),
                          saltedKeyGenerator.bucketOf(saltedKeyGenerator.computeKey(TEST_LOGICAL_KEY)).length);
    });
  }
}
