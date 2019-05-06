package com.infoclinika.mssharing.platform.web.downloader;

/**
 * Thrown when the download request referenced not found
 *
 * @author Alexei Tymchenko
 */
public class DownloadRequestNotFoundException extends Exception {
    public DownloadRequestNotFoundException(int requestId) {
        super("Download request with ID = " + requestId + " not found");
    }
}
