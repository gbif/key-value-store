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
package org.gbif.kvs.grscicoll;

import org.gbif.kvs.Keyed;

import java.io.Serializable;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(setterPrefix = "with")
@lombok.EqualsAndHashCode
public class GrscicollLookupRequest implements Keyed, Serializable {

  protected String institutionCode;
  protected String ownerInstitutionCode;
  protected String institutionId;
  protected String collectionCode;
  protected String collectionId;
  protected String datasetKey;
  protected String country;

  @Override
  public String getLogicalKey() {
    return
            parseStringValue(institutionCode)
                    + parseStringValue(ownerInstitutionCode)
                    + parseStringValue(institutionId)
                    + parseStringValue(collectionCode)
                    + parseStringValue(collectionId)
                    + parseStringValue(datasetKey)
                    + parseStringValue(country);
  }

  private String parseStringValue(String rawValue) {
    return Optional.ofNullable(rawValue).filter(v -> !v.isEmpty()).map(String::trim).orElse("null");
  }
}
