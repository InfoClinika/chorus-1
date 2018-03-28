package com.infoclinika.mssharing.web.integration;

import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.web.helper.AbstractDataBasedTest;
import org.testng.annotations.Test;
import java.util.SortedSet;

import static org.junit.Assert.assertEquals;

/**
 * @author Pavel Kaplin
 */
public class LabHeadManagementTest extends AbstractDataBasedTest {



    @Test
    public void testRemoveUserFromLab() {
        SortedSet<InstrumentItem> instrumentItems = dashboardReader.readInstrumentsWhereUserIsOperator(pavelKaplinAtTeamdev());
        assertEquals(1, instrumentItems.size());
        labHeadManagement.removeUserFromLab(pavelKaplinAtGmail(), firstLab(), pavelKaplinAtTeamdev());
        instrumentItems = dashboardReader.readInstrumentsWhereUserIsOperator(pavelKaplinAtTeamdev());
        assertEquals(0, instrumentItems.size());
    }

}
