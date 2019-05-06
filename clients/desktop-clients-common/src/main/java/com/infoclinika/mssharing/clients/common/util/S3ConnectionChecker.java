package com.infoclinika.mssharing.clients.common.util;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * @author Vladislav Kovchug
 */
public class S3ConnectionChecker {
    private static final int TIME_TO_WAIT_BETWEEN_CONNECTIONS_CHECKS = 5000;

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ConnectionChecker.class);

    public static boolean checkCanAccessToS3() {
        LOGGER.info("Check if can connect to S3.");
        final AmazonS3 amazonS3 = AmazonS3Client.builder()
                .withRegion(Regions.US_EAST_1)
                .build();
        final URL amazonS3Url = amazonS3.getUrl(null, null);
        final HttpHead httpGet = new HttpHead(amazonS3Url.toString());
        try (final CloseableHttpClient client = HttpClientBuilder.create().build()) {
            final CloseableHttpResponse response = client.execute(httpGet);
            response.close();
            return true;
        } catch (Exception e) {
            LOGGER.warn("Can't connect to S3. Error: {}", e.getMessage());
            return false;
        }
    }

    public static void waitForS3Connection() {
        while (!S3ConnectionChecker.checkCanAccessToS3()) {
            LOGGER.info("No connection to S3. Waiting...");
            try {
                Thread.sleep(TIME_TO_WAIT_BETWEEN_CONNECTIONS_CHECKS);
            } catch (InterruptedException e) {
                LOGGER.error("Error. Thread sleep was interrupted", e);
            }
        }
    }

}
