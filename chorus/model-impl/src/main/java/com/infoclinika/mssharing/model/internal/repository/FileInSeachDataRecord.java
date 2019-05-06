package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.RawFile;

/**
 * @author Oleksii Tymchenko
 */
public class FileInSeachDataRecord {
    public final Long fileId;
    public final String fileName;
    public final String cleanedMs1Ref;
    public final RawFile file;

    public FileInSeachDataRecord(Long fileId, String fileName, String cleanedMs1Ref, RawFile file) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.cleanedMs1Ref = cleanedMs1Ref;
        this.file = file;
    }
}
