/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.model.test.instrument;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.ObjectNotFoundException;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.InstrumentItemTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentLineTemplate;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.InstrumentManagementTemplate;
import org.testng.annotations.Test;

import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.size;
import static org.testng.Assert.*;

/**
 * @author Stanislav Kurilin
 */
public class CreatingInstrumentTest extends AbstractInstrumentTest {

    @Test(expectedExceptions = AccessDenied.class)
    public void testOperatorCanNotCreateInstrument() {
        final long bob = uc.createLab3AndBob();
        instrumentManagement.createInstrument(bob, uc.getLab3(), anyInstrumentModel(), instrumentDetails());
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testOperatorCanNotApproveInstrumentCreationRequest() {
        final long bob = uc.createLab3AndBob();
        final Optional<Long> instrumentRequest =
                instrumentManagement.newInstrumentRequest(bob, uc.getLab3(), anyInstrumentModel(), instrumentDetails(), NO_OPERATORS);
        instrumentManagement.approveInstrumentCreation(bob, instrumentRequest.get());
    }

    @Test
    public void testOperatorRequestsInstrumentCreationLabHeadApproves() {
        final long bob = uc.createLab3AndBob();
        final Optional<Long> instrumentRequest =
                instrumentManagement.newInstrumentRequest(bob, uc.getLab3(), anyInstrumentModel(), instrumentDetails(), NO_OPERATORS);

        assertEquals(instrumentReader.readInstruments(bob).size(), 0);

        final LabReaderTemplate.LabLineTemplate lab = labReader.readLab(uc.getLab3());
        instrumentManagement.approveInstrumentCreation(lab.labHead, instrumentRequest.get());

        assertEquals(instrumentReader.readInstruments(bob).size(), 1);
    }

    @Test
    public void testLabHeadUpdatesAndApprovesCreationRequest() {

        final long bob = uc.createLab3AndBob();
        final Optional<Long> instrumentRequest =
                instrumentManagement.newInstrumentRequest(bob, uc.getLab3(), anyInstrumentModel(), instrumentDetails(), NO_OPERATORS);

        assertEquals(instrumentReader.readInstruments(bob).size(), 0);

        final LabReaderTemplate.LabLineTemplate lab = labReader.readLab(uc.getLab3());
        final long model = anyInstrumentModel();
        final InstrumentManagementTemplate.InstrumentDetailsTemplate tempDetails = instrumentDetails();
        final InstrumentManagementTemplate.InstrumentDetailsTemplate details = new InstrumentManagementTemplate.InstrumentDetailsTemplate(
                tempDetails.name,
                tempDetails.serialNumber,
                tempDetails.peripherals
        );

        instrumentManagement.updateNewInstrumentRequest(
                lab.labHead,
                instrumentRequest.get(),
                model,
                details,
                NO_OPERATORS
        );
        final long instrumentId = instrumentManagement.approveInstrumentCreation(lab.labHead, instrumentRequest.get());

        final Set<? extends InstrumentLineTemplate> instrumentItems = instrumentReader.readInstruments(bob);

        assertEquals(instrumentItems.size(), 1);

        final InstrumentItemTemplate instrumentItem = detailsReader.readInstrument(bob, instrumentId);

        assertEquals(instrumentItem.name, details.name);
        assertEquals(instrumentItem.serialNumber, details.serialNumber);
        assertEquals(instrumentItem.peripherals, details.peripherals);

    }

    @Test
    public void testLabHeadCreatesInstrument() {
        final long lab3 = uc.createLab3();
        final LabReaderTemplate.LabLineTemplate lab = labReader.readLab(lab3);
        final int instrumentSize = instrumentReader.readInstrumentsByLab(lab.labHead, lab.id).size();
        instrumentManagement.createInstrument(lab.labHead, lab.id, anyInstrumentModel(), instrumentDetails());

        assertEquals(instrumentReader.readInstrumentsByLab(lab.labHead, lab.id).size(), instrumentSize + 1);

    }

    //No Instruments
    @Test
    public void testNoInstrumentsOnDashboard() {
        final long bob = uc.createLab3AndBob();
        assertEquals(Iterables.size(instrumentReader.readInstruments(bob)), 0);
    }

    @Test
    public void testNoInstrumentsWhenExperimentCreating() {
        final long bob = uc.createLab3AndBob();

        assertNumberOfAvailableInstrumentTypes(bob, 0);
    }

    //Creating
    @Test
    public void testUserCanCreateInstruments() {
        final long bob = uc.createLab3AndBob();
        assertTrue(uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).isPresent());
    }

