package com.infoclinika.mssharing.platform.web.uploader;


import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.logging.Logger;

import static org.apache.commons.fileupload.util.Streams.copy;

/**
 * @author Pavel Kaplin
 */
public class FileSystemFileStorage implements FileStorage {

    public static final String INFO_SEPARATOR = ",";
    private static final Logger log = Logger.getLogger(FileSystemFileStorage.class.getName());
    private final File uploadPath;

    public FileSystemFileStorage(File uploadPath) {
        this.uploadPath = uploadPath;
    }

    @Override
    public void newUpload(String fileName, long totalSize, UUID fileId) throws IOException {
        File dir = getFileDir(fileId);
        if (dir.exists()) {
            throw new IllegalArgumentException("File with id " + fileId + " already exists");
        }
        boolean created = dir.mkdirs();
        if (!created) {
            throw new IllegalStateException("Could not create dir " + dir);
        }
        new FileItemImpl(fileName, totalSize).write(dir);
    }

    private File getFileDir(UUID fileId) {
        return new File(uploadPath, fileId.toString());
    }

    @Override
    public void receivePacket(UUID fileId, int packetNumber, InputStream inputStream, String hash) throws IOException, IncorrectHashException {
        FileItemImpl fileItem = getFileItem(fileId);
        File packetFile = getPacketFile(fileId, packetNumber);
        packetFile.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(packetFile);
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        DigestInputStream digestInputStream = new DigestInputStream(inputStream, md);
        copy(digestInputStream, outputStream, true); // todo check copied size
        byte[] digest = md.digest();
        String calculatedHash = DatatypeConverter.printHexBinary(digest);
        if (!hash.equalsIgnoreCase(calculatedHash)) {
            log.warning("Wrong hash code for " + fileId + ", packet " + packetNumber +
                    ". Expected " + calculatedHash + ", but was " + hash);
            throw new IncorrectHashException();
        }
        log.info("Read packet " + packetNumber + " of file " + fileItem.fileName + " with size " + packetFile.length());
    }

    private File getPacketFile(UUID fileId, int packetNumber) {
        File fileDir = getFileDir(fileId);
        return new File(fileDir, "packet" + packetNumber);
    }

    private FileItemImpl getFileItem(UUID fileId) throws IOException {
        return new FileItemImpl(getFileDir(fileId));
    }

    @Override
    public void finishUpload(UUID fileId) throws IOException {
        FileItemImpl fileItem = getFileItem(fileId);
        File ready = getResultFile(fileId);
        ready.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(ready);

        for (int i = FIRST_PACKET_INDEX; i < getPacketsCount(fileItem); i++) {
            File packetFile = getPacketFile(fileId, i);
            FileInputStream inputStream = new FileInputStream(packetFile);
            copy(inputStream, outputStream, false); //todo check copied size
            packetFile.delete();
        }
        outputStream.close();
    }

    private File getResultFile(UUID fileId) {
        File fileDir = getFileDir(fileId);
        return new File(fileDir, "ready");
    }

    private int getPacketsCount(FileItemImpl fileItem) {
        return (int) Math.ceil((double) fileItem.totalSize / PACKET_SIZE);
    }

    @Override
    public FileDetails getFileDetails(UUID fileId) throws IOException {
        FileItemImpl fileItem = getFileItem(fileId);
        return new FileDetails(fileItem.fileName, fileItem.totalSize);
    }

    @Override
    public InputStream getInputStream(UUID fileId) {
        File resultFile = getFile(fileId);
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

    private class FileItemImpl {
        private final String fileName;
        private final long totalSize;

        public FileItemImpl(String fileName, long totalSize) {
            this.fileName = fileName;
            this.totalSize = totalSize;
        }

        public FileItemImpl(File dir) throws IOException {
            File infoFile = getInfoFile(dir);
            String infoString;
            try {
                infoString = new LineNumberReader(new FileReader(infoFile)).readLine();
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("Info file does not exist " + infoFile, e);
            }
            String[] fileInfoStrings = infoString.split(INFO_SEPARATOR);
            fileName = fileInfoStrings[0];
            totalSize = Long.parseLong(fileInfoStrings[1]);
        }

        void write(File dir) throws IOException {
            File info = getInfoFile(dir);
            boolean infoCreated = info.createNewFile();
            if (!infoCreated) {
                throw new IllegalStateException("Could not create info file " + info);
            }
            FileWriter infoWriter = new FileWriter(info);
            infoWriter.write(fileName + INFO_SEPARATOR + totalSize + INFO_SEPARATOR);
            infoWriter.close();
        }

        private File getInfoFile(File dir) {
            return new File(dir, "info.csv");
        }
    }
}
