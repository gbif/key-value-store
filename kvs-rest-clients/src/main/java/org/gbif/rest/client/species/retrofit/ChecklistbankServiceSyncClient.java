package org.gbif.rest.client.species.retrofit;

import okhttp3.OkHttpClient;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.retrofit.RetrofitClientFactory;
import org.gbif.rest.client.species.ChecklistbankService;
import org.gbif.rest.client.species.IucnRedListCategory;
import org.gbif.rest.client.species.NameUsageMatch;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.gbif.rest.client.retrofit.SyncCall.nullableSyncCall;
import static org.gbif.rest.client.retrofit.SyncCall.syncCall;

/**
 * Synchronous Retrofit service client for the NameMatch GBIF service.
 */
public class ChecklistbankServiceSyncClient implements ChecklistbankService {

  //Wrapped services
  private final ChecklistbankRetrofitService checklistbankRetrofitService;

  private final OkHttpClient okHttpClient;

  /**
   * Creates an instance using the provided configuration settings.
   * @param clientConfiguration Rest client configuration
   */
  public ChecklistbankServiceSyncClient(ClientConfiguration clientConfiguration) {
    okHttpClient = RetrofitClientFactory.createClient(clientConfiguration);
    checklistbankRetrofitService = RetrofitClientFactory.createRetrofitClient(okHttpClient,
                                                                              clientConfiguration.getBaseApiUrl(),
                                                                              ChecklistbankRetrofitService.class);
  }

  /**
   * See {@link ChecklistbankService#match(String, String, String, String, String, String, String, String, boolean, boolean)}
   */
  @Override
  public NameUsageMatch match(String kingdom, String phylum, String clazz, String order, String family, String genus,
                              String rank, String name, boolean verbose, boolean strict) {
    return syncCall(checklistbankRetrofitService.match(kingdom, phylum, clazz, order, family, genus, rank, name, verbose,
                                                       strict));
  }

  /**
   * See {@link ChecklistbankService#getIucnRedListCategory(Integer)}
   */
  @Override
  public IucnRedListCategory getIucnRedListCategory(Integer nubKey) {
    return nullableSyncCall(checklistbankRetrofitService.getIucnRedListCategory(nubKey)).orElse(null);
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
