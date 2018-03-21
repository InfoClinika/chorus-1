package com.infoclinika.mssharing.web.integration;

import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.write.LabHeadManagement;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.web.demo.DemoDataBasedTest;
import org.junit.Test;

import javax.inject.Inject;
import java.util.SortedSet;

import static org.junit.Assert.assertEquals;

/**
 * @author Pavel Kaplin
 */
public class LabHeadManagementTest extends DemoDataBasedTest {

    @Inject
    private LabHeadManagement labHeadManagement;

    @Inject
    private DashboardReader dashboardReader;

    @Test
    public void testRemoveUserFromLab() {
        SortedSet<InstrumentItem> instrumentItems = dashboardReader.readInstrumentsWhereUserIsOperator(pavelKaplinAtTeamdev());
        assertEquals(1, instrumentItems.size());
        labHeadManagement.removeUserFromLab(pavelKaplinAtGmail(), firstLab(), pavelKaplinAtTeamdev());
        instrumentItems = dashboardReader.readInstrumentsWhereUserIsOperator(pavelKaplinAtTeamdev());
        assertEquals(0, instrumentItems.size());
    }

}
