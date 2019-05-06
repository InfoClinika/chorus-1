package com.infoclinika.mssharing.web.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Pavel Kaplin
 *     <p>
 *     Motivated by http://wiki.fasterxml.com/JacksonMixInAnnotations
 *     </p>
 */
abstract class ProjectInfoMixin {
    @JsonCreator
    public ProjectInfoMixin(@JsonProperty("name") String name,
                            @JsonProperty("areaOfResearch") String areaOfResearch,
                            @JsonProperty("description") String description) {
    }
}