    @Test
    public void testLabHeadIsOperatorOfAllInstrumentsInLab() {
        final long paul = uc.createPaul();
        final long bob = uc.createLab3AndBob();
        assertTrue(uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).isPresent());
        assertEquals(size(instrumentReader.readInstrumentsWhereUserIsOperator(paul)), 1);
    }

    @Test(dependsOnMethods = "testLabHeadIsOperatorOfAllInstrumentsInLab")
    public void testLabHeadCannotBeRemovedFromInstrumentInLab() {
        final long paul = uc.createPaul();
        final long bob = uc.createLab3AndBob();
        final Long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        assertEquals(size(instrumentReader.readInstrumentsWhereUserIsOperator(paul)), 1);

        instrumentManagement.setInstrumentOperators(bob, instrument, ImmutableList.of(bob));
        assertEquals(size(instrumentReader.readInstrumentsWhereUserIsOperator(paul)), 1);
        assertEquals(size(instrumentReader.readInstrumentsWhereUserIsOperator(bob)), 1);

    }

    @Test(dependsOnMethods = "testUserCanCreateInstruments")
    public void testUserCanSeeHisInstrumentsOnDashboard() {
        final long bob = uc.createLab3AndBob();
        uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3());
        assertEquals(size(instrumentReader.readInstruments(bob)), 1);
        assertEquals(size(instrumentReader.readInstrumentsWhereUserIsOperator(bob)), 1);
    }

    @Test(dependsOnMethods = "testUserCanCreateInstruments")
    public void testUserCanSeeAllInstrumentsOnDashboardFromHisLab() {
        final long bob = uc.createLab3AndBob();
        uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3());
        final long poll = uc.createPaul();
        assertEquals(size(instrumentReader.readInstruments(poll)), 1);
    }

    @Test
    public void testUserDontSeeInOperatorListLaboratoryInstruments() throws Exception {
        final long poll = uc.createPaul();
        final long bob = uc.createLab3AndBob();
        uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3());
        assertEquals(size(instrumentReader.readInstrumentsWhereUserIsOperator(bob)), 0);
    }

    @Test(dependsOnMethods = "testUserCanSeeAllInstrumentsOnDashboardFromHisLab")
    public void testDontSeeInstrumentsFromOtherLab() {
        final long bob = uc.createLab3AndBob();
        uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3());
        final long kate = uc.createKateAndLab2();
        assertEquals(size(instrumentReader.readInstruments(kate)), 0);
    }

    @Test(dependsOnMethods = "testUserCanCreateInstruments")
    public void testUserCanReadInstrumentDetails() {
        final long bob = uc.createLab3AndBob();

        final DictionaryItem vendor = instrumentCreationHelper.vendors().first();
        final DictionaryItem modelItem = instrumentCreationHelper.models(vendor.id).first();
        final Long instrument = createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), modelItem.id, instrumentDetails()).get();

        final InstrumentItemTemplate instrumentItem = detailsReader.readInstrument(bob, instrument);

        assertEquals(instrumentItem.model, modelItem.name);
    }

    @Test
    public void testInstrumentBecomeAvailableForExperimentCreation() {
        final long bob = uc.createLab3AndBob();
        checkState(experimentCreationHelper.availableInstrumentModels(bob, null).isEmpty());
        uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3());
        assertFalse(experimentCreationHelper.availableInstrumentModels(bob, null).isEmpty());
    }


    //Creating second instrument
    @Test(expectedExceptions = AccessDenied.class)
    public void testCheckingSnUniqueBySameUser() {
        //TODO: [stanislav.kurilin] define behaviour
        final long bob = uc.createLab3AndBob();
        createInstrumentWithConstSN(bob, uc.getLab3());
        createInstrumentWithConstSN(bob, uc.getLab3());
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testCheckingSnUniqueBySameLab() {
        final long bob = uc.createLab3AndBob();
        assertTrue(createInstrumentWithConstSN(bob, uc.getLab3()).isPresent());
        final long poll = uc.createPaul();
        createInstrumentWithConstSN(poll, uc.getLab3());
        //TODO: [stanislav.kurilin] check that system sends an email with a url link to the operators of the instrument to add the user
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testCheckingSnUniqueByDifferentLab() {
        final long bob = uc.createLab3AndBob();
        assertTrue(createInstrumentWithConstSN(bob, uc.getLab3()).isPresent());
        uc.createLab2();
        final long kate = uc.createKateAndLab2();
        createInstrumentWithConstSN(kate, uc.getLab2());
    }

    private Optional<Long> createInstrumentWithConstSN(long bob, long lab) {
        return createInstrumentAndApproveIfNeeded(bob, lab, anyInstrumentModel(),
                new InstrumentManagementTemplate.InstrumentDetailsTemplate(generateString(), "SN", generateString()));
    }

    //Instrument selection become available on experiment creation
    @Test
    public void testAvailableInstrumentTypesContent() {
        final long bob = uc.createLab3AndBob();
        final long model = anyInstrumentModel();
        final long instrument = createInstrumentByModel(bob, uc.getLab3(), model);
        uc.saveFile(bob, instrument);
        assertNumberOfAvailableInstrumentTypes(bob, 1);

        final Iterable<DictionaryItem> instrumentTypes = experimentCreationHelper.availableInstrumentModels(bob, null);
        final DictionaryItem modelItem = instrumentTypes.iterator().next();

        assertNotNull(modelItem.name);
        assertTrue(modelItem.name.contains(" - "));
    }

    @Test(dependsOnMethods = "testAvailableInstrumentTypesContent")
    public void testTypesAreDistinct() {
        final long bob = uc.createLab3AndBob();
        final long model = anyInstrumentModel();
        final long instrumentByModel = createInstrumentByModel(bob, uc.getLab3(), model);
        final long instrumentByModel1 = createInstrumentByModel(bob, uc.getLab3(), model);
        final long instrumentByModel2 = createInstrumentByModel(bob, uc.getLab3(), model);
        uc.saveFile(bob, instrumentByModel);
        uc.saveFile(bob, instrumentByModel1);
        uc.saveFile(bob, instrumentByModel2);

        assertNumberOfAvailableInstrumentTypes(bob, 1);
    }

    @Test(dependsOnMethods = "testAvailableInstrumentTypesContent")
    public void testAvailableOnlyWhereHaveAccess() {
        final long bob = uc.createLab3AndBob();
        createInstrumentByModel(uc.createPaul(), uc.getLab3(), anyInstrumentModel());

        assertNumberOfAvailableInstrumentTypes(bob, 0);
    }

    @Test(dependsOnMethods = "testAvailableInstrumentTypesContent")
    public void testNotAvailableIfPending() {
        final long bob = uc.createLab3AndBob();
        final long instrument = createInstrumentByModel(uc.createPaul(), uc.getLab3(), anyInstrumentModel());
        instrumentManagement.requestAccessToInstrument(bob, instrument);

        assertNumberOfAvailableInstrumentTypes(bob, 0);
    }

    @Test
    public void testAvailableIfOperator() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long instrument = createInstrumentByModel(poll, uc.getLab3(), anyInstrumentModel());
        instrumentManagement.addOperatorDirectly(poll, instrument, bob);
        uc.saveFile(bob, instrument);
        assertNumberOfAvailableInstrumentTypes(bob, 1);
    }

    @Test(dependsOnMethods = "testAvailableInstrumentTypesContent")
    public void testAvailableInstrumentsOnlyIfLabMember() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();
        final long model = anyInstrumentModelByVendor(anyVendor());
        final long byModel = createInstrumentByModel(bob, uc.getLab3(), model);
        final long instrumentByModel = createInstrumentByModel(bob, uc.getLab3(), model);
        final long byModel1 = createInstrumentByModel(kate, uc.getLab2(), model);
        uc.saveFile(kate, byModel1);
        uc.saveFile(bob, byModel);
        uc.saveFile(bob, instrumentByModel);

        assertEquals(size(experimentCreationHelper.availableInstrumentsByModel(bob, model)), 2);
    }

    @Test
    public void testUserCanEditInstrument() {
        final long bob = uc.createLab3AndBob();
        final Long instrument = createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), anyInstrumentModel(), instrumentDetails()).get();
        final String newName = generateString();
        instrumentManagement.editInstrument(bob, instrument, new InstrumentManagementTemplate.InstrumentDetailsTemplate(newName, generateString(),
                generateString()));
        final InstrumentItemTemplate instrumentItem = detailsReader.readInstrument(bob, instrument);
        assertEquals(instrumentItem.name, newName);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testUserIsntAvailableEditInstrument() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();
        final Long instrument = createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), anyInstrumentModel(), instrumentDetails()).get();
        final String newName = generateString();
        instrumentManagement.editInstrument(kate, instrument, new InstrumentManagementTemplate.InstrumentDetailsTemplate(newName, generateString(), generateString()));
    }

    @Test
    public void testUserCanRemoveHisInstrument() {
        final long bob = uc.createLab3AndBob();
        final Long instrument = createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), anyInstrumentModel(), instrumentDetails()).get();
        final long file1 = uc.saveFile(bob, instrument);
        final long file2 = uc.saveFile(bob, instrument);
        assertEquals(fileReader.readFiles(bob, Filter.ALL).size(), 2);
        fileManagement.deleteFile(bob, file1, true);
        fileManagement.deleteFile(bob, file2, true);
        assertEquals(fileReader.readFiles(bob, Filter.ALL).size(), 0);
        assertEquals(instrumentReader.readInstrumentsWhereUserIsOperator(bob).size(), 1);
        instrumentManagement.deleteInstrument(bob, instrument);
        assertEquals(instrumentReader.readInstrumentsWhereUserIsOperator(bob).size(), 0);
    }

    @Test(expectedExceptions = AccessDenied.class, dependsOnMethods = "testUserCanRemoveHisInstrument")
    public void testUserCanRemoveNotHisInstrument() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createJoe();
        final Long instrument = createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), anyInstrumentModel(), instrumentDetails()).get();
        instrumentManagement.deleteInstrument(poll, instrument);
    }

    @Test(expectedExceptions = AccessDenied.class, dependsOnMethods = "testUserCanRemoveHisInstrument")
    public void testUserCanRemoveHisInstrumentWithUploadedFiles() {
        final long bob = uc.createLab3AndBob();
        final Long instrument = createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), anyInstrumentModel(), instrumentDetails()).get();
        final long file = uc.saveFile(bob, instrument);
        instrumentManagement.deleteInstrument(bob, instrument);
    }

    @Test(expectedExceptions = Exception.class)
    public void testCreateInstrumentWithAlreadyUsedSerialNumber() {
        final long bob = uc.createLab3AndBob();
        createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), anyInstrumentModel(),
                new InstrumentManagementTemplate.InstrumentDetailsTemplate("Instrument1", "123", generateString())).get();
        createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), anyInstrumentModel(),
                new InstrumentManagementTemplate.InstrumentDetailsTemplate("Instrument2", "123", generateString())).get();
    }

    @Test
    public void testCanChangeInstrumentName() {
        final long bob = uc.createLab3AndBob();
        final Long instrument = createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), anyInstrumentModel(),
                new InstrumentManagementTemplate.InstrumentDetailsTemplate("Instrument1", "123", generateString())).get();
        final Long instrument2 = createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), anyInstrumentModel(),
                new InstrumentManagementTemplate.InstrumentDetailsTemplate("Instrument2", "1234", generateString())).get();
        instrumentManagement.editInstrument(bob, instrument,
                new InstrumentManagementTemplate.InstrumentDetailsTemplate("Instrument4", "123", generateString()));
        instrumentManagement.editInstrument(bob, instrument,
                new InstrumentManagementTemplate.InstrumentDetailsTemplate("Instrument3", "12345", generateString()));
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testCanNotChangeInstrumentNameInLabWithAlreadyUsedName() {
        final long bob = uc.createLab3AndBob();
        final Long instrument = createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), anyInstrumentModel(),
                new InstrumentManagementTemplate.InstrumentDetailsTemplate("Instrument1", "123", generateString())).get();
        final Long instrument2 = createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), anyInstrumentModel(),
                new InstrumentManagementTemplate.InstrumentDetailsTemplate("Instrument2", "1234", generateString())).get();
        instrumentManagement.editInstrument(bob, instrument,
                new InstrumentManagementTemplate.InstrumentDetailsTemplate("Instrument2", "123", generateString()));
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testCanNotCreateInstrumentWithAlreadyUsedNameInLab() {
        final long bob = uc.createLab3AndBob();
        final Long instrument = createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), anyInstrumentModel(),
                new InstrumentManagementTemplate.InstrumentDetailsTemplate("Instrument1", "123", generateString())).get();
        final Long instrument2 = createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), anyInstrumentModel(),
                new InstrumentManagementTemplate.InstrumentDetailsTemplate("Instrument1", "1234", generateString())).get();
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testCanNotRequestInstrumentCreationWithoutLab() {
        final long bob = uc.createLab3AndBob();
        final long notExistingLabId = 999999;
        createInstrumentCreationRequest(bob, notExistingLabId);
    }

    @Test(expectedExceptions = ObjectNotFoundException.class)
    public void testCantReadDetailsOfRemovedInstrument() {
        final long bob = uc.createLab3AndBob();
        final long instrument = createInstrumentAndApproveIfNeeded(bob, uc.getLab3());
        detailsReader.readInstrument(bob, instrument);
        instrumentManagement.deleteInstrument(bob, instrument);
        detailsReader.readInstrument(bob, instrument);
    }

}
