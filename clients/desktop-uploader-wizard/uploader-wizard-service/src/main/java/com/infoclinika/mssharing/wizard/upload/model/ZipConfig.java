package com.infoclinika.mssharing.wizard.upload.model;

/**
 * @author timofey.kasyanov
 *         date:   29.01.14
 */
public class ZipConfig {

    private final String zipFolderPath;

    public ZipConfig(String zipFolderPath) {
        this.zipFolderPath = zipFolderPath;
    }

    public String getZipFolderPath() {
        return zipFolderPath;
    }
}
