package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.Sharing;

/**
 * @author Herman Zamula
 */
public class FileProjectUsage {
    public final long project;
    public final long file;
    public final Sharing.Type sharingType;

    public FileProjectUsage(long id, long file, Sharing.Type sharingType) {
        this.project = id;
        this.file = file;
        this.sharingType = sharingType;
    }

}
