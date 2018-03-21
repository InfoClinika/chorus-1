package com.infoclinika.mssharing.wizard.upload.service.impl;

import com.infoclinika.mssharing.wizard.upload.model.ZipConfig;
import com.infoclinika.mssharing.wizard.upload.model.ZipFileItem;
import com.infoclinika.mssharing.wizard.upload.service.api.ZipService;
import com.infoclinika.mssharing.wizard.upload.service.api.listener.ZipListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author timofey.kasyanov
 *         date:   29.01.14
 */
@Component
public class ZipServiceImpl implements ZipService {

    private final static Logger LOGGER = Logger.getLogger(ZipServiceImpl.class);

    @Override
    public void zip(ZipFileItem item, ZipConfig config, ZipListener listener) {

        LOGGER.info("Start zipping an item: " + item.getName());

        final byte[] buffer = new byte[1024];
        final List<File> files = item.getFilesToZip();
        final String outputFile = config.getZipFolderPath() + File.separator + item.getName();

        item.setResultFile(new File(outputFile));

        FileOutputStream fos = null;

        if (files.isEmpty()) {

            final String message = "No files to zip";

            LOGGER.error(message);

            item.setError(message);

            listener.onZipError(item);

            return;

        }

        try {

            fos = new FileOutputStream(outputFile);

            final ZipOutputStream zos = new ZipOutputStream(fos);

            listener.onZipStart(item);

            LOGGER.info("Output to Zip : " + outputFile);


            final List<ZipElement> zipElements = getZipElements(item.getFilesToZip());


            for (ZipElement element : zipElements) {

                final ZipEntry zipEntry = new ZipEntry(element.name);

                zos.putNextEntry(zipEntry);

                final FileInputStream fileInputStream = new FileInputStream(element.absolutePath);

                int length;

                while ((length = fileInputStream.read(buffer)) > 0) {

                    if(item.isCanceled()){

                        fileInputStream.close();
                        zos.closeEntry();
                        zos.close();

                        IOUtils.closeQuietly(fos);

                        listener.onZipError(item);

                        return;

                    }

                    listener.zipProgressChanged(item, length);

                    zos.write(buffer, 0, length);

                }

                fileInputStream.close();
            }

            zos.closeEntry();
            zos.close();

            LOGGER.info("Zipping is done: " + outputFile);

        } catch (IOException ex) {

            LOGGER.error(ex.getMessage());

            item.setError(ex.getMessage());

            listener.onZipError(item);

            IOUtils.closeQuietly(fos);

            FileUtils.deleteQuietly(new File(outputFile));

            return;

        } finally {

            IOUtils.closeQuietly(fos);

        }

        item.setFileSize(FileUtils.sizeOf(item.getResultFile()));

        listener.onZipComplete(item);

    }

    private List<ZipElement> getZipElements(List<File> filePathsToZip) throws FileNotFoundException {

        List<ZipElement> list = newArrayList();

        for (File file : filePathsToZip) {

            if (!file.exists()) {
                throw new FileNotFoundException();
            }

            if (file.isDirectory()) {

                fillZipElementsList(file, list, "");

            } else {

                final ZipElement zipElement = new ZipElement(
                        File.separator + file.getName(),
                        file.getAbsolutePath()
                );

                list.add(zipElement);

            }

        }
        return list;
    }

    private void fillZipElementsList(File directory, List<ZipElement> list, String prefix) {

        final String elementNamePrefix = prefix.length() > 0 ? prefix + File.separator : "";

        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (file.isFile()) {

                final ZipElement zipElement = new ZipElement(
                        elementNamePrefix + file.getName(),
                        file.getAbsolutePath()
                );

                list.add(zipElement);

            } else {

                fillZipElementsList(file, list, elementNamePrefix + file.getName());

            }
        }
    }

    private static class ZipElement {

        public final String name;
        public final String absolutePath;

        public ZipElement(String name, String absolutePath) {
            this.name = name;
            this.absolutePath = absolutePath;
        }
    }

}
