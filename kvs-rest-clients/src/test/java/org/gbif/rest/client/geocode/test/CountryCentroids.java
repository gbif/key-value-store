package org.gbif.rest.client.geocode.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.StringJoiner;

/**
 * Tests utility class that loads test data for Geocode reverse lookups.
 * Uses a file taken from: https://developers.google.com/public-data/docs/canonical/countries_csv.
 * This class should be used for test cases only!
 */
public class CountryCentroids {

  //Test file
  private static final String COUNTRIES_FILE = "country_centroids.csv";

  //Column separator of the test  file
  private static final String SEPARATOR = "\t";

  //Maker to ignore lines in the test data file
  private static final String IGNORE_MARKER = "#";

  //List of loaded countries
  private final List<Country> countries;

  /**
   * Creates a new instance using the default test file.
   */
  public CountryCentroids() {
    countries = loadCountriesData(CountryCentroids.class.getClassLoader().getResource(COUNTRIES_FILE).getFile());
  }


  /**
   *
   * @return the list of loaded countries
   */
  public List<Country> getCountries() {
    return countries;
  }

  /**
   * Finds a country byt its ISO country code.
   * @param countryCode ISO country code
   * @return found country, Optional.empty() otherwise
   */
  public Optional<Country> findByCountryCode(String countryCode) {
    return countries.stream().filter(country -> country.getIsoCode().equals(countryCode)).findFirst();
  }

  /**
   * Finds a country byt its coordinate centroid.
   * @param latitude decimal latitude
   * @param longitude decimal longitude
   * @return found country, Optional.empty() otherwise
   */

  public Optional<Country> findByCoordinate(Double latitude, Double longitude) {
    return countries.stream().filter(country -> country.getLatitude().equals(latitude) &&
                                                country.getLongitude().equals(longitude)).findFirst();
  }

  /**
   * Loads the list of countries form a test data file.
   * @param dataFile test file
   * @return the list of countries
   */
  private static List<Country> loadCountriesData(String dataFile) {
    try {
      List<Country> records = new ArrayList<>();
      try (Scanner scanner = new Scanner(new File(dataFile), StandardCharsets.UTF_8.name())) {
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          if(!line.startsWith(IGNORE_MARKER)) {
            records.add(fromLine(line, SEPARATOR));
          }
        }
      }
      return records;
    } catch (FileNotFoundException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  /**
   * Converts a String, split by a separator, into a Country instance.
   * @param line to read
   * @param separator column separator
   * @return a Country parsed from the line
   */
  private static Country fromLine(String line, String separator) {
    String[] lineData = line.split(separator);
    return new Country(lineData[0], Double.parseDouble(lineData[1]), Double.parseDouble(lineData[2]), lineData[3]);
  }

  /**
   * Class to abstract the content of the test data file.
   */
  public static class Country {

    private final String isoCode;
    private final Double latitude;
    private final Double longitude;
    private final String name;

    public Country(String isoCode, Double latitude, Double longitude, String name) {
      this.isoCode = isoCode;
      this.latitude = latitude;
      this.longitude = longitude;
      this.name = name;
    }


    public String getIsoCode() {
      return isoCode;
    }

    public Double getLatitude() {
      return latitude;
    }

    public Double getLongitude() {
      return longitude;
    }

    public String getName() {
      return name;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Country country = (Country) o;
      return Objects.equals(isoCode, country.isoCode) &&
          Objects.equals(latitude, country.latitude) &&
          Objects.equals(longitude, country.longitude) &&
          Objects.equals(name, country.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(isoCode, latitude, longitude, name);
    }

    @Override
    public String toString() {
      return new StringJoiner(", ", Country.class.getSimpleName() + "[", "]")
          .add("isoCode='" + isoCode + "'")
          .add("latitude=" + latitude)
          .add("longitude=" + longitude)
          .add("name='" + name + "'")
          .toString();
    }
  }

}
