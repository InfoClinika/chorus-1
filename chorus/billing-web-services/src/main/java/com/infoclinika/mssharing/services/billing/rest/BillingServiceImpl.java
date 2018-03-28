package com.infoclinika.mssharing.services.billing.rest;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.services.billing.persistence.BillingMigration;
import com.infoclinika.mssharing.services.billing.persistence.helper.PaymentCalculationsHelper;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.persistence.write.PaymentManagement;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import com.infoclinika.mssharing.services.billing.rest.api.model.*;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;

/**
 * Billing logging Web-service implementation
 * All external billing logging actions should be made via this service
 *
 * @author andrii.loboda
 */
public class BillingServiceImpl implements BillingService {
    private static final Logger LOG = Logger.getLogger(BillingServiceImpl.class);
    @Inject
    private PaymentManagement paymentManagement;
    @Inject
    private PaymentCalculationsHelper paymentCalculationsHelper;
    @Inject
    private ChargeableItemUsageReader chargeableItemUsageReader;
    @Inject
    private BillingMigration billingMigration;

    @Override
    public String healthCheck() {
        return "health Check";
    }

    @Override
    public void logProteinIDSearchUsage(long user, long experiment) {
        LOG.debug("Billing Service: log protein ID Search usage, user: " + user + ", experiment: " + experiment);
        paymentManagement.logProteinIDSearchUsage(user, experiment);
        LOG.debug("Billing Service: completed action - log protein ID Search usage, user: " + user + ", experiment: " + experiment);
    }

    @Override
    public void depositStoreCredit(DepositStoreCreditRequest request) {
        LOG.debug("Billing Service: deposit store credit paramsMap: " + request.paramsMap);
        checkNotNull(request);
        paymentManagement.depositStoreCredit(request.paramsMap);
        LOG.debug("Billing Service: completed action - deposit store credit paramsMap: " + request.paramsMap);
    }

    @Override
    public void logDownloadUsage(long actor, long file, long lab) {
        LOG.debug("Billing Service: download usage, actor: " + actor + ", file: " + file + ", lab: " + lab);
        paymentManagement.logDownloadUsage(actor, file, lab);
        LOG.debug("Billing Service: completed action - download usage, actor: " + actor + ", file: " + file + ", lab: " + lab);
    }

    @Override
    public void logPublicDownload(@Nullable Long actor, long file) {
        LOG.debug("Billing Service: log public download, actor:  " + actor + ", file: " + file);
        paymentManagement.logPublicDownload(actor, file);
        LOG.debug("Billing Service: completed action - log public download, actor:  " + actor + ", file: " + file);
    }

    @Override
    public Invoice readInvoiceShortItem(long actor, long lab, long from, long to) {
        LOG.debug("Billing Service: read invoice short item: " + actor);
        final ChargeableItemUsageReader.Invoice invoice = chargeableItemUsageReader.readInvoiceShortItem(actor, lab, new Date(from), new Date(to));
        final Invoice result = ChargeableItemUsageReader.Invoice.WS_TRANSFORM.apply(invoice);
        LOG.debug("Billing Service: completed action - read invoice short item: " + actor);
        return result;
    }

    @Override
    public Collection<InvoiceLabLine> readLabsForUser(long actor) {
        LOG.debug("Billing Service: read labs for user: " + actor);
        final ImmutableSet<InvoiceLabLine> result = from(chargeableItemUsageReader.readLabsForUser(actor)).
                transform(ChargeableItemUsageReader.InvoiceLabLine.WS_TRANSFORM).toSet();
        LOG.debug("Billing Service: completed action - read labs for user: " + actor);
        return result;
    }

    @Override
    public HistoryForMonthReference readMonthsReferences(long actor, long lab, long month) {
        LOG.debug("Billing Service: read read months references: " + actor);
        final java.util.Optional<HistoryForMonthReference> references = chargeableItemUsageReader.readMonthsReferences(actor, lab, new Date(month));
        LOG.debug("Billing Service: completed action - read months references: " + actor);
        return references.orElse(null);
    }

    @Override
    public LabInvoiceDetails readLabDetails(long actor, long lab) {
        LOG.debug("Billing Service: read lab details: " + lab);
        final ChargeableItemUsageReader.LabInvoiceDetails labInvoiceDetails = chargeableItemUsageReader.readLabDetails(actor, lab);
        final LabInvoiceDetails result = ChargeableItemUsageReader.LabInvoiceDetails.WS_TRANSFORM.apply(labInvoiceDetails);
        LOG.debug("Billing Service: completed action - read lab details: " + lab);
        return result;
    }

