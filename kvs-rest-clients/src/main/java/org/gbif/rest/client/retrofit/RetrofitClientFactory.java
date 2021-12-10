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
package org.gbif.rest.client.retrofit;

import org.gbif.rest.client.configuration.ClientConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/** Factory class for okHttp and retrofit clients. */
public final class RetrofitClientFactory {

  private static final Logger LOG = LoggerFactory.getLogger(RetrofitClientFactory.class);

  private RetrofitClientFactory() {}

  /** Creates a {@link OkHttpClient} with a {@link Cache} from a specific {@link ClientConfiguration}. */
  public static OkHttpClient createClient(ClientConfiguration config) {

    OkHttpClient.Builder clientBuilder =
        new OkHttpClient.Builder()
            .connectTimeout(config.getTimeOut(), TimeUnit.SECONDS)
            .readTimeout(config.getTimeOut(), TimeUnit.SECONDS)
            .callTimeout(config.getTimeOut(), TimeUnit.SECONDS);

    clientBuilder.cache(createCache(config.getFileCacheMaxSizeMb()));

    // create the client and return it
    return clientBuilder.build();
  }

  public static <S> S createRetrofitClient(OkHttpClient okHttpClient, String baseApiUrl, Class<S> serviceClass) {
    // create service
    return new Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseApiUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .validateEagerly(true)
            .build().create(serviceClass);
  }

  public static <S> S createRetrofitClient(
      OkHttpClient okHttpClient,
      String baseApiUrl,
      Class<S> serviceClass,
      ObjectMapper objectMapper) {
    // create service
    return new Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(baseApiUrl)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .validateEagerly(true)
        .build().create(serviceClass);
  }

  public static <S> S createRetrofitClient(ClientConfiguration clientConfiguration, String baseApiUrl,
                                           Class<S> serviceClass) {
    // create service
    return new Retrofit.Builder()
                .client(createClient(clientConfiguration))
                .baseUrl(baseApiUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .validateEagerly(true)
                .build().create(serviceClass);
  }

  /**
   * Creates a Cache using a maximum size.
   *
   * @param maxSize of the file cache in MB
   * @return a new instance of file based cache
   */
  private static Cache createCache(long maxSize) {

    try {
      // use a new file cache for the current session
      String cacheName = System.currentTimeMillis() + "-wsCache";
      File httpCacheDirectory = Files.createTempDirectory(cacheName).toFile();
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
