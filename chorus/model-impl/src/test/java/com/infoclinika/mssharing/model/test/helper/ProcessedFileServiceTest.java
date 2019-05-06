package com.infoclinika.mssharing.model.test.helper;

import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.internal.helper.ProcessedFileService;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;

/**
 * @author timofei.kasianov 2/20/18
 */
public class ProcessedFileServiceTest extends AbstractTest {

    @Inject
    private ProcessedFileService processedFileService;

    @Test
    public void testCreate() {

        final long paul = uc.createLab3AndGetPaul();
        final long projectId = uc.createProject(paul);
        final long instrumentId = uc.createInstrumentAndApproveIfNeeded(paul, uc.getLab3()).or(-1L);
        final long experimentId = createExperimentWithInstrumentRestriction(paul, projectId, instrumentId);
        final String fileName = "file-name-" + System.currentTimeMillis();

        final Long fileId = processedFileService.create(paul, experimentId, fileName);
        Assert.assertNotNull(fileId);
        final String link = processedFileService.get(paul, experimentId, fileId);
        Assert.assertNotNull(link);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testUserFromAnotherLabCannotCreate() {

        final long paul = uc.createLab3AndGetPaul();
        final long projectId = uc.createProject(paul);
        final long instrumentId = uc.createInstrumentAndApproveIfNeeded(paul, uc.getLab3()).or(-1L);
        final long experimentId = createExperimentWithInstrumentRestriction(paul, projectId, instrumentId);
        final String fileName = "file-name-" + System.currentTimeMillis();
        final long kate = uc.createKateAndLab2();

        processedFileService.create(kate, experimentId, fileName);
        Assert.fail();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCannotCreateWithExistingName() {
        final long paul = uc.createLab3AndGetPaul();
        final long projectId = uc.createProject(paul);
        final long instrumentId = uc.createInstrumentAndApproveIfNeeded(paul, uc.getLab3()).or(-1L);
        final long experimentId = createExperimentWithInstrumentRestriction(paul, projectId, instrumentId);
        final String fileName = "file-name-" + System.currentTimeMillis();

        final Long fileId = processedFileService.create(paul, experimentId, fileName);
        Assert.assertNotNull(fileId);
        processedFileService.create(paul, experimentId, fileName);
        Assert.fail();
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testUserFromNotExperimentCreatorLabCannotGet() {

        final long paul = uc.createLab3AndGetPaul();
        final long projectId = uc.createProject(paul);
        final long instrumentId = uc.createInstrumentAndApproveIfNeeded(paul, uc.getLab3()).or(-1L);
        final long experimentId = createExperimentWithInstrumentRestriction(paul, projectId, instrumentId);
        final String fileName = "file-name-" + System.currentTimeMillis();

        final Long fileId = processedFileService.create(paul, experimentId, fileName);
        Assert.assertNotNull(fileId);

        final long kate = uc.createKateAndLab2();
        processedFileService.get(kate, experimentId, fileId);
    }

}
