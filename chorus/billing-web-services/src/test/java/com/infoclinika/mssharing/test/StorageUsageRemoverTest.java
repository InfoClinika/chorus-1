package com.infoclinika.mssharing.test;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.helper.AbstractBillingTest;
import com.infoclinika.mssharing.services.billing.persistence.helper.StorageUsageRemover;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Date;

import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Herman Zamula
 */
public class StorageUsageRemoverTest extends AbstractBillingTest {

    @Inject
    private StorageUsageRemover storageUsageRemover;

    @Test
    public void testLogsRemovesCorrectly() {

        long testTime = System.currentTimeMillis();
        long bob = uc.createLab3AndBob();
        final long head = uc.createPaul();
        uc.saveFile(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get());

        storageLogHelper.log(testTime);
        storageLogHelper.log(testTime + MILLIS_IN_HOUR);

        final ImmutableSet<? extends ChargeableItemUsageReader.UsageLine> usageLines =
            analyzableStorageBill(getInvoice(head, uc.getLab3())).usageByUsers.iterator().next().usageLines;
        assertTrue(usageLines.size() == 1);

        storageUsageRemover.removeTillDate(new Date(testTime));

        final ImmutableSet<? extends ChargeableItemUsageReader.UsageLine> usageLinesAfterRemoval =
            analyzableStorageBill(getInvoice(head, uc.getLab3())).usageByUsers.iterator().next().usageLines;
        assertTrue(usageLinesAfterRemoval.size() == 1);

        storageUsageRemover.removeTillDate(new Date(testTime + MILLIS_IN_HOUR));
        assertTrue(analyzableStorageBill(getInvoice(head, uc.getLab3())).usageByUsers.size() == 0);

    }

}
