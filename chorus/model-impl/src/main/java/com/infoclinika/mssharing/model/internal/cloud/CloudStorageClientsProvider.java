package com.infoclinika.mssharing.model.internal.cloud;

import com.amazonaws.services.s3.AmazonS3;
import com.infoclinika.analysis.storage.cloud.CloudStorageFactory;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.analysis.storage.s3.S3CloudStorageService;
import com.infoclinika.common.io.impl.S3FileOperationHandler;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Properties;

/**
 * @author Vladislav Kovchug
 */

@Service
public class CloudStorageClientsProvider {

    @Inject
    private List<Properties> propertiesList;

    @Inject
    public CloudStorageClientsProvider() {

    }

    public CloudStorageService getCloudStorageService() {
        return CloudStorageFactory.service(propertiesList);
    }

    public S3FileOperationHandler getS3FileOperationHandler() {
        final S3CloudStorageService cloudStorageService = (S3CloudStorageService) getCloudStorageService();
        return cloudStorageService.getS3FileOperationHandler();
    }

    public AmazonS3 getAmazonS3Client() {
        //FileOperationHandler.getS3Client creates new instance every time.
        return getS3FileOperationHandler().getS3Client();
    }
}
