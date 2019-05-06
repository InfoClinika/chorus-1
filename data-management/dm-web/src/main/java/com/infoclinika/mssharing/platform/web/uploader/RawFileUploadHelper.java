package com.infoclinika.mssharing.platform.web.uploader;

import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.StoredObjectPathsTemplate;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.fileserver.model.StoredFile;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.FileUploadManagementTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Herman Zamula
 */
@Service
public class RawFileUploadHelper extends AbstractStorageHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RawFileUploadHelper.class);

    @Inject
    private StoredObjectPathsTemplate storedObjectPaths;

    @Inject
    private DetailsReaderTemplate detailsReader;

    @Inject
    private StorageService fileStorageService;

    @Inject
    private FileUploadManagementTemplate fileUploadManagement;

    @Override
    public void feedContentToStorage(long fileItemId, long userId, StoredFile storedFile) {
        final DetailsReaderTemplate.FileItemTemplate itemTemplate = detailsReader.readFile(userId, fileItemId);
        final NodePath nodePath = storedObjectPaths.rawFilePath(userId, itemTemplate.instrumentId, itemTemplate.name);

        LOGGER.debug(
            "Start project attachment saving. File item ID = {}; user ID = {}. Path = {}",
            fileItemId, userId, nodePath
        );

        fileStorageService.put(nodePath, storedFile);

        fileUploadManagement.completeMultipartUpload(userId, fileItemId, nodePath.getPath());

        LOGGER.debug(
            "Project attachment saved successfully. File item ID = {}; user ID = {}. Path = {}",
            fileItemId, userId, nodePath
        );
    }

    @Override
    protected FileData getData(long item, long userId) {
        final DetailsReaderTemplate.FileItemTemplate file = detailsReader.readFile(userId, item);

        return new FileData(file.name, new NodePath(file.bucket, file.contentId));
    }
}
