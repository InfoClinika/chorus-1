/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.test.instrument;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.InstrumentLine;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.model.write.FileMetaDataInfo;
import com.infoclinika.mssharing.platform.fileserver.StoredObject;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.read.Filter;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.get;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * @author Stanislav Kurilin
 */
public class UploadingFilesTest extends AbstractInstrumentTest {
    @Test
    public void testNoInstrumentsOnFiles() {
        final long bob = uc.createLab3AndBob();
        assertEquals(Iterables.size(dashboardReader.instrumentsWithAvailableFiles(bob)), 0);
        assertEquals(Iterables.size(fileReader.readFiles(bob, Filter.ALL)), 0);
    }

    @Test
    public void testNoInstrumentsWhenOnFilesSectionWithoutFiles() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        assertEquals(Iterables.size(dashboardReader.instrumentsWithAvailableFiles(bob)), 0);
        assertEquals(Iterables.size(fileReader.readFiles(bob, Filter.ALL)), 0);
    }

    @Test
    public void testCorrectFileStatusIfContentWasntSet() throws Exception {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        instrumentManagement.createFile(
            bob,
            instrument,
            new FileMetaDataInfo(generateString(), 0, "", null, anySpecies(), false)
        );

        final InstrumentLine next = dashboardReader.readInstruments(bob).iterator().next();
        checkState(next.id == instrument);
        assertEquals(next.files, 1);


        final Set<FileLine> allFilesForBob = fileReader.readFiles(bob, Filter.ALL);
        assertEquals(Iterables.size(allFilesForBob), 1);
        assertEquals(Iterables.size(dashboardReader.instrumentsWithAvailableFiles(bob)), 1);
        assertEquals(Iterables.size(dashboardReader.readFilesByInstrument(bob, instrument)), 1);

        //file should not be available for experiment creation until the content is set
        assertEquals(Iterables.size(experimentCreationHelper.availableFilesByInstrument(bob, 0, instrument)), 0);

        final FileLine file = get(allFilesForBob, 0);
        assertNotNull(file);
        assertNull(file.contentId);
    }

    @Test
    public void testFilesWithNotSettedContentAvailableThrowUploadHelper() throws Exception {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        instrumentManagement.createFile(
            bob,
            instrument,
            new FileMetaDataInfo(generateString(), 0, "", null, anySpecies(), false)
        );
        assertEquals(Iterables.size(uploadHelper.incompleteFiles(bob, instrument)), 1);
    }

    @Test(dependsOnMethods = "testFilesWithNotSettedContentAvailableThrowUploadHelper")
    public void testDiscardedFilesAreNotSettedContentAvailableThrowUploadHelper() throws Exception {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long file = instrumentManagement.createFile(
            bob,
            instrument,
            new FileMetaDataInfo(generateString(), 0, "", null, anySpecies(), false)
        );
        instrumentManagement.discard(bob, file);
        assertEquals(Iterables.size(uploadHelper.incompleteFiles(bob, instrument)), 0);
    }

    @Test
    public void testCancelFileUpload() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long file = instrumentManagement.createFile(
            bob,
            instrument,
            new FileMetaDataInfo(generateString(), 1024, "", null, anySpecies(), false)
        );
        instrumentManagement.cancelUpload(bob, file);
        assertEquals(Iterables.size(uploadHelper.incompleteFiles(bob, instrument)), 0);
    }

    @Test(dependsOnMethods = "testFilesWithNotSettedContentAvailableThrowUploadHelper")
    public void testCompleteUploadFilesAreNotSettedContentAvailableThrowUploadHelper() throws Exception {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long file = instrumentManagement.createFile(
            bob,
            instrument,
            new FileMetaDataInfo(generateString(), 0, "", null, anySpecies(), false)
        );
        instrumentManagement.setContent(bob, file, mock(StoredObject.class));
        assertEquals(Iterables.size(uploadHelper.incompleteFiles(bob, instrument)), 0);
    }

    @Test
    public void testUserCanUploadArchivedDirWithCorrectInstrumentVendor() throws Exception {
        final long bob = uc.createLab3AndBob();
        final long instrument =
            uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), uc.getInstrumentModelWhichSupportArchiveUpload())
                .get();
        final long file = instrumentManagement.createFile(
            bob,
            instrument,
            new FileMetaDataInfo(generateString(), 0, "", null, anySpecies(), true)
        );
        instrumentManagement.setContent(bob, file, mock(StoredObject.class));
        assertEquals(Iterables.size(uploadHelper.incompleteFiles(bob, instrument)), 0);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testUserCannotUploadArchivedDirWithNotCorrectInstrumentVendor() throws Exception {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long file = instrumentManagement.createFile(
            bob,
            instrument,
            new FileMetaDataInfo(generateString(), 0, "", null, anySpecies(), true)
        );
    }

    @Test
    public void testFileAvailableAfterUploadingByOwner() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();

        final long file = uc.saveFile(bob, instrument);

        assertEquals(Iterables.size(dashboardReader.instrumentsWithAvailableFiles(bob)), 1);
        assertEquals(Iterables.size(dashboardReader.readFilesByInstrument(bob, instrument)), 1);
        assertEquals(Iterables.size(fileReader.readFiles(bob, Filter.MY)), 1);
    }

    @Test
    public void testUserDontSeeOwnFilesInShared() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();

        uc.saveFile(bob, instrument);
        assertEquals(Iterables.size(fileReader.readFiles(bob, Filter.MY)), 1);
        assertEquals(Iterables.size(fileReader.readFiles(bob, Filter.SHARED_WITH_ME)), 0);
        assertEquals(Iterables.size(fileReader.readFiles(bob, Filter.ALL)), 1);
    }

    @Test
    public void testFileAppearsOnlyInItsInstrument() {
        final long bob = uc.createLab3AndBob();
        final long instrument1 = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long instrument2 = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();

        uc.saveFile(bob, instrument1);

        assertEquals(Iterables.size(dashboardReader.readFilesByInstrument(bob, instrument1)), 1);
        assertEquals(Iterables.size(dashboardReader.readFilesByInstrument(bob, instrument2)), 0);
    }

    @Test
    public void testReadFileDetails() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long fileId = uc.saveFile(bob, instrument);

        final FileItem details = detailsReader.readFile(bob, fileId);

        assertEquals(details.id, fileId);
        checkNotNull(details.uploadDate);
    }

    @Test
    public void testReadFileByOwner() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long fileId = uc.saveFile(bob, instrument);

        checkHasAccessToFile(bob, uc.getLab3(), fileId);
    }

    @Test
    public void testReadByUserWhoIsOperatorOfUsedInstrument() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final Long lab3 = uc.getLab3();
        final long instrument = createInstrumentAndApproveIfNeeded(bob, lab3);

        final long project = uc.createProject(bob, lab3);

        sharingManagement.makeProjectPublic(bob, project);
        final long fileId = uc.saveFile(bob, instrument);

        final long experiment = createExperiment(bob, project, lab3, noFactoredFile(fileId));
        checkHasAccessToFile(poll, lab3, fileId);
    }

    @Test
    public void testMultipleFilesValidForUpload() {
        final long poll = uc.createPaul();
        final long abSciexInstrument = createInstrumentByModel(poll, uc.getLab3(), abSciexInstrumentModel());

        final List<String> validAbSciexFiles1 = Lists.newArrayList("file.wiff");
        final List<String> validAbSciexFiles2 = Lists.newArrayList("file.wiff", "file.wiff.scan");
        final List<String> validAbSciexFiles3 = Lists.newArrayList("file.wiff", "file.wiff.scan", "file.wiff.mtd");

        final List<String> invalidAbSciexFiles4 = Lists.newArrayList("file.wiff", "file.wiff.mtd", "valid_file.wiff");

        final boolean abSciexFiles1Valid =
            instrumentManagement.checkMultipleFilesValidForUpload(abSciexInstrument, validAbSciexFiles1);
        assertEquals(abSciexFiles1Valid, true);
        final boolean abSciexFiles2Valid =
            instrumentManagement.checkMultipleFilesValidForUpload(abSciexInstrument, validAbSciexFiles2);
        assertEquals(abSciexFiles2Valid, true);
        final boolean abSciexFiles3Valid =
            instrumentManagement.checkMultipleFilesValidForUpload(abSciexInstrument, validAbSciexFiles3);
        assertEquals(abSciexFiles3Valid, true);
        final boolean abSciexFiles4Valid =
            instrumentManagement.checkMultipleFilesValidForUpload(abSciexInstrument, invalidAbSciexFiles4);
        assertEquals(abSciexFiles4Valid, false);

    }

    @Test
    public void testFileCompounds() throws Exception {

        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long fileId = uc.saveFile(bob, instrument);

        final Set<FileLine> fileLines = fileReader.readFilesByInstrument(bob, instrument);
        assertNotEquals(fileLines.size(), 0);

        fileLines.forEach(item -> {
            assertNotNull(item.fileCompounds);
            assertEquals(item.fileCompounds.size(), 0);
        });

    }
}
