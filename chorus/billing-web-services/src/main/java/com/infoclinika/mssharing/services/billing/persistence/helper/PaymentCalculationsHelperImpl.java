package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.model.internal.helper.billing.BillingPropertiesProvider;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.ChargeableItemRepository;
import com.infoclinika.mssharing.services.billing.persistence.enity.*;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.DailyAnalyzableStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.DailyArchiveStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.HourlyAnalyzableStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.HourlyArchiveStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.repository.*;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Optional.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem.Feature.ANALYSE_STORAGE;
import static com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem.Feature.ARCHIVE_STORAGE;
import static com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage.PRICE_PRECISION;
import static com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage.PRICE_SCALE_VALUE;
import static java.lang.String.format;
import static java.math.BigDecimal.valueOf;
import static org.joda.time.Days.daysBetween;

/**
 * @author Herman Zamula
 */
@Component
@Transactional(readOnly = true)
public class PaymentCalculationsHelperImpl implements PaymentCalculationsHelper {

    public static final int BYTES_IN_GB = 1073741824;
    public static final MathContext ROUND_UP = new MathContext(PRICE_PRECISION, RoundingMode.HALF_UP);
    public static final MathContext ROUND_FLOOR = new MathContext(PRICE_PRECISION, RoundingMode.FLOOR);
    public static final BigDecimal SCALE = valueOf(PRICE_SCALE_VALUE);
    private static final Logger LOGGER = Logger.getLogger(PaymentCalculationsHelperImpl.class);

    @Inject
    private ChargeableItemRepository chargeableItemRepository;
    @Inject
    private Collection<FeatureUsageRepository> featureRepositories = newHashSet();
    @Inject
    private StorageVolumeUsageRepository storageVolumeUsageRepository;
    @Inject
    private ProcessingUsageRepository processingUsageRepository;
    @Inject
    private ArchiveStorageVolumeUsageRepository archiveStorageVolumeUsageRepository;
    @Inject
    private Transformers transformers;
    @Inject
    private ApplicationContext context;
    @Inject
    private BillingPropertiesProvider billingPropertiesProvider;
    @Inject
    private DailyAnalyseStorageUsageRepository dailyAnalyseStorageUsageRepository;
    @Inject
    private DailyArchiveStorageUsageRepository dailyArchiveStorageUsageRepository;

    private DateTimeZone dateTimeZone;

    @PersistenceContext(unitName = "mssharing_billing")
    private EntityManager em;

