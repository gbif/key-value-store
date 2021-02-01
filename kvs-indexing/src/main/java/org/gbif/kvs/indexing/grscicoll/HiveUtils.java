package org.gbif.kvs.indexing.grscicoll;

import java.util.List;

import org.gbif.kvs.grscicoll.GrscicollLookupRequest;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hive.hcatalog.common.HCatException;
import org.apache.hive.hcatalog.data.HCatRecord;
import org.apache.hive.hcatalog.data.schema.HCatSchema;
import org.apache.hive.hcatalog.data.schema.HCatSchemaUtils;
import org.apache.thrift.TException;

public class HiveUtils {

  private HiveUtils() {}

  public static GrscicollLookupRequest convertToGrSciCollRequest(
      HCatRecord record, HCatSchema schema) throws HCatException {
    return GrscicollLookupRequest.builder()
        .withOwnerInstitutionCode(record.getString("ownerInstitutionCode", schema))
        .withInstitutionCode(record.getString("institutionCode", schema))
        .withInstitutionId(record.getString("institutionId", schema))
        .withCollectionCode(record.getString("collectionCode", schema))
        .withCollectionId(record.getString("collectionId", schema))
        .withDatasetKey(record.getString("datasetKey", schema))
        .withCountry(record.getString("country", schema))
        .build();
  }

  public static HCatSchema readSchema(GrSciCollLookupIndexingOptions options)
      throws HCatException, TException {
    HiveConf hiveConf = new HiveConf();
    hiveConf.setVar(HiveConf.ConfVars.METASTOREURIS, options.getMetastoreUris());
    HiveMetaStoreClient metaStoreClient = new HiveMetaStoreClient(hiveConf);
    List<FieldSchema> fieldSchemaList =
        metaStoreClient.getSchema(options.getDatabase(), options.getTable());
    return HCatSchemaUtils.getHCatSchema(fieldSchemaList);
  }
}
