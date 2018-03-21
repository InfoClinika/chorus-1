package com.infoclinika.mssharing.integration.test.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

/**
 * @author Sergii Moroz
 */

public class DownloadsHandler {
    private static final Logger LOGGER = Logger.getLogger(DownloadsHandler.class);

    public static final int BUFFER_SIZE = 4096;
    public static final int BYTES_TO_READ_FOR_VERIFICATION = 8192;

   public static boolean testAnonymousDownload(String link) {
        try {
            final DefaultHttpClient client = new DefaultHttpClient();
            final HttpGet request = new HttpGet(link);
            LOGGER.debug("Executing GET request: " + request);
            final HttpResponse response = client.execute(request);
            final StatusLine statusLine = response.getStatusLine();
            LOGGER.debug("Got response status: " + statusLine);

            final int statusCode = statusLine.getStatusCode();
            assertEquals("HTTP code is not 200", HttpStatus.SC_OK, statusCode);

            final HttpEntity entity = response.getEntity();
            final InputStream is = entity.getContent();

            final byte[] destination = new byte[BUFFER_SIZE];
            int totalBytesRead = 0;
            LOGGER.debug("Reading content... ");
            while (is.available() > -1 && totalBytesRead < BYTES_TO_READ_FOR_VERIFICATION) {
                final int readBytes = is.read(destination);
                LOGGER.debug(" + read bytes: " + readBytes);
                totalBytesRead += readBytes;
            }
            if (totalBytesRead < BYTES_TO_READ_FOR_VERIFICATION) {
                fail("The response was too small. Expected at least " + BYTES_TO_READ_FOR_VERIFICATION + ", but got: " + totalBytesRead);
            }

        } catch (IOException e) {
            fail(e.getMessage());
        }
        return true;
    }
}
