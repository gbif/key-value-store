package org.gbif.rest.client.geocode.retrofit;

import okhttp3.OkHttpClient;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.retrofit.RetrofitClientFactory;
import org.gbif.rest.client.geocode.Location;
import org.gbif.rest.client.geocode.GeocodeService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import static org.gbif.rest.client.retrofit.SyncCall.syncCall;

/**
 * Represents an {@link GeocodeService} synchronous client.
 * It wraps a Retrofit client to perform the actual calls.
 */
public class GeocodeServiceSyncClient implements GeocodeService {

  //Retrofit internal client
  private final GeocodeRetrofitService retrofitService;

  private final OkHttpClient okHttpClient;

  /**
   * Creates an instance using the provided configuration settings.
   * @param clientConfiguration Rest client configuration
   */
  public GeocodeServiceSyncClient(ClientConfiguration clientConfiguration) {
    okHttpClient = RetrofitClientFactory.createClient(clientConfiguration);
    retrofitService = RetrofitClientFactory.createRetrofitClient(okHttpClient,
                                                                clientConfiguration.getBaseApiUrl(),
                                                                GeocodeRetrofitService.class);
  }

  /**
   * Performs a synchronous call to the Geocode service.
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   * @return the List of proposed locations, an empty list otherwise
   */
  @Override
  public List<Location> reverse(Double latitude, Double longitude) {
    return syncCall(retrofitService.reverse(latitude, longitude));
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
