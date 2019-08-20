package org.gbif.rest.client.species.retrofit;

import okhttp3.OkHttpClient;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.retrofit.RetrofitClientFactory;
import org.gbif.rest.client.species.NameMatchService;
import org.gbif.rest.client.species.NameUsageMatch;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import static org.gbif.rest.client.retrofit.SyncCall.syncCall;

/**
 * Synchronous Retrofit service client for the NameMatch GBIF service.
 */
public class NameMatchServiceSyncClient implements NameMatchService {

  //Wrapped service
  private final NameMatchRetrofitService nameMatchRetrofitService;

  private final OkHttpClient okHttpClient;


  /**
   * Creates an instance using the provided configuration settings.
   * @param clientConfiguration Rest client configuration
   */
  public NameMatchServiceSyncClient(ClientConfiguration clientConfiguration) {
    okHttpClient = RetrofitClientFactory.createClient(clientConfiguration);
    nameMatchRetrofitService = RetrofitClientFactory.createRetrofitClient(okHttpClient,
                                                                          clientConfiguration.getBaseApiUrl(),
                                                                          NameMatchRetrofitService.class);
  }

  /**
   * See {@link NameMatchService#match(String, String, String, String, String, String, String, String, boolean, boolean)}
   */
  @Override
  public NameUsageMatch match(String kingdom, String phylum, String clazz, String order, String family, String genus,
                              String rank, String name, boolean verbose, boolean strict) {
    return syncCall(nameMatchRetrofitService.match(kingdom, phylum, clazz, order, family, genus, rank, name, verbose,
                                                   strict));
  }

  @Override
  public void close() throws IOException {
    if (Objects.nonNull(okHttpClient) && Objects.nonNull(okHttpClient.cache())
            && Objects.nonNull(okHttpClient.cache().directory())) {
      File cacheDirectory = okHttpClient.cache().directory();
      if (cacheDirectory.exists()) {
        try(Stream<File> files = Files.walk(cacheDirectory.toPath())
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)) {
          files.forEach(File::delete);
        }
      }
    }
  }
}
