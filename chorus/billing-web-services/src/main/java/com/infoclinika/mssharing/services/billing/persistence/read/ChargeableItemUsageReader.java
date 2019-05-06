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
package com.infoclinika.mssharing.services.billing.persistence.read;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.helper.BillingFeatureItem;
import com.infoclinika.mssharing.model.write.billing.BillingManagement.LabPaymentAccountType;
import com.infoclinika.mssharing.services.billing.rest.api.model.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Elena Kurilina
 */
@Transactional(readOnly = true)
public interface ChargeableItemUsageReader {

    Invoice readInvoice(long actor, long lab, Date from, Date to);

    /**
     * Returns Invoice without usage lines by user
     *
     * @param actor lab head
     * @param lab   laboratory id
     * @param from  date of invoice begin
     * @param to    date of invoice end
     * @return Invoice item with usages per feature and per user.
     */
    Invoice readInvoiceShortItem(long actor, long lab, Date from, Date to);

    StorageUsage readStorageUsage(long actor, long lab);

    List<MonthlyCharge> readMonthlyCharges(long actor, long lab, Date date);

    ImmutableSet<InvoiceLabLine> readLabsForUser(long actor);

    LabInvoiceDetails readLabDetails(long actor, long lab);

    PaginationItems.PagedItem<InvoiceLabLine> readPagedAllLabs(long actor, PaginationItems.PagedItemInfo pagedItemInfo);

    BillingFeatureItem readFeatureInfo(long featureId);

    List<BillingFeatureItem> readFeatures();

    HistoryForMonthReference readMonthReference(long userId, long lab, Date month);

    java.util.Optional<DailyUsageLine> readDailyUsageLine(long lab, Date date);

    Long readAnalyzableStorageUsage(long lab);

    Long readAnalyzableStorageUsage(long lab, Date date);

    class StorageUsage {
        public final long rawFilesSize;
        public final long archivedFilesSize;
        public final long searchResultsFilesSize;

        public StorageUsage(long rawFilesSize, long archivedFilesSize,
                            long searchResultsFilesSize) {
            this.rawFilesSize = rawFilesSize;
            this.archivedFilesSize = archivedFilesSize;
            this.searchResultsFilesSize = searchResultsFilesSize;
        }
    }

    class MonthlyCharge {
        public final BillingFeature feature;
        public final String serverDateFormatted;
        public final long featureAmountUsed;
        public final long sizeInBytes;
        public final long charge;
        public final long timestamp;

        public MonthlyCharge(BillingFeature feature, String serverDateFormatted, long featureAmountUsed,
                             long sizeInBytes, long charge, long timestamp) {
            this.feature = feature;
            this.serverDateFormatted = serverDateFormatted;
            this.featureAmountUsed = featureAmountUsed;
            this.sizeInBytes = sizeInBytes;
            this.charge = charge;
            this.timestamp = timestamp;
        }
    }

    class InvoiceLabLine {

        public final long labId;
        public final String labName;
        public final String labHead;
        public final long storeBalance;


        public InvoiceLabLine(long labId, String labName, String labHead,
                              long storeBalance) {
            this.labId = labId;
            this.labName = labName;
            this.labHead = labHead;
            this.storeBalance = storeBalance;
        }

        public static Function<InvoiceLabLine, com.infoclinika.mssharing.services.billing.rest.api.model.InvoiceLabLine>
            WS_TRANSFORM =
            new Function<InvoiceLabLine, com.infoclinika.mssharing.services.billing.rest.api.model.InvoiceLabLine>() {
                @Override
                public com.infoclinika.mssharing.services.billing.rest.api.model.InvoiceLabLine apply(
                    InvoiceLabLine i) {
                    return new com.infoclinika.mssharing.services.billing.rest.api.model.InvoiceLabLine(
                        i.labId, i.labName, i.labHead, i.storeBalance);
                }
            };
    }

    class LabInvoiceDetails {
        public final String headEmail;
        public final String labName;
        public final String url;
        public final int members;
        public final FeaturesData featuresData;
        public final Date lastUpdated;
        public final long storeBalance;
        public final LabPaymentAccountType accountType;
        public final boolean isFree;

        public Set<LabAccountFeatureInfo> labAccountFeatures;

        public LabInvoiceDetails(String headEmail, String labName, String url, int members, FeaturesData featuresData,
                                 Date lastUpdated, long storeBalance, LabPaymentAccountType accountType,
                                 boolean isFree) {
            this.headEmail = headEmail;
            this.labName = labName;
            this.url = url;
            this.members = members;
            this.featuresData = featuresData;
            this.lastUpdated = lastUpdated;
            this.storeBalance = storeBalance;
            this.accountType = accountType;
            this.isFree = isFree;
        }

        public Set<LabAccountFeatureInfo> getLabAccountFeatures() {
            return labAccountFeatures;
        }

        public void setLabAccountFeatures(Set<LabAccountFeatureInfo> labAccountFeatures) {
            this.labAccountFeatures = labAccountFeatures;
        }

