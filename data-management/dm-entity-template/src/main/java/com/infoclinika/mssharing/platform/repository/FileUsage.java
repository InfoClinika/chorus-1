package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;

/**
 * @author Pavel Kaplin
 */
public class FileUsage {
    public final Long file;
    public final ProjectTemplate project;

    public FileUsage(Long file, ProjectTemplate project) {
        this.file = file;
        this.project = project;
    }
}
