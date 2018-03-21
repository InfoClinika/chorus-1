package com.infoclinika.mssharing.platform.web.downloader;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate.FileDataTemplate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URL;
import java.util.Date;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.singleton;

/**
 * @author Herman Zamula
 */
@Service
public class SingleFileDownloadHelperTemplate<DATA extends SingleFileDownloadHelperTemplate.DownloadData> {

    protected static final long EXPIRATION_PERIOD = 12 * 60 * 60 * 1000; //12 hours
    protected final Logger logger = Logger.getLogger(this.getClass());
    @Value("${amazon.active.bucket}")
    protected String storageBucket;
    @Inject
    protected ExperimentDownloadHelperTemplate<?, ?, ?> experimentDownloadHelper;
    @Value("${amazon.key}")
    private String accessKey;
    @Value("${amazon.secret}")
    private String secretKey;
    private AmazonS3 amazonS3;

    public URL getDownloadUrl(final long actor, DATA downloadData) {

        logger.debug(String.format("Request single file download. Actor {%d}, file {%d}", actor, downloadData.file));

        final FileDataTemplate fileData = getOnlyElement(experimentDownloadHelper.readFilesDownloadData(actor, singleton(downloadData.file)));

        return generateDownloadURL(fileData);

    }

    protected URL generateDownloadURL(FileDataTemplate filePath) {

        final Optional<String> key = fromNullable(filePath.contentId);

        Preconditions.checkState(key.isPresent(), "Download path is not specified for file {" + filePath.id + "}");

        final long now = System.currentTimeMillis();
        final Date expirationDate = new Date(now + EXPIRATION_PERIOD);

        return getAmazonS3().generatePresignedUrl(storageBucket, key.get(), expirationDate);
    }

    protected AmazonS3 getAmazonS3() {
        if (amazonS3 == null) {
            final AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            amazonS3 = new AmazonS3Client(credentials);
        }
        return amazonS3;
    }

    public static class DownloadData {
        public final long file;

        public DownloadData(long file) {
            this.file = file;
        }

    }
}
