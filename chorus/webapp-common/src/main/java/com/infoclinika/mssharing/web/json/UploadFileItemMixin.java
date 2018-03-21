package com.infoclinika.mssharing.web.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Alexei Tymchenko
 */
public class UploadFileItemMixin {
    @JsonCreator
    public UploadFileItemMixin(
            @JsonProperty("name") String name,
            @JsonProperty("labels") String labels,
            @JsonProperty("size") long size,
            @JsonProperty("specie") long specie,
            @JsonProperty("archive") boolean archive) {
    }
}
