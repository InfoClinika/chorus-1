package com.infoclinika.mssharing.fileserver.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.infoclinika.mssharing.fileserver.model.ArchivedFile;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.impl.FileStorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

/**
 * @author Herman Zamula
 */
public class ArchiveStorageService implements StorageService<ArchivedFile> {

    public static final int MAX_HTTP_CONNECTIONS = 300;

    @Value("${amazon.key}")
    private String username;
    @Value("${amazon.secret}")
    private String password;
    @Value("${amazon.archive.bucket}")
    private String archiveBucket;

    private final static Logger LOGGER = Logger.getLogger(FileStorageService.class);

    //Hold the client as a static field to avoid its GC`ing. According to https://forums.aws.amazon.com/thread.jspa?threadID=83326
    private static AmazonS3Client client;

    @PostConstruct
    public void initializeAmazonClient() {
        final BasicAWSCredentials credentials = new BasicAWSCredentials(username, password);
        final ClientConfiguration modifiedConf = new ClientConfiguration();
        modifiedConf.setMaxConnections(MAX_HTTP_CONNECTIONS);
        client = new AmazonS3Client(credentials, modifiedConf);
    }

    @Override
    public void put(NodePath path, ArchivedFile object) {
        throw new UnsupportedOperationException("Put in archive storage is not supported.");
    }

    @Override
    public ArchivedFile get(NodePath path) {

        LOGGER.debug("Obtaining the object from bucket = " + archiveBucket + " at node path = " + path);

        try {
            final S3ObjectInputStream objectContent = getAsStream(path);
            return new ArchivedFile(objectContent);
        } catch (AmazonClientException e) {
            final String message = "Cannot obtain the object from path " + path + ". Bucket name = " + archiveBucket;
            LOGGER.warn(message, e);
        }
        return null;
    }

    @Override
    public void delete(NodePath path) {
        throw new UnsupportedOperationException("Delete from archive storage operation is not supported.");
    }

    public S3ObjectInputStream getAsStream(NodePath path) {
        LOGGER.debug("Obtaining the object stream from bucket = " + archiveBucket + " at node path = " + path);

        try {
            final S3Object object = client.getObject(archiveBucket, path.getPath());
            return object.getObjectContent();
        } catch (Exception e) {
            final String message = "Cannot obtain the object stream from path " + path + ". Bucket name = " + archiveBucket;
            LOGGER.warn(message, e);
        }
        return null;
    }
}
