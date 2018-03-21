package com.infoclinika.mssharing.services.billing.persistence.write.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount.LabPaymentAccountType;
import com.infoclinika.mssharing.model.internal.entity.payment.StoreLogEntry;
import com.infoclinika.mssharing.model.internal.entity.payment.TransactionDetails;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.helper.billing.BillingPropertiesProvider;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.services.billing.persistence.enity.*;
import com.infoclinika.mssharing.services.billing.persistence.helper.BillingFeatureChargingHelper;
import com.infoclinika.mssharing.services.billing.persistence.helper.PaymentCalculationsHelper;
import com.infoclinika.mssharing.services.billing.persistence.repository.*;
import com.infoclinika.mssharing.services.billing.persistence.write.PaymentManagement;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.size;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkAccess;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;
import static com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature.PROTEIN_ID_SEARCH;
import static com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature.PUBLIC_DOWNLOAD;
import static java.lang.Math.abs;
import static java.math.BigDecimal.valueOf;

/**
 * @author Elena Kurilina
 */
@Service
@Transactional("billingLoggingTransactionManager")
public class PaymentManagementImpl implements PaymentManagement {

    private static final Logger LOGGER = Logger.getLogger(PaymentManagementImpl.class);

    @Inject
    private Provider<Date> current;
    @Inject
    private UserRepository userRepository;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private TranslationUsageRepository translationUsageRepository;
    @Inject
    private DownloadUsageRepository downloadUsageRepository;
    @Inject
    private PublicDownloadUsageRepository publicDownloadUsageRepository;
    @Inject
    private ProteinIDSearchUsageRepository proteinIDSearchUsageRepository;
    @Inject
    private LabPaymentAccountRepository labPaymentAccountRepository;
    @Inject
    private StoreCreditLogEntryRepository storeCreditLogEntryRepository;
    @Inject
    private TransactionDetailsRepository transactionDetailsRepository;
    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private PaymentCalculationsHelper paymentCalculations;
    @Inject
    private BillingFeatureChargingHelper featureChargingHelper;
    @Inject
    private StorageVolumeUsageRepository storageVolumeUsageRepository;
    @Inject
    private ArchiveStorageVolumeUsageRepository archiveStorageVolumeUsageRepository;
    @Inject
    private ProcessingUsageRepository processingUsageRepository;
    @Inject
    private BillingPropertiesProvider billingPropertiesProvider;
    @Inject
    private BillingFeatureChargingHelper billingFeatureChargingHelper;
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private SubscriptionChangeEntryRepository subscriptionChangeEntryRepository;

    @Value("${billing.storage.archive.download.restore.expiration}")
    private int unarchivedForDownloadMaxDays;
    @Value("${billing.server.timezone}")
    private String timeZoneId;


    @Override
    @Transactional
    public void logStorageVolumeUsage(long actor, long lab, int volumes, long time) {

        final Long volumeCost = billingPropertiesProvider.getEnterpriseAccountVolumeCost();
        final long totalCost = volumeCost * volumes;
        final long scaledTotalCost = paymentCalculations.scalePrice(totalCost);

        final BillingFeatureChargingHelper.ChargedInfo chargedInfo = billingFeatureChargingHelper.charge(lab, scaledTotalCost);
        final StorageVolumeUsage usage = new StorageVolumeUsage(
                actor,
                lab,
                time,
                volumes,
                scaledTotalCost,
                chargedInfo.balance,
                chargedInfo.scaledToPayValue,
                daysSinceEpoch(new Date(time))
        );
        storageVolumeUsageRepository.save(usage);
    }

