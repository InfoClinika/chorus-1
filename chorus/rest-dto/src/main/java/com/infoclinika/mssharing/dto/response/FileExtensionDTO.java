// Copyright (c) 2016, NanoString Technologies, Inc.  All rights reserved.
// Use of this file for any purpose requires prior written consent of NanoString Technologies, Inc.

package com.infoclinika.mssharing.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Yevhen Panko (yevhen.panko@teamdev.com)
 */
public class FileExtensionDTO implements Serializable {
    public final String name;
    public final String zip;
    public final Map<String, AdditionalExtensionImportance> additionalExtensions;

    @JsonCreator
    public FileExtensionDTO(
        @JsonProperty("name") String name,
        @JsonProperty("zip") String zip,
        @JsonProperty("additionalExtensions") Map<String, AdditionalExtensionImportance> additionalExtensions) {
        this.name = name;
        this.zip = zip;
        this.additionalExtensions = additionalExtensions;
    }

    @Override
    public String toString() {
        return "FileExtensionDTO{" +
            "name='" + name + '\'' +
            ", zip='" + zip + '\'' +
            ", additionalExtensions=" + additionalExtensions +
            '}';
    }

    public static enum AdditionalExtensionImportance {
        REQUIRED,
        NOT_REQUIRED
    }
}
