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
package org.gbif.rest.client.species;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
