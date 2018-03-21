package com.infoclinika.mssharing.upload.common.web.test.util;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author timofey.kasyanov
 *         20.03.14.
 */
public class FileCreator {
    private static final String TEMP = "temp";
    private static final String RAW = ".raw";
    private static File Temp_Dir = Files.createTempDir();

    protected FileCreator() {
        throw new RuntimeException("This class is util");
    }

    public static File getTempDir() {
        if (!Temp_Dir.exists()) {
            if (!Temp_Dir.mkdir()) {
                throw new RuntimeException("Error - created Temp dir = " + Temp_Dir);
            }
        }
        return Temp_Dir;
    }

    public static void deleteTempDir() throws IOException {
        if (Temp_Dir.exists()) {
            FileUtils.deleteDirectory(Temp_Dir);
        }
    }

    public static File createTempFile() throws IOException {
        return genData(File.createTempFile(TEMP, RAW, getTempDir()));
    }

    public static File createTempFile(File folder) throws IOException {
        return genData(File.createTempFile(TEMP, RAW, folder));
    }

    public static File createFile(String name) throws IOException {
        return createFile(getTempDir(), name);
    }

    public static File createFile(File folder, String name) throws IOException {
        File file = new File(folder, name);
        if (file.exists()) {
            return file;
        } else {
            if (!file.createNewFile()) {
                throw new RuntimeException("Test file isn't created");
            }
            return genData(file);
        }
    }

    public static File createFolder(String name) throws IOException {
        return createFolder(getTempDir(), name);
    }

    public static File createFolder(File folder, String name) throws IOException {
        File file = new File(folder, name);
        if (file.exists()) {
            return file;
        } else {
            if (!file.mkdirs()) {
                throw new RuntimeException("Test folder isn't created");
            }
            return file;
        }
    }

    public static File createTempFolder(String suffix) throws IOException {
        final String folderName = String.valueOf(System.nanoTime());
        return createFolder(getTempDir(), folderName + suffix);
    }

    private static File genData(File file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        String value = String.valueOf(Integer.MAX_VALUE);
        StringBuilder stringBuilder = new StringBuilder(value);
        int size = (int) (Math.random() * 9 + 1) * 1000;
        for (int i = 1; i < size; i++) {
            stringBuilder.append(value);
        }
        bw.write(stringBuilder.toString());
        bw.close();
        return file;
    }

}
