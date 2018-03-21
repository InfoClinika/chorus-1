package com.infoclinika.mssharing.model.test.write;

import com.infoclinika.analysis.storage.cloud.CloudStorageFactory;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.analysis.storage.cloud.CloudStorageService;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.write.ArchiveSynchronizationManagement;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;

import static junit.framework.Assert.assertTrue;

/**
 * @author yevhen.panko
 */
public class ArchiveSynchronizationManagementTest extends AbstractTest {
    private String[] FILES = new String[]{
            "raw-files/synchronisation/500f_01.RAW",
            "raw-files/synchronisation/500f_02.RAW",
            "raw-files/synchronisation/500f_03.RAW",
            "raw-files/synchronisation/50f_01.RAW",
            "raw-files/synchronisation/50_02.RAW",
            "raw-files/synchronisation/50_03.RAW"
    };

    private static final String ACTIVE_BUCKET = "chorus-unit-tests";
    private static final String ARCHIVE_BUCKET = "chorus-unit-tests-archive";

    @Inject
    private ArchiveSynchronizationManagement archiveSynchronizationManagement;

    @Inject
    private FileMetaDataRepository fileMetaDataRepository;


    public void initWorkflow() {
        final CloudStorageService cloudStorageService = CloudStorageFactory.service();

        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();

        for (String file : FILES) {
            final CloudStorageItemReference copy = new CloudStorageItemReference(ARCHIVE_BUCKET, file);
            cloudStorageService.copy(
                    new CloudStorageItemReference(ACTIVE_BUCKET, file),
                    copy
            );

            long fileId = uc.saveFileWithName(bob, instrument, copy.extractFilename());
            final ActiveFileMetaData fileMetaData = fileMetaDataRepository.findOne(fileId);
            fileMetaData.setContentId(copy.getKey());

            fileMetaDataRepository.save(fileMetaData);
        }
    }

    @Test(enabled = false)
    public void testSynchronizeS3StateWithDB() {
        initWorkflow();

        archiveSynchronizationManagement.synchronizeS3StateWithDB(
                ACTIVE_BUCKET,
                ARCHIVE_BUCKET
        );

        final CloudStorageService cloudStorageService = CloudStorageFactory.service();
        final List<ActiveFileMetaData> files = fileMetaDataRepository.findAll();

        for (ActiveFileMetaData file : files) {
            assertTrue(!cloudStorageService.existsAtCloud(new CloudStorageItemReference(ARCHIVE_BUCKET, file.getContentId())));
        }
    }
}
