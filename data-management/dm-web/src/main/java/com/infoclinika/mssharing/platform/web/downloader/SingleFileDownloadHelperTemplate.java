package com.infoclinika.mssharing.platform.web.downloader;

import com.amazonaws.services.s3.AmazonS3;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate.FileDataTemplate;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URL;
import java.util.Date;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.infoclinika.mssharing.platform.web.downloader.SingleFileDownloadHelperTemplate.DownloadData;
import static java.util.Collections.singleton;

/**
 * @author Herman Zamula
 */
@Service
public abstract class SingleFileDownloadHelperTemplate<DATA extends DownloadData> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(SingleFileDownloadHelperTemplate.class);
    protected static final long EXPIRATION_PERIOD = 12 * 60 * 60 * 1000; //12 hours

    protected final ExperimentDownloadHelperTemplate<?, ?, ?> experimentDownloadHelper;
    protected final AmazonPropertiesProvider amazonPropertiesProvider;

    @Inject
    public SingleFileDownloadHelperTemplate(ExperimentDownloadHelperTemplate<?, ?, ?> experimentDownloadHelper,
                                            AmazonPropertiesProvider amazonPropertiesProvider) {
        this.experimentDownloadHelper = experimentDownloadHelper;
        this.amazonPropertiesProvider = amazonPropertiesProvider;
    }

    protected abstract AmazonS3 getAmazonS3();

    public URL getDownloadUrl(final long actor, DATA downloadData) {
        LOGGER.debug("Request single file download. Actor {}, file {}", actor, downloadData.file);

        final FileDataTemplate fileData =
            getOnlyElement(experimentDownloadHelper.readFilesDownloadData(actor, singleton(downloadData.file)));

        return generateDownloadURL(fileData);
    }

    protected URL generateDownloadURL(FileDataTemplate filePath) {
        final Optional<String> key = Optional.ofNullable(filePath.contentId);

        checkState(key.isPresent(), "Download path is not specified for file {" + filePath.id + "}");

        final long now = System.currentTimeMillis();
        final Date expirationDate = new Date(now + EXPIRATION_PERIOD);

        return getAmazonS3().generatePresignedUrl(
            amazonPropertiesProvider.getActiveBucket(),
            key.get(),
            expirationDate
        );
    }

    public static class DownloadData {
        public final long file;

        public DownloadData(long file) {
            this.file = file;
        }

    }
}
