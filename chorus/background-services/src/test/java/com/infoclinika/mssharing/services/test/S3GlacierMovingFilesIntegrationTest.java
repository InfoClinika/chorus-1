package com.infoclinika.mssharing.services.test;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import org.testng.Assert;

/**
 * @author Herman Zamula
 */
public class S3GlacierMovingFilesIntegrationTest {

    private static final String KEY = "AKIAI7XAOGC6U2SLQIGA";
    private static final String SECRET = "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";
    public static final String RAW_FILE_NAME = "500f_01.RAW";
    private static final String SOURCE_BUCKET = "chorus-unit-tests";
    private static final String DEST_BUCKET = "chorus-archive";

    //    @BeforeTest
    public void checkFilesPresentInTestBucket() {
        final AmazonS3 s3Client = getAmazonS3();
        final String testRawFileKey = "moving-files-test-data" + StorageService.DELIMITER + RAW_FILE_NAME;
        try {
            s3Client.getObjectMetadata(SOURCE_BUCKET, testRawFileKey);
        } catch (AmazonS3Exception e) {
            s3Client.copyObject(SOURCE_BUCKET, "raw-files" + StorageService.DELIMITER + RAW_FILE_NAME, SOURCE_BUCKET,
                testRawFileKey
            );
        }
    }


    //    @Test(enabled = false)
    public void testMovingFilesBetweenBuckets() {
        final AmazonS3 s3Client = getAmazonS3();
        final String testRawFileKey = "moving-files-test-data" + StorageService.DELIMITER + RAW_FILE_NAME;
        final CopyObjectRequest copyObjectRequest =
            new CopyObjectRequest(SOURCE_BUCKET, testRawFileKey, DEST_BUCKET, testRawFileKey);
        s3Client.copyObject(copyObjectRequest);
        s3Client.deleteObject(SOURCE_BUCKET, testRawFileKey);
        Assert.assertNotNull(s3Client.getObject(DEST_BUCKET, testRawFileKey));
    }

    private AmazonS3 getAmazonS3() {
        final BasicAWSCredentials credentials = new BasicAWSCredentials(KEY, SECRET);
        final AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        return AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider).build();
    }

}
