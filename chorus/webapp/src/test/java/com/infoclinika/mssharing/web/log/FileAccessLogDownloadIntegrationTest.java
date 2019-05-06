package com.infoclinika.mssharing.web.log;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.internal.entity.FileAccessLog;
import com.infoclinika.mssharing.model.read.FileAccessLogReader;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.UserReader;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.web.demo.DemoDataBasedTest;
import com.infoclinika.mssharing.web.downloader.ChorusDownloadData;
import com.infoclinika.mssharing.web.downloader.ChorusSingleFileDownloadHelper;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

/**
 * @author vladislav.kovchug
 */
public class FileAccessLogDownloadIntegrationTest extends DemoDataBasedTest {

    @Inject
    private FileAccessLogReader fileAccessLogReader;

    @Inject
    private UserReader userReader;

    @Inject
    private ChorusSingleFileDownloadHelper chorusSingleFileDownloadHelper;

    @Inject
    private SecurityHelper securityHelper;

    @Test(enabled = false, description = "test was disabled for open-chorus")
    public void test_file_download_logging() throws InterruptedException {

        final Long demoUser = demoUser();

        final SecurityHelper.UserDetails admin =
            securityHelper.getUserDetailsByEmail(demoDataPropertiesProvider.getAdminEmail());

        UserManagementTemplate.PersonInfo personInfo = userReader.readPersonInfo(demoUser);
        final Set<FileLine> fileLines = dashboardReader.readFiles(demoUser, Filter.ALL);
        final Set<FileLine> sortedFilesById = new TreeSet<>(Comparator.comparingLong(o -> o.id));
        sortedFilesById.addAll(fileLines);

        final FileLine file = sortedFilesById.iterator().next();

        chorusSingleFileDownloadHelper.getDownloadUrl(demoUser, new ChorusDownloadData(file.id, file.labId));
        Thread.sleep(1000);
        final Optional<FileAccessLogReader.FileAccessLogDTO> lastLogEntry = readLastLog(admin.id);
        if (lastLogEntry.isPresent()) {
            FileAccessLogReader.FileAccessLogDTO lastLog = lastLogEntry.get();

            assertEquals(lastLog.operationType, FileAccessLog.OperationType.FILE_DOWNLOAD_STARTED.toString());
            assertEquals(lastLog.fileName, file.name);
            assertEquals(lastLog.userEmail, personInfo.email);
            assertEquals(lastLog.userLabName, file.labName);
            assertEquals(lastLog.fileContentId, file.contentId);
            assertEquals(lastLog.fileArchiveId, file.archiveId);
        } else {
            fail("No logs recorded.");
        }
    }

    private Optional<FileAccessLogReader.FileAccessLogDTO> readLastLog(long actor) {
        final PagedItem<FileAccessLogReader.FileAccessLogDTO> logs =
            fileAccessLogReader.readLogs(actor, new PagedItemInfo(1, 0, "id", false, ""));

        final Iterator<FileAccessLogReader.FileAccessLogDTO> iterator = logs.iterator();
        FileAccessLogReader.FileAccessLogDTO lastLog = null;
        while (iterator.hasNext()) {
            lastLog = iterator.next();
        }
        return Optional.fromNullable(lastLog);
    }

}
