package com.infoclinika.mssharing.services.billing.persistence.read.strategy;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingChargeType;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.model.internal.repository.ChargeableItemRepository;
import com.infoclinika.mssharing.model.internal.repository.FeatureUsageByUser;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.persistence.helper.PaymentCalculationsHelper;
import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;
import com.infoclinika.mssharing.services.billing.persistence.repository.FeatureUsageRepository;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.services.billing.rest.api.model.BillingChargeType.BYTE;
import static com.infoclinika.mssharing.model.internal.read.Transformers.transformFeature;
import static com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader.UsageByUser;
import static java.lang.String.format;

/**
 * @author Herman Zamula
 */
@Transactional(value = "billingLoggingTransactionManager", readOnly = true)
public abstract class AbstractFeatureLogStrategy implements FeatureLogStrategy {

    protected final ChargeableItem.Feature feature;
    @Inject
    protected PaymentCalculationsHelper paymentCalculations;
    @Inject
    private ChargeableItemRepository featureRepository;
    private final FeatureUsageRepository<?> featureUsageRepository;

    protected final Logger logger = Logger.getLogger(this.getClass());

    protected AbstractFeatureLogStrategy(ChargeableItem.Feature feature, FeatureUsageRepository<?> featureUsageRepository) {
        this.feature = feature;
        this.featureUsageRepository = featureUsageRepository;
    }

    @Override
    public boolean accept(ChargeableItem.Feature billingFeature) {
        return billingFeature.equals(feature);
    }

    protected final Comparator<ChargeableItemUsage> dateComparator = (o1, o2) -> Longs.compare(o1.getTimestamp(), o2.getTimestamp());

    protected final Ordering<ChargeableItemUsage> itemUsageOrdering = Ordering.from(dateComparator);

    @Override
    public ChargeableItemUsageReader.ChargeableItemBill readBill(long lab, Date dateFrom, Date dateTo) {
        logger.debug(format("Reading bill for lab {%d}. From {%s} to {%s}", lab, dateFrom, dateTo));

        final long fromInMills = dateFrom.getTime();
        final long toInMills = dateTo.getTime();

        final Long unscaled = featureUsageRepository.sumAllRawPricesByLabUnscaled(lab, fromInMills, toInMills);
        logger.debug("Price was read...");
        final List<? extends ChargeableItemUsage> itemUsages = featureUsageRepository.findByLab(lab, fromInMills, toInMills);
        logger.debug("Usages was read...");
        final List<FeatureUsageByUser> usageByUsers = featureUsageRepository.groupUsagesByUser(lab, fromInMills, toInMills);
        logger.debug("Usages by user was read...");
        Long filesCount = featureUsageRepository.countFiles(lab, fromInMills, toInMills);
        if(filesCount == null) {
            filesCount = 0L;
        }

        logger.debug(format("Invoice data loaded. Total price {%d}, usages size: {%d}, usages by user: {%d}, files count: {%d}",
                unscaled, itemUsages.size(), usageByUsers.size(), filesCount));

        return transformToChargeableItemBill(itemUsages, BYTE, usageByUsers, filesCount, unscaled, false);

    }

    @Override
    public ChargeableItemUsageReader.ChargeableItemBill readShortBill(final long lab, Date day) {

        logger.debug(format("Reading {%s} feature bill for lab {%d}. Day {%s}", this.getClass().getSimpleName(), lab, day));

        final int daySinceEpoch = paymentCalculations.calculationDaySinceEpoch(day);

        final Long unscaled = featureUsageRepository.sumAllRawPricesByLabUnscaled(lab, daySinceEpoch);
        logger.debug("Price was read...");
        final List<? extends ChargeableItemUsage> groupedByLab = featureUsageRepository.findGroupedByLab(lab, daySinceEpoch);
        logger.debug("Usages was read...");
        final List<FeatureUsageByUser> usageByUsers = featureUsageRepository.groupUsagesByUser(lab, daySinceEpoch);
        logger.debug("Usages by user was read...");
        Long filesCount = featureUsageRepository.countFiles(lab, daySinceEpoch);
        if(filesCount == null) {
            filesCount = 0L;
        }

        logger.debug(format("Invoice data loaded. Total price {%d}, usages size: {%d}, usages by user: {%d}, files count: {%d}",
                unscaled, groupedByLab.size(), usageByUsers.size(), filesCount));

        return transformToChargeableItemBill(groupedByLab, BYTE, usageByUsers, filesCount, unscaled, true);
    }

