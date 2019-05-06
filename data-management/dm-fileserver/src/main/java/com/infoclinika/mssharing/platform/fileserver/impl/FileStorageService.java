/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.fileserver.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.fileserver.model.StoredFile;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Optional;

import static com.infoclinika.mssharing.platform.fileserver.StorageServiceHelper.createAmazonS3Client;

/**
 * The implementation of the storage service, specific for the hosting-provided storage mechanism.
 * <p/>
 * Used to store the file-packed data.
 *
 * @author Oleksii Tymchenko
 */
public class FileStorageService implements StorageService<StoredFile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageService.class);
    private static final int MAX_UPLOAD_ATTEMPTS = 5;

    // Hold the client as a static field to avoid its GC`ing.
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
    public synchronized void put(NodePath path, StoredFile object) {
        int retryCount = 0;
        boolean uploaded = false;
        while (retryCount < MAX_UPLOAD_ATTEMPTS && !uploaded) {
            LOGGER.debug(
                "Attempt # {}. Putting the object {} to path {}. Bucket name = {}",
                (retryCount + 1),
                object,
                path,
                getRawFilesBucket(path)
            );

            try {
                final PutObjectRequest putObjectRequest = newPutObjectRequest(path, object);
                client.putObject(putObjectRequest);

                LOGGER.debug(
                    "Attempt # {} is SUCCESSFUL. The object {}  has been uploaded to path {}. Bucket name = {}",
                    (retryCount + 1),
                    object,
                    path,
                    getRawFilesBucket(path)
                );
                uploaded = true;
            } catch (Exception e) {
                LOGGER.warn(
                    "Attempt # {}. Cannot put the object {} to path {}. Bucket name = {}",
                    (retryCount + 1),
                    object,
                    path,
                    getRawFilesBucket(path),
                    e
                );
                retryCount++;
                if (retryCount < MAX_UPLOAD_ATTEMPTS) {
                    LOGGER.warn("Retrying now.");
                }
            }
        }
        if (!uploaded) {
            final String message = "Upload FAILED. Ran out of " + MAX_UPLOAD_ATTEMPTS + " attempts — Cannot put the " +
                "object " + object + " to path " + path + ". Bucket name = " + getRawFilesBucket(
                path);
            LOGGER.error(message);
            throw new RuntimeException(message);
        }
    }

    private PutObjectRequest newPutObjectRequest(NodePath path, StoredFile object) {
        //as per the AmazonS3Client code from AWS SDK
        final ObjectMetadata metadata = new ObjectMetadata();
        if (object.getSize() != null) {
            metadata.setContentLength(object.getSize());
        }

        return new PutObjectRequest(getRawFilesBucket(path), path.getPath(), object.getInputStream(), metadata);
    }

    @Override
    public StoredFile get(NodePath path) {
        LOGGER.debug("Obtaining the object from bucket = {} at node path = {}", getRawFilesBucket(path), path);

        try {
            return getStoredFile(path).orElse(null);
        } catch (AmazonClientException e) {
            LOGGER.warn("Cannot obtain the object from path {}. Bucket name = {}", path, getRawFilesBucket(path), e);
        }
        return null;
    }

    @Override
    public void delete(NodePath path) {
        LOGGER.debug("Deleting the object from bucket = {} at node path = {}", getRawFilesBucket(path), path);
        try {
            client.deleteObject(getRawFilesBucket(path), path.getPath());
        } catch (AmazonClientException e) {
            LOGGER.warn("Cannot delete the object by path {}. Bucket name = {}", path, getRawFilesBucket(path), e);
        }
    }

    /**
     * Will be removed
     */
    @Deprecated
    public S3ObjectInputStream getAsStream(NodePath path) {
        LOGGER.debug("Obtaining the object stream from bucket = {} at node path = {}", getRawFilesBucket(path), path);

        try {
            final S3Object object = client.getObject(getRawFilesBucket(path), path.getPath());

            return object.getObjectContent();
        } catch (Exception e) {
            LOGGER.warn(
                "Cannot obtain the object stream from path {}. Bucket name = {}",
                path,
                getRawFilesBucket(path),
                e
            );
        }

        return null;
    }

    private Optional<StoredFile> getStoredFile(NodePath path) {
        LOGGER.debug("Obtaining the object stream from bucket = {} at node path = {}", getRawFilesBucket(path), path);

        try {
            final GetObjectRequest getObjectRequest = newGetObjectRequest(path);
            final S3Object object = client.getObject(getObjectRequest);
            final StoredFile storedFile = toStoredFile(object);

            return Optional.of(storedFile);
        } catch (Exception e) {
            LOGGER.warn(
                "Cannot obtain the object stream from path {}. Bucket name = {}",
                path,
                getRawFilesBucket(path),
                e
            );
        }

        return Optional.empty();
    }

    private GetObjectRequest newGetObjectRequest(NodePath path) {
        return new GetObjectRequest(getRawFilesBucket(path), path.getPath());
    }

    private StoredFile toStoredFile(S3Object object) {
        final S3ObjectInputStream objectContent = object.getObjectContent();
        final StoredFile storedFile = new StoredFile(objectContent);
        storedFile.setSize(object.getObjectMetadata().getContentLength());

        return storedFile;
    }

    private String getRawFilesBucket(NodePath path) {
        return StringUtils.isBlank(path.getBucket())
            ? amazonPropertiesProvider.getActiveBucket()
            : path.getBucket();
    }
}
