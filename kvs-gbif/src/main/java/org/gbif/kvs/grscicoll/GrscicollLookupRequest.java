package org.gbif.kvs.grscicoll;

import java.io.Serializable;
import java.util.Optional;

import org.gbif.kvs.hbase.Indexable;

import org.apache.avro.reflect.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(setterPrefix = "with", builderClassName = "Builder")
public class GrscicollLookupRequest implements Serializable, Indexable {

  @Nullable String institutionCode;
  @Nullable String ownerInstitutionCode;
  @Nullable String institutionId;
  @Nullable String collectionCode;
  @Nullable String collectionId;
  @Nullable String datasetKey;
  @Nullable String country;

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
