/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.services.billing.persistence.read.impl;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.helper.BillingFeatureItem;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.payment.AccountChargeableItemData;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem.Feature;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.internal.helper.billing.DatabaseBillingPropertiesProvider;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.ChargeableItemRepository;
import com.infoclinika.mssharing.model.internal.repository.LabPaymentAccountRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.read.BillingInfoReader;
import com.infoclinika.mssharing.model.write.billing.BillingManagement;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.services.billing.persistence.enity.ArchiveStorageVolumeUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.StorageVolumeUsage;
import com.infoclinika.mssharing.services.billing.persistence.helper.PaymentCalculationsHelper;
import com.infoclinika.mssharing.services.billing.persistence.helper.StorageUsageHelper;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.persistence.read.strategy.FeatureLogStrategy;
import com.infoclinika.mssharing.services.billing.persistence.repository.ArchiveStorageVolumeUsageRepository;
import com.infoclinika.mssharing.services.billing.persistence.repository.DailySummaryRepository;
import com.infoclinika.mssharing.services.billing.persistence.repository.StorageVolumeUsageRepository;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import com.infoclinika.mssharing.services.billing.rest.api.model.DailyUsageLine;
import com.infoclinika.mssharing.services.billing.rest.api.model.HistoryForMonthReference;
import com.infoclinika.mssharing.services.billing.rest.api.model.LabAccountFeatureInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Functions.compose;
import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.model.internal.read.Transformers.*;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkAccess;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * @author Elena Kurilina
 */
