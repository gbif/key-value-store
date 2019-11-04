package org.gbif.kvs.hbase;

/**
 * Exponential backoff randomized retry.
 */
public class LoaderRetryConfig {

  private static final int DEFAULT_MAX_ATTEMPTS = 3;
  private static final long DEFAULT_INITIAL_INTERVAL = 500;
  private static final double DEFAULT_MULTIPLIER = 1.5;
  private static final double DEFAULT_RANDOMIZATION_FACTOR = 0.5;

  public static LoaderRetryConfig DEFAULT = new LoaderRetryConfig();


  private final Integer maxAttempts;

  private final Long initialIntervalMillis;

  private final Double multiplier;

  private final Double randomizationFactor;

  public LoaderRetryConfig(Integer maxAttempts, Long initialIntervalMillis, Double multiplier, Double randomizationFactor) {
    this.maxAttempts = maxAttempts;
    this.initialIntervalMillis = initialIntervalMillis;
    this.multiplier = multiplier;
    this.randomizationFactor = randomizationFactor;
  }

  private LoaderRetryConfig() {
    this(DEFAULT_MAX_ATTEMPTS, DEFAULT_INITIAL_INTERVAL, DEFAULT_MULTIPLIER, DEFAULT_RANDOMIZATION_FACTOR);
  }

  /**
   * Maximum number of attempts/retries.
   */
  public Integer getMaxAttempts() {
    return maxAttempts;
  }

  /**
   * Initial interval after first retry.
   */
  public Long getInitialIntervalMillis() {
    return initialIntervalMillis;
  }

  /**
   * Exponential backoff multiplier factor.
   */
  public Double getMultiplier() {
    return multiplier;
  }

  /**
   * Jitter random factor.
   */
  public Double getRandomizationFactor() {
    return randomizationFactor;
  }
}
