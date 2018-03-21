package com.infoclinika.mssharing.platform.web.downloader;

/**
 * Thrown when something goes wrong during the download request processing.
 *
 * @author Alexei Tymchenko
 */
public class DownloadRequestProcessingException extends Exception {
    public DownloadRequestProcessingException(int requestId) {
        super("Error processing the download request with ID = " + requestId);
    }
}
