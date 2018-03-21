package com.infoclinika.mssharing.model.test.write;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.helper.items.ChorusFileData;
import com.infoclinika.mssharing.model.internal.entity.FileAccessLog;
import com.infoclinika.mssharing.model.read.FileAccessLogReader;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.model.write.FileAccessLogService;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.read.AccessLevel;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Iterator;

import static com.google.common.base.Optional.fromNullable;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author vladislav.kovchug
 */
public class FileAccessLogTest extends AbstractTest {

    @Inject
    FileAccessLogService fileAccessLogService;

    @Inject
    FileAccessLogReader fileAccessLogReader;

    @Test
    public void test_upload_start_log() throws InterruptedException {
        final long bob = uc.createLab3AndBob();
        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(bob);
        final FileItem fileDetails = getUserAnyFileDetails(bob);

        fileAccessLogService.logFileUploadStart(bob, fileDetails.instrumentId, fileDetails.id);
        Thread.sleep(1000);
        final Optional<FileAccessLogReader.FileAccessLogDTO> lastLog = readLastLog();

        if (lastLog.isPresent()) {
            assertEquals(lastLog.get().operationType, FileAccessLog.OperationType.FILE_UPLOAD_STARTED.toString());
            assertEquals(lastLog.get().fileName, fileDetails.name);
            assertEquals(lastLog.get().userEmail, personInfo.email);
            assertEquals(lastLog.get().userLabName, fileDetails.labName);
        } else {
            fail("No logs added.");
        }
    }

    @Test
    public void test_upload_confirm_log() throws InterruptedException {
        final long bob = uc.createLab3AndBob();
        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(bob);
        final FileItem fileDetails = getUserAnyFileDetails(bob);

        fileAccessLogService.logFileUploadConfirm(bob, uc.getLab3(), fileDetails.id);
        Thread.sleep(1000);
        final Optional<FileAccessLogReader.FileAccessLogDTO> lastLog = readLastLog();

        if (lastLog.isPresent()) {
            assertEquals(lastLog.get().operationType, FileAccessLog.OperationType.FILE_UPLOAD_CONFIRMED.toString());
            assertEquals(lastLog.get().fileName, fileDetails.name);
            assertEquals(lastLog.get().userEmail, personInfo.email);
            assertEquals(lastLog.get().userLabName, fileDetails.labName);
        } else {
            fail("No logs added.");
        }
    }


    @Test
    public void test_file_delete_log() throws InterruptedException {
        final long bob = uc.createLab3AndBob();
        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(bob);
        final FileItem fileDetails = getUserAnyFileDetails(bob);
        final long deletedFile = instrumentManagement.moveFileToTrash(bob, fileDetails.id);

        fileAccessLogService.logFileDelete(deletedFile);
        Thread.sleep(1000);
        final Optional<FileAccessLogReader.FileAccessLogDTO> lastLog = readLastLog();

        if (lastLog.isPresent()) {
            assertEquals(lastLog.get().operationType, FileAccessLog.OperationType.FILE_DELETED.toString());
            assertEquals(lastLog.get().fileName, fileDetails.name);
            assertEquals(lastLog.get().userEmail, personInfo.email);
            assertEquals(lastLog.get().userLabName, fileDetails.labName);
        } else {
            fail("No logs added.");
        }
    }

    @Test
    public void test_file_delete_permanently_log() throws InterruptedException {
        final long bob = uc.createLab3AndBob();
        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(bob);
        final FileItem fileDetails = getUserAnyFileDetails(bob);

        fileAccessLogService.logFileDeletePermanently(bob, fileDetails.id);
        Thread.sleep(1000);
        final Optional<FileAccessLogReader.FileAccessLogDTO> lastLog = readLastLog();

        if (lastLog.isPresent()) {
            assertEquals(lastLog.get().operationType, FileAccessLog.OperationType.FILE_DELETED_PERMANENTLY.toString());
            assertEquals(lastLog.get().fileName, fileDetails.name);
            assertEquals(lastLog.get().userEmail, personInfo.email);
            assertEquals(lastLog.get().userLabName, fileDetails.labName);
        } else {
            fail("No logs added.");
        }
    }

