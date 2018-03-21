package com.infoclinika.mssharing.model.internal.repository;

import java.util.Date;

/**
 * @author Elena Kurilina
 */
public class FileLastAccess {
    public final long id;
    public final String contentId;
    public final String archiveId;
    public final Date lastAccess;
    public final long lab;

    public FileLastAccess(long id, String contentId, Date lastAccess, String archiveId, long lab) {
        this.id = id;
        this.contentId = contentId;
        this.lastAccess = lastAccess;
        this.archiveId = archiveId;
        this.lab = lab;
    }
}
