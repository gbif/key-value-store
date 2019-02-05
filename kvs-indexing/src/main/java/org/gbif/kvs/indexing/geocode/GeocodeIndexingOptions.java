package org.gbif.kvs.indexing.geocode;

import org.gbif.kvs.indexing.options.HBaseIndexingOptions;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;

/**
 * Apache Beam options for indexing into HBase Geocode country lookups.
 */
public interface GeocodeIndexingOptions extends HBaseIndexingOptions {

    @Description("HBase column qualifier to stored the preferred country code")
    @Default.String("c")
    String getCountryCodeColumnQualifier();
    void setCountryCodeColumnQualifier(String countryCodeColumnQualifier);

    @Description("HBase column qualifier to stored geocode JSON response")
    @Default.String("j")
    String getJsonColumnQualifier();
    void setJsonColumnQualifier(String jsonColumnQualifier);
}
