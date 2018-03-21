package com.infoclinika.mssharing.integration.test.data.file;

/**
 * @author Alexander Orlov
 */
public enum FileTranslationStatus {

    NOT_TRANSLATED("Not translated"),
    TRANSLATION_IN_PROGRESS("Translation in progress");

    private String title;

    private FileTranslationStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
