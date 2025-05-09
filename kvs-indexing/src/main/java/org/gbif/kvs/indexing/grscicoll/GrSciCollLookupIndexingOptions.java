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

import org.gbif.kvs.indexing.options.HBaseIndexingOptions;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;

public interface GrSciCollLookupIndexingOptions extends HBaseIndexingOptions {

  @Description("HBase column qualifier to stored GrSciColl Lookup JSON response")
  @Default.String("j")
  String getJsonColumnQualifier();

  void setJsonColumnQualifier(String jsonColumnQualifier);

  @Description("Hive database")
  String getDatabase();

  void setDatabase(String database);

  @Description("Source table with classifications (see project readme)")
  @Default.String("occurrence_collections")
  String getTable();

  void setTable(String table);

  @Description("Uri to hive Metastore, e.g.: thrift://hivesever2:9083")
  String getMetastoreUris();

  void setMetastoreUris(String metastoreUris);

  @Description("Whether to use snapshotting")
  @Default.Boolean(true)
  Boolean getUseSnapshotting();

  void setUseSnapshotting(Boolean useSnapshotting);

  @Description("Trino server")
  String getTrinoServer();

  void setTrinoServer(String trinoServer);

  @Description("Trino user")
  String getTrinoUser();

  void setTrinoUser(String trinoUser);

  @Description("Trino password")
  String getTrinoPassword();

  void setTrinoPassword(String trinoPassword);

  @Description("Trino catalog")
  @Default.String("hive")
  String getTrinoCatalog();

  void setTrinoCatalog(String trinoCatalog);

  @Description("Trino DB name")
  String getTrinoDbName();

  void setTrinoDbName(String trinoDbName);

  @Description("Trino target table")
  String getTrinoTargetTable();

  void setTrinoTargetTable(String trinoTargetTable);
}
