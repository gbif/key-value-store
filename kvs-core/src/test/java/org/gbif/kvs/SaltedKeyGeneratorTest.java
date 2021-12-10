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
package org.gbif.kvs;

import java.nio.charset.StandardCharsets;
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


  @Test
  public void testKeys() {

    System.out.println("First batch");
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("a"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("b"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("c"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("d"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("e"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("f"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("g"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("h"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("i"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("j"), StandardCharsets.UTF_8));


    System.out.println("Second batch");
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("l"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("m"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("n"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("o"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("p"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("q"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("r"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("s"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("t"), StandardCharsets.UTF_8));
    System.out.println(new String(SALT_KEY_GENERATOR.computeKey("u"), StandardCharsets.UTF_8));

  }
}
