package org.gbif.rest.client.geocode;

import java.io.Serializable;
import java.util.List;

import org.gbif.rest.client.configuration.ClientConfiguration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds the list of location returned from the Geocode service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeocodeResponse implements Serializable {

    private List<Location> locations;
}
