package com.infoclinika.mssharing.web.uploader;

import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.platform.web.uploader.AbstractStorageHelper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author andrii.loboda
 */
@Service
public class ExperimentAnnotationAttachmentUploadHelper extends AbstractStorageHelper {

    @Inject
    private StoredObjectPaths storedObjectPaths;

    public ExperimentAnnotationAttachmentUploadHelper() {
    }

    @Override
    protected FileData getData(long item, long userId) {
        return new FileData(
                null,
                storedObjectPaths.experimentAnnotationAttachmentPath(userId, item)
        );
    }
}
