/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.model.test.instrument;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.FileReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.FileManagementTemplate;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.get;
import static org.testng.Assert.*;

/**
 * @author Stanislav Kurilin
 */
@SuppressWarnings("unchecked")
public class UploadingFilesTest extends AbstractInstrumentTest {

    @Test
    public void testNoInstrumentsOnFiles() {
        final long bob = uc.createLab3AndBob();
        assertEquals(Iterables.size(instrumentReader.readInstruments(bob)), 0);
        assertEquals(Iterables.size(fileReader.readFiles(bob, Filter.ALL)), 0);
    }


    @Test
    public void testCorrectFileStatusIfContentWasntSet() throws Exception {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        fileManagement.createFile(bob, instrument, new FileManagementTemplate.FileMetaDataInfoTemplate(generateString(), 0, "", null, anySpecies(), false));

        final InstrumentReaderTemplate.InstrumentLineTemplate next = instrumentReader.readInstruments(bob).iterator().next();
        checkState(next.id == instrument);
        assertEquals(next.files, 1);


        final Set<FileReaderTemplate.FileLineTemplate> allFilesForBob = fileReader.readFiles(bob, Filter.ALL);
        assertEquals(Iterables.size(allFilesForBob), 1);
        assertEquals(Iterables.size(fileReader.readFilesByInstrument(bob, instrument)), 1);

        //file should not be available for experiment creation until the content is set
        assertEquals(Iterables.size(experimentCreationHelper.availableFilesByInstrument(bob, 0, instrument)), 0);

        final FileReaderTemplate.FileLineTemplate file = get(allFilesForBob, 0);
        assertNotNull(file);
        assertNull(file.contentId);
    }

    @Test
    public void testFilesWithNotSettedContentAvailableThrowUploadHelper() throws Exception {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        fileManagement.createFile(bob, instrument, new FileManagementTemplate.FileMetaDataInfoTemplate(generateString(), 0, "", null, anySpecies(), false));
        assertEquals(Iterables.size(fileReader.readUnfinishedFiles(bob)), 1);
    }

    @Test(dependsOnMethods = "testFilesWithNotSettedContentAvailableThrowUploadHelper")
    public void testDiscardedFilesAreNotSettedContentAvailableThrowUploadHelper() throws Exception {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long file = fileManagement.createFile(bob, instrument, new FileManagementTemplate.FileMetaDataInfoTemplate(generateString(), 0, "", null, anySpecies(), false));
        fileUploadManagement.cancelUpload(bob, file);
        assertEquals(Iterables.size(fileReader.readUnfinishedFiles(bob)), 0);
    }

