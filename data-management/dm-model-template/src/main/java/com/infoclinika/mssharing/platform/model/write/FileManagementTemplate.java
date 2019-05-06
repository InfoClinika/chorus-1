package com.infoclinika.mssharing.platform.model.write;

/**
 * @author Herman Zamula
 */
public interface FileManagementTemplate<FILE_META_DATA_INFO extends FileManagementTemplate.FileMetaDataInfoTemplate> {

    long createFile(long actor, long instrument, FILE_META_DATA_INFO fileMetaDataInfo);

    void updateFile(long actor, long file, FILE_META_DATA_INFO fileMetaDataInfo);

    void deleteFile(long actor, long file, boolean permanently);

    class FileMetaDataInfoTemplate {

        public final String fileName;
        public final long sizeInBytes;
        public final String labels;
        public final String destinationPath;
        public final long species;
        public final boolean archive;
        public final String bucket;
        public final String storedKey;
        public final boolean readOnly;

        public FileMetaDataInfoTemplate(String fileName,
                                        long sizeInBytes,
                                        String labels,
                                        String destinationPath,
                                        long species,
                                        boolean archive) {
            this(
                fileName,
                sizeInBytes,
                labels,
                destinationPath,
                species,
                archive,
                null,
                null,
                false
            );
        }

        public FileMetaDataInfoTemplate(String fileName,
                                        long sizeInBytes,
                                        String labels,
                                        String destinationPath,
                                        long species,
                                        boolean archive,
                                        String bucket,
                                        String storedKey,
                                        boolean readOnly) {
            this.fileName = fileName;
            this.sizeInBytes = sizeInBytes;
            this.labels = labels;
            this.destinationPath = destinationPath;
            this.species = species;
            this.archive = archive;
            this.bucket = bucket;
            this.storedKey = storedKey;
            this.readOnly = readOnly;
        }
    }

}
