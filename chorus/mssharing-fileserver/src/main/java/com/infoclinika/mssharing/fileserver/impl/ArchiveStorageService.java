package com.infoclinika.mssharing.fileserver.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.infoclinika.mssharing.fileserver.model.ArchivedFile;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.impl.FileStorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static com.infoclinika.mssharing.platform.fileserver.StorageServiceHelper.createAmazonS3Client;

/**
 * @author Herman Zamula
 */
public class ArchiveStorageService implements StorageService<ArchivedFile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageService.class);
    public static final int MAX_HTTP_CONNECTIONS = 300;

    //Hold the client as a static field to avoid its GC`ing.
    // According to https://forums.aws.amazon.com/thread.jspa?threadID=83326
    private static AmazonS3 client;

    @Inject
    private AmazonPropertiesProvider amazonPropertiesProvider;

    @PostConstruct
    public void initializeAmazonClient() {
        client = createAmazonS3Client(
            amazonPropertiesProvider.getAccessKey(),
            amazonPropertiesProvider.getSecretKey(),
            amazonPropertiesProvider.isUseRoles()
        );
    }

    @Override
    public void put(NodePath path, ArchivedFile object) {
        throw new UnsupportedOperationException("Put in archive storage is not supported.");
    }

    @Override
    public ArchivedFile get(NodePath path) {
        final String archiveBucket = amazonPropertiesProvider.getArchiveBucket();
        LOGGER.debug("Obtaining the object from bucket = {}  at node path = {}", archiveBucket, path);

        try {
            final S3ObjectInputStream objectContent = getAsStream(path);
            return new ArchivedFile(objectContent);
        } catch (AmazonClientException e) {
            LOGGER.warn("Cannot obtain the object from path {}. Bucket name = {}", path, archiveBucket, e);
        }
        return null;
    }

    @Override
    public void delete(NodePath path) {
        throw new UnsupportedOperationException("Delete from archive storage operation is not supported.");
    }

    public S3ObjectInputStream getAsStream(NodePath path) {
        final String archiveBucket = amazonPropertiesProvider.getArchiveBucket();
        LOGGER.debug("Obtaining the object stream from bucket = {}  at node path = {}", archiveBucket, path);

        try {
            final S3Object object = client.getObject(archiveBucket, path.getPath());
            return object.getObjectContent();
        } catch (Exception e) {
            LOGGER.warn("Cannot obtain the object stream from path {}. Bucket name = {}", path, archiveBucket, e);
        }
        return null;
    }
}
