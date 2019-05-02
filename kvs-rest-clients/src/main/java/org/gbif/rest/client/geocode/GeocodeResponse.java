package org.gbif.rest.client.geocode;

import java.util.Collection;
import java.util.Objects;

/**
 * Holds the list of location returned from the Geocode service.
 */
public class GeocodeResponse {


    private Collection<Location> locations;

    public Collection<Location> getLocations() {
        return locations;
    }

    public void setLocations(Collection<Location> locations) {
        this.locations = locations;
    }

    public GeocodeResponse() {
    }

    public GeocodeResponse(Collection<Location> locations) {
        this.locations = locations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeocodeResponse that = (GeocodeResponse) o;
        return Objects.equals(locations, that.locations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locations);
    }


}