    @Override
    public void logArchiveStorageVolumeUsage(long actor, long lab, int volumes, long time) {
        final Long volumeCost = billingPropertiesProvider.getEnterpriseAccountArchiveVolumeCost();
        final long totalCost = volumeCost * volumes;
        final long scaledTotalCost = paymentCalculations.scalePrice(totalCost);

        final BillingFeatureChargingHelper.ChargedInfo chargedInfo = billingFeatureChargingHelper.charge(lab, scaledTotalCost);
        final ArchiveStorageVolumeUsage usage = new ArchiveStorageVolumeUsage(
                actor,
                lab,
                time,
                volumes,
                scaledTotalCost,
                chargedInfo.balance,
                chargedInfo.scaledToPayValue,
                daysSinceEpoch(new Date(time))
        );
        archiveStorageVolumeUsageRepository.save(usage);
    }

    @Override
    public void logProcessingUsage(long actor, long lab, long time) {

        final long totalCost = billingPropertiesProvider.getProcessingFeatureCost();
        final long scaledTotalCost = paymentCalculations.scalePrice(totalCost);
        final BillingFeatureChargingHelper.ChargedInfo chargedInfo = billingFeatureChargingHelper.charge(lab, scaledTotalCost);
        processingUsageRepository.save(
                new ProcessingUsage(
                        actor,
                        lab,
                        time,
                        scaledTotalCost,
                        chargedInfo.balance,
                        chargedInfo.scaledToPayValue,
                        daysSinceEpoch(new Date(time - 1))
                )
        );
    }

    @Override
    public void logLabBecomeEnterprise(long actor, long lab, long time) {

        final LabPaymentAccount account = labPaymentAccountRepository.findByLab(lab);
        final StorageVolumeUsage storageLast = storageVolumeUsageRepository.findLast(lab);
        final ArchiveStorageVolumeUsage archiveLast = archiveStorageVolumeUsageRepository.findLast(lab);

        if(storageLast == null) {
            final StorageVolumeUsage usage = new StorageVolumeUsage(
                    actor,
                    lab,
                    time,
                    0,
                    0,
                    account.getStoreBalance(),
                    account.getScaledToPayValue(),
                    daysSinceEpoch(new Date(time))
            );

            storageVolumeUsageRepository.save(usage);
        }

        if(archiveLast == null) {
            final ArchiveStorageVolumeUsage usage = new ArchiveStorageVolumeUsage(
                    actor,
                    lab,
                    time,
                    0,
                    0,
                    account.getStoreBalance(),
                    account.getScaledToPayValue(),
                    daysSinceEpoch(new Date(time))
            );

            archiveStorageVolumeUsageRepository.save(usage);
        }

        logLabSubscriptionChange(actor, lab, time, LabPaymentAccountType.ENTERPRISE);
    }

    @Override
    public void logLabBecomeFree(long actor, long lab, long time) {
        logLabSubscriptionChange(actor, lab, time, LabPaymentAccountType.FREE);
    }

    @Transactional
    @Override
    public void depositStoreCredit(Map<String, String> paramsMap) {
        if (storeCreditLogEntryRepository.findByTransaction(paramsMap.get("txn_id")) == null) {

            final long lab = Long.valueOf(paramsMap.get("custom"));
            final long amount = toCents(paramsMap.get("payment_gross"));

            LOGGER.debug("*** Laboratory top up amount: " + amount + ", lab: " + lab);

            final LabPaymentAccount account = labPaymentAccountRepository.findByLab(lab);
            final TransactionDetails details = new TransactionDetails(paramsMap, account);
            transactionDetailsRepository.save(details);

            //final long totalToPay = paymentCalculations.calculateStoreBalanceForFeature(lab);
            account.setStoreBalance((account.getStoreBalance() + amount) /*- totalToPay*/);
            account.setPayByStore(account.getStoreBalance() < 0 ? abs(account.getStoreBalance()) : 0);

            labPaymentAccountRepository.save(account);

            final StoreLogEntry storeLogEntry = new StoreLogEntry(amount, lab,
                    current.get(),
                    account.getPayByStore(),
                    StoreLogEntry.Direction.IN,
                    account.getStoreBalance(),
                    paramsMap.get("txn_id"));
            storeLogEntry.transactionDetails = details;
            storeCreditLogEntryRepository.save(storeLogEntry);

            LOGGER.debug("Top Up for " + amount + " lab: " + lab);
        }
    }

