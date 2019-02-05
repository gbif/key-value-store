package org.gbif.rest.client.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Factory class for http client. */
public final class HttpClientFactory {

  private static final Logger LOG = LoggerFactory.getLogger(HttpClientFactory.class);

  private HttpClientFactory() {}

  /** Creates a {@link OkHttpClient} with a {@link Cache} from a specific {@link ClientConfiguration}. */
  public static OkHttpClient createClient(ClientConfiguration config) {

    OkHttpClient.Builder clientBuilder =
        new OkHttpClient.Builder()
            .connectTimeout(config.getTimeOut(), TimeUnit.SECONDS)
            .readTimeout(config.getTimeOut(), TimeUnit.SECONDS);

    clientBuilder.cache(createCache(config.getFileCacheMaxSizeMb()));

    // create the client and return it
    return clientBuilder.build();
  }

  /**
   * Creates a Cache using a maximum size.
   *
   * @param maxSize of the file cache in MB
   * @return a new instance of file based cache
   */
  private static Cache createCache(long maxSize) {

    try {
      // create cache file
      File httpCacheDirectory;
      // use a new file cache for the current session
      String cacheName = System.currentTimeMillis() + "-wsCache";
      httpCacheDirectory = Files.createTempDirectory(cacheName).toFile();
      httpCacheDirectory.deleteOnExit();
      LOG.info("Cache file created - {}", httpCacheDirectory.getAbsolutePath());
      // create cache
      return new Cache(httpCacheDirectory, maxSize);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Cannot run without the ability to create temporary cache directory", e);
    }
  }
}
