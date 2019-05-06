package com.infoclinika.mssharing.model.internal.write;

import com.amazonaws.services.s3.transfer.TransferManager;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.model.write.LogUploader;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.util.Calendar;

/**
 * @author Elena Kurilina
 */

@Service
public class LogUploaderImpl implements LogUploader {
    private static final String LOGS = "logs/";

    private final CloudStorageClientsProvider cloudStorageClientsProvider;
    private final AmazonPropertiesProvider amazonPropertiesProvider;

    @Inject
    public LogUploaderImpl(CloudStorageClientsProvider cloudStorageClientsProvider,
                           AmazonPropertiesProvider amazonPropertiesProvider) {
        this.cloudStorageClientsProvider = cloudStorageClientsProvider;
        this.amazonPropertiesProvider = amazonPropertiesProvider;
    }

    @Override
    public URL uploadFile(File log) {
        TransferManager transferManager = cloudStorageClientsProvider.getS3FileOperationHandler().getTransferManager();
        transferManager.upload(amazonPropertiesProvider.getActiveBucket(), LOGS + log.getName(), log);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, amazonPropertiesProvider.getPresignedURLDuration());

        return transferManager.getAmazonS3Client().generatePresignedUrl(
            amazonPropertiesProvider.getActiveBucket(),
            LOGS + log.getName(),
            calendar.getTime()
        );

    }


}
