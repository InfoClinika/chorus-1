package com.infoclinika.mssharing.platform.web.uploader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Files storage abstraction
 *
 * @author Pavel Kaplin
 */
public interface FileStorage {

    /**
     * Constants should match ones in JS
     */
    int PACKET_SIZE = 512 * 512 * 10;
    int FIRST_PACKET_INDEX = 0;

    /**
     * Start new upload, record information about file into some storage.
     *
     * @param fileName  file name
     * @param totalSize total file size in bytes
     * @param fileId    unique generated file id
     */
    void newUpload(String fileName, long totalSize, UUID fileId) throws IOException;

    void receivePacket(UUID fileId, int packetNumber, InputStream inputStream, String hash)
        throws IOException, IncorrectHashException;

    void finishUpload(UUID fileId) throws IOException;

    FileDetails getFileDetails(UUID fileId) throws IOException;

    InputStream getInputStream(UUID fileId);

    File getFile(UUID fileId);
}
