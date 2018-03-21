/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.helper;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.platform.model.helper.CorsRequestSignerTemplate;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
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

import static com.google.common.base.Optional.fromNullable;
import static com.infoclinika.mssharing.platform.fileserver.StorageService.DELIMITER;

/**
 * @author Oleksii Tymchenko
 */
@Service("requestSigner")
public class CorsRequestSignerImpl implements CorsRequestSignerTemplate {
    private static final Logger LOG = Logger.getLogger(CorsRequestSignerImpl.class);

    public static final String PROTOCOL = "https://";
    public static final String S3_URL = "s3.amazonaws.com";
    public static final String MIME_TYPE = "application/octet-stream";
    public static final String AMAZON_UPLOAD_HEADERS = "x-amz-acl:private";
    public static final String AMAZON_SSE_HEADERS = "x-amz-server-side-encryption:AES256";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    public static final String UTF_8 = "UTF-8";

    @Value("${amazon.key}")
    private String amazonKey;

    @Value("${amazon.secret}")
    private String amazonSecret;

    @Value("${amazon.active.bucket}")
    private String defaultBucket;

    @Value("${amazon.serverside.encryption}")
    private String serverSideEncryption;

    @Value("${amazon.archive.bucket}")
    private String archiveBucket;

    @Inject
    private FileMetaDataRepository fileMetaDataRepository;


    @Override
    public String signSingleFileUploadRequest(long userId, String objectName) {

        final String bucket = getBucketForSingleRequest(objectName);
        long expireTime = new Date().getTime() / 1000 + (60 * 5); // 2000 minutes from now
        objectName = urlencode(objectName);

        String stringToSign = "PUT\n\n"
                + MIME_TYPE + "\n"
                + expireTime + "\n"
                + AMAZON_UPLOAD_HEADERS + "\n"
                + (useServerSideEncryption() ? AMAZON_SSE_HEADERS + "\n": "")
                + DELIMITER + bucket + DELIMITER + objectName;

        String signature = urlencode(calculateRFC2104HMAC(stringToSign, amazonSecret));

        return urlencode(PROTOCOL + bucket + "." + S3_URL + DELIMITER + objectName
                + "?AWSAccessKeyId=" + amazonKey + "&Expires=" + expireTime + "&Signature=" + signature);
    }

    @Override
    public SignedRequest signForSingleFileUploadRequest(long userId, String objectName) {
        try {
            return new SignedRequest(null, URLDecoder.decode(signSingleFileUploadRequest(userId, objectName), "UTF-8"), null);
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
                + (useServerSideEncryption() ? AMAZON_SSE_HEADERS + "\n": "")
                + DELIMITER + bucket + DELIMITER + objectName + queryParams;

        LOG.info(" **** StringToSign: " + stringToSign);

        final String signature = calculateRFC2104HMAC(stringToSign, amazonSecret);

        final String authorization = "AWS " + amazonKey + ":" + signature;
        final String host = PROTOCOL + bucket + ".s3.amazonaws.com" + DELIMITER + objectName + queryParams;

        return new SignedRequest(formattedDate, host, authorization);
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
                + DELIMITER + objectBucket + DELIMITER + objectName + queryParams;

        LOG.info(" **** StringToSign: " + stringToSign);

        final String signature = calculateRFC2104HMAC(stringToSign, amazonSecret);

        final String authorization = "AWS " + amazonKey + ":" + signature;
        final String host = PROTOCOL + objectBucket + ".s3.amazonaws.com" + DELIMITER + objectName + queryParams;

        return new SignedRequest(formattedDate, host, authorization);
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
                + DELIMITER + objectBucket + DELIMITER + objectName + queryParams;

        LOG.info(" **** StringToSign: " + stringToSign);

        final String signature = calculateRFC2104HMAC(stringToSign, amazonSecret);

        final String authorization = "AWS " + amazonKey + ":" + signature;
        final String host = PROTOCOL + objectBucket + ".s3.amazonaws.com" + DELIMITER + objectName + queryParams;

        return new SignedRequest(formattedDate, host, authorization);
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
                + DELIMITER + objectBucket + DELIMITER + objectName + queryParams;

        LOG.info(" **** StringToSign: " + stringToSign);

        final String signature = calculateRFC2104HMAC(stringToSign, amazonSecret);

        final String authorization = "AWS " + amazonKey + ":" + signature;
        final String host = PROTOCOL + objectBucket + ".s3.amazonaws.com" + DELIMITER + objectName + queryParams;

        return new SignedRequest(formattedDate, host, authorization);
    }


    @Override
    public SignedRequest signCompleteUploadRequest(long userId, String objectName, String uploadId, boolean addCharsetToContentType) {

        final String bucket = processCompleteUploadRequest(objectName);
        final String formattedDate = getAmzFormattedDate();
        objectName = urlencode(objectName);

        final String queryParams = "?uploadId=" + uploadId;
        final String textXmlContentType = "text/xml";
        final String contentType = addCharsetToContentType ? (textXmlContentType + "; charset=UTF-8") : textXmlContentType;
        String stringToSign = "POST\n\n"
                + contentType + "\n"
                + "\n"
                + "x-amz-date:" + formattedDate + "\n"
                + DELIMITER + bucket + DELIMITER + objectName + queryParams;

        LOG.info(" **** StringToSign: " + stringToSign);

        final String signature = calculateRFC2104HMAC(stringToSign, amazonSecret);

        final String authorization = "AWS " + amazonKey + ":" + signature;
        final String host = PROTOCOL + bucket + ".s3.amazonaws.com" + DELIMITER + objectName + queryParams;

        return new SignedRequest(formattedDate, host, authorization);
    }

    @Override
    public boolean useServerSideEncryption() {
        return "true".equals(serverSideEncryption);
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

        return fromNullable(fileMetaDataRepository.findByDestinationPath(objectName)).transform(new Function<ActiveFileMetaData, String>() {
            @Override
            public String apply(ActiveFileMetaData metaFile) {
                final String bucket = bucketForFileMetaData(metaFile);
                metaFile.setFileUploadBucket(bucket);
                fileMetaDataRepository.save(metaFile);
                return bucket;
            }
        }).or(defaultBucket);
    }

    private String getBucketForSingleRequest(String object) {

        return fromNullable(fileMetaDataRepository.findByDestinationPath(object)).transform(new Function<ActiveFileMetaData, String>() {
            @Override
            public String apply(ActiveFileMetaData metaFile) {
                return bucketForFileMetaData(metaFile);
            }
        }).or(defaultBucket);
    }

    private String processCompleteUploadRequest(String objectName) {

        return fromNullable(fileMetaDataRepository.findByDestinationPath(objectName)).transform(new Function<ActiveFileMetaData, String>() {
            @Override
            public String apply(ActiveFileMetaData input) {
                final String bucket = input.getFileUploadBucket();
                input.setFileUploadBucket(null);
                fileMetaDataRepository.save(input);
                return bucket;
            }
        }).or(defaultBucket);

    }

    private String bucketForFileMetaData(ActiveFileMetaData metaFile) {
        return defaultBucket;
    }

    private String getObjectBucket(String objectName) {

        return fromNullable(fileMetaDataRepository.findByDestinationPath(objectName)).transform(new Function<ActiveFileMetaData, String>() {
            @Override
            public String apply(ActiveFileMetaData input) {
                return input.getFileUploadBucket();
            }
        }).or(defaultBucket);
    }

}