    @Test
    public void test_file_archive_start_log() throws InterruptedException {
        final long bob = uc.createLab3AndBob();
        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(bob);
        final FileItem fileDetails = getUserAnyFileDetails(bob);

        fileAccessLogService.logFileArchiveStart(bob, fileDetails.id);
        Thread.sleep(1000);
        final Optional<FileAccessLogReader.FileAccessLogDTO> lastLog = readLastLog();

        if (lastLog.isPresent()) {
            assertEquals(lastLog.get().operationType, FileAccessLog.OperationType.FILE_ARCHIVE_STARTED.toString());
            assertEquals(lastLog.get().fileName, fileDetails.name);
            assertEquals(lastLog.get().userEmail, personInfo.email);
            assertEquals(lastLog.get().userLabName, fileDetails.labName);
        } else {
            fail("No logs added.");
        }
    }

    @Test
    public void test_file_archive_confirmed_log() throws InterruptedException {
        final long bob = uc.createLab3AndBob();
        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(bob);
        final FileItem fileDetails = getUserAnyFileDetails(bob);

        fileAccessLogService.logFileArchiveConfirm(fileDetails.id);
        Thread.sleep(1000);
        final Optional<FileAccessLogReader.FileAccessLogDTO> lastLog = readLastLog();

        if (lastLog.isPresent()) {
            assertEquals(lastLog.get().operationType, FileAccessLog.OperationType.FILE_ARCHIVE_CONFIRMED.toString());
            assertEquals(lastLog.get().fileName, fileDetails.name);
            assertEquals(lastLog.get().userEmail, personInfo.email);
            assertEquals(lastLog.get().userLabName, fileDetails.labName);
        } else {
            fail("No logs added.");
        }
    }

    @Test
    public void test_file_download_log() throws InterruptedException {
        final long bob = uc.createLab3AndBob();
        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(bob);
        final FileItem fileDetails = getUserAnyFileDetails(bob);

        fileAccessLogService.logFileDownload(bob, new ChorusFileData(fileDetails.contentId, fileDetails.archiveId, fileDetails.name, false, null,
                uc.getLab3(), fileDetails.id,
                fromNullable(null),
                AccessLevel.PUBLIC));

        Thread.sleep(1000);
        final Optional<FileAccessLogReader.FileAccessLogDTO> lastLog = readLastLog();

        if (lastLog.isPresent()) {
            assertEquals(lastLog.get().operationType, FileAccessLog.OperationType.FILE_DOWNLOAD_STARTED.toString());
            assertEquals(lastLog.get().fileName, fileDetails.name);
            assertEquals(lastLog.get().userEmail, personInfo.email);
            assertEquals(lastLog.get().userLabName, fileDetails.labName);
        } else {
            fail("No logs added.");
        }
    }

    private FileItem getUserAnyFileDetails(long userId) {
        final com.infoclinika.mssharing.model.write.FileItem fileItem = anyFile(userId).get(0);
        return detailsReader.readFile(userId, fileItem.id);
    }

    private Optional<FileAccessLogReader.FileAccessLogDTO> readLastLog() {
        final PagedItem<FileAccessLogReader.FileAccessLogDTO> logs =
                fileAccessLogReader.readLogs(admin(), new PagedItemInfo(1, 0, "id", false, ""));

        final Iterator<FileAccessLogReader.FileAccessLogDTO> iterator = logs.iterator();
        FileAccessLogReader.FileAccessLogDTO lastLog = null;
        while (iterator.hasNext()) {
            lastLog = iterator.next();
        }
        return Optional.fromNullable(lastLog);
    }


}
