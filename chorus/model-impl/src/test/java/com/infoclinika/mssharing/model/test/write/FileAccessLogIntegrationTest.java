package com.infoclinika.mssharing.model.test.write;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.internal.entity.FileAccessLog;
import com.infoclinika.mssharing.model.read.FileAccessLogReader;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.model.write.FileMetaDataInfo;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author vladislav.kovchug
 */


public class FileAccessLogIntegrationTest extends AbstractTest {

    @Inject
    private FileAccessLogReader fileAccessLogReader;

    private FileLine createAndReadFile(long userId){
        final long instrument = uc.createInstrumentAndApproveIfNeeded(userId, uc.getLab3()).get();
        long fileId = instrumentManagement.createFile(userId, instrument, new FileMetaDataInfo(generateString(), 0, "", null, anySpecies(), false));

        final Set<FileLine> fileLines = fileReader.readFilesByInstrument(userId, instrument);
        return fileLines.iterator().next();
    }

    @Test
    public void test_file_upload_start_logging() throws InterruptedException {
        final long bob = uc.createLab3AndBob();
        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(bob);
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();

        instrumentManagement.startUploadFile(bob, instrument, new FileMetaDataInfo(generateString(), 0, "", null, anySpecies(), false));
        final Set<FileLine> fileLines = fileReader.readFilesByInstrument(bob, instrument);
        final FileLine file = fileLines.iterator().next();

        Thread.sleep(1000);
        final Optional<FileAccessLogReader.FileAccessLogDTO> lastLog = readLastLog();

        if (lastLog.isPresent()) {
            assertEquals(lastLog.get().operationType, FileAccessLog.OperationType.FILE_UPLOAD_STARTED.toString());
            assertEquals(lastLog.get().fileName, file.name);
            assertEquals(lastLog.get().userEmail, personInfo.email);
            assertEquals(lastLog.get().userLabName, file.labName);
        } else {
            fail("No logs recorded.");
        }
    }

    @Test(enabled = false)
    public void test_file_upload_complete_logging() throws InterruptedException {
        final long bob = uc.createLab3AndBob();
        setFeaturePerLab(ApplicationFeature.TRANSLATION, Lists.newArrayList(uc.getLab3()));
        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(bob);
        final FileLine file = createAndReadFile(bob);

        String uploadedFileContentId = "test";
        instrumentManagement.completeMultipartUpload(bob, file.id, uploadedFileContentId);

        Thread.sleep(1000);
        final Optional<FileAccessLogReader.FileAccessLogDTO> lastLog = readLastLog();

        if (lastLog.isPresent()) {
            assertEquals(lastLog.get().operationType, FileAccessLog.OperationType.FILE_UPLOAD_CONFIRMED.toString());
            assertEquals(lastLog.get().fileName, file.name);
            assertEquals(lastLog.get().userEmail, personInfo.email);
            assertEquals(lastLog.get().userLabName, file.labName);
            assertEquals(lastLog.get().fileContentId, uploadedFileContentId);
        } else {
            fail("No logs recorded.");
        }
    }

    @Test
    public void test_file_delete_logging() throws InterruptedException {
        final long bob = uc.createLab3AndBob();
        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(bob);

        final com.infoclinika.mssharing.model.write.FileItem fileItem = anyFile(bob).get(0);

        final FileItem fileDetails = detailsReader.readFile(bob, fileItem.id);
        final long deleted = instrumentManagement.moveFileToTrash(bob, fileItem.id);
        instrumentManagement.deleteFile(deleted);

        Thread.sleep(1000);
        final Optional<FileAccessLogReader.FileAccessLogDTO> lastLog = readLastLog();
        if(lastLog.isPresent()){
            assertEquals(lastLog.get().operationType, FileAccessLog.OperationType.FILE_DELETED.toString());
            assertEquals(lastLog.get().fileName, fileDetails.name);
            assertEquals(lastLog.get().userEmail, personInfo.email);
            assertEquals(lastLog.get().userLabName, fileDetails.labName);
            assertEquals(lastLog.get().fileContentId, fileDetails.contentId);
            assertEquals(lastLog.get().fileArchiveId, fileDetails.archiveId);
        } else {
            fail("No logs recorded.");
        }
    }

