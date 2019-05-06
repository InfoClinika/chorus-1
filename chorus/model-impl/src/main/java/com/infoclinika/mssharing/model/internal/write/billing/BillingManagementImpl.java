package com.infoclinika.mssharing.model.internal.write.billing;

import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.payment.AccountChargeableItemData;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.internal.helper.billing.DatabaseBillingPropertiesProvider;
import com.infoclinika.mssharing.model.internal.repository.AccountChargeableItemDataRepository;
import com.infoclinika.mssharing.model.internal.repository.ChargeableItemRepository;
import com.infoclinika.mssharing.model.internal.repository.LabPaymentAccountRepository;
import com.infoclinika.mssharing.model.write.billing.BillingManagement;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.propertiesprovider.BillingPropertiesProvider;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingChargeType;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import com.infoclinika.mssharing.services.billing.rest.api.model.StorageUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem.ChargeType.GB;
import static com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount.LabPaymentAccountType.ENTERPRISE;
import static com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount.LabPaymentAccountType.FREE;
import static com.infoclinika.mssharing.model.internal.read.Transformers.transformFeature;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkAccess;

/**
 * @author andrii.loboda
 */
@Service
public class BillingManagementImpl implements BillingManagement {
    private static final Logger LOGGER = LoggerFactory.getLogger(BillingManagementImpl.class);
    private static final long ENTERPRISE_ACCOUNT_AVAILABLE_STORAGE_SIZE = Long.MAX_VALUE;

    @Inject
    private LabPaymentAccountRepository labPaymentAccountRepository;

    @Inject
    private ChargeableItemRepository chargeableItemRepository;

    @Resource(name = "billingService")
    private BillingService billingService;

    @Inject
    private DatabaseBillingPropertiesProvider propertiesProvider;

    @Inject
    private AccountChargeableItemDataRepository accountChargeableItemDataRepository;

    @Inject
    private RuleValidator ruleValidator;

    @Inject
    private BillingPropertiesProvider billingPropertiesProvider;

    @Override
    public long createChargeableItem(int price, BillingFeature feature, int chargeValue, BillingChargeType type) {
        final ChargeableItem item = chargeableItemRepository.findByFeature(transformFeature(feature));
        if (item != null) {
            return item.getId();
        }
        return chargeableItemRepository.save(new ChargeableItem(
            price,
            transformFeature(feature),
            chargeValue,
            transformChargeType(type)

        )).getId();
    }

    @Override
    public void makeLabAccountEnterprise(long actor, long lab) {

        if (!ruleValidator.canUserManageLabAccount(actor, lab)) {
            throw new AccessDenied("Access Denied");
        }

        final LabPaymentAccount account = labPaymentAccountRepository.findByLab(lab);

        if (account.getType() == ENTERPRISE) {
            throw new RuntimeException("Lab account is already Enterprise. Lab ID: " + lab);
        }

        final Date now = new Date();

        account.setType(ENTERPRISE);
        account.setLastTypeUpdateDate(now);

        activateFeature(account, ChargeableItem.Feature.ARCHIVE_STORAGE_VOLUMES);
        activateFeature(account, ChargeableItem.Feature.ARCHIVE_STORAGE);
        activateFeature(account, ChargeableItem.Feature.STORAGE_VOLUMES);

        billingService.logLabBecomeEnterprise(actor, lab, now.getTime());
        labPaymentAccountRepository.save(account);
    }

