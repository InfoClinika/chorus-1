package com.infoclinika.mssharing.model.test.sharing;

import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.InstrumentLine;
import com.infoclinika.mssharing.platform.model.PagedItem;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


//TODO <herman.zamula>: Create unit tests for paged instruments
public class PagedInstrumentsTest extends AbstractPagedItemTest {

    @Test
    public void testDefaultHasOperatorAccess() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3());
        final InstrumentLine instrument = dashboardReader.readInstruments(bob, getPagedItemRequest()).iterator().next();
        assertEquals(instrument.access, DashboardReader.InstrumentAccess.OPERATOR);
    }

    @Test
    public void testDefaultPagedHasOperatorAccess() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3());
        final PagedItem<InstrumentLine> instrument =
            dashboardReader.readInstrumentsByLab(bob, uc.getLab3(), getPagedItemRequest("laboratory"));
        assertTrue(instrument.items.size() == 1);
        assertEquals(instrument.iterator().next().access, DashboardReader.InstrumentAccess.OPERATOR);
    }


}
