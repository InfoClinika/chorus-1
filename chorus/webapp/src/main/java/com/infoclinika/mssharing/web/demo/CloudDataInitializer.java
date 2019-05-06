package com.infoclinika.mssharing.web.demo;

import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.analysis.storage.cloud.CloudStorageItemReference.CLOUD_REFERENCE_URL_SEPARATOR;

/**
 * @author Vladislav Kovchug
 */
@Service
public class CloudDataInitializer {
    private static final List<String> FOLDERS_TO_COPY = newArrayList(
        "fasta-dbs",
        "raw-files"
    );

    private static final String MAIL_FOLDER_NAME = "mail";
    private static final String CLASSPATH_PREFIX = "classpath*:";
    private static final int COPY_THREADS_NUMBER = 30;


    private static final Logger LOGGER = LoggerFactory.getLogger(CloudDataInitializer.class);
    private static final int NUMBER_OF_UPLOADS_TO_SHOW_PROGRESS = 10;

    private final DemoDataPropertiesProvider propertiesProvider;
    private final CloudStorageClientsProvider cloudStorageClientsProvider;
    private final AmazonPropertiesProvider amazonPropertiesProvider;

    @Inject
    public CloudDataInitializer(
        DemoDataPropertiesProvider propertiesProvider,
        CloudStorageClientsProvider cloudStorageClientsProvider,
        AmazonPropertiesProvider amazonPropertiesProvider
    ) {
        this.propertiesProvider = propertiesProvider;
        this.cloudStorageClientsProvider = cloudStorageClientsProvider;
        this.amazonPropertiesProvider = amazonPropertiesProvider;
    }

    public void initializeCloudData() {
        LOGGER.info(
            "Copy cloud data from template bucket={} to active bucket={}",
            propertiesProvider.getTemplatesBucket(),
            amazonPropertiesProvider.getActiveBucket()
        );
        final CloudStorageService storageService = cloudStorageClientsProvider.getCloudStorageService();

        LOGGER.info("Read list of images from: {}", propertiesProvider.getMailingImageTemplatesLocation());
        final File tempFolder = Files.createTempDir();
        final List<File> resourcesFromFolder =
            getResourcesFromFolder(propertiesProvider.getMailingImageTemplatesLocation(), tempFolder);
        LOGGER.info("Uploading found images to {} folder. Images: {}", MAIL_FOLDER_NAME, resourcesFromFolder);
        resourcesFromFolder.forEach(image -> {
            storageService.uploadToCloud(
                image,
                amazonPropertiesProvider.getActiveBucket(),
                MAIL_FOLDER_NAME + CLOUD_REFERENCE_URL_SEPARATOR + image.getName()
            );
        });
        deleteTempFolder(tempFolder);


        FOLDERS_TO_COPY.forEach(folder -> {
            LOGGER.info("Copy {} folder", folder);

            final List<CloudStorageItemReference> files =
                storageService.list(propertiesProvider.getTemplatesBucket(), folder, Optional.absent());
            LOGGER.info("Found: {} files: {}", files.size(), files);

            LOGGER.info("Coping found files...");
            final long copyBegin = System.currentTimeMillis();
            copyFilesInThreadPool(files, amazonPropertiesProvider.getActiveBucket(), storageService);
            LOGGER.info(
                "Copy of {} files completed in {} (millis)",
                files.size(),
                System.currentTimeMillis() - copyBegin
            );
        });


    }

    private void copyFilesInThreadPool(
        List<CloudStorageItemReference> files, String destBucket,
        CloudStorageService storageService
    ) {
        final ExecutorService executorService = Executors.newFixedThreadPool(COPY_THREADS_NUMBER);
        final AtomicInteger atomicInteger = new AtomicInteger();

        List<Callable<Void>> tasks = new ArrayList<>(files.size());
        files.forEach(fileRef -> {
            tasks.add(() -> {
                final CloudStorageItemReference destFileRef =
                    new CloudStorageItemReference(destBucket, fileRef.getKey());
                if (!storageService.existsAtCloud(destFileRef)) {
                    storageService.copy(fileRef, destFileRef);
                }

                final int fileNumber = atomicInteger.incrementAndGet();
                if (fileNumber % NUMBER_OF_UPLOADS_TO_SHOW_PROGRESS == 0 || fileNumber == files.size()) {
                    LOGGER.info("Copying progress: {}/{}", fileNumber, files.size());
                }
                return null;
            });
        });

        try {
            final List<Future<Void>> futures = executorService.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get();
            }

            executorService.shutdown();
        } catch (Exception e) {
            final String erroeMessage = "Can't copy files from template bucket.";
            LOGGER.error(erroeMessage, e);
            throw new RuntimeException(erroeMessage, e);
        }
    }

    private List<File> getResourcesFromFolder(String folderPath, File tempFolder) {
        final ArrayList<File> result = new ArrayList<>();
        final String formattedPath;
        if (folderPath.endsWith("/")) {
            formattedPath = CLASSPATH_PREFIX + folderPath + "*";
        } else {
            formattedPath = CLASSPATH_PREFIX + folderPath + "/*";
        }

        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
            final Resource[] resources = resolver.getResources(formattedPath);

            for (Resource resource : resources) {
                final File tmpFile = new File(tempFolder, resource.getFilename());
                FileUtils.copyInputStreamToFile(resource.getInputStream(), tmpFile);
                result.add(tmpFile);
            }
        } catch (IOException e) {
            final String message = "Can't read resources list from path: " + formattedPath;
            LOGGER.error(message);
            throw new RuntimeException(message, e);
        }


        return result;
    }

    private void deleteTempFolder(File tempFolder) {
        try {
            FileUtils.deleteDirectory(tempFolder);
        } catch (IOException e) {
            LOGGER.error("Can't Delete folder", e);
        }
    }

}
