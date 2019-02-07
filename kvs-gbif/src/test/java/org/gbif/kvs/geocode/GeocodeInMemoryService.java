package org.gbif.kvs.geocode;

import org.gbif.rest.client.geocode.GeocodeResponse;
import org.gbif.rest.client.geocode.GeocodeService;

import java.util.Collection;
import java.util.Map;

public class GeocodeInMemoryService implements GeocodeService {


  private final Map<LatLng, Collection<GeocodeResponse>> store;

  public GeocodeInMemoryService(Map<LatLng, Collection<GeocodeResponse>> store) {
    this.store = store;
  }

  @Override
  public Collection<GeocodeResponse> reverse(Double latitude, Double longitude) {
    return store.get(LatLng.create(latitude, longitude));
  }
}
