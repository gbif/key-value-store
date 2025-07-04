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
package org.gbif.kvs.species;

import org.gbif.kvs.Keyed;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import feign.Param;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder(setterPrefix = "with")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameUsageMatchRequest implements Keyed, Serializable {

    protected String checklistKey;
    protected String usageKey;
    protected String taxonID;
    protected String taxonConceptID;
    protected String scientificNameID;
    protected String scientificName;
    protected String scientificNameAuthorship;
    protected String taxonRank;
    protected String verbatimTaxonRank;
    protected String genericName;
    protected String specificEpithet;
    protected String infraspecificEpithet;
    protected String kingdom;
    protected String phylum;
    @Param("class") protected String clazz;
    protected String order;
    protected String superfamily;
    protected String family;
    protected String subfamily;
    protected String tribe;
    protected String subtribe;
    protected String genus;
    protected String subgenus;
    protected String species;
    protected Boolean strict;
    protected Boolean verbose;

    @Override
    public String getLogicalKey() {
        return Stream.of(checklistKey, scientificNameID, taxonConceptID, taxonID, kingdom, phylum, clazz, order,
                        superfamily, family, subfamily, tribe, subtribe, genus, subgenus, species,
                        scientificName, genericName, specificEpithet,
                        infraspecificEpithet, scientificNameAuthorship, taxonRank)
                .map(s -> s == null ? "" : s.trim()).collect(Collectors.joining("|"));
    }
}
