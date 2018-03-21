package com.infoclinika.mssharing.model.write;

import com.infoclinika.mssharing.platform.model.write.FileManagementTemplate;

/**
 * @author Herman Zamula
 */
public class FileMetaDataInfo extends FileManagementTemplate.FileMetaDataInfoTemplate {

    public FileMetaDataInfo(String fileName, long sizeInBytes, String labels, String destinationPath, long species, boolean archive) {
        super(fileName, sizeInBytes, labels, destinationPath, species, archive);
    }
}
