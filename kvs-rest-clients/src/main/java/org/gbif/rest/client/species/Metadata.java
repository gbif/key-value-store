package org.gbif.rest.client.species;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class Metadata implements Serializable {

    String created;
    BuildInfo buildInfo ;
    IndexMetadata mainIndex;
    List<IndexMetadata> identifierIndexes = new ArrayList<>();
    List<IndexMetadata> ancillaryIndexes = new ArrayList<>();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IndexMetadata {
        String datasetKey;
        String gbifKey;
        String datasetTitle;
        Long sizeInMB = 0L;
        Long nameUsageCount = 0L;
        Long matchesToMain;
        Map<String, Long> nameUsageByRankCount = new HashMap<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BuildInfo {
        String sha;
        String url;
        String html_url;
        String message;
        String name;
        String email;
        String date;
    }
}


