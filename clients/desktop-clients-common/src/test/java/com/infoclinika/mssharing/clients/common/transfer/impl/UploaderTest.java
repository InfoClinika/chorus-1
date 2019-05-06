package com.infoclinika.mssharing.clients.common.transfer.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3Client;
import com.infoclinika.mssharing.clients.common.transfer.api.Uploader;
import com.infoclinika.mssharing.clients.common.util.PauseSemaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author timofey.kasyanov
 *     date: 25.02.14.
 */
public class UploaderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploaderTest.class);
    private static final int RETRY_COUNT = 25;

    public static void main(String[] args) {

        final AmazonS3Client amazonS3 = new AmazonS3Client(
            new BasicAWSCredentials("AKIAI7XAOGC6U2SLQIGA", "lHA3gxigrzxFsa5uYurAHECVFMvjl0g5sKJLnj71"),
            new ClientConfiguration()
                .withMaxErrorRetry(RETRY_COUNT)
                .withProtocol(Protocol.HTTP)

        );

        final File file = new File("path to the file");
        final long fileSize = file.length();
        final String bucket = "chorus-development";
        final String key = "test_uploader_" + file.getName();
        final UploaderConfiguration uploaderConfiguration =
            UploaderConfiguration.getDefaultConfiguration(new S3ClientProvider() {
                @Override
                public AmazonS3Client get() {
                    return amazonS3;
                }
            }, bucket);
        final UploadItem uploadItem = new UploadItem(file, key);
        final PauseSemaphore pauseSemaphore = new PauseSemaphore();
        final Uploader uploader = new UploaderImpl(uploaderConfiguration, pauseSemaphore);

        try {

            uploader.upload(uploadItem, new ProgressListener() {

                private long bytes = 0;
                private long lastBytes = 0;
                private final Timer timer = new Timer(true);

                {
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {

                            final long diff = bytes - lastBytes;
                            lastBytes = bytes;

                            final double uploaded = lastBytes != 0 ? (double) lastBytes / (double) fileSize : 0;

                            if (uploaded >= 1) {
                                timer.cancel();
                            }

                            LOGGER.info("Speed: {} KB / s. Uploaded: {}", diff / 1000, uploaded);
                        }
                    }, 1000, 1000);
                }

                @Override
                public void progressChanged(ProgressEvent progressEvent) {

                    bytes += progressEvent.getBytesTransferred();

                }
            });

        } finally {

            LOGGER.info("Deleting S3 object. Key: {}", key);

            try {

                amazonS3.deleteObject(bucket, key);

                LOGGER.info("S3 object deleted successfully. Key: {}", key);

            } catch (Exception ex) {
                LOGGER.error("Cannot delete S3 object: {}", key);
            }

        }


    }

}
