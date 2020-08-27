package org.gbif.rest.client.grscicoll.retrofit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.gbif.api.vocabulary.Country;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.grscicoll.LookupResponse;
import org.gbif.rest.client.grscicoll.LookupService;
import org.gbif.rest.client.retrofit.RetrofitClientFactory;
import org.gbif.rest.client.retrofit.SyncCall;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

public class LookupServiceSyncClient implements LookupService {

  // retrofit client
  private final LookupRetrofitService retrofitService;

  private final OkHttpClient okHttpClient;

  public LookupServiceSyncClient(ClientConfiguration clientConfiguration) {
    okHttpClient = RetrofitClientFactory.createClient(clientConfiguration);
    retrofitService =
        RetrofitClientFactory.createRetrofitClient(
            okHttpClient,
            clientConfiguration.getBaseApiUrl(),
            LookupRetrofitService.class,
            new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
  }

  @Override
  public LookupResponse lookup(
      String institutionCode,
      String ownerInstitutionCode,
      String institutionId,
      String collectionCode,
      String collectionId,
      UUID datasetKey,
      Country country) {
    return SyncCall.syncCall(
        retrofitService.lookup(
            institutionCode,
            ownerInstitutionCode,
            institutionId,
            collectionCode,
            collectionId,
            datasetKey,
            country,
            false));
  }

  @Override
  public void close() throws IOException {
    if (Objects.nonNull(okHttpClient)
        && Objects.nonNull(okHttpClient.cache())
        && Objects.nonNull(okHttpClient.cache().directory())) {
      File cacheDirectory = okHttpClient.cache().directory();
      if (cacheDirectory.exists()) {
        try (Stream<File> files =
            Files.walk(cacheDirectory.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)) {
          files.forEach(File::delete);
        }
      }
    }
  }
}
