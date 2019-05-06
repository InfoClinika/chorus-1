package com.infoclinika.mssharing.web.controller.request;

import java.util.Set;

/**
 * @author Alexander Orlov
 */
public class CancelUploadRequest {
    public Set<FileItem> files;

    public static class FileItem {
        public long id;
    }
}