    @Override
    public PagedItemInfo.PagedItem<InvoiceLabLine> readPagedAllLabs(ReadPagedAllLabsRequest request) {
        LOG.debug("Billing Service: read paged all labs: " + request.actor);
        final Optional<PaginationItems.AdvancedFilterQueryParams> advancedFilterQueryParams = getAdvancedFilterQueryParamsOptional(request.wsPpagedItemInfo);
        PaginationItems.PagedItemInfo pagedItemInfo = new PaginationItems.PagedItemInfo(request.wsPpagedItemInfo.items, request.wsPpagedItemInfo.page,
                request.wsPpagedItemInfo.sortingField, request.wsPpagedItemInfo.isSortingAsc,
                request.wsPpagedItemInfo.filterQuery, advancedFilterQueryParams
        );
        final PaginationItems.PagedItem<ChargeableItemUsageReader.InvoiceLabLine> invoice = chargeableItemUsageReader.readPagedAllLabs(request.actor, pagedItemInfo);
        final PagedItemInfo.PagedItem<InvoiceLabLine> result = new PagedItemInfo.PagedItem<>(invoice.totalPages, invoice.itemsCount, invoice.pageNumber, invoice.pageSize,
                from(invoice.items).transform(ChargeableItemUsageReader.InvoiceLabLine.WS_TRANSFORM).toList());
        LOG.debug("Billing Service: completed action - read paged all labs: " + request.actor);
        return result;
    }

    @Override
    public BillingFeatureItem readFeatureInfo(long feature) {
        LOG.debug("Billing Service: read feature info: " + feature);
        final com.infoclinika.mssharing.model.helper.BillingFeatureItem billingFeatureItem = chargeableItemUsageReader.readFeatureInfo(feature);
        final BillingFeatureItem result = com.infoclinika.mssharing.model.helper.BillingFeatureItem.WS_TRANSFORM.apply(billingFeatureItem);
        LOG.debug("Billing Service: completed action - read feature info: " + feature);
        return result;
    }

    @Override
    public List<BillingFeatureItem> readFeatures() {
        LOG.debug("Billing Service: read all features infos.");
        final List<com.infoclinika.mssharing.model.helper.BillingFeatureItem> features = chargeableItemUsageReader.readFeatures();
        final List<BillingFeatureItem> transformedFeatures = features.stream().map(com.infoclinika.mssharing.model.helper.BillingFeatureItem.WS_TRANSFORM::apply).collect(Collectors.toList());
        LOG.debug("Billing Service: completed action - read features");
        return transformedFeatures;
    }

    @Override
    public long calculateRoundedPriceByUnscaled(long actualBalance, long unscaledValue) {
        LOG.debug("Billing Service: calculate rounded price by unscaled: {actualBalance:" + actualBalance + ", unscaledValue" + unscaledValue + "}");
        final long result = paymentCalculationsHelper.calculateRoundedPriceByUnscaled(actualBalance, unscaledValue);
        LOG.debug("Billing Service: completed action - calculate rounded price by unscaled: {actualBalance:" + actualBalance + ", unscaledValue" + unscaledValue + "}");
        return result;
    }

    @Override
    public long calculateTotalToPayForLabForDay(long lab, long day) {
        final Date dayDate = new Date(day);
        LOG.debug("Billing Service: calculate total to pay for lab for day: { lab: " + lab + ", day: " + dayDate + "}");
        final long result = paymentCalculationsHelper.calculateTotalToPayForLabForDay(lab, dayDate);
        LOG.debug("Billing Service: completed action - calculate total to pay for lab for day: { lab: " + lab + ", day: " + dayDate + "}");
        return result;
    }

    @Nullable
    @Override
    public Long calculateStoreBalanceForDay(long lab, long day) {
        final Date dayDate = new Date(day);
        LOG.debug("Billing Service: calculate store balance for day: { lab: " + lab + ", day: " + dayDate + "}");
        final Long result = paymentCalculationsHelper.calculateStoreBalanceForDay(lab, dayDate).orNull();
        LOG.debug("Billing Service: completed action - calculate store balance for day: { lab: " + lab + ", day: " + dayDate + "}");
        return result;
    }

    @Override
    public long calculateTotalToPayForLab(long lab, long from, long to) {
        final Date fromDate = new Date(from);
        final Date toDate = new Date(to);
        LOG.debug("Billing Service: calculate total to pay for lab:{ lab: " + lab + ", from: " + fromDate + ", to: " + toDate + "}");
        final long result = paymentCalculationsHelper.calculateTotalToPayForLab(lab, fromDate, toDate);
        LOG.debug("Billing Service: completed action - calculate total to pay for lab:{ lab: " + lab + ", from: " + fromDate + ", to: " + toDate + "}");
        return result;
    }

    @Nullable
    @Override
    public Long calculateStoreBalance(long lab, long current, long nextDay) {
        final Date currentDate = new Date(current);
        final Date nextDayDate = new Date(nextDay);
        LOG.debug("Billing Service: calculate store balance: { lab: " + lab + ", current: " + currentDate + ", nextDay: " + nextDayDate + "}");
        final Long result = paymentCalculationsHelper.calculateStoreBalance(lab, currentDate, nextDayDate).orNull();
        LOG.debug("Billing Service: completed action - calculate store balance: { lab: " + lab + ", current: " + currentDate + ", nextDay: " + nextDayDate + "}");
        return result;
    }

