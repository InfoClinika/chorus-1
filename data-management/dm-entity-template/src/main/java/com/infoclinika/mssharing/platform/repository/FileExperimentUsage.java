package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.Sharing;

/**
 * @author Herman Zamula
 */
public class FileExperimentUsage {
    public final long file;
    public final long experiment;
    public final Sharing.Type sharingType;

    public FileExperimentUsage(long experiment, long file, Sharing.Type sharingType) {
        this.file = file;
        this.experiment = experiment;
        this.sharingType = sharingType;
    }
}
