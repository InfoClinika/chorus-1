package com.infoclinika.mssharing.services.billing.rest.api.model;


/**
 * @author Herman Zamula
 */
public enum BillingFeature {

    ARCHIVE_STORAGE("Daily archive storage"),
    ANALYSE_STORAGE("Daily active storage"),
    DOWNLOAD("Download"),
    PUBLIC_DOWNLOAD("Public data download"),
    STORAGE_VOLUMES("Storage"),
    ARCHIVE_STORAGE_VOLUMES("Archive storage")
    //PUBLIC_ARCHIVED_DOWNLOAD_TEMP_STORAGE("Temporary storage for download of public archived data")
    ;

    private final String value;

    BillingFeature(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
