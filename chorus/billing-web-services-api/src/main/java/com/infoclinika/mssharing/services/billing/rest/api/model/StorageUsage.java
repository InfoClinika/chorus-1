package com.infoclinika.mssharing.services.billing.rest.api.model;

/**
 * @author timofei.kasianov 5/23/16
 */
public class StorageUsage {
    public long rawFilesSize;
    public long archivedFilesSize;
    public long translatedFilesSize;
    public long searchResultsFilesSize;

    public StorageUsage(long rawFilesSize, long archivedFilesSize, long translatedFilesSize, long searchResultsFilesSize) {
        this.rawFilesSize = rawFilesSize;
        this.archivedFilesSize = archivedFilesSize;
        this.translatedFilesSize = translatedFilesSize;
        this.searchResultsFilesSize = searchResultsFilesSize;
    }

    public StorageUsage() {
    }
}
