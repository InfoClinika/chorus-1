package com.infoclinika.mssharing.services.billing.persistence;

import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.payment.AccountChargeableItemData;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.internal.entity.view.ExperimentDashboardRecord;
import com.infoclinika.mssharing.model.internal.helper.billing.BillingPropertiesProvider;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.write.billing.BillingManagement;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.services.billing.persistence.helper.StorageUsageHelper;
import com.infoclinika.mssharing.services.billing.persistence.repository.DailyAnalyseStorageUsageRepository;
import com.infoclinika.mssharing.services.billing.persistence.repository.DailyArchiveStorageUsageRepository;
import com.infoclinika.mssharing.services.billing.persistence.write.PaymentManagement;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author timofei.kasianov 4/28/16
 */
@Component
@Transactional
public class BillingMigration {

    private static final long MAX_RUN_INTERVAL = 30 * 24 * 60 * 60 * 1000L;

    @Inject
    private LabPaymentAccountRepository labPaymentAccountRepository;
    @Inject
    private ChargeableItemRepository chargeableItemRepository;
    @Inject
    private PaymentManagement paymentManagement;
    @Inject
    private DailyAnalyseStorageUsageRepository dailyAnalyseStorageUsageRepository;
    @Inject
    private DailyArchiveStorageUsageRepository dailyArchiveStorageUsageRepository;
    @Inject
    private BillingPropertiesProvider billingPropertiesProvider;
    @Inject
    private BillingManagement billingManagement;
    @Inject
    private StorageUsageHelper storageUsageHelper;
    @Inject
    private LabRepository labRepository;
    @Inject
    private RuleValidator ruleValidator;
    @PersistenceContext(unitName = "mssharing")
    private EntityManager em;

    public void migrateAccounts(long admin) {
        if(!ruleValidator.hasAdminRights(admin)) {
            throw new AccessDenied("Only admin can run billing migration");
        }
        labPaymentAccountRepository.findAll().forEach(account -> migrateAccount(admin, account));
    }

    private void migrateAccount(long admin, LabPaymentAccount account) {
        resetEnabledFeature(account);
        resetBalance(admin, account);
        makeEnterpriseIfNeeded(account);
    }

    private void resetEnabledFeature(LabPaymentAccount account) {


        final Iterable<ChargeableItem> chargeableItems = chargeableItemRepository.findEnabledByDefault();
        account.getBillingData().getFeaturesData().clear();
        account.getBillingData().getFeaturesData().addAll(StreamSupport
                .stream(chargeableItems.spliterator(), false)
                .map(input -> new AccountChargeableItemData(true, input, account))
                .collect(Collectors.toSet()));

        labPaymentAccountRepository.save(account);
    }

    private void resetBalance(long admin, LabPaymentAccount account) {
        final long storeBalance = account.getStoreBalance();
        if(storeBalance < 0) {
            if(account.getScaledToPayValue() != 0) {
                account.setScaledToPayValue(0);
                labPaymentAccountRepository.save(account);
            }
            paymentManagement.depositStoreCredit(admin, account.getLab().getId(), Math.abs(storeBalance));
        }
    }

    private void makeEnterpriseIfNeeded(LabPaymentAccount account) {

        final Long ladId = account.getLab().getId();
        final Long labHeadId = getLabHead(ladId);
        final Long freeAccountStorageLimit = billingPropertiesProvider.getFreeAccountStorageLimit();
        final Long lastProcessedDaySinceEpoch = dailyAnalyseStorageUsageRepository.getLastProcessedDaySinceEpoch(ladId);
        final long storageUsageForDay = lastProcessedDaySinceEpoch != null ?
                getAnalyzableStorageUsage(ladId, lastProcessedDaySinceEpoch) :
                0;

        if(storageUsageForDay > freeAccountStorageLimit) {
            billingManagement.makeLabAccountEnterprise(labHeadId, ladId);
        } else {

            final Long freeAccountArchiveStorageLimit = billingPropertiesProvider.getFreeAccountArchiveStorageLimit();
            final Long lastDayArchiveStorage = dailyArchiveStorageUsageRepository.getLastProcessedDaySinceEpoch(ladId);
            final long archiveStorageUsageForDay = lastDayArchiveStorage != null ?
                    dailyArchiveStorageUsageRepository.getStorageUsageForDay(ladId, lastDayArchiveStorage) :
                    0;

            if(archiveStorageUsageForDay > freeAccountArchiveStorageLimit) {
                billingManagement.makeLabAccountEnterprise(labHeadId, ladId);
            }

        }
    }

    private long getLabHead(long labId) {
        final Lab lab = labRepository.findOne(labId);
        return lab.getHead().getId();
    }

    private List<ExperimentDashboardRecord> findExperimentsByLab(long lab) {
        final TypedQuery<ExperimentDashboardRecord> query = em.createQuery(ExperimentRepository.FIND_ALL_BY_LAB_WITH_ADVANCED_FILTER, ExperimentDashboardRecord.class);
        query.setParameter("lab", lab);
        return query.getResultList();
    }

    private long getAnalyzableStorageUsage(long lab, long daySinceEpoch) {
        final long rawFilesSize = storageUsageHelper.getRawFilesSize(lab, daySinceEpoch);
        final long translatedFilesSize = storageUsageHelper.getTranslatedFilesSize(lab, daySinceEpoch);
        final long searchResultsFilesSize = storageUsageHelper.getSearchResultsFilesSize(lab);
        return rawFilesSize + translatedFilesSize + searchResultsFilesSize;
    }
}
