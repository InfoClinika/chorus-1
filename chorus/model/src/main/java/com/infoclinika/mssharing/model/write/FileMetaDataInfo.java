package com.infoclinika.mssharing.model.write;

import com.infoclinika.mssharing.platform.model.write.FileManagementTemplate;

/**
 * @author Herman Zamula
 */
public class FileMetaDataInfo extends FileManagementTemplate.FileMetaDataInfoTemplate {

    public FileMetaDataInfo(String fileName, long sizeInBytes, String labels, String destinationPath, long species,
                            boolean archive) {
        super(fileName, sizeInBytes, labels, destinationPath, species, archive);
    }

    public FileMetaDataInfo(String fileName, long sizeInBytes, String labels, String destinationPath, long species,
                            boolean archive, String bucket, String storedKey, boolean readOnly) {
        super(fileName, sizeInBytes, labels, destinationPath, species, archive, bucket, storedKey, readOnly);
    }
}
