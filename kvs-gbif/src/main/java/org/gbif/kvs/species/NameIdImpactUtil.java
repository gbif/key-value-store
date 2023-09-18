package org.gbif.kvs.species;

import com.google.common.collect.ImmutableMap;
import org.gbif.rest.client.configuration.ChecklistbankClientsConfiguration;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.gbif.rest.client.species.ChecklistbankService;
import org.gbif.rest.client.species.NameUsageMatch;
import org.gbif.rest.client.species.retrofit.ChecklistbankServiceSyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;

/**
 * This is utility intended for developers to run in their IDE on within the shaded jar.
 * Takes an input file of verbatim names and current backbone match, and determines if that would change if run using
 * the provided configuration. This is developed with the aim of assessing the impact of enabling a checklist to use for
 * name/taxa IDs (e.g. WoRMS). The input data can be created using a Hive query such as:
 * <pre>
 *     CREATE TABLE tim.worms ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' AS
 * SELECT
 *   v_scientificNameID,
 *   v_kingdom,
 *   v_phylum,
 *   v_class,
 *   v_order,
 *   v_family,
 *   v_genus,
 *   v_scientificName,
 *   v_genericName,
 *   v_specificEpithet,
 *   v_infraspecificEpithet,
 *   v_scientificNameAuthorship,
 *   coalesce(v_taxonRank,v_verbatimTaxonRank) as rank,
 *   taxonKey,
 *   acceptedTaxonKey,
 *   scientificName,
 *   acceptedScientificName,
 *   count(*) AS records
 * FROM prod_h.occurrence
 * WHERE v_scientificNameID LIKE ('urn:lsid:marine%') OR v_scientificNameID LIKE ('htt%://www.marinedata%')
 * GROUP BY
 *   v_scientificNameID,
 *   v_kingdom,
 *   v_phylum,
 *   v_class,
 *   v_order,
 *   v_family,
 *   v_genus,
 *   v_scientificName,
 *   v_genericName,
 *   v_specificEpithet,
 *   v_infraspecificEpithet,
 *   v_scientificNameAuthorship,
 *   coalesce(v_taxonRank,v_verbatimTaxonRank),
 *   taxonKey,
 *   acceptedTaxonKey,
 *   scientificName,
 *   acceptedScientificName;
 * </pre>
 */
public class NameIdImpactUtil {
  private static final Logger LOG = LoggerFactory.getLogger(NameIdImpactUtil.class);

  public static void main(String[] args) throws IOException {
    ClientConfiguration client = ClientConfiguration.builder()
        .withBaseApiUrl("https://api.gbif.org/v1/")
        .build();

    ChecklistbankService clb = new ChecklistbankServiceSyncClient(
        ChecklistbankClientsConfiguration.builder()
            .checklistbankClientConfiguration(client)
            .nameUsageClientConfiguration(client)
            .build());

    Map<String, String> prefixes = ImmutableMap.of("http://marinespecies.org/data.php?id=", "urn:lsid:marinespecies.org:taxname:");
    Map<String, String> prefixToDataset = ImmutableMap.of("urn:lsid:marinespecies.org:taxname:", "2d59e5db-57ad-41ff-97d6-11f5fb264527");
    BackboneMatchByID idMatcher = new BackboneMatchByID(clb, prefixes, prefixToDataset);

    int processed = 0;
    int changed = 0;
    int same = 0;
    int none = 0;
    try (
        BufferedReader TSVReader = new BufferedReader(new FileReader(args[0]));
        BufferedWriter writer = Files.newBufferedWriter(new File(args[1]).toPath());
    ) {
      writer.write("scientificNameID\tkingdom\tphylum\tclass\torder\tfamily\tgenus\tscientificName\tgenericName\t" +
          "specificEpithet\tinfraspecificEpithet\tscientificNameAuthorship\trank\t" +
          "currentTaxonKey\tcurrentAcceptedTaxonKey\tcurrentScientificName\tcurrentAcceptedScientificName\toccurrenceCount\t" +
          "" +
          "newTaxonKey\tnewAcceptedTaxonKey\tnewName\tnewAcceptedName");
      writer.newLine();

      String line;
      while ((line = TSVReader.readLine()) != null) {
        processed++;
        line = line.replaceAll("\\\\N", ""); // remove Hive's NULLs
        String[] fields = line.split("\t");
        Identification id = Identification.builder()
            .withScientificNameID(fields[0])
            .withKingdom(fields[1])
            .withPhylum(fields[2])
            .withClazz(fields[3])
            .withOrder(fields[4])
            .withFamily(fields[5])
            .withGenus(fields[6])
            .withScientificName(fields[7])
            .withGenericName(fields[8])
            .withSpecificEpithet(fields[9])
            .withInfraspecificEpithet(fields[10])
            .withScientificNameAuthorship(fields[11])
            .withRank(fields[12])
            .build();

        String taxonKey = fields[13];
        String acceptedTaxonKey = fields[14];
        String originalScientificName = fields[7];
        String scientificName = fields[15];
        String acceptedScientificName = fields[16];

        NameUsageMatch m = NameUsageMatchKVStoreFactory.matchAndDecorate(clb, id, idMatcher);
        if (m.getUsage() != null) {
          if (m.getUsage().getKey() == Integer.parseInt(taxonKey)) {
            same++;
          } else {
            changed++;
            int newUsageKey = m.getUsage().getKey();
            int newAcceptedUsageKey = m.getAcceptedUsage() == null ? newUsageKey : m.getAcceptedUsage().getKey();
            String newName = m.getUsage().getName();
            String newAcceptedName = m.getAcceptedUsage() == null ? newName : m.getAcceptedUsage().getName();

            LOG.debug("ID[{}] and verbatim[{}] changed taxonKey[{} to {}], scientificName[{} to {}], acceptedTaxonKey[{} to {}], acceptedScientificName[{} to {}]",
                id.getScientificNameID(),
                originalScientificName,
                taxonKey, newUsageKey,
                scientificName, newName,
                acceptedTaxonKey, newAcceptedUsageKey,
                acceptedScientificName, newAcceptedName);

            // write changed data only
            writer.write(line);
            writer.write('\t');
            writer.write(String.valueOf(newUsageKey));
            writer.write('\t');
            writer.write(String.valueOf(newAcceptedUsageKey));
            writer.write('\t');
            writer.write(newName);
            writer.write('\t');
            writer.write(newAcceptedName);
            writer.write('\t');
            writer.newLine();
            writer.flush();
          }
        } else {
          none++;
          LOG.error("Unexpected null lookup for ID [{}], scientificName[{}]", id.getScientificName(), id.getScientificName());
        }
        LOG.debug("Processed [{}], same[{}], changed[{}], none[{}]", processed, same, changed, none);

      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    clb.close();
  }
}