    @Override
    public void makeLabAccountFree(long actor, long lab) {

        if (!ruleValidator.canUserManageLabAccount(actor, lab)) {
            throw new RuntimeException("Access Denied");
        }

        final LabPaymentAccount account = labPaymentAccountRepository.findByLab(lab);

        if (account.getType() == FREE) {
            throw new RuntimeException("Lab account is already Free. Lab ID: " + lab);
        }

        final MakeAccountFreeCheckResult checkResult = checkCanMakeAccountFree(actor, lab);

        if (!checkResult.canChange) {
            throw new RuntimeException(
                "Not sufficient amount of time passed since becoming enterprise to become " +
                    "free again or storage usage exceeds limits. Lab ID: " +
                    lab + " Expected time to pass: " + billingPropertiesProvider.getPlanChangeDuration());
        }

        final Date now = new Date();

        account.setType(FREE);
        account.setLastTypeUpdateDate(now);

        deactivateFeature(account, ChargeableItem.Feature.ARCHIVE_STORAGE_VOLUMES);
        deactivateFeature(account, ChargeableItem.Feature.ARCHIVE_STORAGE);
        deactivateFeature(account, ChargeableItem.Feature.STORAGE_VOLUMES);

        billingService.logLabBecomeFree(actor, lab, now.getTime());
        labPaymentAccountRepository.save(account);
    }

    @Override
    public MakeAccountFreeCheckResult checkCanMakeAccountFree(long actor, long lab) {

        if (!ruleValidator.canUserManageLabAccount(actor, lab)) {
            throw new RuntimeException("Access Denied");
        }

        final LabPaymentAccount account = labPaymentAccountRepository.findByLab(lab);

        if (account.isFree()) {
            return MakeAccountFreeCheckResult.ok();
        }

        final StorageUsage storageUsage = billingService.readStorageUsage(actor, lab);
        final long analyzableStorageLimitExceeded = analyzableStorageLimitExceededSizeForFreeAccount(storageUsage);
        final long archiveStorageLimitExceeded = archiveStorageLimitExceededSizeForFreeAccount(storageUsage);
        final boolean planChangeDurationPassed = isPlanChangeDurationPassed(account);
        long allowedAfterTimestamp = 0;

        if (!planChangeDurationPassed) {
            final Instant instant = account.getLastTypeUpdateDate().toInstant();
            final ZonedDateTime lastUpdateDate =
                ZonedDateTime.from(instant.atZone(ZoneId.of(billingPropertiesProvider.getTimeZoneId())));
            final Duration preciseDuration = Duration.parse(billingPropertiesProvider.getPlanChangeDuration());
            final int planChangeDurationMonths = billingPropertiesProvider.getPlanChangeDurationMonths();
            final ZonedDateTime whenAllowed = lastUpdateDate
                .plusMonths((long) planChangeDurationMonths)
                .plusNanos(preciseDuration.toNanos());
            allowedAfterTimestamp = whenAllowed.toInstant().toEpochMilli();
        }

        final boolean canChange =
            analyzableStorageLimitExceeded <= 0 && archiveStorageLimitExceeded <= 0 && planChangeDurationPassed;

        return new MakeAccountFreeCheckResult(
            canChange,
            allowedAfterTimestamp,
            analyzableStorageLimitExceeded,
            archiveStorageLimitExceeded
        );
    }

    @Override
    public long availableStorageSize(long actor, long lab) {

        final LabPaymentAccount account = labPaymentAccountRepository.findByLab(lab);

        if (account.getType() == FREE) {
            try {
                final long uploadLimitForLab = propertiesProvider.getFreeAccountStorageLimit();
                final StorageUsage storageUsage = billingService.readStorageUsage(actor, lab);
                final long totalFilesSizeForLab = storageUsage.rawFilesSize;
                return uploadLimitForLab - totalFilesSizeForLab;
            } catch (Exception ex) {
                LOGGER.warn("Error occurred when retrieve available storage size", ex);
            }
        }

        return ENTERPRISE_ACCOUNT_AVAILABLE_STORAGE_SIZE;
    }

    @Override
    public void updateLabAccountSubscriptionDetails(long actor, SubscriptionInfo subscriptionInfo) {
        LOGGER.debug("Updating account subscription details using next info: {}", subscriptionInfo);

        final long lab = subscriptionInfo.labId;
        final LabPaymentAccount account = labPaymentAccountRepository.findByLab(lab);

        // handle lab account type change
        if (!account.getType().name().equals(subscriptionInfo.accountType.name())) {
            if (LabPaymentAccountType.ENTERPRISE.equals(subscriptionInfo.accountType)) {
                makeLabAccountEnterprise(actor, lab);
            } else {
                makeLabAccountFree(actor, lab);
            }
        }
    }

