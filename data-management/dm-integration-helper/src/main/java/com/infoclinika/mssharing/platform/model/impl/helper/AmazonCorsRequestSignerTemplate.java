package com.infoclinika.mssharing.platform.model.impl.helper;

import com.infoclinika.mssharing.platform.model.helper.CorsRequestSignerTemplate;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.infoclinika.mssharing.platform.fileserver.StorageService.DELIMITER;

/**
 * @author Herman Zamula
 */
public abstract class AmazonCorsRequestSignerTemplate implements CorsRequestSignerTemplate {

    private static final String PROTOCOL = "https://";
    private static final String S3_URL = PROTOCOL + "s3.amazonaws.com";
    private static final String MIME_TYPE = "application/octet-stream";
    private static final String AMAZON_UPLOAD_HEADERS = "x-amz-acl:private";
    private static final String AMAZON_SECURITY_TOKEN_HEADER = "x-amz-security-token";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final String UTF_8 = "UTF-8";
    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonCorsRequestSignerTemplate.class);

    protected static String getAmzFormattedDate() {
        final Date date = new Date();

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        return simpleDateFormat.format(date);
    }

    protected static String urlencode(String s) {
        try {
            return URLEncoder.encode(s, UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String calculateRFC2104HMAC(String data, String key) {
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

    protected abstract String getAmazonSecret();

    protected abstract String getBucket(long userId, String objectName);

    protected abstract String getAmazonKey();

    public abstract String getAmazonToken();

    @Override
    public String signSingleFileUploadRequest(long userId, String objectName) {

        beforeSignSingleFileUploadRequest(userId, objectName);

        final String bucket = getBucket(userId, objectName);
        long expireTime = new Date().getTime() / 1000 + (60 * 5);
        objectName = urlencode(objectName);


        String stringToSign = "PUT\n\n"
            + MIME_TYPE + "\n"
            + expireTime + "\n"
            + AMAZON_UPLOAD_HEADERS + "\n"
            + (useSessionToken() ? AMAZON_SECURITY_TOKEN_HEADER + ":" + getAmazonToken() + "\n" : "")
            + DELIMITER + bucket + DELIMITER + objectName;

        String signature = urlencode(calculateRFC2104HMAC(stringToSign, getAmazonSecret()));

        return urlencode(S3_URL + DELIMITER + bucket + DELIMITER + objectName
            + "?AWSAccessKeyId=" + getAmazonKey() + "&Expires=" + expireTime + "&Signature=" + signature);
    }

    @Override
    public SignedRequest signForSingleFileUploadRequest(long userId, String objectName) {
        return new SignedRequest(null, signSingleFileUploadRequest(userId, objectName), null, getAmazonToken());
    }

    protected void beforeSignSingleFileUploadRequest(long userId, String objectName) {

    }

    @Override
    public SignedRequest signInitialUploadRequest(long userId, String objectName) {

        beforeSignInitialUploadRequest(userId, objectName);

        final String bucket = getBucket(userId, objectName);
        final String formattedDate = getAmzFormattedDate();
        objectName = urlencode(objectName);

        final String queryParams = "?uploads";
        String stringToSign = "POST\n\n"
            + "\n"
            + "\n"
            + AMAZON_UPLOAD_HEADERS + "\n"
            + "x-amz-date:" + formattedDate + "\n"
            + (useSessionToken() ? AMAZON_SECURITY_TOKEN_HEADER + ":" + getAmazonToken() + "\n" : "")
            + DELIMITER + bucket + DELIMITER + objectName + queryParams;

        LOGGER.info(" **** StringToSign: {}", stringToSign);

        final String signature = calculateRFC2104HMAC(stringToSign, getAmazonSecret());

        final String authorization = "AWS " + getAmazonKey() + ":" + signature;
        final String host = PROTOCOL + bucket + ".s3.amazonaws.com" + DELIMITER + objectName + queryParams;

        return new SignedRequest(formattedDate, host, authorization, getAmazonToken());
    }

    protected void beforeSignInitialUploadRequest(long userId, String objectName) {

    }

    @Override
    public SignedRequest signUploadPartRequest(long userId, String objectName, long partNumber, String uploadId) {

        beforeSignUploadPartRequest(uploadId, objectName, partNumber, uploadId);

        final String objectBucket = getBucket(userId, objectName);
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

    protected void beforeSignUploadPartRequest(String uploadId, String objectName, long partNumber, String uploadId1) {

    }

    @Override
    public SignedRequest signListPartsRequest(long userId, String objectName, String uploadId) {

        beforeSignListPartsRequest(userId, objectName, uploadId);

        final String objectBucket = getBucket(userId, objectName);
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

    protected void beforeSignListPartsRequest(long userId, String objectName, String uploadId) {

    }

    @Override
    public SignedRequest signAbortUploadRequest(long userId, String objectName, String uploadId) {

        beforeSignAbortUploadRequest(userId, objectName, uploadId);

        final String objectBucket = getBucket(userId, objectName);
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

    protected void beforeSignAbortUploadRequest(long userId, String objectName, String uploadId) {

    }

    // --- Helper methods ----

    @Override
    public SignedRequest signCompleteUploadRequest(long userId, String objectName, String uploadId,
                                                   boolean addCharsetToContentType) {

        beforeSignCompleteUploadRequest(userId, objectName, uploadId, addCharsetToContentType);

        final String bucket = getBucket(userId, objectName);
        final String formattedDate = getAmzFormattedDate();
        objectName = urlencode(objectName);

        final String queryParams = "?uploadId=" + uploadId;
        final String textXmlContentType = "text/xml";
        final String contentType =
            addCharsetToContentType ? (textXmlContentType + "; charset=UTF-8") : textXmlContentType;
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

    protected void beforeSignCompleteUploadRequest(long userId, String objectName, String uploadId,
                                                   boolean addCharsetToContentType) {

    }

    @Override
    public boolean useServerSideEncryption() {
        return false;
    }
}
