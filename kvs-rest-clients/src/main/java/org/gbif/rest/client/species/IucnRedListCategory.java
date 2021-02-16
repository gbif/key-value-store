package org.gbif.rest.client.species;

import java.io.Serializable;

import lombok.Data;

/**
 * The IUCN RedList Category associated to a species.
 */
@Data
public class IucnRedListCategory implements Serializable {

  private String category;

  private String code;

  private Integer iucnRedListSpeciesKey;

  private String iucnRedListName;
}
