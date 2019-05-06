package com.infoclinika.mssharing.platform.model.test.instrument.model;

import com.google.common.collect.Sets;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.read.InstrumentModelReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.InstrumentModelReaderTemplate.InstrumentModelLineTemplate;
import com.infoclinika.mssharing.platform.model.test.helper.AbstractTest;
import com.infoclinika.mssharing.platform.model.write.InstrumentModelManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.InstrumentModelManagementTemplate.InstrumentModelDetails;
import com.infoclinika.mssharing.platform.repository.InstrumentModelRepositoryTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;

/**
 * @author timofei.kasianov 12/16/16
 */
@SuppressWarnings("unchecked")
public class InstrumentModelManagementTest extends AbstractTest {

    private static final long NOT_EXISTING_MODEL_ID = -1L;
    private static final String UPDATED_MODEL_NAME = "Updated model name";
    @Inject
    private InstrumentModelManagementTemplate modelManagement;
    @Inject
    private InstrumentModelReaderTemplate modelReader;
    @Inject
    private InstrumentModelRepositoryTemplate<InstrumentModel> instrumentModelRepository;

    @Test
    public void testCreate() {

        final long admin = admin();
        final long modelId = modelManagement.create(admin, getInstrumentModelDetails());
        final InstrumentModelLineTemplate createdModel = modelReader.readById(admin, modelId);

        Assert.assertEquals(createdModel.name, getInstrumentModelDetails().name);
        Assert.assertEquals(createdModel.technologyType.name, getInstrumentModelDetails().technologyType);
        Assert.assertEquals(createdModel.vendor.name, getInstrumentModelDetails().vendor);
        Assert.assertEquals(createdModel.instrumentType.name, getInstrumentModelDetails().instrumentType);
        Assert.assertEquals(createdModel.extensions, getInstrumentModelDetails().extensions);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testNotAdminCantCreate() {
        final long bob = uc.createLab3AndBob();
        modelManagement.create(bob, getInstrumentModelDetails());
        Assert.fail();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCreateWithNameDuplicate() {
        final long admin = admin();
        modelManagement.create(admin, getInstrumentModelDetails());
        modelManagement.create(admin, getInstrumentModelDetails());
        Assert.fail();
    }

    @Test
    public void testUpdate() {

        final long admin = admin();
        final long modelId = modelManagement.create(admin, getInstrumentModelDetails());
        final InstrumentModelLineTemplate createdModel = modelReader.readById(admin, modelId);

        final InstrumentModelDetails changedModelDetails = createModelWithNewName(createdModel, UPDATED_MODEL_NAME);

        modelManagement.update(admin, createdModel.id, changedModelDetails);
        final InstrumentModelLineTemplate updateModel = modelReader.readById(admin, modelId);

        Assert.assertEquals(updateModel.name, changedModelDetails.name);
    }

    private InstrumentModelDetails createModelWithNewName(InstrumentModelLineTemplate sourceModel,
                                                          String newModelName) {
        return new InstrumentModelDetails(
            newModelName,
            sourceModel.technologyType.name,
            sourceModel.vendor.name,
            sourceModel.instrumentType.name,
            sourceModel.extensions,
            sourceModel.additionalFiles,
            sourceModel.folderArchiveSupport
        );
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testNotAdminCantUpdate() {

        final long admin = admin();
        final long modelId = modelManagement.create(admin, getInstrumentModelDetails());
        final InstrumentModelLineTemplate createdModel = modelReader.readById(admin, modelId);

        final InstrumentModelDetails changedModelDetails = createModelWithNewName(createdModel, UPDATED_MODEL_NAME);
        final long bob = uc.createLab3AndBob();
        modelManagement.update(bob, createdModel.id, changedModelDetails);

        Assert.fail();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testUpdateNotExisting() {
        modelManagement.update(admin(), NOT_EXISTING_MODEL_ID, getInstrumentModelDetails());
        Assert.fail();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testUpdateWithNameDuplicate() {

        final long admin = admin();
        final long modelId = modelManagement.create(admin, getInstrumentModelDetails());
        final InstrumentModelLineTemplate createdModel = modelReader.readById(admin, modelId);
        final InstrumentModelDetails anotherModelDetails = createModelWithNewName(createdModel, "Another model name");
        final long anotherModelId = modelManagement.create(admin, anotherModelDetails);
        modelManagement.update(admin, anotherModelId, getInstrumentModelDetails());

        Assert.fail();
    }

    @Test
    public void testDelete() {

        final long admin = admin();
        final long modelId = modelManagement.create(admin, getInstrumentModelDetails());
        final InstrumentModelLineTemplate createdModel = modelReader.readById(admin, modelId);

        Assert.assertNotNull(createdModel);
        modelManagement.delete(admin, modelId);
        Assert.assertFalse(instrumentModelRepository.exists(modelId));
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testNotAdminCantDelete() {
        final long admin = admin();
        final long modelId = modelManagement.create(admin, getInstrumentModelDetails());
        final long bob = uc.createLab3AndBob();
        modelManagement.delete(bob, modelId);
        Assert.fail();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCantDeleteNotExisting() {
        modelManagement.delete(admin(), NOT_EXISTING_MODEL_ID);
        Assert.fail();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCantDeleteWithInstruments() {
        final long admin = admin();
        final long modelId = modelManagement.create(admin, getInstrumentModelDetails());
        final long bob = uc.createLab3AndBob();
        final Long labId = uc.getLab3();
        createInstrumentAndApproveIfNeeded(bob, labId, modelId, instrumentDetails());
        modelManagement.delete(admin, modelId);
        Assert.fail();
    }


    private InstrumentModelDetails getInstrumentModelDetails() {
        return new InstrumentModelDetails(
            "My Instrument Model",
            "My Technology Type",
            "My Vendor",
            "My Instrument Type",
            Sets.newHashSet(".my", ".own")
        );
    }
}
