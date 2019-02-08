package org.gbif.kvs.indexing.geocode;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.kvs.geocode.LatLng;

import java.util.Optional;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/** Utility to convert HBase occurrence records into LatLng objects. */
class OccurrenceHBaseBuilder {

  // Occurrence column family
  private static final byte[] CF = Bytes.toBytes("o");

  /** Private constructor of utility class. */
  private OccurrenceHBaseBuilder() {
    // DO NOTHING
  }

  /**
   * Translates an HBase record/result into a LatLng object.
   *
   * @param result HBase row/col
   * @return a LatLng object with the coordinates info
   */
  static LatLng toLatLng(Result result) {
    LatLng.Builder builder = LatLng.builder();
    getDouble(result, DwcTerm.decimalLatitude).ifPresent(builder::withLatitude);
    getDouble(result, DwcTerm.decimalLongitude).ifPresent(builder::withLongitude);
    return builder.build();
  }

  /**
   * Tries to get the column value mapped to the {@link DwcTerm} form the HBase record.
   *
   * @param result HBase record
   * @param term DwcTerm to perform the field lookup
   * @return a optional double value if the column is present, Optional.absent() otherwise
   */
  private static Optional<Double> getDouble(Result result, DwcTerm term) {
    return Optional.ofNullable(result.getValue(CF, Bytes.toBytes(term.simpleName())))
        .map(Bytes::toDouble);
  }
}
