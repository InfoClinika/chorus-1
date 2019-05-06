package com.infoclinika.mssharing.platform.web.uploader;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static org.apache.commons.fileupload.util.Streams.copy;

/**
 * @author Pavel Kaplin
 */
public class FileSystemFileStorage implements FileStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemFileStorage.class.getName());
    private static final String INFO_SEPARATOR = ",";
    private final File uploadPath;

    public FileSystemFileStorage(File uploadPath) {
        this.uploadPath = uploadPath;
    }

    @Override
    public void newUpload(String fileName, long totalSize, UUID fileId) throws IOException {
        final File dir = getFileDir(fileId);

        if (dir.exists()) {
            throw new IllegalArgumentException("File with id " + fileId + " already exists");
        }

        boolean created = dir.mkdirs();

        if (!created) {
            throw new IllegalStateException("Could not create dir " + dir);
        }

        new FileItemImpl(fileName, totalSize).write(dir);
    }

    @Override
    public void receivePacket(UUID fileId,
                              int packetNumber,
                              InputStream inputStream,
                              String hash) throws IOException, IncorrectHashException {
        final FileItemImpl fileItem = getFileItem(fileId);
        final File packetFile = getPacketFile(fileId, packetNumber);
        packetFile.createNewFile();

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        final DigestInputStream digestInputStream = new DigestInputStream(inputStream, md);

        copy(digestInputStream, new FileOutputStream(packetFile), true); // todo check copied size

        byte[] digest = md.digest();
        final String calculatedHash = printHexBinary(digest);

        if (!hash.equalsIgnoreCase(calculatedHash)) {
            LOGGER.warn(
                "Wrong hash code for {}, packet {}. Expected {}, but was {}",
                fileId, packetNumber, calculatedHash, hash
            );
            throw new IncorrectHashException();
        }

        LOGGER.info(
            "Read packet {} of file {} with size {}",
            packetNumber, fileItem.fileName, packetFile.length()
        );
    }

    @Override
    public void finishUpload(UUID fileId) throws IOException {
        final FileItemImpl fileItem = getFileItem(fileId);
        final File ready = getResultFile(fileId);
        ready.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(ready)) {
            for (int i = FIRST_PACKET_INDEX; i < getPacketsCount(fileItem); i++) {
                final File packetFile = getPacketFile(fileId, i);

                try (FileInputStream inputStream = new FileInputStream(packetFile);) {
                    copy(inputStream, outputStream, false);
                }

                packetFile.delete();
            }
        }
    }

    @Override
    public FileDetails getFileDetails(UUID fileId) throws IOException {
        final FileItemImpl fileItem = getFileItem(fileId);

        return new FileDetails(fileItem.fileName, fileItem.totalSize);
    }

    @Override
    public InputStream getInputStream(UUID fileId) {
        final File resultFile = getFile(fileId);
        try {
            return new FileInputStream(resultFile);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File is not ready " + fileId, e);
        }
    }

    @Override
    public File getFile(UUID fileId) {
        return getResultFile(fileId);
    }

    private File getFileDir(UUID fileId) {
        return new File(uploadPath, fileId.toString());
    }

    private File getPacketFile(UUID fileId, int packetNumber) {
        final File fileDir = getFileDir(fileId);

        return new File(fileDir, "packet" + packetNumber);
    }

    private FileItemImpl getFileItem(UUID fileId) throws IOException {
        return new FileItemImpl(getFileDir(fileId));
    }

    private File getResultFile(UUID fileId) {
        File fileDir = getFileDir(fileId);
        return new File(fileDir, "ready");
    }

    private int getPacketsCount(FileItemImpl fileItem) {
        return (int) Math.ceil((double) fileItem.totalSize / PACKET_SIZE);
    }

    private class FileItemImpl {
        private final String fileName;
        private final long totalSize;

        public FileItemImpl(String fileName, long totalSize) {
            this.fileName = fileName;
            this.totalSize = totalSize;
        }

        public FileItemImpl(File dir) throws IOException {
            final File infoFile = getInfoFile(dir);

            String infoString;
            try {
                infoString = new LineNumberReader(new FileReader(infoFile)).readLine();
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("Info file does not exist " + infoFile, e);
            }

            final String[] fileInfoStrings = infoString.split(INFO_SEPARATOR);
            fileName = fileInfoStrings[0];
            totalSize = Long.parseLong(fileInfoStrings[1]);
        }

        void write(File dir) throws IOException {
            final File info = getInfoFile(dir);
            final boolean infoCreated = info.createNewFile();

            if (!infoCreated) {
                throw new IllegalStateException("Could not create info file " + info);
            }

            try (FileWriter infoWriter = new FileWriter(info)) {
                infoWriter.write(fileName + INFO_SEPARATOR + totalSize + INFO_SEPARATOR);
            }
        }

        private File getInfoFile(File dir) {
            return new File(dir, "info.csv");
        }
    }
}
