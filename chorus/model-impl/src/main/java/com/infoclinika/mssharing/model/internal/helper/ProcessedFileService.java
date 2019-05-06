package com.infoclinika.mssharing.model.internal.helper;

/**
 * Created by slava on 5/31/17.
 */
public interface ProcessedFileService {
    Long create(long userId, long experimentId, String filename);

    String get(long userId, long experimentId, long fileId);

    void uploadDone(long userId, long experimentId, long fileId);
}
