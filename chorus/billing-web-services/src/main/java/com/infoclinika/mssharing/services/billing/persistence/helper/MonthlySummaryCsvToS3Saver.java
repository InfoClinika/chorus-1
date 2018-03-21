package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.analysis.storage.s3.S3CloudStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.util.Date;

import static com.infoclinika.mssharing.services.billing.persistence.helper.MonthlySummaryCsvSaver.MONTH_FORMAT;

/**
 * @author Herman Zamula
 */
@Component
public class MonthlySummaryCsvToS3Saver {
    private static final Logger LOG = LoggerFactory.getLogger(MonthlySummaryCsvToS3Saver.class);

    @Value("${amazon.billing.prefix}")
    private String billingPrefix;
    @Value("${amazon.active.bucket}")
    private String activeBucket;

    @Inject
    private MonthlySummaryCsvSaver monthlySummaryCsvSaver;

    public CloudStorageItemReference saveToCloud(Date month) {
        final CloudStorageService storageService = new S3CloudStorageService();
        final File resultDir = monthlySummaryCsvSaver.saveUsagesToCsv(month);

        return storageService.uploadFolderContentsToCloud(resultDir, getTargetReference());
    }

    private CloudStorageItemReference getTargetReference() {
        return new CloudStorageItemReference(activeBucket, billingPrefix);

    }

}