    @Test
    public void testCancelFileUpload() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long file = fileManagement.createFile(bob, instrument, new FileManagementTemplate.FileMetaDataInfoTemplate(generateString(), 1024, "", null, anySpecies(), false));
        fileUploadManagement.cancelUpload(bob, file);
        assertEquals(Iterables.size(fileReader.readUnfinishedFiles(bob)), 0);
    }

    @Test(dependsOnMethods = "testFilesWithNotSettedContentAvailableThrowUploadHelper")
    public void testCompleteUploadFilesAreNotSettedContentAvailableThrowUploadHelper() throws Exception {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long file = fileManagement.createFile(bob, instrument, new FileManagementTemplate.FileMetaDataInfoTemplate(generateString(), 0, "", null, anySpecies(), false));
        setContent(bob, file);
        assertEquals(Iterables.size(fileReader.readUnfinishedFiles(bob)), 0);
    }

    @Test
    public void testUserCanUploadArchivedDirWithCorrectInstrumentVendor() throws Exception {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), uc.getInstrumentModelWhichSupportArchiveUpload()).get();
        final long file = fileManagement.createFile(bob, instrument, new FileManagementTemplate.FileMetaDataInfoTemplate(generateString(), 0, "", null, anySpecies(), false));
        setContent(bob, file);
        assertEquals(Iterables.size(fileReader.readUnfinishedFiles(file)), 0);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testUserCannotUploadArchivedDirWithNotCorrectInstrumentVendor() throws Exception {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        fileManagement.createFile(bob, instrument, new FileManagementTemplate.FileMetaDataInfoTemplate(generateString(), 0, "", null, anySpecies(), true));
    }

    @Test
    public void testFileAvailableAfterUploadingByOwner() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();

        final long file = uc.saveFile(bob, instrument);

        assertEquals(Iterables.size(fileReader.readFilesByInstrument(bob, instrument)), 1);
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

        assertEquals(Iterables.size(fileReader.readFilesByInstrument(bob, instrument1)), 1);
        assertEquals(Iterables.size(fileReader.readFilesByInstrument(bob, instrument2)), 0);
    }

    @Test
    public void testReadFileDetails() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final long fileId = uc.saveFile(bob, instrument);

        final DetailsReaderTemplate.FileItemTemplate details = detailsReader.readFile(bob, fileId);

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

        instrumentManagement.addOperatorDirectly(bob, instrument, poll);

        final long project = uc.createProject(bob, lab3);

        sharingManagement.makeProjectPublic(bob, project);
        final long fileId = uc.saveFile(bob, instrument);

        final long experiment = createExperiment(bob, project, lab3, noFactoredFile(fileId));
        checkHasAccessToFile(poll, lab3, fileId);
    }

    //TODO: checkMultipleFilesValidForUpload is not implemented in DefaultFileUploadManagement. Enable when it will be implemented
    @Test(enabled = false)
    public void testMultipleFilesValidForUpload() {
        final long poll = uc.createPaul();
        final long abSciexInstrument = createInstrumentByModel(poll, uc.getLab3(), abSciexInstrumentModel());

        final List<String> validAbSciexFiles1 = Lists.newArrayList("file.wiff");
        final List<String> validAbSciexFiles2 = Lists.newArrayList("file.wiff", "file.wiff.scan");
        final List<String> validAbSciexFiles3 = Lists.newArrayList("file.wiff", "file.wiff.scan", "file.wiff.mtd");

        final List<String> invalidAbSciexFiles4 = Lists.newArrayList("file.wiff", "file.wiff.mtd", "valid_file.wiff");

        final boolean abSciexFiles1Valid = fileUploadManagement.checkMultipleFilesValidForUpload(abSciexInstrument, validAbSciexFiles1);
        assertEquals(abSciexFiles1Valid, true);
        final boolean abSciexFiles2Valid = fileUploadManagement.checkMultipleFilesValidForUpload(abSciexInstrument, validAbSciexFiles2);
        assertEquals(abSciexFiles2Valid, true);
        final boolean abSciexFiles3Valid = fileUploadManagement.checkMultipleFilesValidForUpload(abSciexInstrument, validAbSciexFiles3);
        assertEquals(abSciexFiles3Valid, true);
        final boolean abSciexFiles4Valid = fileUploadManagement.checkMultipleFilesValidForUpload(abSciexInstrument, invalidAbSciexFiles4);
        assertEquals(abSciexFiles4Valid, false);

    }

    @Test
    public void testIsFileAlreadyUploaded() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), uc.getInstrumentModelWhichSupportArchiveUpload()).get();
        final String existedFileName = generateString();
        final String nonExistedFileName = generateString();
        final long file = fileManagement.createFile(bob, instrument, new FileManagementTemplate.FileMetaDataInfoTemplate(existedFileName, 0, "", null, anySpecies(), false));
        setContent(bob, file);

        assertTrue(fileUploadManagement.isFileAlreadyUploadedForInstrument(bob, instrument, existedFileName));
        assertFalse(fileUploadManagement.isFileAlreadyUploadedForInstrument(bob, instrument, nonExistedFileName));
    }

    @Test
    public void testFindFileByNameForInstrument() {
        final long bob = uc.createLab3AndBob();
        final long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), uc.getInstrumentModelWhichSupportArchiveUpload()).get();
        final String existedFileName = generateString();
        final String nonExistedFileName = generateString();
        final long file = fileManagement.createFile(bob, instrument, new FileManagementTemplate.FileMetaDataInfoTemplate(existedFileName, 0, "", null, anySpecies(), false));
        setContent(bob, file);

        assertTrue(!fileReader.readByNameForInstrument(bob, instrument, existedFileName).isEmpty());
        assertTrue(fileReader.readByNameForInstrument(bob, instrument, nonExistedFileName).isEmpty());
    }
}
