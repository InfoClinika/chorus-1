package com.infoclinika.mssharing.platform.model.test.write;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.test.helper.AbstractTest;
import org.testng.annotations.Test;

import java.util.SortedSet;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * @author Bogdan Kovalev
 *         Created on 11/14/16.
 */
public class LabHeadManagementImplTest extends AbstractTest {

    @Test
    public void testRemoveUserFromLab() throws Exception {
        final long lab3Id = uc.createLab3();
        final long lab3HeadId = labReader.readLab(lab3Id).labHead;
        final long kateId = uc.createKateAndLab2();
        uc.addKateToLab3();

        final long lab3InstrumentId = uc.createInstrumentAndApproveIfNeeded(lab3HeadId, lab3Id).get();
        instrumentManagement.addOperatorDirectly(lab3HeadId, lab3InstrumentId, kateId);

        SortedSet<InstrumentItem> instrumentsWithKate = instrumentReader.readInstrumentsWhereUserIsOperator(kateId);
        assertEquals(instrumentsWithKate.first().id, lab3InstrumentId, "Kate should be in lab3's instrument's operators list");

        labHeadManagement.removeUserFromLab(lab3HeadId, lab3Id, kateId);

        ImmutableSet kateLabs = labReader.readUserLabs(kateId);
        instrumentsWithKate = instrumentReader.readInstrumentsWhereUserIsOperator(kateId);

        assertFalse(kateLabs.contains(lab3Id), "Kate should not be in lab3");
        assertEquals(instrumentsWithKate.size(), 0, "Kate should not be in lab3's instrument operators list");
    }

}
