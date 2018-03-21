package com.infoclinika.mssharing.integration.test.data.file;

/**
 * @author Alexander Orlov
 */
public enum FileArchivingStatus {

    ARCHIVED ("Request the file download"),
    UNARCHIVED("File is ready to download"),
    IN_PROGRESS_OF_UNARCHIVING ("File unArchiving request is already in progress");

    private String name;

    private FileArchivingStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
