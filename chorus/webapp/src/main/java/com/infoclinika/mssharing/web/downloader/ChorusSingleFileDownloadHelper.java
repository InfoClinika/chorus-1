package com.infoclinika.mssharing.web.downloader;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.helper.items.ChorusFileData;
import com.infoclinika.mssharing.model.write.FileAccessLogService;
import com.infoclinika.mssharing.platform.web.downloader.SingleFileDownloadHelperTemplate;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Optional.fromNullable;
import static java.lang.String.format;

/**
 * @author timofey.kasyanov, Herman Zamula
 *         date: 06.05.2014
 */
@Component
public class ChorusSingleFileDownloadHelper extends SingleFileDownloadHelperTemplate<ChorusDownloadData> {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Value("${amazon.archive.bucket}")
    private String archiveBucket;
    @Resource(name = "billingRestService")
    private BillingService billingService;
    @Inject
    private SecurityHelper securityHelper;

    @Inject
    private FileAccessLogService fileAccessLogService;


    public URL getDownloadUrl(final long actor, ChorusDownloadData downloadData) {

        logger.debug(format("Request single file download. Actor {%d}, file {%d}, requested lab {%d}", actor, downloadData.file, downloadData.lab));

        final ChorusFileData fileData = (ChorusFileData) experimentDownloadHelper.readFilesDownloadData(actor, Collections.singleton(downloadData.file)).iterator().next();

        final URL url = generateDownloadURL(fileData);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    logDownloadUsage(actor, fileData, downloadData.lab);
                } catch (Exception e) {
                    logger.error("Download usage is not logged: " + e.getMessage());
                    throw Throwables.propagate(e);
                }
            }
        });

        fileAccessLogService.logFileDownload(actor, fileData);
        return url;

    }

    @Async
    private void logDownloadUsage(long actor, ChorusFileData file, Long billingLab) {
        final Long labToBill = !isFileInUserLab(actor, file) ? billingLab : file.billLab.or(file.lab);
        switch (file.accessLevel) {
            case SHARED:
            case PRIVATE:
                billingService.logDownloadUsage(actor, file.id, labToBill);
                break;
            case PUBLIC:
                billingService.logPublicDownload(actor, file.id);
                break;
        }
    }

    private boolean isFileInUserLab(long actor, ChorusFileData file) {
        return securityHelper.getUserDetails(actor).labs.contains(file.billLab.or(file.lab));
    }


    private URL generateDownloadURL(ChorusFileData filePath) {

        final String bucket = filePath.archiveId == null ? storageBucket : archiveBucket;
        final Optional<String> key = fromNullable(filePath.archiveId).or(fromNullable(filePath.contentId));

        Preconditions.checkState(key.isPresent(), "Download path is not specified for file {" + filePath.id + "}");

        final long now = System.currentTimeMillis();
        final Date expirationDate = new Date(now + EXPIRATION_PERIOD);

        return getAmazonS3().generatePresignedUrl(bucket, key.get(), expirationDate);
    }

}
