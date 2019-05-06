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
package com.infoclinika.mssharing.model.internal.helper;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.google.common.base.Throwables;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.platform.model.helper.CorsRequestSignerTemplate;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import static com.infoclinika.mssharing.platform.fileserver.StorageService.DELIMITER;

/**
 * @author Oleksii Tymchenko
 */
@Service("requestSigner")
public class CorsRequestSignerImpl implements CorsRequestSignerTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(CorsRequestSignerImpl.class);

    private static final String PROTOCOL = "https://";
    private static final String S3_URL = "s3.amazonaws.com";
    private static final String MIME_TYPE = "application/octet-stream";
    private static final String AMAZON_UPLOAD_HEADERS = "x-amz-acl:private";
    private static final String AMAZON_SSE_HEADERS = "x-amz-server-side-encryption:AES256";
    private static final String AMAZON_SECURITY_TOKEN_HEADER = "x-amz-security-token";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final String UTF_8 = "UTF-8";

    private final FileMetaDataRepository fileMetaDataRepository;
    private final AWSCredentialsProvider credentialsProvider;
    private final AmazonPropertiesProvider amazonPropertiesProvider;

    @Inject
    public CorsRequestSignerImpl(FileMetaDataRepository fileMetaDataRepository,
                                 CloudStorageClientsProvider cloudStorageClientsProvider,
                                 AmazonPropertiesProvider amazonPropertiesProvider) {
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.credentialsProvider = cloudStorageClientsProvider.getS3FileOperationHandler().getCredentialsProvider();
        this.amazonPropertiesProvider = amazonPropertiesProvider;
    }

    @Override
    public String signSingleFileUploadRequest(long userId, String objectName) {
        final String bucket = getBucketForSingleRequest(objectName);
        long expireTime = new Date().getTime() / 1000 + (60 * 5); // 2000 minutes from now
        objectName = urlencode(objectName);

        String stringToSign = "PUT\n\n"
            + MIME_TYPE + "\n"
            + expireTime + "\n"
            + AMAZON_UPLOAD_HEADERS + "\n"
            + (useServerSideEncryption() ? AMAZON_SSE_HEADERS + "\n" : "")
            + (useSessionToken() ? AMAZON_SECURITY_TOKEN_HEADER + ":" + getAmazonToken() + "\n" : "")
            + DELIMITER + bucket + DELIMITER + objectName;

        String signature = urlencode(calculateRFC2104HMAC(stringToSign, getAmazonSecret()));

        return urlencode(PROTOCOL + bucket + "." + S3_URL + DELIMITER + objectName
                             + "?AWSAccessKeyId=" + getAmazonKey() + "&Expires=" +
                             expireTime + "&Signature=" + signature
        );
    }

    private String getAmazonKey() {
        if (credentialsProvider != null) {
            return credentialsProvider.getCredentials().getAWSAccessKeyId();
        }

        return "";
    }

    private String getAmazonSecret() {
        if (credentialsProvider != null) {
            return credentialsProvider.getCredentials().getAWSSecretKey();
        }

        return "";
    }

    @Override
    public String getAmazonToken() {
        if (credentialsProvider != null && credentialsProvider instanceof InstanceProfileCredentialsProvider) {
            return ((BasicSessionCredentials) credentialsProvider.getCredentials()).getSessionToken();
        }

        return "";
    }

    @Override
    public boolean useSessionToken() {
        return (credentialsProvider != null && credentialsProvider instanceof InstanceProfileCredentialsProvider);
    }

    @Override
    public SignedRequest signForSingleFileUploadRequest(long userId, String objectName) {
        try {
            return new SignedRequest(
                null,
                URLDecoder.decode(signSingleFileUploadRequest(userId, objectName), "UTF-8"),
                null,
                getAmazonToken()
            );
        } catch (UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
        }
    }

    //new multipart upload request signing methods
    //todo[tymchenko]: generify methods

    @Override
    public SignedRequest signInitialUploadRequest(long userId, String objectName) {
        final String bucket = processObjectForInitialUploadRequest(objectName);
        final String formattedDate = getAmzFormattedDate();
        objectName = urlencode(objectName);

        final String queryParams = "?uploads";
        String stringToSign = "POST\n\n"
            + "\n"
            + "\n"
            + AMAZON_UPLOAD_HEADERS + "\n"
            + "x-amz-date:" + formattedDate + "\n"
            + (useServerSideEncryption() ? AMAZON_SSE_HEADERS + "\n" : "")
            + (useSessionToken() ? AMAZON_SECURITY_TOKEN_HEADER + ":" + getAmazonToken() + "\n" : "")
            + DELIMITER + bucket + DELIMITER + objectName + queryParams;

        LOGGER.info(" **** StringToSign: {}", stringToSign);

        final String signature = calculateRFC2104HMAC(stringToSign, getAmazonSecret());

        final String authorization = "AWS " + getAmazonKey() + ":" + signature;
        final String host = PROTOCOL + bucket + ".s3.amazonaws.com" + DELIMITER + objectName + queryParams;

        return new SignedRequest(formattedDate, host, authorization, getAmazonToken());
    }


    @Override
    public SignedRequest signUploadPartRequest(long userId, String objectName, long partNumber, String uploadId) {
        final String objectBucket = getObjectBucket(objectName);
        final String formattedDate = getAmzFormattedDate();
        objectName = urlencode(objectName);

        final String queryParams = "?partNumber=" + partNumber + "&uploadId=" + uploadId;
        String stringToSign = "PUT\n\n"
            + MIME_TYPE + "\n"
            + "\n"
            + "x-amz-date:" + formattedDate + "\n"
            + (useSessionToken() ? AMAZON_SECURITY_TOKEN_HEADER + ":" + getAmazonToken() + "\n" : "")
            + DELIMITER + objectBucket + DELIMITER + objectName + queryParams;

        LOGGER.info(" **** StringToSign: {}", stringToSign);

        final String signature = calculateRFC2104HMAC(stringToSign, getAmazonSecret());

        final String authorization = "AWS " + getAmazonKey() + ":" + signature;
        final String host = PROTOCOL + objectBucket + ".s3.amazonaws.com" + DELIMITER + objectName + queryParams;

        return new SignedRequest(formattedDate, host, authorization, getAmazonToken());
    }

    @Override
    public SignedRequest signListPartsRequest(long userId, String objectName, String uploadId) {
        final String objectBucket = getObjectBucket(objectName);
        final String formattedDate = getAmzFormattedDate();
        objectName = urlencode(objectName);

        final String queryParams = "?uploadId=" + uploadId;
        String stringToSign = "GET\n\n"
            + "\n"
            + "\n"
            + "x-amz-date:" + formattedDate + "\n"
            + (useSessionToken() ? AMAZON_SECURITY_TOKEN_HEADER + ":" + getAmazonToken() + "\n" : "")
            + DELIMITER + objectBucket + DELIMITER + objectName + queryParams;

        LOGGER.info(" **** StringToSign: {}", stringToSign);

        final String signature = calculateRFC2104HMAC(stringToSign, getAmazonSecret());

        final String authorization = "AWS " + getAmazonKey() + ":" + signature;
        final String host = PROTOCOL + objectBucket + ".s3.amazonaws.com" + DELIMITER + objectName + queryParams;

        return new SignedRequest(formattedDate, host, authorization, getAmazonToken());
    }


    @Override
    public SignedRequest signAbortUploadRequest(long userId, String objectName, String uploadId) {
        final String objectBucket = getObjectBucket(objectName);
        final String formattedDate = getAmzFormattedDate();
        objectName = urlencode(objectName);

        final String queryParams = "?uploadId=" + uploadId;
        String stringToSign = "DELETE\n\n"
            + "\n"
            + "\n"
            + "x-amz-date:" + formattedDate + "\n"
            + (useSessionToken() ? AMAZON_SECURITY_TOKEN_HEADER + ":" + getAmazonToken() + "\n" : "")
            + DELIMITER + objectBucket + DELIMITER + objectName + queryParams;

        LOGGER.info(" **** StringToSign: {}", stringToSign);

        final String signature = calculateRFC2104HMAC(stringToSign, getAmazonSecret());

        final String authorization = "AWS " + getAmazonKey() + ":" + signature;
        final String host = PROTOCOL + objectBucket + ".s3.amazonaws.com" + DELIMITER + objectName + queryParams;

        return new SignedRequest(formattedDate, host, authorization, getAmazonToken());
    }


    @Override
    public SignedRequest signCompleteUploadRequest(long userId,
                                                   String objectName,
                                                   String uploadId,
                                                   boolean addCharsetToContentType) {
        final String bucket = processCompleteUploadRequest(objectName);
        final String formattedDate = getAmzFormattedDate();
        objectName = urlencode(objectName);

        final String queryParams = "?uploadId=" + uploadId;
        final String textXmlContentType = "text/xml";
        final String contentType = addCharsetToContentType
            ? (textXmlContentType + "; charset=UTF-8")
            : textXmlContentType;

        String stringToSign = "POST\n\n"
            + contentType + "\n"
            + "\n"
            + "x-amz-date:" + formattedDate + "\n"
            + (useSessionToken() ? AMAZON_SECURITY_TOKEN_HEADER + ":" + getAmazonToken() + "\n" : "")
            + DELIMITER + bucket + DELIMITER + objectName + queryParams;

        LOGGER.info(" **** StringToSign: {}", stringToSign);

        final String signature = calculateRFC2104HMAC(stringToSign, getAmazonSecret());

        final String authorization = "AWS " + getAmazonKey() + ":" + signature;
        final String host = PROTOCOL + bucket + ".s3.amazonaws.com" + DELIMITER + objectName + queryParams;

        return new SignedRequest(formattedDate, host, authorization, getAmazonToken());
    }

    @Override
    public boolean useServerSideEncryption() {
        return amazonPropertiesProvider.isServersideEncryptionEnabled();
    }

    // --- Helper methods ----

    private static String getAmzFormattedDate() {
        final Date date = new Date();

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        return simpleDateFormat.format(date);
    }

    private static String urlencode(String s) {
        try {
            return URLEncoder.encode(s, UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String calculateRFC2104HMAC(String data, String key) {
        String result;
        try {

            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);

            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());

            // base64-encode the hmac
            result = new String(Base64.encodeBase64(rawHmac), UTF_8);

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return result;
    }

    private String processObjectForInitialUploadRequest(String objectName) {
        return Optional.ofNullable(fileMetaDataRepository.findByDestinationPath(objectName)).map(metaFile -> {
            final String bucket = bucketForFileMetaData(metaFile);
            metaFile.setFileUploadBucket(bucket);
            fileMetaDataRepository.save(metaFile);
            return bucket;
        }).orElse(amazonPropertiesProvider.getActiveBucket());
    }

    private String getBucketForSingleRequest(String object) {

        return Optional.ofNullable(fileMetaDataRepository.findByDestinationPath(object))
            .map(this::bucketForFileMetaData)
            .orElse(amazonPropertiesProvider.getActiveBucket());
    }

    private String processCompleteUploadRequest(String objectName) {

        return Optional.ofNullable(fileMetaDataRepository.findByDestinationPath(objectName)).map(input -> {
            final String bucket = input.getFileUploadBucket();
            input.setFileUploadBucket(null);
            fileMetaDataRepository.save(input);
            return bucket;
        }).orElse(amazonPropertiesProvider.getActiveBucket());

    }

    private String bucketForFileMetaData(ActiveFileMetaData metaFile) {
        return amazonPropertiesProvider.getActiveBucket();
    }

    private String getObjectBucket(String objectName) {
        return Optional.ofNullable(fileMetaDataRepository.findByDestinationPath(objectName))
            .map(ActiveFileMetaData::getFileUploadBucket)
            .orElse(amazonPropertiesProvider.getActiveBucket());
    }

}
