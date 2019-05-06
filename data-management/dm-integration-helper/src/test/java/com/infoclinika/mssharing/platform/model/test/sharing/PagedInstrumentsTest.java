package com.infoclinika.mssharing.platform.model.test.sharing;

import com.infoclinika.mssharing.platform.model.PagedItem;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentAccess;
import static com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentLineTemplate;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


//TODO <herman.zamula>: Create unit tests for paged instruments
public class PagedInstrumentsTest extends AbstractPagedItemTest {

    @Test
    public void testDefaultHasOperatorAccess() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3());
        final InstrumentLineTemplate instrument =
            instrumentReader.readInstruments(bob, getPagedItemRequest()).iterator().next();
        assertEquals(instrument.access, InstrumentAccess.OPERATOR);
    }

    @Test
    public void testDefaultPagedHasAperatorAccess() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        uc.createInstrumentAndApproveIfNeeded(poll, uc.getLab3());
        final PagedItem<? extends InstrumentLineTemplate> instrument =
            instrumentReader.readInstrumentsByLab(bob, uc.getLab3(), getPagedItemRequest("laboratory"));
        assertTrue(instrument.items.size() == 1);
        assertEquals(instrument.iterator().next().access, InstrumentAccess.OPERATOR);
    }


}
