package com.infoclinika.mssharing.platform.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

@Table(name = "vendor_extension")
@Entity
public class VendorExtension extends AbstractPersistable<Long> {

    @Basic
    private String extension;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "extension_additional")
    @MapKeyColumn(name = "extension")
    @Column(name = "importance")
    private Map<String, Importance> additionalFilesExtensions = newHashMap();

    @Basic
    private String zipExtension;

    public VendorExtension(String extension, String zipExtension, Map<String, Importance> additionalFilesExtensions) {
        this.extension = extension;
        this.zipExtension = zipExtension;
        this.additionalFilesExtensions = additionalFilesExtensions;
    }

    public VendorExtension() {
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getZipExtension() {
        return zipExtension;
    }

    public void setZipExtension(String zipExtension) {
        this.zipExtension = zipExtension;
    }

    public Map<String, Importance> getAdditionalFilesExtensions() {
        return additionalFilesExtensions;
    }

    public enum Importance {
        REQUIRED,
        NOT_REQUIRED
    }
}