    protected ChargeableItemUsageReader.ChargeableItemBill transformToChargeableItemBill(List<? extends ChargeableItemUsage> logs,
                                                                                         BillingChargeType chargeType,
                                                                                         List<FeatureUsageByUser> usageByUsers,
                                                                                         long filesCount, long unscaledTotal,
                                                                                         boolean withoutPerFile) {

        final BillingFeature billingFeature = transformFeature(feature);
        final FeatureUsageContext featureUsage = transformToUsageByUser(logs, usageByUsers, withoutPerFile);
        final ChargeableItem chargeableItem = featureRepository.findByFeature(feature);

        return new ChargeableItemUsageReader.ChargeableItemBill(billingFeature.getValue(),
                paymentCalculations.unscalePrice(unscaledTotal),
                billingFeature,
                featureUsage.getTotalValueUsage(),
                chargeType,
                featureUsage.getUsageByUsers(),
                Optional.of(filesCount),
                featureUsage.getUsageByUsers().size(),
                chargeableItem.getPrice(), unscaledTotal);
    }

    private FeatureUsageContext transformToUsageByUser(List<? extends ChargeableItemUsage> usages, List<FeatureUsageByUser> usageByUsers, boolean withoutPerFile) {

        final FeatureUsageContext featureUsage = new FeatureUsageContext();
        Map<Long, Map<Long, Set<ChargeableItemUsage>>> usageMap = transformToUsage(usages);

        for (FeatureUsageByUser usageByUser : usageByUsers) {
            final FeatureUsageContext byUser = processUser(usageByUser, fromNullable(usageMap.get(usageByUser.getUser())).or(of()), withoutPerFile);
            featureUsage.addToTotalAmount(byUser.getTotalAmount());
            featureUsage.getUsageByUsers().addAll(byUser.getUsageByUsers());
            featureUsage.addToTotalValueUsage(byUser.getTotalValueUsage());
        }

        return featureUsage;
    }

    private Map<Long, Map<Long, Set<ChargeableItemUsage>>> transformToUsage(List<? extends ChargeableItemUsage> usages) {

        Map<Long, Map<Long, Set<ChargeableItemUsage>>> usageMap = new HashMap<>();

        for (ChargeableItemUsage usage : usages) {
            final long user = fromNullable(usage.getUser()).or(0l);
            if (usageMap.get(user) == null) {
                usageMap.put(user, new HashMap<>());
            }
            Map<Long, Set<ChargeableItemUsage>> map = usageMap.get(user);
            if (map.get(usage.getFile()) == null) {
                map.put(usage.getFile(), new HashSet<>());
            }
            map.get(usage.getFile()).add(usage);
        }

        return usageMap;
    }

    @SuppressWarnings("all")
    private FeatureUsageContext processUser(FeatureUsageByUser usageByUser, Map<Long, Set<ChargeableItemUsage>> usagePerUser, boolean withoutPerFile) {

        final FeatureUsageContext featureUsage = new FeatureUsageContext();
        final Function<Set<ChargeableItemUsage>, ChargeableItemUsageReader.UsageLine> usageLineFn = usageLineFn();
        final ImmutableSet.Builder<ChargeableItemUsageReader.UsageLine> usageLines = ImmutableSet.builder();

        long total = 0;
        long totalUnscaledPrice = 0;
        for (Long file : usagePerUser.keySet()) {
            final ChargeableItemUsageReader.UsageLine usageLine = usageLineFn.apply(usagePerUser.get(file));
            total += usageLine.usedFeatureValue;
            totalUnscaledPrice += usageLine.loggedPrice;
            if (!withoutPerFile) {
                usageLines.add(usageLine);
            }
        }

        final long finalPriceByUser = paymentCalculations.unscalePriceNotRound(totalUnscaledPrice);

        featureUsage.getUsageByUsers().add(new UsageByUser(usageByUser.getUserName(), usageByUser.getUser(), total, finalPriceByUser,
                usageLines.build(), usageByUser.getFilesCount(), 0));
        featureUsage.setTotalValueUsage(total);
        featureUsage.setTotalAmount(finalPriceByUser);

        return featureUsage;
    }

    protected abstract Function<Set<ChargeableItemUsage>, ChargeableItemUsageReader.UsageLine> usageLineFn();

    protected class FeatureUsageContext {
        private long totalAmount = 0;
        private final Collection<UsageByUser> usageByUsers = newHashSet();
        private long totalValueUsage = 0;

        public long getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(long totalAmount) {
            this.totalAmount = totalAmount;
        }

        public Collection<UsageByUser> getUsageByUsers() {
            return usageByUsers;
        }

        public long getTotalValueUsage() {
            return totalValueUsage;
        }

        public void setTotalValueUsage(long totalValueUsage) {
            this.totalValueUsage = totalValueUsage;
        }

        public void addToTotalValueUsage(long amount) {
            this.totalValueUsage += amount;
        }

        public void addToTotalAmount(long amount) {
            this.totalAmount += amount;
        }
    }

    protected class UsageParams {
        public long totalPrice = 0;
        public long totalHours = 0;
    }

    public static <T extends Iterable<? extends Number>> long sum(T prices) {
        long total = 0;
        for (Number price : prices) {
            total += price.longValue();
        }
        return total;
    }
}