@Component
public class ChargeableItemUsageReaderImpl implements ChargeableItemUsageReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChargeableItemUsageReaderImpl.class);

    private final Comparator<AccountChargeableItemData> itemBillComparator =
        Comparator.comparing(o -> o.getChargeableItem().getFeature());
    @Inject
    private PaymentCalculationsHelper paymentCalculations;
    @Inject
    private UserRepository userRepository;
    @Inject
    private LabPaymentAccountRepository labPaymentAccountRepository;
    @Inject
    private ChargeableItemRepository chargeableItemRepository;
    @Inject
    private Collection<FeatureLogStrategy> featureStrategies = newArrayList();
    @Inject
    private RuleValidator validator;
    @Inject
    private StoredObjectPaths storedObjectPaths;
    @Inject
    private DailySummaryRepository dailySummaryRepository;
    @Inject
    private StorageVolumeUsageRepository storageVolumeUsageRepository;
    @Inject
    private ArchiveStorageVolumeUsageRepository archiveStorageVolumeUsageRepository;
    @Inject
    private DatabaseBillingPropertiesProvider databaseBillingPropertiesProvider;
    @Inject
    private StorageUsageHelper storageUsageHelper;

    @Inject
    private Transformers transformers;
    @Inject
    private BillingInfoReader billingInfoReader;
    @Inject
    private CloudStorageClientsProvider cloudStorageClientsProvider;

    private final Function<ChargeableItemBill, Long> totalCostTransformer = input -> input.total;

    @Override
    public Invoice readInvoice(long actor, long lab, Date from, Date to) {
        checkNotNull(from);
        checkNotNull(to);
        if (!validator.canReadLabBilling(actor, lab)) {
            throw new AccessDenied(String.format("User {%d} is not lab head of lab {%d}", actor, lab));
        }
        final LabPaymentAccount account = labPaymentAccountRepository.findByLab(lab);

        return formInvoice(account, formFeatureItem(account, rangeChargeableItemBillLogFn(lab, from, to, true)));
    }

    @Override
    public Invoice readInvoiceShortItem(long actor, long lab, Date from, Date to) {
        checkNotNull(from);
        checkNotNull(to);
        if (!validator.canReadLabBilling(actor, lab)) {
            throw new AccessDenied(String.format("User {%d} is not lab head of lab {%d}", actor, lab));
        }
        final LabPaymentAccount account = labPaymentAccountRepository.findByLab(lab);

        return formInvoice(account, formFeatureItem(account, rangeChargeableItemBillLogFn(lab, from, to, false)));
    }

    @Override
    public StorageUsage readStorageUsage(long actor, long lab) {
        final long rawFilesSize = storageUsageHelper.getRawFilesSize(lab, null);
        final long archivedFilesSize = storageUsageHelper.getArchivedFilesSize(lab, null);
        final long searchResultsFilesSize = storageUsageHelper.getSearchResultsFilesSize(lab);
        return new StorageUsage(rawFilesSize, archivedFilesSize, searchResultsFilesSize);
    }

    @Override
    public List<MonthlyCharge> readMonthlyCharges(long actor, long lab, Date date) {

        final ZoneId zoneId = ZoneId.of(transformers.serverTimezone.getID());
        final List<MonthlyCharge> result = Lists.newLinkedList();

        final ZonedDateTime dateProvided = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), zoneId);
        final StorageVolumeUsage storageVolumeUsage = storageVolumeUsageRepository.findLast(lab);

        if (storageVolumeUsage != null) {
            final ZonedDateTime nextChargeDate =
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(storageVolumeUsage.getTimestamp()), zoneId).plusMonths(1);
            final long maximumStorageUsage = paymentCalculations.calculateMaximumStorageUsage(
                lab,
                new Date(storageVolumeUsage.getTimestamp()),
                new Date(dateProvided.toInstant().toEpochMilli())
            );
            final int volumes = paymentCalculations.calculateStorageVolumes(maximumStorageUsage);
            final long cost = paymentCalculations.calculateStorageCost(volumes);
            result.add(
                new MonthlyCharge(
                    BillingFeature.STORAGE_VOLUMES,
                    transformers.historyLineDateFormat.format(new Date(nextChargeDate.toInstant().toEpochMilli())),
                    volumes,
                    maximumStorageUsage,
                    cost,
                    nextChargeDate.toInstant().toEpochMilli()
                )
            );
        }

        final ArchiveStorageVolumeUsage archiveStorageVolumeUsage = archiveStorageVolumeUsageRepository.findLast(lab);

        if (archiveStorageVolumeUsage != null) {
            final ZonedDateTime nextChargeDate =
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(archiveStorageVolumeUsage.getTimestamp()), zoneId)
                    .plusMonths(1);
            final long maximumArchiveStorageUsage = paymentCalculations.calculateMaximumArchiveStorageUsage(
                lab,
                new Date(archiveStorageVolumeUsage.getTimestamp()),
                new Date(dateProvided.toInstant().toEpochMilli())
            );
            final int volumes = paymentCalculations.calculateStorageVolumes(maximumArchiveStorageUsage);
            final long cost = paymentCalculations.calculateArchiveStorageCost(volumes);
            result.add(
                new MonthlyCharge(
                    BillingFeature.ARCHIVE_STORAGE_VOLUMES,
                    transformers.historyLineDateFormat.format(new Date(nextChargeDate.toInstant().toEpochMilli())),
                    volumes,
                    maximumArchiveStorageUsage,
                    cost,
                    nextChargeDate.toInstant().toEpochMilli()
                )
            );
        }

        return result;
    }

    private boolean autoprolongateFeature(long lab, BillingFeature billingFeature) {
        final Set<LabAccountFeatureInfo> labAccountFeatureInfos = billingInfoReader.readLabAccountFeatures(lab);
        final Optional<LabAccountFeatureInfo> featureUsage = labAccountFeatureInfos
            .stream()
            .filter(feature -> feature.name.equals(billingFeature.name()))
            .findFirst();
        return featureUsage.isPresent() && featureUsage.get().autoProlongate;
    }


    @Override
    public ImmutableSet<InvoiceLabLine> readLabsForUser(long actor) {
        Collection<LabPaymentAccount> labs = labPaymentAccountRepository.findByLabHeadId(actor);
        List<InvoiceLabLine> invoices =
            labs.stream().map(lab -> formLabLine(lab.getLab().getHead(), lab)).collect(Collectors.toList());
        return ImmutableSet.copyOf(invoices);
    }

    @Override
    public LabInvoiceDetails readLabDetails(long actor, long lab) {
        if (!validator.canReadLabBilling(actor, lab)) {
            throw new AccessDenied(String.format("User {%d} is not lab head of lab {%d}", actor, lab));
        }
        final LabPaymentAccount account = labPaymentAccountRepository.findByLab(lab);
        final Lab laboratory = account.getLab();

        final Set<LabAccountFeatureInfo> features = account.getBillingData().getFeaturesData()
            .stream()
            .map(
                f -> new LabAccountFeatureInfo(f.getChargeableItem().getFeature().name(), f.isActive(), account.getId(),
                    f.isAutoProlongate(), f.getQuantity()
                ))
            .collect(Collectors.toSet());

        final LabInvoiceDetails labInvoiceDetails = new LabInvoiceDetails(laboratory.getHead().getEmail(),
            laboratory.getName(), laboratory.getInstitutionUrl(),
            laboratory.getMembersAmount(),
            new FeaturesData(from(account.getBillingData().getFeaturesData())
                .transform(compose(toStringFunction(),
                    compose(BILLING_FEATURE_TRANSFORMER, CHARGEABLE_ITEM_FROM_ACCOUNT_TRANSFORMER)
                ))
                .toSet()),
            account.getAccountCreationDate(),
            paymentCalculations
                .calculateRoundedPriceByUnscaled(account.getStoreBalance(), account.getScaledToPayValue()),
            BillingManagement.LabPaymentAccountType.valueOf(account.getType().name()),
            account.isFree()
        );
        labInvoiceDetails.setLabAccountFeatures(features);

        return labInvoiceDetails;
    }

    @Override
    public PaginationItems.PagedItem<InvoiceLabLine> readPagedAllLabs(long actor,
                                                                      PaginationItems.PagedItemInfo pagedItemInfo) {
        PageRequest request = Transformers.PagedItemsTransformer.toPageRequest(LabPaymentAccount.class, pagedItemInfo);
        checkArgument(userRepository.findOne(actor).isAdmin());
        Page<LabPaymentAccount> labs = labPaymentAccountRepository.finaPagedAll(request);
        Set<InvoiceLabLine> invoices = new HashSet<>();
        for (LabPaymentAccount lab : labs) {
            final User user = lab.getLab().getHead();
            invoices.add(formLabLine(user, lab));
        }
        return new PaginationItems.PagedItem<>(
            labs.getTotalPages(),
            labs.getTotalElements(),
            labs.getNumber(),
            labs.getSize(),
            newArrayList(invoices)
        );
    }

    @Override
    public BillingFeatureItem readFeatureInfo(long featureId) {
        final ChargeableItem item = checkNotNull(chargeableItemRepository.findOne(featureId));
        final BillingFeature billingFeature = transformFeature(item.getFeature());
        return new BillingFeatureItem(
            item.getPrice(), billingFeature, billingFeature.getValue(), transformChargeType(item.getChargeType()),
            item.getChargeValue()
        );
    }

    @Override
    public List<BillingFeatureItem> readFeatures() {
        final Iterable<ChargeableItem> items = chargeableItemRepository.findAll();
        return newArrayList(items)
            .stream()
            .map(item -> {
                final BillingFeature billingFeature = transformFeature(item.getFeature());
                return new BillingFeatureItem(
                    item.getPrice(), billingFeature, billingFeature.getValue(),
                    transformChargeType(item.getChargeType()), item.getChargeValue()
                );
            })
            .collect(toList());
    }

    @Override
    public HistoryForMonthReference readMonthReference(long userId, long lab, Date month) {

        checkAccess(
            validator.canReadLabBilling(userId, lab), "User cannot read lab history. User=" + userId + ", Lab=" + lab);

        final ZoneId zoneId = transformers.serverTimezone.toZoneId();
        final LabPaymentAccount account = labPaymentAccountRepository.findByLab(lab);
        final ZonedDateTime accountCreationDate =
            ZonedDateTime.ofInstant(account.getAccountCreationDate().toInstant(), zoneId);
        final ZonedDateTime specifiedMonth = ZonedDateTime.ofInstant(month.toInstant(), zoneId);
        final ZonedDateTime nextMonthInHistory = specifiedMonth.minusMonths(1);
        return new HistoryForMonthReference(
            month,
            null,
            nextMonthInHistory.isAfter(accountCreationDate)
        );
    }

    @Override
    public Optional<DailyUsageLine> readDailyUsageLine(long lab, Date date) {

        return ofNullable(dailySummaryRepository
            .findByLabIdAndServerDayFormatted(lab, transformers.historyLineDateFormat.format(date)))
            .map(dailySummary -> new DailyUsageLine(
                dailySummary.getLabId(),
                dailySummary.getDate(),
                dailySummary.getServerDayFormatted(),
                dailySummary.getTimeZoneId(),
                dailySummary.getBalance(),
                dailySummary.getAmount()
            ));
    }

    @Override
    public Long readAnalyzableStorageUsage(long lab) {
        final long rawFilesSize = storageUsageHelper.getRawFilesSize(lab, null);
        final long searchResultsFilesSize = storageUsageHelper.getSearchResultsFilesSize(lab);
        return rawFilesSize + searchResultsFilesSize;
    }

    @Override
    public Long readAnalyzableStorageUsage(long lab, Date date) {
        final long daySinceEpoch = paymentCalculations.calculationDaySinceEpoch(date);
        final long rawFilesSize = storageUsageHelper.getRawFilesSize(lab, daySinceEpoch);
        final long searchResultsFilesSize = storageUsageHelper.getSearchResultsFilesSize(lab);
        return rawFilesSize + searchResultsFilesSize;
    }

    private InvoiceLabLine formLabLine(User user, LabPaymentAccount account) {
        final Lab lab = account.getLab();
        return new InvoiceLabLine(
            lab.getId(),
            lab.getName(),
            user.getFullName(),
            paymentCalculations
                .calculateRoundedPriceByUnscaled(account.getStoreBalance(), account.getScaledToPayValue())
        );
    }

    private Invoice formInvoice(LabPaymentAccount account, InvoiceFeatureItem featuresItem) {

        final Lab lab = account.getLab();
        final Date now = new Date();
        final long balance = account.getStoreBalance();
        final long unscalePrice = paymentCalculations.unscalePrice(account.getScaledToPayValue());
        final long storeBalance = balance - unscalePrice;

        final Long[] prices = from(featuresItem.features).transform(totalCostTransformer).toArray(Long.class);

        return new Invoice(
            lab.getName(),
            lab.getId(),
            storeBalance,
            sum(prices),
            now,
            featuresItem
        );
    }

    private InvoiceFeatureItem formFeatureItem(final LabPaymentAccount account,
                                               Function<AccountChargeableItemData,
                                               ChargeableItemBill> chargeableItemRetrieverFn) {

        final Set<AccountChargeableItemData> featuresForLab = account.getBillingData().getFeaturesData();
        final Set<AccountChargeableItemData> perFileFeatures = featuresForLab.stream()
            .filter(itemData -> newArrayList(Feature.values()).contains(itemData.getChargeableItem().getFeature()))
            .collect(Collectors.toSet());

        final ImmutableSortedSet<AccountChargeableItemData> sortedData =
            ImmutableSortedSet.copyOf(itemBillComparator, perFileFeatures);


        return new InvoiceFeatureItem(from(sortedData.descendingSet())
            .transform(chargeableItemRetrieverFn)
            .toSet()
        );
    }

    private Function<AccountChargeableItemData, ChargeableItemBill>
        rangeChargeableItemBillLogFn(final long lab,
                                     final Date from,
                                     final Date to,
                                     final boolean withUsageByUser) {

        final Date day = (new Date(from.getTime() + (to.getTime() - from.getTime()) / 2));

        return input -> {

            final ChargeableItem chargeableItem = input.getChargeableItem();
            final List<FeatureLogStrategy> logStrategies = getFeatureReaderStrategy(chargeableItem.getFeature());

            checkArgument(!logStrategies.isEmpty(), "No strategy is found for feature " + chargeableItem.getFeature());

            final Function<FeatureLogStrategy, ChargeableItemBill> withUsageByUserFn =
                logStrategy -> logStrategy.readBill(lab, from, to);
            final Function<FeatureLogStrategy, ChargeableItemBill> shortBillFn =
                logStrategy -> logStrategy.readShortBill(lab, day);

            final FluentIterable<ChargeableItemBill> billStream =
                from(logStrategies).transform(withUsageByUser ? withUsageByUserFn : shortBillFn);
            return billStream.firstMatch(bill -> bill.usageByUsers.size() > 0).or(billStream.first().get());

        };

    }

    private List<FeatureLogStrategy> getFeatureReaderStrategy(Feature input) {

        return featureStrategies.stream()
            .filter(storage -> storage.accept(input))
            .collect(toList());

    }

    private long sum(Long[] nums) {
        long total = 0;
        for (Long num : nums) {
            total += num;
        }
        return total;
    }


}
