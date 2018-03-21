package com.infoclinika.mssharing.services.test;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Herman Zamula
 */
public class S3GlacierMovingFilesIntegrationTest {

    private static final String KEY = "";
    private static final String SECRET = "";
    public static final String RAW_FILE_NAME = "500f_01.RAW";
    private final AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(KEY, SECRET));
    private static final String SOURCE_BUCKET = "chorus-unit-tests";
    private static final String DEST_BUCKET = "chorus-archive";

    @BeforeTest
    public void checkFilesPresentInTestBucket() {
        final String testRawFileKey = "moving-files-test-data" + StorageService.DELIMITER + RAW_FILE_NAME;
        try {
            s3Client.getObjectMetadata(SOURCE_BUCKET, testRawFileKey);
        } catch (AmazonS3Exception e) {
            s3Client.copyObject(SOURCE_BUCKET, "raw-files" + StorageService.DELIMITER + RAW_FILE_NAME, SOURCE_BUCKET, testRawFileKey);
        }
    }


    @Test(enabled = false)
    public void testMovingFilesBetweenBuckets() {
        final String testRawFileKey = "moving-files-test-data" + StorageService.DELIMITER + RAW_FILE_NAME;
        final CopyObjectRequest copyObjectRequest = new CopyObjectRequest(SOURCE_BUCKET, testRawFileKey, DEST_BUCKET, testRawFileKey);
        s3Client.copyObject(copyObjectRequest);
        s3Client.deleteObject(SOURCE_BUCKET, testRawFileKey);
        Assert.assertNotNull(s3Client.getObject(DEST_BUCKET, testRawFileKey));
    }

}
