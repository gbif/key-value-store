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
package org.gbif.rest.client.species;

import org.gbif.api.vocabulary.TaxonomicStatus;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * The IUCN RedList Category associated to a species.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IucnRedListCategory implements Serializable {

  private String category;

  private String code;

  private Integer usageKey;

  private String scientificName;

  private TaxonomicStatus taxonomicStatus;

  private String acceptedName;

  private Integer acceptedUsageKey;

}
