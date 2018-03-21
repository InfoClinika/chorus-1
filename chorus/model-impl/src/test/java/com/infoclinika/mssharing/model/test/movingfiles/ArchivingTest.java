package com.infoclinika.mssharing.model.test.movingfiles;

import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.model.GlacierDownloadListener;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.read.DownloadFileReader;
import org.mockito.Matchers;
import org.testng.annotations.Test;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * @author Elena Kurilina
 */


public class ArchivingTest extends AbstractTest {

    @Test
    public void testMoveToGlacierFormS3() {
        long bob = uc.createLab3AndBob();
        long id = uc.saveFile(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get());
        fileMovingManager.moveToArchiveStorage(id);
        DownloadFileReader.FileItemLocation location = downloadFileReader.readFileLocation(id);
        assertNull(location.contendId);
        assertNotNull(location.archiveId);
    }

    @Test
    public void testMoveToGlacierAndStartDownloadJob() {

        long bob = uc.createLab3AndBob();
        long fileId = uc.saveFile(bob);
        fileMovingManager.moveToArchiveStorage(fileId);
        fileMovingManager.requestFilesUnarchiving(newHashSet(fileId), bob);
        DownloadFileReader.DownloadFileJob job = downloadFileReader.readJobByFile(fileId);
        ImmutableList<DownloadFileReader.DownloadFileGroup> groups = downloadFileReader.readGroupByJob(job.id);
        assertEquals(job.fileMetaData.longValue(), fileId);
        assertTrue(groups.size() == 1);
        assertTrue(groups.get(0).users.contains(bob));

    }

    @Test
    public void testMoveToStorageCompletedFile() {

        long bob = uc.createLab3AndBob();
        long fileId = uc.saveFile(bob);
        fileMovingManager.moveToArchiveStorage(fileId);
        fileMovingManager.requestFilesUnarchiving(newHashSet(fileId), bob);
        DownloadFileReader.DownloadFileJob before = downloadFileReader.readJobByFile(fileId);
        fileMovingManager.moveReadyToUnarchiveToAnalysableStorage();
        DownloadFileReader.FileItemLocation location = downloadFileReader.readFileLocation(fileId);
        assertNotNull(location.contendId);
    }

    @Test
    public void testJobDeletedAfterMoveToStorage() {

        long bob = uc.createLab3AndBob();
        long fileId = uc.saveFile(bob);
        fileMovingManager.moveToArchiveStorage(fileId);
        fileMovingManager.requestFilesUnarchiving(newHashSet(fileId), bob);
        DownloadFileReader.DownloadFileJob before = downloadFileReader.readJobByFile(fileId);
        fileMovingManager.moveReadyToUnarchiveToAnalysableStorage();
        DownloadFileReader.DownloadFileJob job1 = downloadFileReader.readJobByFile(fileId);
        assertNull(job1);
    }

    @Test
    public void testSeveralJobDeletedForSeveralRequests() {

        long bob = uc.createLab3AndBob();
        long fileId = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);
        final long file3 = uc.saveFile(uc.createPaul());

        fileMovingManager.moveToArchiveStorage(fileId);
        fileMovingManager.moveToArchiveStorage(file2);
        fileMovingManager.moveToArchiveStorage(file3);

        fileMovingManager.requestFilesUnarchiving(newHashSet(fileId), bob);
        fileMovingManager.requestFilesUnarchiving(newHashSet(fileId, file2), uc.createPaul());
        fileMovingManager.requestFilesUnarchiving(newHashSet(fileId, file2), uc.createKateAndLab2());
        fileMovingManager.requestFilesUnarchiving(newHashSet(file2), bob);
        fileMovingManager.requestFilesUnarchiving(newHashSet(file2), uc.createPaul());
        fileMovingManager.requestFilesUnarchiving(newHashSet(file3, file2), uc.createPaul());
        fileMovingManager.requestFilesUnarchiving(newHashSet(fileId, file2, file3), bob);


        assertNotNull(downloadFileReader.readJobByFile(fileId));
        assertNotNull(downloadFileReader.readJobByFile(file2));

        fileMovingManager.moveReadyToUnarchiveToAnalysableStorage();

        assertNull(downloadFileReader.readJobByFile(fileId));
        assertNull(downloadFileReader.readJobByFile(file2));
        assertNull(downloadFileReader.readJobByFile(file3));

    }

    @Test
    public void testNotificationAboutCompletedDownload() {

        long bob = uc.createLab3AndBob();
        long fileId = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);
        fileMovingManager.moveToArchiveStorage(fileId);
        fileMovingManager.moveToArchiveStorage(file2);
        fileMovingManager.requestFilesUnarchiving(newHashSet(fileId), bob);
        fileMovingManager.requestFilesUnarchiving(newHashSet(file2), bob);
        DownloadFileReader.DownloadFileJob before = downloadFileReader.readJobByFile(fileId);
