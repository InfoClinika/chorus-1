package com.infoclinika.mssharing.web.controller.service;

import com.infoclinika.mssharing.model.internal.entity.upload.FileDetails;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadDetails;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadFileStatus;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadType;
import com.infoclinika.mssharing.web.controller.v2.service.TrackingUploadServiceImpl;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.testng.annotations.BeforeMethod;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Vitalii Petkanych
 */
public class TrackingUploadServiceImplTest {

    private static final long ID = 123;
    private static final String BUCKET = "bucket";
    private static final String TEST1_TXT = "test1.txt";
    private static final String TEST2_TXT = "test2.txt";

    private TrackingUploadServiceImpl cut;
    private UploadDetails uploadDetails;

    @BeforeMethod
    public void setUp() {
        final FileDetails f1 = new FileDetails();
        f1.setFileName(TEST1_TXT);

        final FileDetails f2 = new FileDetails();
        f2.setFileName(TEST2_TXT);

        cut = new TrackingUploadServiceImpl();
        uploadDetails = new UploadDetails();
        uploadDetails.setType(UploadType.S3_COPY);
        uploadDetails.setUrl("s3://" + BUCKET);
        uploadDetails.setFiles(Arrays.asList(f1, f2));
        uploadDetails.setId(ID);
    }

    //    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStartTrackingWithoutType() {
        uploadDetails.setType(null);
        cut.startTracking(uploadDetails);
    }

    //    @Test
    public void testStartTrackingS3() {
        uploadDetails.setType(UploadType.S3_COPY);
        cut.startTracking(uploadDetails);
    }

    //    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStartTrackingDesktop() {
        uploadDetails.setType(UploadType.DESKTOP);
        cut.startTracking(uploadDetails);
    }

    //    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStartTrackingDirect() {
        uploadDetails.setType(UploadType.DIRECT);
        cut.startTracking(uploadDetails);
    }

    //    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testStartTrackingFTP() {
        uploadDetails.setType(UploadType.FTP);
        cut.startTracking(uploadDetails);
    }

    //    @Test
    public void testUpdateFileProgress() {
        final long bytes = 10;
        cut.startTracking(uploadDetails);
        cut.updateFileProgress(cut.trackingId(BUCKET, TEST1_TXT), bytes);
        final UploadDetails uploadDetails = cut.getTrackingDetails(ID);
        assertThat(uploadDetails.getFiles(), hasItem(both(hasFileName(TEST1_TXT)).and(hasSizeUploaded(bytes))));
    }

    //    @Test
    public void testUpdateFileStatus() throws Exception {
        cut.startTracking(uploadDetails);
        UploadDetails uploadDetails = cut.getTrackingDetails(ID);
        assertThat(uploadDetails.getFiles(), hasItem(both(hasFileName(TEST1_TXT)).and(hasStatus(null))));
        cut.updateFileStatus(cut.trackingId(BUCKET, TEST1_TXT), UploadFileStatus.STARTED);
        assertThat(
            uploadDetails.getFiles(),
            hasItem(both(hasFileName(TEST1_TXT)).and(hasStatus(UploadFileStatus.STARTED)))
        );
    }

    private Matcher<FileDetails> hasFileName(String fileName) {
        return new FeatureMatcher<FileDetails, String>(equalTo(fileName), "fileName", "fileName") {
            @Override
            protected String featureValueOf(FileDetails fileDetails) {
                return fileDetails.getFileName();
            }
        };
    }

    private Matcher<FileDetails> hasSizeUploaded(Long sizeUploaded) {
        return new FeatureMatcher<FileDetails, Long>(equalTo(sizeUploaded), "sizeUploaded", "sizeUploaded") {
            @Override
            protected Long featureValueOf(FileDetails fileDetails) {
                return fileDetails.getSizeUploaded();
            }
        };
    }

    private Matcher<FileDetails> hasStatus(UploadFileStatus status) {
        return new FeatureMatcher<FileDetails, UploadFileStatus>(equalTo(status), "status", "status") {
            @Override
            protected UploadFileStatus featureValueOf(FileDetails fileDetails) {
                return fileDetails.getStatus();
            }
        };
    }
}
