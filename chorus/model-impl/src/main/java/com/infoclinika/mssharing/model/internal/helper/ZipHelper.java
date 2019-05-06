package com.infoclinika.mssharing.model.internal.helper;

import com.google.common.base.Throwables;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author : Alexander Serebriyan
 */


public class ZipHelper {

    public static void zipDir(File contentToZip, File output) {
        final ZipOutputStream zipOutputStream;
        try (FileOutputStream fileOutputStream = new FileOutputStream(output.getAbsolutePath())) {
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            for (File file : contentToZip.listFiles()) {
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOutputStream.putNextEntry(zipEntry);
                final byte[] data = Files.readAllBytes(file.toPath());
                zipOutputStream.write(data, 0, data.length);
                zipOutputStream.closeEntry();
            }
            zipOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Throwables.propagate(e);
        }
    }
}
