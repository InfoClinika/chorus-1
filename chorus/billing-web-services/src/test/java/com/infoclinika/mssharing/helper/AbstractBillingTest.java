package com.infoclinika.mssharing.helper;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.helper.BillingFeaturesHelper;
import com.infoclinika.mssharing.services.billing.persistence.helper.PaymentCalculationsHelper;
import com.infoclinika.mssharing.services.billing.persistence.helper.StorageAndProcessingFeaturesUsageAnalyser;
import com.infoclinika.mssharing.services.billing.persistence.helper.StorageLogHelper;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.persistence.write.PaymentManagement;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import static com.google.common.base.Preconditions.checkState;
import static com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature.ANALYSE_STORAGE;
import static org.mockito.Mockito.reset;

/**
 * @author andrii.loboda
 */
@ContextConfiguration(classes = BillingSpringConfig.class)
public class AbstractBillingTest extends AbstractTest {
    public static final long GB_IN_BYTES = 1073741824L;
    public static final long MILLIS_IN_HOUR = 60 * 60 * 1000;

    @Inject
    @Named("noS3storageLogHelper")
    protected StorageLogHelper storageLogHelper;
    @Inject
    protected ChargeableItemUsageReader usageReader;
    @Inject
    protected PaymentManagement paymentManagement;
    @Inject
    private BillingRepositories billingRepositories;
    @Inject
    protected BillingFeaturesHelper billingFeaturesHelper;
    @Inject
    protected PaymentCalculationsHelper paymentCalculations;
    @Inject
    protected ChargeableItemUsageReader chargeableItemUsageReader;
    @Inject
    protected StorageAndProcessingFeaturesUsageAnalyser storageAndProcessingFeaturesUsageAnalyser;

    @BeforeMethod
    @Override
    public void setUp() {
        super.setUp();

        reset(super.applicationContext.getBean(Notifier.class));
        for (CrudRepository repo : billingRepositories.get()) {
            try {
                checkState(repo.count() == 0, repo.findAll());
            } catch (RuntimeException ex) {
                log.error("Failed on deletion {}", repo);
                throw Throwables.propagate(ex);
            }
        }
    }

    @AfterMethod
    @SuppressWarnings("unchecked")
    @Override
    public void tearDown() {
        super.tearDown();
        for (CrudRepository repo : billingRepositories.get()) {
            for (Object e : repo.findAll()) {
                try {
                    repo.delete(e);
                } catch (RuntimeException ex) {
                    log.error("Failed on deletion {}", e);
                    throw Throwables.propagate(ex);
                }
            }
            repo.deleteAll();
        }
    }

    protected ChargeableItemUsageReader.ChargeableItemBill getBill(ChargeableItemUsageReader.Invoice invoice,
                                                                   final BillingFeature feature) {
        return invoice.featureItem.features.stream().filter(input -> input.type.equals(feature)).findFirst().get();
    }

    protected long toCents(Long dollars) {
        return (long) (Double.valueOf(dollars) * 100);
    }

    protected ChargeableItemUsageReader.ChargeableItemBill analyzableStorageBill(
        ChargeableItemUsageReader.Invoice invoice) {
        return getBill(invoice, ANALYSE_STORAGE);
    }

    protected ChargeableItemUsageReader.Invoice getInvoice(long actor, long lab) {
        return chargeableItemUsageReader
            .readInvoice(actor, lab, new Date(new Date().getTime() - 1000 * 60 * 10), new Date());
    }

    protected ChargeableItemUsageReader.Invoice getInvoice(long actor, long lab, long from, long to) {
        return chargeableItemUsageReader.readInvoice(actor, lab, new Date(from), new Date(to));
    }

    protected void simulateHours(int hours) {
        simulateHours(System.currentTimeMillis(), hours, false);
    }

    protected long simulateHours(long from, int hours, boolean simulateProcessingAndStorageVolumesUsageLogging) {
        int j = 1;
        long now = from;
        for (int i = 1; i <= hours; i++) {
            storageLogHelper.log(now);
            if (simulateProcessingAndStorageVolumesUsageLogging) {
                storageAndProcessingFeaturesUsageAnalyser.analyseStorageVolumeUsage(now);
            }

            now += MILLIS_IN_HOUR;  // add 1 hour in millis
            try {
                //TODO:2016-03-04:herman.zamula: Thread.sleep hack is used for the test usages logging on the slow
                // machines. Fix storageLogHelper for the tests
                Thread.sleep(10);
                if (j == 24) {
                    Thread.sleep(10);
                    storageLogHelper.sumLogs(new Date(now));
                    j = 0;
                }
                j++;
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }

        return now;
    }

    protected void depositStoreCredit(long lab, long amount) {
        final Map<String, String> paramsMap = Maps.newHashMap();
        paramsMap.put("custom", Long.toString(lab));
        paramsMap.put("payment_gross", Long.toString(amount));
        paymentManagement.depositStoreCredit(paramsMap);
    }

    protected void logStorage() {
        storageLogHelper.log(1000);
        storageLogHelper.sumLogs(new Date());
    }

    protected long createFilesArchiveAndLog(long actor, long lab) {
        long totalFilesSize = 0;
        final int minimumFilesCount = 5;
        final int maximumFilesCount = 10;
        final int minimumFileSize = 110;
        final int maximumFileSize = 300;
        final Random random = new Random(new Random().nextLong());
        final int filesCount = random.nextInt(maximumFilesCount - minimumFilesCount + 1) + minimumFilesCount;

        for (int file = 0; file < filesCount; file++) {
            final long fileSize =
                GB_IN_BYTES * (random.nextInt(maximumFileSize - minimumFileSize + 1) + minimumFileSize);
            totalFilesSize += fileSize;
            final long fileId =
                uc.saveFileWithSize(actor, uc.createInstrumentAndApproveIfNeeded(actor, lab).get(), fileSize);
            fileMovingManager.moveToArchiveStorage(fileId);
        }

        simulateHours(24);

        return totalFilesSize;
    }

    protected long createFilesAndLog(long actor, long lab) {
        long totalFilesSize = 0;
        final int minimumFilesCount = 5;
        final int maximumFilesCount = 10;
        final int minimumFileSize = 110;
        final int maximumFileSize = 300;
        final Random random = new Random(new Random().nextLong());
        final int filesCount = random.nextInt(maximumFilesCount - minimumFilesCount + 1) + minimumFilesCount;

        for (int file = 0; file < filesCount; file++) {
            final long fileSize =
                GB_IN_BYTES * (random.nextInt(maximumFileSize - minimumFileSize + 1) + minimumFileSize);
            totalFilesSize += fileSize;
            uc.saveFileWithSize(actor, uc.createInstrumentAndApproveIfNeeded(actor, lab).get(), fileSize);
        }

        simulateHours(24);

        return totalFilesSize;
    }
}
