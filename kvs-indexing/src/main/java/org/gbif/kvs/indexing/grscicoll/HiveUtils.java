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
package org.gbif.kvs.indexing.grscicoll;

import org.gbif.kvs.grscicoll.GrscicollLookupRequest;

import java.util.List;

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
