package com.infoclinika.mssharing.autoimporter.service.util;

import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.service.api.ObservableList;
import com.infoclinika.mssharing.autoimporter.service.api.internal.ObserverList;
import com.infoclinika.mssharing.autoimporter.service.impl.NotificationType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.autoimporter.model.bean.ItemStatus.*;

/**
 * author Ruslan Duboveckij
 */
@Component
public class ZipUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ZipUtils.class);

    @Inject
    private Configuration configuration;

    public void zip(UploadItem uploadItem, ObservableList observableList) {

        final List<File> filesToZip = uploadItem.getFilesToZip();
        final ObserverList<UploadItem> observer = (ObserverList<UploadItem>) observableList.getObserver();
        final String folder = observableList.getWatchFolder();

        final ZipObject zipObject = new ZipObject(
            uploadItem.getFile().getAbsolutePath(),
            filesToZip,
            observer,
            folder
        );

        zipObject.zip(uploadItem);


    }

    public UploadItem zip(ObservableList observableList, String name, File file) {

        if (file.isDirectory()) {

            return zip(observableList, name, newArrayList(file.listFiles()));

        } else {

            return zip(observableList, name, newArrayList(file));

        }

    }

    public UploadItem zip(ObservableList observableList, String name, Collection<File> files) {

        LOG.info("Zipping {}. Files: {}", name, files);

        final String outputFile =
            configuration.getZipFolderPath() + File.separator + name + ".zip";

        LOG.info("Output file: {}", outputFile);

        final UploadItem uploadItem = new UploadItem(new File(outputFile));

        final List<File> filesToZip = newArrayList(files);
        final String folder = observableList.getWatchFolder();

        final ZipObject zipObject = new ZipObject(
            outputFile,
            filesToZip,
            observableList.getObserver(),
            folder
        );

        return zipObject.zip(uploadItem);
    }

    private static class ZipObject {
        private final String outputFile;
        private final String inputFolder;
        private final List<String> files = newArrayList();
        private final ObserverList<UploadItem> observer;
        private final String watchFolder;

        public ZipObject(String outputFile,
                         List<File> inputFiles,
                         ObserverList<UploadItem> observerUploadList,
                         String watchFolder) {

            this.outputFile = outputFile;
            this.observer = observerUploadList;
            this.watchFolder = watchFolder;
            this.inputFolder = inputFiles.get(0).getParent();

            for (final File file : inputFiles) {
                generateFileList(file);
            }
        }

        /**
         * Traverse a directory and get all files,
         * and add the file into files
         *
         * @param node file or directory
         */
        private void generateFileList(File node) {
            if (node.isFile()) {
                files.add(generateZipEntry(node.getAbsoluteFile().toString()));
            } else if (node.isDirectory()) {
                String[] subNote = node.list();
                for (String filename : subNote) {
                    generateFileList(new File(node, filename));
                }
            }
        }

        /**
         * Format the file path for zip
         *
         * @param file file path
         * @return Formatted file path
         */
        private String generateZipEntry(String file) {
            return file.substring(inputFolder.length() + 1, file.length());
        }

        /**
         * Warning - zipping error if input file from other folder
         */
        public UploadItem zip(UploadItem uploadItem) {

            final byte[] buffer = new byte[1024];
            FileOutputStream fos = null;

            if (files.isEmpty()) {

                LOG.error("Folder {} is empty", inputFolder);

                uploadItem.setStatus(ERROR);
                observer.notify(NotificationType.STATUS_VALUE, watchFolder, uploadItem);

            } else {

                uploadItem.setStatus(ZIPPING);
                observer.notify(NotificationType.STATUS_VALUE, watchFolder, uploadItem);

                try {

                    fos = new FileOutputStream(outputFile);

                    final ZipOutputStream zos = new ZipOutputStream(fos);
                    int current = 0;

                    LOG.info("Output to Zip :{} ", outputFile);

                    for (String file : files) {

                        LOG.info("File added in {} : {}", outputFile, file);

                        zos.putNextEntry(new ZipEntry(file));

                        final FileInputStream in = new FileInputStream(inputFolder + File.separator + file);

                        int len;

                        while ((len = in.read(buffer)) > 0) {

                            if (uploadItem.isCanceled()) {
                                IOUtils.closeQuietly(fos);
                                return uploadItem;
                            }

                            uploadItem.setZippedValue(current);

                            observer.notify(NotificationType.ZIP_VALUE, watchFolder, uploadItem);

                            current += len;

                            zos.write(buffer, 0, len);
                        }

                        in.close();
                    }

                    zos.closeEntry();
                    zos.close();

                    LOG.info("Zipping is done: {}", outputFile);

                } catch (IOException ex) {

                    LOG.error(ex.getMessage());

                    uploadItem.setStatus(ERROR);

                    observer.notify(NotificationType.STATUS_VALUE, watchFolder, uploadItem);

                    IOUtils.closeQuietly(fos);

                    new File(outputFile).delete();

                    return uploadItem;

                } finally {
                    IOUtils.closeQuietly(fos);
                }
            }

            long size = FileUtils.sizeOf(uploadItem.getFile());

            uploadItem.setZippedValue(uploadItem.getZipSize());
            observer.notify(NotificationType.ZIP_VALUE, watchFolder, uploadItem);

            uploadItem.setSize(size);
            observer.notify(NotificationType.SIZE_VALUE, watchFolder, uploadItem);

            uploadItem.setStatus(WAITING);
            observer.notify(NotificationType.STATUS_VALUE, watchFolder, uploadItem);

            return uploadItem;

        }
    }
}
