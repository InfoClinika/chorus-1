package com.infoclinika.mssharing.web.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams;
import com.infoclinika.mssharing.model.PaginationItems.PagedItem;
import com.infoclinika.mssharing.model.helper.BillingHelper;
import com.infoclinika.mssharing.model.read.BillingInfoReader;
import com.infoclinika.mssharing.model.read.PaymentHistoryReader;
import com.infoclinika.mssharing.model.write.billing.BillingManagement;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService.ReadPagedAllLabsRequest;
import com.infoclinika.mssharing.services.billing.rest.api.model.*;
import com.infoclinika.mssharing.web.controller.request.UpdateLabAccountRequest;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import com.infoclinika.mssharing.web.downloader.BillingHistoryDownloadHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.model.read.PaymentHistoryReader.HistoryForLab;
import static com.infoclinika.mssharing.model.write.billing.BillingManagement.*;
import static com.infoclinika.mssharing.model.write.billing.BillingManagement.LabPaymentAccountType.valueOf;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;
import static com.infoclinika.mssharing.services.billing.rest.api.model.PagedItemInfo.AdvancedFilterQueryParams.AdvancedFilterPredicateItem;

/**
 * @author Elena Kurilina
 */
@Controller
@RequestMapping("/billing")
public class BillingController extends PagedItemsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillingController.class);
    private static Function<PaginationItems.PagedItemInfo, PagedItemInfo> PAGED_ITEM_INFO_WS = input -> {
        final PagedItemInfo.AdvancedFilterQueryParams advancedFilterQueryParams = getAdvancedFilterQueryParams(input);
        final Optional<String> s = Optional.of("");
        return new PagedItemInfo(input.items, input.page, input.sortingField, input.isSortingAsc,
            input.filterQuery, advancedFilterQueryParams
        );
    };
    @Resource(name = "billingService")
    private BillingService billingService;
    @Inject
    private PaymentHistoryReader paymentHistoryReader;
    @Inject
    private BillingHelper billingHelper;
    @Inject
    private BillingHistoryDownloadHelper billingHistoryDownloadHelper;
    @Inject
    private BillingManagement billingManagement;
    @Inject
    private BillingInfoReader billingInfoReader;

    private static PagedItemInfo.AdvancedFilterQueryParams getAdvancedFilterQueryParams(
        PaginationItems.PagedItemInfo input
    ) {
        final AdvancedFilterQueryParams advancedFilterQueryParams = input.advancedFilter.orNull();
        final PagedItemInfo.AdvancedFilterQueryParams advancedFilterQueryParamsWS;
        if (advancedFilterQueryParams == null) {
            advancedFilterQueryParamsWS = null;
        } else {
            advancedFilterQueryParamsWS = new PagedItemInfo.AdvancedFilterQueryParams(
                advancedFilterQueryParams.conjunction,
                from(advancedFilterQueryParams.predicates).transform(input1 -> new AdvancedFilterPredicateItem(
                    input1.prop, input1.value,
                    AdvancedFilterPredicateItem.AdvancedFilterOperator.valueOf(
                        input1.operator.name())
                )).toList()
            );
        }

        return advancedFilterQueryParamsWS;
    }

    @ResponseBody
    @RequestMapping(value = "/invoice")
    public Invoice getInvoice(
        @RequestParam long lab,
        @RequestParam long dateFrom,
        @RequestParam long dateTo,
        Principal principal
    ) {
        return billingService.readInvoiceShortItem(getUserId(principal), lab, dateFrom, dateTo);
    }

    @RequestMapping(value = "/getPendingCharges")
    @ResponseBody
    public ValueResponse<List<PendingCharge>> getPendingCharges(@RequestParam long lab, Principal principal) {
        return new ValueResponse<>(billingService.getPendingChargesForLab(
            getUserId(principal),
            lab,
            new Date().getTime()
        ));
    }

    @RequestMapping(value = "/getStorageUsage")
    @ResponseBody
    public ValueResponse<StorageUsage> getStorageUsage(@RequestParam long lab, Principal principal) {
        return new ValueResponse<>(billingService.readStorageUsage(getUserId(principal), lab));
    }

    @ResponseBody
    @RequestMapping(value = "/list")
    public Collection<InvoiceLabLine> list(Principal principal) {
        return billingService.readLabsForUser(getUserId(principal));
    }

    @ResponseBody
    @RequestMapping(value = "/labDetails")
    public LabInvoiceDetails labDetails(@RequestParam long lab, Principal principal) {
        return billingService.readLabDetails(getUserId(principal), lab);
    }

    @RequestMapping(value = "/updateLabAccountSubscriptionDetails", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void updateLabAccountSubscriptionDetails(@RequestBody UpdateLabAccountRequest request, Principal principal) {

        final LabPaymentAccountType labPaymentAccountType = valueOf(request.getAccountType());
        final SubscriptionInfo subscriptionInfo = new SubscriptionInfo(
            request.getLabId(),
            request.getStorageVolumesCount(),
            request.isProcessingEnabled(),
            request.isAutoprolongateProcessing(),
            labPaymentAccountType
        );

        final long userId = getUserId(principal);

        billingManagement.updateLabAccountSubscriptionDetails(userId, subscriptionInfo);
    }

    @ResponseBody
    @RequestMapping("/features")
    public ImmutableSortedSet<com.infoclinika.mssharing.model.helper.BillingFeatureItem> features() {
        return billingHelper.billingFeatures();
    }

    @RequestMapping(value = "/paged", method = RequestMethod.GET)
    @ResponseBody
    public PagedItem<InvoiceLabLine> getPagedLabAccounts(
        @RequestParam int page, @RequestParam int items,
        @RequestParam String sortingField, @RequestParam boolean asc,
        @RequestParam(required = false) @Nullable String filterQuery,
        Principal principal
    ) {
        final PaginationItems.PagedItemInfo pagedInfo =
            (PaginationItems.PagedItemInfo) createPagedInfo(page, items, sortingField, asc, filterQuery);
        final PagedItemInfo.PagedItem<InvoiceLabLine> pagedResult = billingService.readPagedAllLabs(
            new ReadPagedAllLabsRequest(getUserId(principal), PAGED_ITEM_INFO_WS.apply(pagedInfo)));

        return new PagedItem<>(pagedResult.totalPages, pagedResult.itemsCount, pagedResult.pageNumber,
            pagedResult.pageSize, pagedResult.items
        );
    }

    @ResponseBody
    @RequestMapping("/allHistory")
    public HistoryForLab getAllHistory(@RequestParam long lab, Principal principal) {
        return paymentHistoryReader.readAll(getUserId(principal), lab);
    }

    @ResponseBody
    @RequestMapping("/moreHistory")
    public HistoryForLab getHistory(
        @RequestParam long lab,
        @RequestParam long previousCount,
        Principal principal
    ) {
        try {
            return paymentHistoryReader.readNextHistory(getUserId(principal), lab, previousCount, 7);
        } catch (Exception e) {
            LOGGER.info("MoreHistory: errorMessage: " + e.getMessage(), e);
            throw Throwables.propagate(e);
        }
    }

    @ResponseBody
    @RequestMapping("/moreMonthlyHistoryReference")
    public HistoryForMonthReference moreMonthlyHistoryReference(
        @RequestParam long lab,
        @RequestParam long month,
        Principal principal
    ) {
        return paymentHistoryReader.readMonthReference(getUserId(principal), lab, new Date(month));
    }

    @ResponseBody
    @RequestMapping("/featureInfo")
    public BillingFeatureItem featureInfo(@RequestParam long feature) {
        return billingService.readFeatureInfo(feature);
    }

    @ResponseBody
    @RequestMapping("/history/download")
    public void downloadHistory(
        @RequestParam long lab,
        @RequestParam String path,
        HttpServletResponse response,
        Principal principal
    ) throws IOException {
        billingHistoryDownloadHelper.download(getUserId(principal), lab, path, response);
    }

    @ResponseBody
    @RequestMapping("/labAccountFeatures")
    public ImmutableMap<Long, Set<LabAccountFeatureInfo>> listLabAccountFeatures(
        @RequestParam("labs") Set<Long> labs,
        Principal principal
    ) {
        final ImmutableMap.Builder<Long, Set<LabAccountFeatureInfo>> builder = ImmutableMap.builder();
        try {
            for (Long lab : labs) {
                builder.put(lab, billingInfoReader.readLabAccountFeatures(lab));
            }
        } catch (Exception e) {
            LOGGER.info("Can't receive list of features for labs with ids: " + labs);
        }

        return builder.build();
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/topup", method = RequestMethod.POST)
    public void topUpLabBalance(@RequestBody(required = true) AdminTopUpRequest topUpRequest, Principal admin) {
        billingManagement.topUpLabBalance(getUserId(admin), topUpRequest.lab, topUpRequest.amount);
    }

    @ResponseBody
    @RequestMapping(value = "/featuresPrices")
    public Map<String, String> getFeaturesPrices() {
        final List<BillingFeatureItem> features = billingService.readFeatures();
        final HashMap<String, String> featurePricesMap = new HashMap<>();
        for (BillingFeatureItem feature : features) {
            featurePricesMap.put(feature.name, String.valueOf(feature.price));
        }

        return featurePricesMap;
    }

    @RequestMapping(value = "/makeAccountFree", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void makeAccountFree(@RequestBody LabRequest request, Principal principal) {
        billingManagement.makeLabAccountFree(getUserId(principal), request.getLabId());
    }

    @RequestMapping(value = "/makeAccountEnterprise", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void makeAccountEnterprise(@RequestBody LabRequest request, Principal principal) {
        billingManagement.makeLabAccountEnterprise(getUserId(principal), request.getLabId());
    }

    @RequestMapping(value = "/checkCanMakeAccountFree", method = RequestMethod.GET)
    @ResponseBody
    public ValueResponse<MakeAccountFreeCheckResult> checkCanMakeAccountFree(
        @RequestParam long lab,
        Principal principal
    ) {
        final MakeAccountFreeCheckResult makeAccountFreeCheckResult =
            billingManagement.checkCanMakeAccountFree(getUserId(principal), lab);
        return new ValueResponse<>(makeAccountFreeCheckResult);
    }

    @RequestMapping(value = "/getBillingProperties")
    @ResponseBody
    public Map<String, String> getBillingProperties(Principal principal) {
        return billingInfoReader.readBillingProperties(getUserId(principal));
    }

    public static class LabRequest {
        private long labId;

        public long getLabId() {
            return labId;
        }

        public void setLabId(long labId) {
            this.labId = labId;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EnableProcessingFeatureRequest {
        private long labId;
        private boolean processingEnabled;
        private boolean autoprolongateProcessing;

        public long getLabId() {
            return labId;
        }

        public void setLabId(long labId) {
            this.labId = labId;
        }

        public boolean isAutoprolongateProcessing() {
            return autoprolongateProcessing;
        }

        public void setAutoprolongateProcessing(boolean autoprolongateProcessing) {
            this.autoprolongateProcessing = autoprolongateProcessing;
        }

        public boolean isProcessingEnabled() {
            return processingEnabled;
        }

        public void setProcessingEnabled(boolean processingEnabled) {
            this.processingEnabled = processingEnabled;
        }
    }

    public static class AdminTopUpRequest {
        public long amount;
        public long lab;
    }
}
