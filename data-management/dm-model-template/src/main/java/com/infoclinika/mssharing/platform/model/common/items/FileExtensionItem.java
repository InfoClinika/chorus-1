package com.infoclinika.mssharing.platform.model.common.items;

import java.util.Map;
import java.util.Objects;

/**
 * @author Herman Zamula
 */
public class FileExtensionItem {
    public final String name;
    public final String zip;
    public final Map<String, AdditionalExtensionImportance> additionalExtensions;

    public FileExtensionItem(String name, String zip, Map<String, AdditionalExtensionImportance> additionalExtensions) {
        this.name = name;
        this.zip = zip;
        this.additionalExtensions = additionalExtensions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileExtensionItem)) {
            return false;
        }
        FileExtensionItem that = (FileExtensionItem) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(zip, that.zip) &&
            Objects.equals(additionalExtensions, that.additionalExtensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, zip, additionalExtensions);
    }
}
