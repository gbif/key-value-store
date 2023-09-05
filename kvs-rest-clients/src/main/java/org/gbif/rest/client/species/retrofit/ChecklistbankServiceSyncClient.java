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
package org.gbif.rest.client.species.retrofit;

import org.gbif.rest.client.configuration.ChecklistbankClientsConfiguration;
import org.gbif.rest.client.retrofit.RetrofitClientFactory;
import org.gbif.rest.client.species.ChecklistbankService;
import org.gbif.rest.client.species.IucnRedListCategory;
import org.gbif.rest.client.species.NameUsageMatch;
import org.gbif.rest.client.species.NameUsageSearchResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import okhttp3.OkHttpClient;

import static org.gbif.rest.client.retrofit.SyncCall.nullableSyncCall;
import static org.gbif.rest.client.retrofit.SyncCall.syncCall;

/**
 * Synchronous Retrofit service client for the NameMatch GBIF service.
 */
public class ChecklistbankServiceSyncClient implements ChecklistbankService {

  //NameUsageSearchResponse match and CLB services are split in two clients since effectively are deployed in two different backends
  //Wrapped services
  private final ChecklistbankRetrofitService checklistbankRetrofitService;

  private final NameMatchRetrofitService nameMatchRetrofitService;

  private final OkHttpClient clbOkHttpClient;

  private final OkHttpClient nameMatchOkHttpClient;

  /**
   * Creates an instance using the provided configuration settings.
   * @param clientConfigurations Rest client configuration
   */
  public ChecklistbankServiceSyncClient(ChecklistbankClientsConfiguration clientConfigurations) {
    clbOkHttpClient = RetrofitClientFactory.createClient(clientConfigurations.getChecklistbankClientConfiguration());
    checklistbankRetrofitService = RetrofitClientFactory.createRetrofitClient(clbOkHttpClient,
                                                                              clientConfigurations.getChecklistbankClientConfiguration().getBaseApiUrl(),
                                                                              ChecklistbankRetrofitService.class);

    nameMatchOkHttpClient = RetrofitClientFactory.createClient(clientConfigurations.getChecklistbankClientConfiguration());
    nameMatchRetrofitService = RetrofitClientFactory.createRetrofitClient(nameMatchOkHttpClient,
                                                                          clientConfigurations.getNameUsageClientConfiguration().getBaseApiUrl(),
                                                                          NameMatchRetrofitService.class);
  }

  @Override
  public NameUsageMatch match(Integer usageKey, String kingdom, String phylum, String clazz, String order, String family, String genus,
                              String scientificName, String genericName, String specificEpithet,
                              String infraspecificEpithet, String scientificNameAuthorship, String rank, boolean verbose,
                              boolean strict) {
    return syncCall(nameMatchRetrofitService.match(usageKey, kingdom, phylum, clazz, order, family, genus, scientificName,
            genericName, specificEpithet, infraspecificEpithet, scientificNameAuthorship, rank, verbose, strict));
  }

  /**
   * See {@link ChecklistbankService#getIucnRedListCategory(Integer)}
   */
  @Override
  public IucnRedListCategory getIucnRedListCategory(Integer nubKey) {
    return nullableSyncCall(checklistbankRetrofitService.getIucnRedListCategory(nubKey)).orElse(null);
  }

  /**
   * See {@link ChecklistbankService#lookupNameUsage(String, String)}
   */
  @Override
  public NameUsageSearchResponse lookupNameUsage(String datasetKey, String sourceId) {
    return nullableSyncCall(checklistbankRetrofitService.lookupNameUsage(datasetKey, sourceId)).orElse(null);
  }

  @Override
  public void close() throws IOException {
    close(clbOkHttpClient);
    close(nameMatchOkHttpClient);
  }

  public void close(OkHttpClient okHttpClient) throws IOException {
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