//        fileMovingManager.downloadToAnalysableStorageRetrievedFile(before.jobId);
        reset(notificator());
        fileMovingManager.moveReadyToUnarchiveToAnalysableStorage();
        verify(notificator(), only()).sendFileReadyToDownloadNotification(eq(Long.valueOf(bob)), anyCollection());
    }

    @Test
    public void testNotificationAboutCompletedDownloadForAllUsers() {

        long bob = uc.createLab3AndBob();
        long joe = uc.createJoe();
        long fileId = uc.saveFile(bob);
        long file2 = uc.saveFile(bob);
        fileMovingManager.moveToArchiveStorage(fileId);
        fileMovingManager.requestFilesUnarchiving(newHashSet(fileId), bob);
        fileMovingManager.requestFilesUnarchiving(newHashSet(file2), bob);
        fileMovingManager.requestFilesUnarchiving(newHashSet(fileId), joe);
        DownloadFileReader.DownloadFileJob before = downloadFileReader.readJobByFile(fileId);
//        fileMovingManager.downloadToAnalysableStorageRetrievedFile(before.jobId);
        fileMovingManager.moveReadyToUnarchiveToAnalysableStorage();
        verify(notificator()).sendFileReadyToDownloadNotification(eq(Long.valueOf(bob)), anyCollection());
        verify(notificator()).sendFileReadyToDownloadNotification(eq(Long.valueOf(joe)), anyCollection());
    }

    @Test
    public void testNotSendNotificationIfGroupNotReady() {

        long bob = uc.createLab3AndBob();
        long catFile = uc.saveFile(bob);
        long dogFile = uc.saveFile(bob);
        fileMovingManager.moveToArchiveStorage(catFile);
        fileMovingManager.moveToArchiveStorage(dogFile);
        fileMovingManager.requestFilesUnarchiving(newHashSet(catFile, dogFile), bob);
        fileMovingManager.downloadToAnalysableStorageRetrievedFile(catFile);

        verify(notificator(), never()).sendFileReadyToDownloadNotification(eq(Long.valueOf(bob)), anyCollection());
    }

    @Test
    public void testDuplicateJobsNotCreate() {

        long bob = uc.createLab3AndBob();

        long cat = uc.saveFile(bob);
        long dog = uc.saveFile(bob);
        long mouse = uc.saveFile(bob);

        fileMovingManager.moveToArchiveStorage(cat);
        fileMovingManager.moveToArchiveStorage(dog);
        fileMovingManager.moveToArchiveStorage(mouse);
        fileMovingManager.requestFilesUnarchiving(newHashSet(cat, dog, mouse), bob);
        fileMovingManager.requestFilesUnarchiving(newHashSet(cat, mouse), bob);
        fileMovingManager.requestFilesUnarchiving(newHashSet(dog, mouse), bob);
        DownloadFileReader.DownloadFileJob mouseJob = downloadFileReader.readJobByFile(mouse);
        ImmutableList<DownloadFileReader.DownloadFileGroup> mouseGroups = downloadFileReader.readGroupByJob(mouseJob.id);
        assertTrue(mouseGroups.size() == 3);
    }

    @Test
    public void testJobNotDeleteAfterOneGroupComplete() {

        long bob = uc.createLab3AndBob();

        long cat = uc.saveFile(bob);
        long dog = uc.saveFile(bob);
        long mouse = uc.saveFile(bob);

        fileMovingManager.moveToArchiveStorage(cat);
        fileMovingManager.moveToArchiveStorage(dog);
        fileMovingManager.moveToArchiveStorage(mouse);
        fileMovingManager.requestFilesUnarchiving(newHashSet(cat, dog, mouse), bob);
        fileMovingManager.requestFilesUnarchiving(newHashSet(cat, mouse), bob);
        fileMovingManager.requestFilesUnarchiving(newHashSet(dog, mouse), bob); // will be complete
        fileMovingManager.downloadToAnalysableStorageRetrievedFile(dog);
        fileMovingManager.downloadToAnalysableStorageRetrievedFile(mouse);
        DownloadFileReader.DownloadFileJob mouseJob = downloadFileReader.readJobByFile(mouse);
        assertTrue(mouseJob != null);
    }

    @Test
    public void testListenerWasCalled() {


        long bob = uc.createLab3AndBob();
        long file = uc.saveFile(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get());
        fileMovingManager.moveToArchiveStorage(file);
        GlacierDownloadListener<ActiveFileMetaData> callback = (GlacierDownloadListener<ActiveFileMetaData>) mock(GlacierDownloadListener.class);

        final String listenerId = glacierDownloadListeners.addListener(callback);
        fileMovingManager.moveFilesToStorageAndListen(newHashSet(file), listenerId);
        DownloadFileReader.DownloadFileJob mouseJob = downloadFileReader.readJobByFile(file);
        fileMovingManager.moveReadyToUnarchiveToAnalysableStorage();
        verify(callback).onFileDownloaded(Matchers.any(ActiveFileMetaData.class));
    }

}