    @Override
    public void topUpLabBalance(long admin, long lab, long amountCents) {

        checkAccess(
            ruleValidator.hasAdminRights(admin),
            "User has no admin rights to Top Up lab balance. User=" + admin + ", lab=" + lab
        );
        checkArgument(amountCents > 0, "Top up amount less than 0!");

        billingService.storeCreditForLab(admin, lab, amountCents);

    }

    private Optional<AccountChargeableItemData> getFeatureForAccount(
        LabPaymentAccount account,
        ChargeableItem.Feature featureType
    ) {
        return account.getBillingData().getFeaturesData()
            .stream()
            .filter(f -> f.getChargeableItem().getFeature().equals(featureType))
            .findFirst();
    }

    private ChargeableItem.ChargeType transformChargeType(BillingChargeType type) {
        switch (type) {
            case PER_GB:
                return GB;
            default:
                throw new AssertionError("Unknown type: " + type);
        }
    }

    private boolean canLabAccountAffordProcessing(LabPaymentAccount account) {
        final long currentBalance = account.getStoreBalance();
        return currentBalance >= 0;
    }

    private void deactivateFeature(LabPaymentAccount account, ChargeableItem.Feature billingFeature) {
        updateFeatureUsage(account, billingFeature, false, 0, false);
    }

    private AccountChargeableItemData activateFeature(
        LabPaymentAccount account,
        ChargeableItem.Feature billingFeature
    ) {
        return updateFeatureUsage(account, billingFeature, true, 0, true);
    }

    private AccountChargeableItemData updateFeatureUsage(
        LabPaymentAccount account,
        ChargeableItem.Feature billingFeature,
        boolean isActive,
        int quantity,
        boolean autoProlongate
    ) {

        final ChargeableItem chargeableItem = chargeableItemRepository.findByFeature(billingFeature);
        final Optional<AccountChargeableItemData> featureUsageOptional = account.getBillingData().getFeaturesData()
            .stream()
            .filter(f -> f.getChargeableItem().getId().equals(chargeableItem.getId()))
            .findFirst();

        final AccountChargeableItemData featureUsage;

        if (featureUsageOptional.isPresent()) {
            featureUsage = featureUsageOptional.get();
            featureUsage.setQuantity(quantity);
            featureUsage.setAutoProlongate(autoProlongate);
            featureUsage.setChangeDate(new Date());
            featureUsage.setActive(isActive);
        } else {
            featureUsage = new AccountChargeableItemData(chargeableItem, account, quantity, isActive, autoProlongate);
            account.getBillingData().getFeaturesData().add(featureUsage);
        }

        return accountChargeableItemDataRepository.save(featureUsage);
    }

    private long analyzableStorageLimitExceededSizeForFreeAccount(StorageUsage usage) {
        final long analyzableStorageSize =
            usage.rawFilesSize + usage.searchResultsFilesSize;
        return analyzableStorageSize - propertiesProvider.getFreeAccountStorageLimit();
    }

    private long archiveStorageLimitExceededSizeForFreeAccount(StorageUsage usage) {
        return usage.archivedFilesSize - propertiesProvider.getFreeAccountArchiveStorageLimit();
    }

    private boolean isPlanChangeDurationPassed(LabPaymentAccount account) {
        final Instant instant = account.getLastTypeUpdateDate().toInstant();
        final String timeZoneId = billingPropertiesProvider.getTimeZoneId();
        final ZonedDateTime lastUpdateDate = ZonedDateTime.from(instant.atZone(ZoneId.of(timeZoneId)));
        final ZonedDateTime now = ZonedDateTime.from(Instant.now().atZone(ZoneId.of(timeZoneId)));
        final Duration preciseDuration = Duration.parse(billingPropertiesProvider.getPlanChangeDuration());
        final int planChangeDurationMonths = billingPropertiesProvider.getPlanChangeDurationMonths();

        return lastUpdateDate.plus(preciseDuration).plusMonths((long) planChangeDurationMonths).isBefore(now);
    }

}
