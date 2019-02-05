package org.gbif.kvs;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

/** Test class for {@link SaltedKeyGenerator}. */
public class SaltedKeyGeneratorTest {

  // Number of buckets/splits
  private static final int NUM_OF_BUCKETS = 4;

  // Salted key generator for 10 splits/buckets
  private static final SaltedKeyGenerator SALT_KEY_GENERATOR =
      new SaltedKeyGenerator(NUM_OF_BUCKETS);

  // Logical test key
  private static final String TEST_LOGICAL_KEY = "abcd";

  /** Asserts that a generated salted contains the expected prefix and sufix. */
  @Test
  public void saltedKeyGenerationTest() {

    String saltedKey = new String(SALT_KEY_GENERATOR.computeKey(TEST_LOGICAL_KEY));

    Assert.assertTrue(
        "Salted key must end with provided logical key", saltedKey.endsWith(TEST_LOGICAL_KEY));

    String bucket = new String(SALT_KEY_GENERATOR.bucketOf(saltedKey));

    Assert.assertTrue(
        "Salted key must end with provided logical key", saltedKey.startsWith(bucket));
  }

  /** Tests that salted key are evenly distributed in buckets. */
  @Test
  public void evenlyDistributedKeys() {

    Map<String, Long> counts =
        IntStream.rangeClosed(1, 16)
            .mapToObj(key -> SALT_KEY_GENERATOR.computeKey(Integer.toString(key)))
            .collect(
                Collectors.groupingBy(
                    key -> new String(SALT_KEY_GENERATOR.bucketOf(key)), Collectors.counting()));

    // The elements must be allocated in all the buckets
    Assert.assertEquals("Wrong number of expected buckets", NUM_OF_BUCKETS, counts.size());

    // The expected average is at least 4 elements per bucket
    counts.values().stream()
        .mapToDouble(count -> count)
        .average()
        .ifPresent(average -> Assert.assertEquals(average, NUM_OF_BUCKETS, 0.0001));
  }
}