    private final LoadingCache<ChargeableItem.Feature, ChargeableItem> featureChargeableItemLoadingCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<ChargeableItem.Feature, ChargeableItem>() {
                @Override
                @ParametersAreNonnullByDefault
                public ChargeableItem load(ChargeableItem.Feature key) throws Exception {
                    return chargeableItemRepository.findByFeature(key);
                }
            });

    @PostConstruct
    public void setup() {
        dateTimeZone = DateTimeZone.forTimeZone(transformers.serverTimezone);
    }

    private BigDecimal getStoragePriceForHour(Date date, long bytes, int GBPerMonthPrice) {

        final DateTime dateTime = new DateTime(date, dateTimeZone);

        return valueOf(GBPerMonthPrice)
                .divide(valueOf(dateTime.dayOfMonth().getMaximumValue()), ROUND_UP)
                .divide(valueOf(24), ROUND_UP)
                .divide(valueOf(BYTES_IN_GB), ROUND_UP)
                .multiply(valueOf(bytes), ROUND_UP)
                .multiply(SCALE, ROUND_UP)  // scale for not lose small values
                .setScale(0, RoundingMode.HALF_DOWN);
    }


    @Override
    public long unscalePrice(long price) {
        return valueOf(price)
                .divide(SCALE, ROUND_UP)
                .setScale(0, RoundingMode.HALF_UP).longValue();
    }

    @Override
    public long scalePrice(long realPrice) {
        return realPrice * PRICE_SCALE_VALUE;
    }

    @Override
    public long unscalePriceNotRound(long price) {
        return valueOf(price)
                .divide(SCALE, ROUND_FLOOR)
                .setScale(0, RoundingMode.DOWN)
                .longValue();
    }

    @Override
    public long calculateRoundedPriceByUnscaled(long actualBalance, long unscaledValue) {
        return actualBalance - valueOf(unscaledValue)
                .divide(SCALE, ROUND_UP)
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    private <T extends Iterable<? extends Number>> long sum(T prices) {
        long total = 0;
        for (Number price : prices) {
            total += price.longValue();
        }
        return total;
    }

    @Override
    public int calculationDaySinceEpoch(Date timestamp) {
        return daysBetween(new DateTime(0).withZone(dateTimeZone), new DateTime(timestamp).withZone(dateTimeZone)).getDays();
    }

    @Override
    public long calculateTotalToPayForLab(long lab, Date fromDate, Date toDate) {
        LOGGER.debug(format("Calculating total to pay for lab {%d}. From {%s} to {%s}", lab, fromDate, toDate));
        long total = 0;
        for (FeatureUsageRepository repo : featureRepositories) {
            total += repo.sumAllRawPricesByLabUnscaled(lab, fromDate.getTime(), toDate.getTime());
        }
        total += storageVolumeUsageRepository.sumAllRawPricesByLabUnscaled(lab, fromDate.getTime(), toDate.getTime());
        total += processingUsageRepository.sumAllRawPricesByLabUnscaled(lab, fromDate.getTime(), toDate.getTime());
        total += archiveStorageVolumeUsageRepository.sumAllRawPricesByLabUnscaled(lab, fromDate.getTime(), toDate.getTime());
        final long unscaledPrice = unscalePrice(total);
        LOGGER.debug(format("Total price: {%d}", unscaledPrice));
        return unscaledPrice;
    }

    @Override
    public int calculateStorageVolumes(long bytes) {
        final Long freeAccountStorageLimit = billingPropertiesProvider.getFreeAccountStorageLimit();
        if(freeAccountStorageLimit >= bytes) {
            return 0;
        }
        final Long enterpriseAccountVolumeSize = billingPropertiesProvider.getEnterpriseAccountVolumeSize();
        final int volumesCount = (int) (bytes / enterpriseAccountVolumeSize);
        if(bytes % enterpriseAccountVolumeSize > 0) {
            return volumesCount + 1;
        }
        return volumesCount;
    }

    @Override
    public int calculateArchiveStorageVolumes(long bytes) {
        final Long freeAccountStorageLimit = billingPropertiesProvider.getFreeAccountArchiveStorageLimit();
        if(freeAccountStorageLimit >= bytes) {
            return 0;
        }
        final Long enterpriseAccountArchiveVolumeSize = billingPropertiesProvider.getEnterpriseAccountArchiveVolumeSize();
        final int volumesCount = (int) (bytes / enterpriseAccountArchiveVolumeSize);
        if(bytes % enterpriseAccountArchiveVolumeSize > 0) {
            return volumesCount + 1;
        }
        return volumesCount;
    }

    @Override
    public long calculateStorageCost(int volumes) {
        return volumes * billingPropertiesProvider.getEnterpriseAccountVolumeCost();
    }

    @Override
    public long calculateArchiveStorageCost(int volumes) {
        return volumes * billingPropertiesProvider.getEnterpriseAccountArchiveVolumeCost();
    }

    @Override
    public long calculateMaximumStorageUsage(long lab, Date from, Date to) {
        final int fromDay = calculationDaySinceEpoch(from);
        final int toDay = calculationDaySinceEpoch(to);
        final List<Long> totalBytesPerDay = dailyAnalyseStorageUsageRepository.getTotalBytesPerDay(lab, fromDay, toDay);
        return totalBytesPerDay.stream().max(Long::compare).orElse(0L);
    }

    @Override
    public long calculateMaximumArchiveStorageUsage(long lab, Date from, Date to) {
        final int fromDay = calculationDaySinceEpoch(from);
        final int toDay = calculationDaySinceEpoch(to);
        final List<Long> totalBytesPerDay = dailyArchiveStorageUsageRepository.getTotalBytesPerDay(lab, fromDay, toDay);
        return totalBytesPerDay.stream().max(Long::compare).orElse(0L);
    }

    @Override
    public long calculateTotalToPayForLabForDay(long lab, Date day) {
        LOGGER.debug(format("Calculating total to pay for lab {%d}. Day {%s}", lab, day));
        long total = 0;
        for (FeatureUsageRepository repo : featureRepositories) {
            total += repo.sumAllRawPricesByLabUnscaled(lab, calculationDaySinceEpoch(day));
        }
        total += storageVolumeUsageRepository.sumAllRawPricesByLabUnscaled(lab, calculationDaySinceEpoch(day));
        total += processingUsageRepository.sumAllRawPricesByLabUnscaled(lab, calculationDaySinceEpoch(day));
        total += archiveStorageVolumeUsageRepository.sumAllRawPricesByLabUnscaled(lab, calculationDaySinceEpoch(day));
        final long unscaledPrice = unscalePrice(total);
        LOGGER.debug(format("Total price: {%d}", unscaledPrice));
        return unscaledPrice;
    }

    @Override
    public long sumPrices(Iterable<? extends Number> prices) {
        return sum(prices);
    }

    @Override
    public Optional<Long> calculateStoreBalance(final long lab, final Date from, final Date to) {

        LOGGER.debug(format("Calculating store balance for lab {%d}. From {%s} to {%s}", lab, from, to));

        final Iterable<BalanceEntry> lastLoggedUsages = loggedUsages(lab, from, to);

        final Ordering<BalanceEntry> ordering = new Ordering<BalanceEntry>() {
            @Override
            public int compare(BalanceEntry left, BalanceEntry right) {
                return Long.compare(left.getTimestamp(), right.getTimestamp());
            }
        };

        if (!lastLoggedUsages.iterator().hasNext()) {
            return absent();
        }

        final BalanceEntry max = ordering.max(lastLoggedUsages);
        final Optional<Long> optionalBalance = Optional.of(calculateRoundedPriceByUnscaled(max.getBalance(), max.getScaledToPayValue()));

        LOGGER.debug(format("Calculated balance: {%s}", optionalBalance));

        return optionalBalance;
    }

    @Override
    public long caclulateTotalPrice(long lab, BillingFeature feature, Date from, Date to) {

        final FeatureUsageRepository repository = transformFeature(feature);

        checkNotNull(repository, "Unknown feature: " + feature);

        return unscalePrice(repository.sumAllRawPricesByLabUnscaled(lab, from.getTime(), to.getTime()));

    }

    public FeatureUsageRepository transformFeature(BillingFeature feature) {
        switch (feature) {
            case ARCHIVE_STORAGE:
                return (FeatureUsageRepository) context.getBean("dailyArchiveStorageUsageRepository");
            case ANALYSE_STORAGE:
                return (FeatureUsageRepository) context.getBean("dailyAnalyseStorageUsageRepository");
            case DOWNLOAD:
                return (FeatureUsageRepository) context.getBean("downloadUsageRepository");
            case PROTEIN_ID_SEARCH:
                return (FeatureUsageRepository) context.getBean("proteinIDSearchUsageRepository");
            case PUBLIC_DOWNLOAD:
                return (FeatureUsageRepository) context.getBean("publicDownloadUsageRepository");
        }
        return null;
    }

    /**
     * @param lab laboratory for calculation
     * @param day day to calculate store balance
     * @return Optional.absent() if no logged usages for this lab found
     */
    @Override
    public Optional<Long> calculateStoreBalanceForDay(long lab, Date day) {

        LOGGER.debug(format("Calculating store balance for lab {%d}. Dau {%s}", lab, day));

        final Iterable<BalanceEntry> lastLoggedUsages = loggedUsages(lab, day);

        final Ordering<BalanceEntry> ordering = new Ordering<BalanceEntry>() {
            @Override
            public int compare(BalanceEntry left, BalanceEntry right) {
                return Long.compare(left.getTimestamp(), right.getTimestamp());
            }
        };

        if (!lastLoggedUsages.iterator().hasNext()) {
            return absent();
        }

        final BalanceEntry max = ordering.max(lastLoggedUsages);
        final Optional<Long> optionalBalance = Optional.of(calculateRoundedPriceByUnscaled(max.getBalance(), max.getScaledToPayValue()));

        LOGGER.debug(format("Calculated balance: {%s}", optionalBalance));

        return optionalBalance;
    }

    private Iterable<BalanceEntry> loggedUsages(long lab, Date day) {
        return presentInstances(of(
                findLastLogged(lab, day, HourlyAnalyzableStorageUsage.class),
                findLastLogged(lab, day, DailyAnalyzableStorageUsage.class),
                findLastLogged(lab, day, HourlyArchiveStorageUsage.class),
                findLastLogged(lab, day, DailyArchiveStorageUsage.class),
                findLastLogged(lab, day, TranslationUsage.class),
                findLastLogged(lab, day, DownloadUsage.class),
                findLastLogged(lab, day, ProteinIDSearchUsage.class),
                findLastLogged(lab, day, PublicDownloadUsage.class),
                findLastLogged(lab, day, StorageVolumeUsage.class),
                findLastLogged(lab, day, ArchiveStorageVolumeUsage.class),
                findLastLogged(lab, day, ProcessingUsage.class)
        ));
    }

    private <T extends BalanceEntry> Optional<BalanceEntry> findLastLogged(long lab, Date from, Date to, Class<T> tClass) {
        final List<T> result = em.createQuery("SELECT u FROM " + tClass.getSimpleName() + " u WHERE u.lab = :lab AND u.timestamp > :startDate AND u.timestamp <= :endDate ORDER BY u.timestamp DESC ", tClass)
                .setParameter("lab", lab).setParameter("startDate", from.getTime()).setParameter("endDate", to.getTime()).setMaxResults(1).getResultList();
        if (result.isEmpty()) {
            return absent();
        }
        return fromNullable((BalanceEntry) result.iterator().next());
    }

    private Iterable<BalanceEntry> loggedUsages(long lab, Date from, Date to) {
        return presentInstances(of(
                findLastLogged(lab, from, to, HourlyAnalyzableStorageUsage.class),
                findLastLogged(lab, from, to, DailyAnalyzableStorageUsage.class),
                findLastLogged(lab, from, to, HourlyArchiveStorageUsage.class),
                findLastLogged(lab, from, to, DailyArchiveStorageUsage.class),
                findLastLogged(lab, from, to, TranslationUsage.class),
                findLastLogged(lab, from, to, DownloadUsage.class),
                findLastLogged(lab, from, to, ProteinIDSearchUsage.class),
                findLastLogged(lab, from, to, PublicDownloadUsage.class),
                findLastLogged(lab, from, to, StorageVolumeUsage.class),
                findLastLogged(lab, from, to, ArchiveStorageVolumeUsage.class),
                findLastLogged(lab, from, to, ProcessingUsage.class)
        ));
    }

    @Override
    public long countLoggedUsagesInRangePerFeature(long lab, Date from, Date to) {
        return Iterables.size(loggedUsages(lab, from, to));
    }

    @Override
    public long countLoggedUsagesInRangePerFeature(long lab, Date day) {
        return Iterables.size(loggedUsages(lab, day));
    }

    private <T extends BalanceEntry> Optional<BalanceEntry> findLastLogged(long lab, Date day, Class<T> tClass) {
        final List<T> result = em.createQuery("SELECT u FROM " + tClass.getSimpleName() + " u WHERE (u.lab = :lab AND u.day=:day) ORDER BY u.timestamp DESC ", tClass)
                .setParameter("lab", lab).setParameter("day", Long.valueOf(calculationDaySinceEpoch(day))).setMaxResults(1).getResultList();
        if (result.isEmpty()) {
            return absent();
        }
        return fromNullable((BalanceEntry) result.iterator().next());
    }

    @Override
    public BigDecimal calculateScaledFeaturePrice(long bytes, BillingFeature feature) {
        final int price = featureChargeableItemLoadingCache.getUnchecked(Transformers.transformFeature(feature)).getPrice();
        return valueOf(bytes)
                .multiply(valueOf(price)
                        .divide(valueOf(BYTES_IN_GB), ROUND_UP), ROUND_UP)
                .multiply(SCALE, ROUND_UP)///scaling for not lose small values
                .setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateScaledFeaturePriceForEachLab(long bytes, BillingFeature feature, int count) {
        final int price = featureChargeableItemLoadingCache.getUnchecked(Transformers.transformFeature(feature)).getPrice();
        return valueOf(bytes)
                .multiply(valueOf(price)
                        .divide(valueOf(BYTES_IN_GB), ROUND_UP), ROUND_UP)
                .multiply(SCALE, ROUND_UP)///scaling for not lose small values
                .divide(valueOf(count), ROUND_UP)
                .setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal scalePriceBetweenLabs(BigDecimal price, int labsCount) {
        return price.divide(valueOf(labsCount), ROUND_UP)
                .setScale(0, RoundingMode.HALF_UP);
    }

}