        public static java.util.function.Function<LabInvoiceDetails,
            com.infoclinika.mssharing.services.billing.rest.api.model.LabInvoiceDetails> WS_TRANSFORM = l -> {
                final com.infoclinika.mssharing.services.billing.rest.api.model.FeaturesData fd =
                    FeaturesData.WS_TRANSFORM.apply(l.featuresData);

                final com.infoclinika.mssharing.services.billing.rest.api.model.LabInvoiceDetails labInvoiceDetails =
                    new com.infoclinika.mssharing.services.billing.rest.api.model.LabInvoiceDetails(
                        l.headEmail, l.labName, l.url, l.members, fd, l.lastUpdated, l.storeBalance,
                        l.accountType.name(), l.isFree
                    );
                labInvoiceDetails.setLabAccountFeatures(l.getLabAccountFeatures());
                return labInvoiceDetails;
            };
    }

    class Invoice {
        public final long total;
        public final long storeBalance;
        public final long labId;
        public final String labName;
        public final Date date;
        public final InvoiceFeatureItem featureItem;

        public Invoice(String labName, long labId, long storeBalance, long total,
                       Date timestamp, InvoiceFeatureItem featureItem) {
            this.labName = labName;
            this.total = total;
            this.storeBalance = storeBalance;
            this.labId = labId;
            this.date = timestamp;
            this.featureItem = featureItem;
        }

        public static Function<Invoice, com.infoclinika.mssharing.services.billing.rest.api.model.Invoice>
            WS_TRANSFORM = i -> new com.infoclinika.mssharing.services.billing.rest.api.model.Invoice(
                i.labName, i.labId, i.storeBalance, i.total, i.date,
                InvoiceFeatureItem.WS_TRANSFORM.apply(i.featureItem)
            );
    }

    class ChargeableItemBill {

        public final String name;
        public final long total;
        public final BillingFeature type;
        public final BillingChargeType loggedChargeValueType;
        public final long totalLoggedChargeValue;
        public final Collection<UsageByUser> usageByUsers;
        public final Optional<Long> totalFiles;
        public final long totalUsers;
        public final long price;
        public final long unscaledTotal;

        public ChargeableItemBill(String name, long total, BillingFeature type, long totalChargeValue,
                                  BillingChargeType loggedChargeValueType, Collection<UsageByUser> usageByUsers,
                                  Optional<Long> totalFiles, long totalUsers, long price, long unscaledTotal) {
            this.name = name;
            this.total = total;
            this.type = type;
            this.totalLoggedChargeValue = totalChargeValue;
            this.loggedChargeValueType = loggedChargeValueType;
            this.usageByUsers = usageByUsers;
            this.totalFiles = totalFiles;
            this.totalUsers = totalUsers;
            this.price = price;
            this.unscaledTotal = unscaledTotal;
        }

        public static Function<ChargeableItemBill,
            com.infoclinika.mssharing.services.billing.rest.api.model.ChargeableItemBill> WS_TRANSFORM = i -> {
                List<com.infoclinika.mssharing.services.billing.rest.api.model.UsageByUser> usagesByUser =
                    from(i.usageByUsers).transform(UsageByUser.WS_TRANSFORM).toList();
                return new com.infoclinika.mssharing.services.billing.rest.api.model.ChargeableItemBill(
                    i.name, i.total, i.type, i.totalLoggedChargeValue, i.loggedChargeValueType, usagesByUser,
                    i.totalFiles.orNull(), i.totalUsers, i.price, i.unscaledTotal
                );
            };
    }

    class UsageByUser {

        public final String userName;
        public final long userId;
        public final long totalUsedFeatureValue;
        public final long totalPrice;
        public final ImmutableSet<? extends UsageLine> usageLines;
        public final long filesCount;
        public final long balance;

        public UsageByUser(String userName, long userId, long totalUsedFeatureValue, long totalPrice,
                           ImmutableSet<? extends UsageLine> usageLines, long filesCount, long balance) {
            this.userName = userName;
            this.userId = userId;
            this.totalUsedFeatureValue = totalUsedFeatureValue;
            this.totalPrice = totalPrice;
            this.usageLines = usageLines;
            this.filesCount = filesCount;
            this.balance = balance;
        }

        public static Function<UsageByUser, com.infoclinika.mssharing.services.billing.rest.api.model.UsageByUser>
            WS_TRANSFORM =
            new Function<UsageByUser, com.infoclinika.mssharing.services.billing.rest.api.model.UsageByUser>() {
                @Override
                public com.infoclinika.mssharing.services.billing.rest.api.model.UsageByUser apply(UsageByUser u) {
                    final Set<? extends com.infoclinika.mssharing.services.billing.rest.api.model.UsageByUser.UsageLine>
                        transformUsageLines = UsageByUserTransformers.transformUsageLines(u.usageLines);
                    return new com.infoclinika.mssharing.services.billing.rest.api.model.UsageByUser(
                        u.userName, u.userId, u.totalUsedFeatureValue, u.totalPrice, transformUsageLines, u.filesCount,
                        u.balance
                    );
                }


            };
    }

