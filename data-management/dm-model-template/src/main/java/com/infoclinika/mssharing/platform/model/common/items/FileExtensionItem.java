package com.infoclinika.mssharing.platform.model.common.items;

import java.util.Map;

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
    @SuppressWarnings("all")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileExtensionItem that = (FileExtensionItem) o;

        if (additionalExtensions != null ? !additionalExtensions.equals(that.additionalExtensions) : that.additionalExtensions != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (zip != null ? !zip.equals(that.zip) : that.zip != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (zip != null ? zip.hashCode() : 0);
        result = 31 * result + (additionalExtensions != null ? additionalExtensions.hashCode() : 0);
        return result;
    }
}