    @Nullable
    @Override
    public DailyUsageLine getDailyUsageLine(long lab, long day) {
        return chargeableItemUsageReader.readDailyUsageLine(lab, new Date(day)).orElse(new DailyUsageLine());
    }

    @Override
    public void logLabBecomeEnterprise(long actor, long lab, long time) {
        LOG.debug(String.format("Billing Service: lab enterprise, actor: %s, lab: %s, timestamp: %s", actor, lab, time));
        paymentManagement.logLabBecomeEnterprise(actor, lab, time);
        LOG.debug(String.format("Billing Service: completed action - lab enterprise, actor: %s, lab: %s, timestamp: %s", actor, lab, time));
    }

    @Override
    public void logLabBecomeFree(@FormParam("actor") long actor, @FormParam("lab") long lab, @FormParam("time") long time) {
        LOG.debug(String.format("Billing Service: lab free, actor: %s, lab: %s, timestamp: %s", actor, lab, time));
        paymentManagement.logLabBecomeFree(actor, lab, time);
        LOG.debug(String.format("Billing Service: completed action - lab free, actor: %s, lab: %s, timestamp: %s", actor, lab, time));
    }

    @Override
    public void logProcessingUsage(long actor, long lab, long time) {
        LOG.debug(String.format("Billing Service: processing usage, actor: %s, lab: %s, timestamp: %s", actor, lab, time));
        paymentManagement.logProcessingUsage(actor, lab, time);
        LOG.debug(String.format("Billing Service: completed action - processing usage, actor: %s, lab: %s, timestamp: %s", actor, lab, time));
    }

    @Override
    public void storeCreditForLab(long admin, long lab, long amount) {
        LOG.debug(String.format("Billing Service: store log credit, actor: %s, lab: %s, timestamp: %s", admin, lab, amount));
        paymentManagement.depositStoreCredit(admin, lab, amount);
        LOG.debug(String.format("Billing Service: completed action - store log credit, actor: %s, lab: %s, timestamp: %s", admin, lab, amount));
    }

    @Nullable
    @Override
    public List<PendingCharge> getPendingChargesForLab(long actor, long lab, long timestamp) {
        final List<ChargeableItemUsageReader.MonthlyCharge> monthlyCharges = chargeableItemUsageReader.readMonthlyCharges(actor, lab, new Date(timestamp));
        return monthlyCharges
                .stream()
                .map(
                        charge -> new PendingCharge(
                                charge.feature,
                                charge.serverDateFormatted,
                                charge.featureAmountUsed,
                                charge.sizeInBytes,
                                charge.charge,
                                charge.timestamp
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    public void runMigration(long admin) {
        LOG.debug("Billing Service: run account migration");
        billingMigration.migrateAccounts(admin);
        LOG.debug("Billing Service: account migration finished");
    }

    @Override
    public StorageUsage readStorageUsage(long actor, long lab) {
        final ChargeableItemUsageReader.StorageUsage storageUsage = chargeableItemUsageReader.readStorageUsage(actor, lab);
        return new StorageUsage(
                storageUsage.rawFilesSize,
                storageUsage.archivedFilesSize,
                storageUsage.translatedFilesSize,
                storageUsage.searchResultsFilesSize
        );
    }

    private static Optional<PaginationItems.AdvancedFilterQueryParams> getAdvancedFilterQueryParamsOptional(PagedItemInfo wsPpagedItemInfo) {
        final PagedItemInfo.AdvancedFilterQueryParams af = wsPpagedItemInfo.advancedFilter;
        final PaginationItems.AdvancedFilterQueryParams advancedFilterQueryParams = af == null ? null : new PaginationItems.AdvancedFilterQueryParams(af.conjunction, from(af.predicates).transform(new Function<PagedItemInfo.AdvancedFilterQueryParams.AdvancedFilterPredicateItem, PaginationItems.AdvancedFilterQueryParams.AdvancedFilterPredicateItem>() {
            @Override
            public PaginationItems.AdvancedFilterQueryParams.AdvancedFilterPredicateItem apply(PagedItemInfo.AdvancedFilterQueryParams.AdvancedFilterPredicateItem input) {
                final PaginationItems.AdvancedFilterQueryParams.AdvancedFilterPredicateItem.AdvancedFilterOperator operator = PaginationItems.AdvancedFilterQueryParams.AdvancedFilterPredicateItem.AdvancedFilterOperator.valueOf(input.operator.name());
                final PaginationItems.AdvancedFilterQueryParams.AdvancedFilterPredicateItem result = new PaginationItems.AdvancedFilterQueryParams.AdvancedFilterPredicateItem(
                        input.prop, input.value, operator);
                return result;
            }

        }).toList());
        return Optional.fromNullable(advancedFilterQueryParams);
    }

}