    @Override
    public void depositStoreCredit(long admin, long lab, long amountCents) {

        checkAccess(ruleValidator.hasAdminRights(admin),
                "User has no admin rights to Top Up lab balance. User=" + admin + ", lab=" + lab);
        checkArgument(amountCents > 0, "Top up amount less than 0!");

        LOGGER.debug("Top up balance for lab " + lab + ". Amount in cents: " + amountCents);

        final LabPaymentAccount paymentAccount = checkPresence(labPaymentAccountRepository.findByLab(lab), "Lab no exists. Id=" + lab);

        paymentAccount.addBalance(amountCents);
        paymentAccount.setPayByStore(paymentAccount.getStoreBalance() < 0 ? abs(paymentAccount.getStoreBalance()) : 0);

        labPaymentAccountRepository.save(paymentAccount);

        createAndSaveStoreLogEntry(amountCents, paymentAccount);

        LOGGER.debug("Balance was sucessfully top up. Lab=" + lab + ". Current balance=" + paymentAccount.getStoreBalance());


    }

    private void createAndSaveStoreLogEntry(long amountCents, LabPaymentAccount paymentAccount) {

        final StoreLogEntry storeLogEntry = new StoreLogEntry(amountCents, paymentAccount.getLab().getId(),
                current.get(),
                paymentAccount.getPayByStore(),
                StoreLogEntry.Direction.IN,
                paymentAccount.getStoreBalance(),
                "admin-top-up");

        storeCreditLogEntryRepository.save(storeLogEntry);

    }

    @Transactional
    @Override
    public void logProteinIDSearchUsage(long creator, long experiment) {

        final ActiveExperiment activeExperiment = experimentRepository.findOne(experiment);
        final User experimentCreator = activeExperiment.getCreator();

        final Optional<Lab> billLab = fromNullable(activeExperiment.getBillLaboratory() == null ? activeExperiment.getLab() : activeExperiment.getBillLaboratory());

        if (!billLab.isPresent()) {
            LOGGER.warn("** Laboratory to send billing is not specified for experiment [" + activeExperiment.getId() + "]. Skipping...");
            return;
        }

        final Long lab = billLab.get().getId();
        final long dataSizeSum = paymentCalculations.sumPrices(
                activeExperiment.getRawFiles().getData()
                        .stream()
                        .map(input -> input.getFileMetaData().getSizeInBytes())
                        .collect(Collectors.toList())
        );
        final int filesCount = activeExperiment.getRawFiles().getData().size();
        final BigDecimal featurePrice = paymentCalculations.calculateScaledFeaturePrice(dataSizeSum, PROTEIN_ID_SEARCH);
        final Date timestamp = current.get();
        final ProteinIDSearchUsage searchUsage = new ProteinIDSearchUsage(
                lab,
                experimentCreator.getId(),
                activeExperiment.getId(),
                activeExperiment.getName(),
                dataSizeSum,
                timestamp,
                experimentCreator.getFullName(),
                featurePrice.longValue(),
                filesCount
        );

        searchUsage.setDay(daysSinceEpoch(timestamp));

        charge(lab, featurePrice, searchUsage);

        proteinIDSearchUsageRepository.save(searchUsage);

    }

    @Transactional
    @Override
    public void logDownloadUsage(long actor, long file, long lab) {
        //TODO: Validation
        //TODO: Refactor to avoid copy-paste
        final ActiveFileMetaData fileMetaData = fileMetaDataRepository.findOne(file);
        final User user = userRepository.findOne(actor);
        final BigDecimal amount = paymentCalculations.calculateScaledFeaturePrice(fileMetaData.getSizeInBytes(), BillingFeature.DOWNLOAD);

        final Date timestamp = current.get();
        final DownloadUsage entry = new DownloadUsage(
                lab,
                actor,
                file,
                fileMetaData.getSizeInBytes(),
                timestamp,
                user.getFullName(),
                fileMetaData.getInstrument().getName(),
                amount.longValue(),
                fileMetaData.getName()
        );

        entry.setDay(daysSinceEpoch(timestamp));

        charge(lab, amount, entry);

        downloadUsageRepository.save(entry);
    }

