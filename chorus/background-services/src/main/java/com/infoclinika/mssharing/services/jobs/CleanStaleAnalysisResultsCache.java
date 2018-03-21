package com.infoclinika.mssharing.services.jobs;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.infoclinika.common.io.impl.S3FileOperationHandler;
import com.infoclinika.common.io.impl.S3PropertiesReader;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;
import java.util.LinkedList;

@Service
public class CleanStaleAnalysisResultsCache {

    @Inject
    private ExperimentRepository experimentRepository;

    private static final String KEY = S3PropertiesReader.getProp("amazon.accessKey");
    private static final String SECRET = S3PropertiesReader.getProp("amazon.secretKey");
    private static final String BUCKET_NAME = S3PropertiesReader.getProp("amazon.default-bucket");
    private final AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(KEY, SECRET));
    private final static String PROCESSING_FOLDER = "protein-search";
    private static final Logger LOGGER = Logger.getLogger(CleanStaleAnalysisResultsCache.class);

    public void delete(final String folderName) {
        LOGGER.info("Remove folder " + folderName);
        S3FileOperationHandler.RetriableProcess retriableProcess = new S3FileOperationHandler.RetriableProcess() {
            public Object doJob() throws Exception {
                long sizeToRemove = 0;
                int countToRemove = 0;
                DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(BUCKET_NAME);
                for(ObjectListing listing = s3Client.listObjects(BUCKET_NAME, folderName); listing.getObjectSummaries().size() != 0; listing = s3Client.listObjects(BUCKET_NAME, folderName)) {
                    LinkedList<DeleteObjectsRequest.KeyVersion> keysToDelete = new LinkedList<>();

                    for (S3ObjectSummary objectSummary : listing.getObjectSummaries()) {
                        if(objectSummary.getKey().startsWith(folderName)) {
                            keysToDelete.add(new DeleteObjectsRequest.KeyVersion(objectSummary.getKey()));
                            countToRemove++;
                            sizeToRemove += objectSummary.getSize();
                        }
                    }
                    deleteObjectsRequest.setKeys(keysToDelete);
                    s3Client.deleteObjects(deleteObjectsRequest);
                }
                LOGGER.info("Removed: " + countToRemove + " objects, " + sizeToRemove + " bytes");
                return null;
            }
        };
        retriableProcess.doWithRetries(10, 200L, "Error deleting object from S3 at " + folderName);
    }

    private void removeSubFoldersForProteinSearch(String subFolder, Date expirationDate) {
        if (subFolder == null || !subFolder.matches(PROCESSING_FOLDER + "/\\d+/\\S+")) {
            throw new RuntimeException("Subfolder name is incorrect: " + subFolder);
        }
        ObjectListing objectListing;
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(BUCKET_NAME).withPrefix(subFolder + "/");
        do {
            objectListing = s3Client.listObjects(listObjectsRequest);
            if (objectListing.getObjectSummaries().size() < 1){
                return;
            }
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                if (objectSummary.getLastModified().after(expirationDate)) {
                    // Folder contains not stale files
                    return;
                }
                listObjectsRequest.setMarker(objectListing.getNextMarker());
            }
        } while (objectListing.isTruncated());
        delete(subFolder);
    }

    // Run service at 2am each Sunday
    @Scheduled(cron = "* * 2 * * SUN")
    private void removeExpiredAnalysisResults() throws InterruptedException {
        final long week = (7 * 24 * 60 * 60 * 1000);
        final Date weekAgo = new Date(System.currentTimeMillis() - week);

        LOGGER.info("Start cleaning stale analysis results cache");
        for (ActiveExperiment experiment: experimentRepository.findAll()){
            removeSubFoldersForProteinSearch(PROCESSING_FOLDER + "/" + experiment.getId() + "/results/analysis-results", weekAgo);
            Thread.sleep(100);
        }
        LOGGER.info("Finish cleaning stale analysis results cache");
    }

}
