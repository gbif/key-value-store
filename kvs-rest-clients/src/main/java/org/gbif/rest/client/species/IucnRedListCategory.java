package org.gbif.rest.client.species;

import org.gbif.api.vocabulary.TaxonomicStatus;

import java.io.Serializable;

import lombok.Data;

/**
 * The IUCN RedList Category associated to a species.
 */
@Data
public class IucnRedListCategory implements Serializable {

  private String category;

  private String code;

  private Integer usageKey;

  private String scientificName;

  private TaxonomicStatus taxonomicStatus;

  private String acceptedName;

  private Integer acceptedUsageKey;

}
