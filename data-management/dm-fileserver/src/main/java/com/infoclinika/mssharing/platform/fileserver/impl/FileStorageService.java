/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.fileserver.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.google.common.base.Optional;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.fileserver.model.StoredFile;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

/**
 * The implementation of the storage service, specific for the hosting-provided storage mechanism.
 * <p/>
 * Used to store the file-packed data.
 *
 * @author Oleksii Tymchenko
 */
public class FileStorageService implements StorageService<StoredFile> {

    public static final int MAX_UPLOAD_ATTEMPTS = 5;
    public static final int MAX_HTTP_CONNECTIONS = 300;
    private static final String ATTEMPT = "Attempt #";
    private static final String TO_PATH = "to path";
    private static final String BUCKET_NAME = "Bucket name";
    private static final String CANNOT_PUT_THE_OBJECT = "Cannot put the object";
    private static final String AT_NODE_PATH = "at node path";
    private static final String EQUAL = " = ";
    private static final String CANNOT_OBTAIN_THE_OBJECT_STREAM_FROM_PATH = "Cannot obtain the object stream from path";
    private static final String DOT = ". ";
    private static final String WHITE_SPACE = " ";
    //Hold the client as a static field to avoid its GC`ing. According to https://forums.aws.amazon.com/thread.jspa?threadID=83326
    protected static AmazonS3Client client;
    protected final Logger logger = Logger.getLogger(this.getClass());
    @Value("${amazon.key}")
    private String username;
    @Value("${amazon.secret}")
    private String password;
    @Value("${amazon.active.bucket}")
    private String rawFilesBucket;

    public FileStorageService() {
    }

    @PostConstruct
    public void initializeAmazonClient() {
        final BasicAWSCredentials credentials = new BasicAWSCredentials(username, password);
        final ClientConfiguration modifiedConf = new ClientConfiguration();
        modifiedConf.setMaxConnections(MAX_HTTP_CONNECTIONS);
        client = new AmazonS3Client(credentials, modifiedConf);
    }

    @Override
    public synchronized void put(NodePath path, StoredFile object) {
        int retryCount = 0;
        boolean uploaded = false;
        while (retryCount < MAX_UPLOAD_ATTEMPTS && !uploaded) {
            logger.debug(ATTEMPT + WHITE_SPACE + (retryCount + 1) + ". Putting the object " + object +
                    WHITE_SPACE + TO_PATH + WHITE_SPACE + path + DOT + BUCKET_NAME + EQUAL + getRawFilesBucket());
            try {

                final PutObjectRequest putObjectRequest = newPutObjectRequest(path, object);
                client.putObject(putObjectRequest);

                logger.debug(ATTEMPT + WHITE_SPACE + (retryCount + 1) + " is SUCCESSFUL. " +
                        "The object " + object + " has been uploaded " + TO_PATH + WHITE_SPACE + path + DOT +
                        BUCKET_NAME + EQUAL + getRawFilesBucket());
                uploaded = true;
            } catch (Exception e) {
                final String message = ATTEMPT + (retryCount + 1) + DOT + CANNOT_PUT_THE_OBJECT +
                        WHITE_SPACE + object + WHITE_SPACE + TO_PATH + WHITE_SPACE + path + DOT +
                        BUCKET_NAME + EQUAL + getRawFilesBucket();
                logger.warn(message, e);
                retryCount++;
                if (retryCount < MAX_UPLOAD_ATTEMPTS) {
                    logger.warn("Retrying now.");
                }
            }
        }
        if (!uploaded) {
            final String message = "Upload FAILED. Ran out of " + MAX_UPLOAD_ATTEMPTS + " attempts — " +
                    CANNOT_PUT_THE_OBJECT + WHITE_SPACE + object + WHITE_SPACE + TO_PATH + WHITE_SPACE + path + DOT +
                    BUCKET_NAME + EQUAL + getRawFilesBucket();
            logger.error(message);
            throw new RuntimeException(message);
        }
    }

    protected PutObjectRequest newPutObjectRequest(NodePath path, StoredFile object) {
        //as per the AmazonS3Client code from AWS SDK
        final ObjectMetadata metadata = new ObjectMetadata();
        if (object.getSize() != null) {
            metadata.setContentLength(object.getSize());
        }

        return new PutObjectRequest(getRawFilesBucket(), path.getPath(), object.getInputStream(), metadata);
    }

    @Override
    public StoredFile get(NodePath path) {
        logger.debug("Obtaining the object from bucket = " + getRawFilesBucket() + WHITE_SPACE +
                AT_NODE_PATH + EQUAL + path);

        try {
            return getStoredFile(path).orNull();
        } catch (AmazonClientException e) {
            final String message = "Cannot obtain the object from path " + path + DOT +
                    BUCKET_NAME + EQUAL + getRawFilesBucket();
            logger.warn(message, e);
        }
        return null;
    }

    @Override
    public void delete(NodePath path) {
        logger.debug("Deleting the object from bucket = " + getRawFilesBucket() + WHITE_SPACE +
                AT_NODE_PATH + EQUAL + path);
        try {
            client.deleteObject(getRawFilesBucket(), path.getPath());
        } catch (AmazonClientException e) {
            final String message = "Cannot delete the object by path " + path + DOT +
                    BUCKET_NAME + EQUAL + getRawFilesBucket();
            logger.warn(message, e);
        }

    }

    /**
     * Will be removed
     */
    @Deprecated
    public S3ObjectInputStream getAsStream(NodePath path) {
        logger.debug("Obtaining the object stream from bucket = " + getRawFilesBucket() + WHITE_SPACE +
                AT_NODE_PATH + EQUAL + path);

        try {
            final S3Object object = client.getObject(getRawFilesBucket(), path.getPath());
            return object.getObjectContent();
        } catch (Exception e) {
            final String message = CANNOT_OBTAIN_THE_OBJECT_STREAM_FROM_PATH + WHITE_SPACE + path + DOT +
                    BUCKET_NAME + EQUAL + getRawFilesBucket();
            logger.warn(message, e);
        }
        return null;
    }

    private Optional<StoredFile> getStoredFile(NodePath path) {
        logger.debug("Obtaining the object stream from bucket = " + getRawFilesBucket() + WHITE_SPACE +
                AT_NODE_PATH + EQUAL + path);

        try {
            final GetObjectRequest getObjectRequest = newGetObjectRequest(path);
            final S3Object object = client.getObject(getObjectRequest);
            final StoredFile storedFile = toStoredFile(object);
            return Optional.of(storedFile);
        } catch (Exception e) {
            final String message = CANNOT_OBTAIN_THE_OBJECT_STREAM_FROM_PATH + WHITE_SPACE + path + DOT +
                    BUCKET_NAME + EQUAL + getRawFilesBucket();
            logger.warn(message, e);
        }
        return Optional.absent();
    }

    protected GetObjectRequest newGetObjectRequest(NodePath path) {
        return new GetObjectRequest(getRawFilesBucket(), path.getPath());
    }

    private StoredFile toStoredFile(S3Object object) {
        final S3ObjectInputStream objectContent = object.getObjectContent();
        final StoredFile storedFile = new StoredFile(objectContent);
        storedFile.setSize(object.getObjectMetadata().getContentLength());
        return storedFile;
    }

    public String getRawFilesBucket() {
        return rawFilesBucket;
    }

    public void setRawFilesBucket(String rawFilesBucket) {
        this.rawFilesBucket = rawFilesBucket;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
