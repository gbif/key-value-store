package org.gbif.kvs.geocode;

import org.gbif.kvs.KeyValueStore;
import org.gbif.kvs.hbase.HBaseStore;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;
import org.gbif.rest.client.geocode.retrofit.GeocodeServiceSyncClient;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Factory of Geocode KV instances. */
public class GeocodeKVStoreFactory {

  private static final Logger LOG = LoggerFactory.getLogger(GeocodeKVStoreFactory.class);

  // Used to store and retrieve JSON values stored in HBase
  private static final ObjectMapper MAPPER = new ObjectMapper();

  {
    MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  /** Hidden constructor. */
  private GeocodeKVStoreFactory() {
    // DO NOTHING
  }

  /**
   * Returns a function that maps HBase results into single String values.
   *
   * @param columnFamily HBase column in which values are stored
   * @param columnQualifier HBase column qualifier in which values are stored
   * @return a Result to String mapping function
   */
  public static Function<Result, String> simpleResultMapper(
      byte[] columnFamily, byte[] columnQualifier) {
    return result -> Bytes.toString(result.getValue(columnFamily, columnQualifier));
  }

  /**
   * Function that extracts the first country code of a list of {@link GeocodeResponse}.
   *
   * @return a function that extracts the first country code
   */
  public static Function<Collection<GeocodeResponse>, String> countryCodeMapper() {
    return geocodeResponses -> {
      if (Objects.nonNull(geocodeResponses) && geocodeResponses.iterator().hasNext()) {
        return geocodeResponses.iterator().next().getIsoCountryCode2Digit();
      }
      return null;
    };
  }

  /**
   * Creates a mutator function that maps a key and a list of {@link GeocodeResponse} into a {@link
   * Put}.
   *
   * @param columnFamily HBase column in which values are stored
   * @param countryCodeColumnQualifier HBase column qualifier in which the country code is stored
   * @param jsonColumnQualifier HBase column qualifier in which json responses are stored
   * @return a mapper from a key geocode responses into HBase Puts
   */
  public static BiFunction<byte[], Collection<GeocodeResponse>, Put> valueMutator(
      byte[] columnFamily, byte[] countryCodeColumnQualifier, byte[] jsonColumnQualifier) {
    return (key, geocodeResponses) -> {
      try {
        if (Objects.nonNull(geocodeResponses) && !geocodeResponses.isEmpty()) {
          Put put = new Put(key);
          put.addColumn(
              columnFamily,
              countryCodeColumnQualifier,
              Bytes.toBytes(countryCodeMapper().apply(geocodeResponses)));
          put.addColumn(
              columnFamily, jsonColumnQualifier, MAPPER.writeValueAsBytes(geocodeResponses));
          return put;
        }
        return null;
      } catch (IOException ex) {
        LOG.error("Error serializing response into bytes", ex);
        throw new RuntimeException(ex);
      }
    };
  }

  /**
   * Create a new instance of a Geocode KV store/cache backed by an HBase table. This instance only
   * returns the ISO country code of the resulting geocode lookup.
   *
   * @param configuration KV store configuration
   * @param geocodeClientConfiguration Rest client configuration for the GeocodeService client
   * @return a new instance of Geocode KV store
   * @throws IOException if the Rest client can't be created
   */
  public static KeyValueStore<LatLng, String> simpleGeocodeKVStore(GeocodeKVStoreConfiguration configuration,
                                                                   ClientConfiguration geocodeClientConfiguration) throws IOException {
    return simpleGeocodeKVStore(configuration, new GeocodeServiceSyncClient(geocodeClientConfiguration));

  }

  /**
   * Create a new instance of a Geocode KV store/cache backed by an HBase table. This instance only
   * returns the ISO country code of the resulting geocode lookup.
   *
   * @param configuration KV store configuration
   * @param geocodeService instance of the Rest GeocodeService client
   * @return a new instance of Geocode KV store
   * @throws IOException if the Rest client can't be created
   */
  public static KeyValueStore<LatLng, String> simpleGeocodeKVStore(GeocodeKVStoreConfiguration configuration,
                                                                   GeocodeService geocodeService) throws IOException {
    return HBaseStore.<LatLng, String, Collection<GeocodeResponse>>builder()
        .withHBaseStoreConfiguration(configuration.getHBaseKVStoreConfiguration())
        .withResultMapper(
            simpleResultMapper(
                Bytes.toBytes(configuration.getHBaseKVStoreConfiguration().getColumnFamily()),
                Bytes.toBytes(configuration.getCountryCodeColumnQualifier())))
        .withValueMapper(countryCodeMapper())
        .withValueMutator(
            valueMutator(
                Bytes.toBytes(configuration.getHBaseKVStoreConfiguration().getColumnFamily()),
                Bytes.toBytes(configuration.getCountryCodeColumnQualifier()),
                Bytes.toBytes(configuration.getJsonColumnQualifier())))
        .withLoader(
            latLng -> {
              try {
                return geocodeService.reverse(latLng.getLatitude(), latLng.getLongitude());
              } catch (Exception ex) {
                LOG.error("Error contacting geocode service", ex);
                throw new IllegalStateException(ex);
              }
            })
        .build();

  }
}