    @Test
    public void test_file_delete_permanently_logging() throws InterruptedException {
        final long bob = uc.createLab3AndBob();
        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(bob);

        final com.infoclinika.mssharing.model.write.FileItem fileItem = anyFile(bob).get(0);

        final FileItem fileDetails = detailsReader.readFile(bob, fileItem.id);
        instrumentManagement.removeFilesPermanently(bob, of(fileItem.id));

        Thread.sleep(1000);
        final Optional<FileAccessLogReader.FileAccessLogDTO> lastLog = readLastLog();
        if (lastLog.isPresent()) {
            assertEquals(lastLog.get().operationType, FileAccessLog.OperationType.FILE_DELETED_PERMANENTLY.toString());
            assertEquals(lastLog.get().fileName, fileDetails.name);
            assertEquals(lastLog.get().userEmail, personInfo.email);
            assertEquals(lastLog.get().userLabName, fileDetails.labName);
            assertEquals(lastLog.get().fileContentId, fileDetails.contentId);
            assertEquals(lastLog.get().fileArchiveId, fileDetails.archiveId);
        } else {
            fail("No logs in database.");
        }
    }

    @Test
    public void test_file_archive_start_logging() throws InterruptedException {
        setBilling(true);
        final long bob = uc.createLab3AndBob();
        billingManagement.makeLabAccountEnterprise(uc.createPaul(), uc.getLab3());
        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(bob);
        final FileLine file = createAndReadFile(bob);

        fileOperationsManager.markFilesToArchive(bob, of(file.id));

        Thread.sleep(1000);
        final Optional<FileAccessLogReader.FileAccessLogDTO> lastLog = readLastLog();

        if (lastLog.isPresent()) {
            assertEquals(lastLog.get().operationType, FileAccessLog.OperationType.FILE_ARCHIVE_STARTED.toString());
            assertEquals(lastLog.get().fileName, file.name);
            assertEquals(lastLog.get().userEmail, personInfo.email);
            assertEquals(lastLog.get().userLabName, file.labName);
            assertEquals(lastLog.get().fileContentId, file.contentId);
            assertEquals(lastLog.get().fileArchiveId, file.archiveId);
        } else {
            fail("No logs recorded.");
        }
    }

    @Test
    public void test_file_archive_completed_logging() throws InterruptedException {
        setBilling(true);
        final long bob = uc.createLab3AndBob();
        final com.infoclinika.mssharing.model.write.FileItem fileItem = anyFile(bob).get(0);
        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(bob);

        fileMovingManager.moveToArchiveStorage(fileItem.id);
        final FileItem fileDetails = detailsReader.readFile(bob, fileItem.id);

        Thread.sleep(1000);
        final Optional<FileAccessLogReader.FileAccessLogDTO> lastLog = readLastLog();

        if (lastLog.isPresent()) {
            assertEquals(lastLog.get().operationType, FileAccessLog.OperationType.FILE_ARCHIVE_CONFIRMED.toString());
            assertEquals(lastLog.get().fileName, fileDetails.name);
            assertEquals(lastLog.get().userEmail, personInfo.email);
            assertEquals(lastLog.get().userLabName, fileDetails.labName);
            assertEquals(lastLog.get().fileContentId, fileDetails.contentId);
            assertEquals(lastLog.get().fileArchiveId, fileDetails.archiveId);
        } else {
            fail("No logs recorded.");
        }
    }

    private Optional<FileAccessLogReader.FileAccessLogDTO> readLastLog(){
        final PagedItem<FileAccessLogReader.FileAccessLogDTO> logs =
                fileAccessLogReader.readLogs(admin(), new PagedItemInfo(1, 0, "id", false, ""));

        final Iterator<FileAccessLogReader.FileAccessLogDTO> iterator = logs.iterator();
        FileAccessLogReader.FileAccessLogDTO lastLog = null;
        while(iterator.hasNext()){
            lastLog = iterator.next();
        }
        return Optional.fromNullable(lastLog);
    }

}
