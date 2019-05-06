package com.infoclinika.mssharing.web.downloader;

import com.amazonaws.services.s3.AmazonS3;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.helper.items.ChorusFileData;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.model.write.FileAccessLogService;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate;
import com.infoclinika.mssharing.platform.web.downloader.SingleFileDownloadHelperTemplate;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Optional.fromNullable;
import static com.infoclinika.mssharing.model.features.ApplicationFeature.BILLING;
import static java.lang.String.format;

/**
 * @author timofey.kasyanov, Herman Zamula
 *     date: 06.05.2014
 */
@Component
public class ChorusSingleFileDownloadHelper extends SingleFileDownloadHelperTemplate<ChorusDownloadData> {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Resource(name = "billingService")
    private BillingService billingService;

    private final SecurityHelper securityHelper;
    private final FeaturesHelper featuresHelper;
    private final FileAccessLogService fileAccessLogService;
    private final CloudStorageClientsProvider cloudStorageClientsProvider;

    private AmazonS3 amazonS3;

    @Inject
    public ChorusSingleFileDownloadHelper(ExperimentDownloadHelperTemplate<?, ?, ?> experimentDownloadHelper,
                                          AmazonPropertiesProvider amazonPropertiesProvider,
                                          SecurityHelper securityHelper,
                                          FeaturesHelper featuresHelper,
                                          FileAccessLogService fileAccessLogService,
                                          CloudStorageClientsProvider cloudStorageClientsProvider) {
        super(experimentDownloadHelper, amazonPropertiesProvider);
        this.securityHelper = securityHelper;
        this.featuresHelper = featuresHelper;
        this.fileAccessLogService = fileAccessLogService;
        this.cloudStorageClientsProvider = cloudStorageClientsProvider;
    }


    public URL getDownloadUrl(final long actor, ChorusDownloadData downloadData) {

        LOGGER.debug(format(
            "Request single file download. Actor {%d}, file {%d}, requested lab {%d}",
            actor, downloadData.file, downloadData.lab
        ));

        final ChorusFileData fileData = (ChorusFileData) experimentDownloadHelper.readFilesDownloadData(
            actor,
            Collections.singleton(downloadData.file)
        ).iterator().next();

        final URL url = generateDownloadURL(fileData);

        executorService.submit(() -> {
            try {
                logDownloadUsage(actor, fileData, downloadData.lab);
            } catch (Exception e) {
                LOGGER.error("Download usage is not logged: {}", e.getMessage());
                throw Throwables.propagate(e);
            }
        });

        fileAccessLogService.logFileDownload(actor, fileData);

        return url;
    }

    @Override
    protected AmazonS3 getAmazonS3() {
        if (amazonS3 == null) {
            amazonS3 = cloudStorageClientsProvider.getAmazonS3Client();
        }

        return amazonS3;
    }

    private void logDownloadUsage(long actor, ChorusFileData file, Long billingLab) {
        if (featuresHelper.isEnabled(BILLING)) {
            final Long labToBill = !isFileInUserLab(actor, file) ? billingLab : file.billLab.or(file.lab);
            switch (file.accessLevel) {
                case SHARED:
                case PRIVATE:
                    billingService.logDownloadUsage(actor, file.id, labToBill);
                    break;
                case PUBLIC:
                    billingService.logPublicDownload(actor, file.id);
                    break;
                default:
                    throw new RuntimeException("Unsupported Access Level: " + file.accessLevel);
            }
        }
    }

    private boolean isFileInUserLab(long actor, ChorusFileData file) {
        return securityHelper.getUserDetails(actor).labs.contains(file.billLab.or(file.lab));
    }


    private URL generateDownloadURL(ChorusFileData filePath) {
        final String bucket;
        final com.google.common.base.Optional<String> key;
        if (StringUtils.isBlank(filePath.bucket)) {
            bucket = filePath.archiveId == null
                ? amazonPropertiesProvider.getActiveBucket()
                : amazonPropertiesProvider.getArchiveBucket();
            key = fromNullable(filePath.archiveId).or(fromNullable(filePath.contentId));
        } else {
            bucket = filePath.bucket;
            key = fromNullable(filePath.contentId);
        }

        Preconditions.checkState(key.isPresent(), "Download path is not specified for file {" + filePath.id + "}");

        final long now = System.currentTimeMillis();
        final Date expirationDate = new Date(now + EXPIRATION_PERIOD);

        return getAmazonS3().generatePresignedUrl(bucket, key.get(), expirationDate);
    }
}
