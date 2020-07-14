package org.gbif.rest.client.geocode;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Holds the list of location returned from the Geocode service.
 */
public class GeocodeResponse implements Serializable {


    private List<Location> locations;

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public GeocodeResponse() {
    }

    public GeocodeResponse(List<Location> locations) {
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
