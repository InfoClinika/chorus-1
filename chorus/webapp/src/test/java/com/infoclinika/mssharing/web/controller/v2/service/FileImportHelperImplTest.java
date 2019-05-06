package com.infoclinika.mssharing.web.controller.v2.service;

import com.infoclinika.mssharing.model.internal.entity.upload.FileDetails;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.InstrumentReader;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.web.demo.DemoDataBasedTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

/**
 * @author timofei.kasianov 2/13/18
 */
public class FileImportHelperImplTest extends DemoDataBasedTest {

    @Inject
    private FileImportHelper fileImportHelper;
    @Inject
    private InstrumentReader instrumentReader;
    @Inject
    private DashboardReader dashboardReader;
    @Inject
    private DetailsReader detailsReader;

    @Test
    public void testCreateFileMetadata() {

        final long userId = demoUser();
        final SortedSet<? extends InstrumentItem> instruments =
            instrumentReader.readInstrumentItemsWhereUserIsOperator(userId);
        final long instrumentId = instruments.first().id;
        final FileDetails fileDetails = new FileDetails(
            UUID.randomUUID().toString(),
            -1,
            1024
        );

        final int filesCountBeforeCreation = dashboardReader.readFilesByInstrument(userId, instrumentId).size();
        fileImportHelper.createFileMetadata(userId, instrumentId, fileDetails);
        final Set<FileLine> files = dashboardReader.readFilesByInstrument(userId, instrumentId);

        Assert.assertEquals(filesCountBeforeCreation + 1, files.size());
    }

    @Test
    public void testRemoveFileMetadata() {

        final long userId = demoUser();
        final SortedSet<? extends InstrumentItem> instruments =
            instrumentReader.readInstrumentItemsWhereUserIsOperator(userId);
        final long instrumentId = instruments.first().id;
        final FileDetails fileDetails = new FileDetails(
            UUID.randomUUID().toString(),
            -1,
            1024
        );

        final Set<FileLine> filesBefore = dashboardReader.readFilesByInstrument(userId, instrumentId);
        fileImportHelper.createFileMetadata(userId, instrumentId, fileDetails);

        final Set<FileLine> filesAfter = dashboardReader.readFilesByInstrument(userId, instrumentId);
        final FileLine createdFile = filesAfter
            .stream()
            .filter(f -> filesBefore.stream().noneMatch(ff -> ff.id == f.id))
            .findFirst()
            .orElseThrow(RuntimeException::new);

        fileImportHelper.removeFileMetadata(createdFile.id);

        final Set<FileLine> withoutRemoved = dashboardReader.readFilesByInstrument(userId, instrumentId);

        Assert.assertTrue(withoutRemoved.stream().noneMatch(f -> f.id == createdFile.id));
    }

    @Test
    public void testIsFileAlreadyUploaded() {

        final long userId = demoUser();
        final SortedSet<? extends InstrumentItem> instruments =
            instrumentReader.readInstrumentItemsWhereUserIsOperator(userId);
        final long instrumentId = instruments.first().id;
        final String fileName = UUID.randomUUID().toString();
        final FileDetails fileDetails = new FileDetails(
            fileName,
            -1,
            1024
        );

        Assert.assertFalse(fileImportHelper.isFileAlreadyUploaded(userId, instrumentId, fileName));
        fileImportHelper.createFileMetadata(userId, instrumentId, fileDetails);
        Assert.assertTrue(fileImportHelper.isFileAlreadyUploaded(userId, instrumentId, fileName));
    }


    @Test
    public void testCreateFileReference() {

        final long userId = demoUser();
        final SortedSet<? extends InstrumentItem> instruments =
            instrumentReader.readInstrumentItemsWhereUserIsOperator(userId);
        final long instrumentId = instruments.first().id;
        final String fileName = UUID.randomUUID().toString();
        final FileDetails fileDetails = new FileDetails(
            fileName,
            -1,
            1024
        );

        final long fileId = fileImportHelper.createFileReference(
            "bucket",
            "bucket/path/" + fileName,
            true,
            fileDetails.getSize(),
            "",
            userId,
            instrumentId,
            fileDetails.getSpecieId()
        );

        final FileItem fileItem = detailsReader.readFile(userId, fileId);

        Assert.assertNotNull(fileId);
        Assert.assertEquals(fileItem.name, fileName);
    }

}