    @Transactional
    @Override
    public void logPublicDownload(@Nullable Long actor, long file) {
        //TODO: Validation
        //TODO: Refactor to avoid copy-paste
        final ActiveFileMetaData fileMetaData = fileMetaDataRepository.findOne(file);
        final Optional<User> user = actor != null ? Optional.of(userRepository.findOne(actor)) : Optional.<User>absent();

        final Date timestamp = current.get();
        final Iterable<LabPaymentAccount> labPaymentAccounts = labPaymentAccountRepository.findAll();

        final ImmutableSet.Builder<PublicDownloadUsage> usages = ImmutableSet.builder();

        final boolean wasArchivedDownloadOnly = fileMetaData.getStorageData().isArchivedDownloadOnly();

        final BigDecimal totalScaledPriceForEachLab = doGetPriceForFileForEachLab(fileMetaData, timestamp, size(labPaymentAccounts));

        for (LabPaymentAccount labPaymentAccount : labPaymentAccounts) {

            final Long lab = labPaymentAccount.getLab().getId();

            final PublicDownloadUsage entry = new PublicDownloadUsage(
                    lab,
                    actor,
                    file,
                    fileMetaData.getSizeInBytes(),
                    timestamp,
                    user.isPresent() ? user.get().getFullName() : "Anonymous user",
                    fileMetaData.getInstrument().getName(),
                    totalScaledPriceForEachLab.longValue(),
                    fileMetaData.getName(),
                    wasArchivedDownloadOnly
            );
            entry.setDay(daysSinceEpoch(timestamp));
            charge(lab, totalScaledPriceForEachLab, entry);
            usages.add(entry);

        }

        publicDownloadUsageRepository.save(usages.build());
    }

    private BigDecimal doGetPriceForFileForEachLab(ActiveFileMetaData fileMetaData, Date timestamp, int size) {

        final boolean archivedDownloadOnly = fileMetaData.getStorageData().isArchivedDownloadOnly();
        final boolean archivedDownloadCharged = fromNullable(fileMetaData.getStorageData().isArchivedDownloadCharged()).or(false);
        final BigDecimal amountForDownload = paymentCalculations.calculateScaledFeaturePrice(fileMetaData.getSizeInBytes(), PUBLIC_DOWNLOAD);

        if (archivedDownloadOnly && !archivedDownloadCharged) {
            final BigDecimal storagePriceForHour = BigDecimal.ZERO;

            fileMetaData.getStorageData().setArchivedDownloadCharged(true);
            fileMetaDataRepository.save(fileMetaData);

            return paymentCalculations.scalePriceBetweenLabs(
                    storagePriceForHour.multiply(valueOf(unarchivedForDownloadMaxDays * 24)).add(amountForDownload), size);
        } else {
            return paymentCalculations.scalePriceBetweenLabs(amountForDownload, size);
        }

    }

    //TODO: Charge usage by the interval (Ex. every hour) as well as the Active and Archive storage
    @Deprecated
    private void charge(long lab, BigDecimal amount, ChargeableItemUsage entry) {
        final BillingFeatureChargingHelper.ChargedInfo charge = featureChargingHelper.charge(lab, amount.longValue());
        entry.setScaledToPayValue(charge.scaledToPayValue);
        entry.setBalance(charge.balance);
    }

    private int daysSinceEpoch(Date timestamp) {
        return paymentCalculations.calculationDaySinceEpoch(timestamp);
    }


    private long toCents(String amount) {
        return (long) (Double.valueOf(amount) * 100);
    }

    private void logLabSubscriptionChange(long actor, long lab, long timestamp, LabPaymentAccountType subscription) {
        subscriptionChangeEntryRepository.save(new SubscriptionChangeEntry(actor, lab, subscription.getValue(), timestamp));
    }

}