    class UsageByUserTransformers {
        public static final long DEFAULT_FILE_VALUE = 0L;

        private static final java.util.function.Function<ExperimentUsageLine,
            com.infoclinika.mssharing.services.billing.rest.api.model.UsageByUser.ExperimentUsageLine>
            ExperimentUsageLine_WS = e ->
                new com.infoclinika.mssharing.services.billing.rest.api.model.UsageByUser.ExperimentUsageLine(
                    e.usedFeatureValue, e.price, e.loggedPrice, e.balance, e.experimentId,
                    e.experimentName, e.timestamp
                );

        private static final Function<FileUsageLine,
            com.infoclinika.mssharing.services.billing.rest.api.model.UsageByUser.FileUsageLine> FileUsageLine_WS = f ->
                new com.infoclinika.mssharing.services.billing.rest.api.model.UsageByUser.FileUsageLine(
                    f.fileId.or(DEFAULT_FILE_VALUE), f.usedFeatureValue, f.instrument, f.price, f.loggedPrice,
                    f.balance, f.hours, f.fileName, f.timestamp
                );

        public static Set<? extends com.infoclinika.mssharing.services.billing.rest.api.model.UsageByUser.UsageLine>
            transformUsageLines(Set<? extends UsageLine> usageLineSet) {

            if (usageLineSet.isEmpty()) {
                return newHashSet();
            }
            if (usageLineSet.iterator().next() instanceof FileUsageLine) {
                final Set<FileUsageLine> filesUageLineSet = (Set<FileUsageLine>) usageLineSet;
                return FluentIterable.from(filesUageLineSet).transform(FileUsageLine_WS).toSet();
            } else if (usageLineSet.iterator().next() instanceof ExperimentUsageLine) {
                final Set<ExperimentUsageLine> filesUageLineSet = (Set<ExperimentUsageLine>) usageLineSet;
                return FluentIterable.from(filesUageLineSet).transform(ExperimentUsageLine_WS::apply).toSet();
            }
            throw new IllegalStateException("Unknown type usage line: " + usageLineSet.iterator().next().getClass());
        }
    }

    class UsageLine {
        public final long usedFeatureValue;
        public final long price;
        public final long loggedPrice;
        public final long balance;
        public final Date timestamp;

        public UsageLine(long usedFeatureValue, long price, long loggedPrice, long balance, Date timestamp) {
            this.usedFeatureValue = usedFeatureValue;
            this.price = price;
            this.loggedPrice = loggedPrice;
            this.balance = balance;
            this.timestamp = timestamp;
        }
    }

    class FileUsageLine extends UsageLine {

        public final Optional<Long> fileId;
        public final String instrument;
        public final String fileName;
        public final int hours;

        public FileUsageLine(Long fileId, long usedFeatureValue,
                             String instrument, long price, long loggedPrice, long balance, int days, String fileName,
                             Date date) {
            super(usedFeatureValue, price, loggedPrice, balance, date);
            this.fileId = Optional.fromNullable(fileId);
            this.instrument = instrument;
            this.hours = days;
            this.fileName = fileName;
        }
    }

    class ExperimentUsageLine extends UsageLine {
        public final long experimentId;
        public final String experimentName;

        public ExperimentUsageLine(long usedFeatureValue, long price, long loggedPrice, long balance, long experimentId,
                                   String experimentName, Date date) {
            super(usedFeatureValue, price, loggedPrice, balance, date);
            this.experimentId = experimentId;
            this.experimentName = experimentName;
        }
    }

    class FeaturesData {

        public final Set<String> features;

        public FeaturesData(Set<String> features) {
            this.features = features;
        }

        public static Function<FeaturesData, com.infoclinika.mssharing.services.billing.rest.api.model.FeaturesData>
            WS_TRANSFORM = fd -> new com.infoclinika.mssharing.services.billing.rest.api.model.FeaturesData(
                fd.features
            );
    }

    /**
     * @author Herman Zamula
     */
    class InvoiceFeatureItem {

        public final Set<ChargeableItemBill> features;

        public InvoiceFeatureItem(Set<ChargeableItemBill> features) {
            this.features = features;
        }

        public static Function<InvoiceFeatureItem,
            com.infoclinika.mssharing.services.billing.rest.api.model.InvoiceFeatureItem>
            WS_TRANSFORM = i -> {
                final ImmutableSet<com.infoclinika.mssharing.services.billing.rest.api.model.ChargeableItemBill>
                    features = from(i.features).transform(ChargeableItemBill.WS_TRANSFORM)
                    .toSortedSet(Comparator.comparing(bill -> bill.type));
                return new com.infoclinika.mssharing.services.billing.rest.api.model.InvoiceFeatureItem(features);
            };
    }
}
