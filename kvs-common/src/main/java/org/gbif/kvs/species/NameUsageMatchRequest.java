package org.gbif.kvs.species;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gbif.kvs.Keyed;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuperBuilder(setterPrefix = "with")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameUsageMatchRequest implements Keyed, Serializable {

    protected String usageKey;
    protected String taxonID;
    protected String taxonConceptID;
    protected String scientificNameID;
    protected String scientificName;
    protected String authorship;
    protected String rank;
    protected String verbatimRank;
    protected String genericName;
    protected String specificEpithet;
    protected String infraspecificEpithet;
    protected String kingdom;
    protected String phylum;
    protected String clazz;
    protected String order;
    protected String family;
    protected String genus;
    protected String subgenus;
    protected String species;
    protected Boolean strict;
    protected Boolean verbose;

    @Override
    public String getLogicalKey() {
        return Stream.of(scientificNameID, taxonConceptID, taxonID, kingdom, phylum, clazz, order, family, genus, subgenus, species,
                        scientificName, genericName, specificEpithet, infraspecificEpithet, authorship, rank)
                .map(s -> s == null ? "" : s.trim()).collect(Collectors.joining("|"));
    }
}
