package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem.Feature;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.internal.repository.LabPaymentAccountRepository;
import com.infoclinika.mssharing.services.billing.persistence.enity.MonthlySummary;
import com.infoclinika.mssharing.services.billing.persistence.repository.MonthlySummaryRepository;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;

import static com.google.common.base.Optional.presentInstances;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.model.internal.read.Transformers.transformFeature;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author Herman Zamula
 */
@Component
public class MonthlySummaryUsageLogger {

    private static final Logger LOG = Logger.getLogger(MonthlySummaryUsageLogger.class);
    public static final int MAX_ATTEMPTS = 5;
    private final TimeZone timeZone;
    @Inject
    private PaymentCalculationsHelper calculationsHelper;
    @Inject
    private LabPaymentAccountRepository accountRepository;
    @Inject
    private MonthlySummaryRepository monthlySummaryRepository;

    @Inject
    protected MonthlySummaryUsageLogger(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public void logMonth(Date month) {

        LOG.debug("Start logging usages for the date month: " + month);

        final List<LabPaymentAccount> labs = newArrayList(accountRepository.findAll());

        LOG.debug("Found " + labs.size() + " accounts");

        final DateTime dateTime = new DateTime(month, DateTimeZone.forTimeZone(timeZone));
        final Date startOfMonth = dateTime.dayOfMonth().withMinimumValue().millisOfDay().withMinimumValue().toDate();
        final Date endOfMonth = dateTime.dayOfMonth().withMaximumValue().minuteOfDay().withMaximumValue().toDate();

        LOG.debug("Start logging. Start of month: " + startOfMonth + ", end of month: " + endOfMonth);

        final List<Optional<MonthlySummary>> monthlySummaries = labs.stream()
                .map(createSummaryFn(startOfMonth, endOfMonth))
                .collect(toList());

        LOG.debug("End logging usages for month. Total monthly summary records saved: " + size(presentInstances(monthlySummaries)));

    }

    private Function<LabPaymentAccount, Optional<MonthlySummary>> createSummaryFn(Date startOfMonth, Date endOfMonth) {

        return account -> {

            int i = 1;
            boolean completed = false;
            Optional<MonthlySummary> summary = Optional.absent();

            do {
                try {
                    LOG.debug("Attampt #" + i + " of " + MAX_ATTEMPTS);
                    summary = Optional.of(logAccount(startOfMonth, endOfMonth, account));
                    completed = true;
                } catch (Exception ex) {
                    LOG.warn("Unexpected exception while saving monthly usages. Retrying.", ex);
                    completed = false;
                    i++;
                }
            } while (i < MAX_ATTEMPTS && !completed);

            return summary;

        };

    }

    private MonthlySummary logAccount(Date startOfMonth, Date endOfMonth, LabPaymentAccount account) {
        final Long labId = account.getLab().getId();
        LOG.debug("Logging for lab: " + labId);

        final MonthlySummary summary = new MonthlySummary();
        final Map<Feature, Long> totalByFeature = getTotalByFeature(startOfMonth, endOfMonth, labId);

        summary.getTotalByFeature().putAll(totalByFeature);
        summary.setMonthlyTotal(totalByFeature.entrySet().stream().mapToLong(Map.Entry::getValue).sum());
        summary.setCalculationDate(new Date());
        summary.setEndMonthBalance(calculationsHelper.calculateStoreBalance(labId, startOfMonth, endOfMonth).or(account.getStoreBalance()));
        summary.setLabId(labId);
        summary.setLoggedMonth(startOfMonth);

        return monthlySummaryRepository.save(summary);
    }

    private Map<Feature, Long> getTotalByFeature(Date startOfMonth, Date endOfMonth, long labId) {
        return newArrayList(Feature.getPerFileMembers()).stream()
                .map(feature -> {
                    final long featurePrice = calculationsHelper.caclulateTotalPrice(labId,
                            transformFeature(feature), startOfMonth, endOfMonth);
                    return new Pair<>(feature, featurePrice);
                })
                .collect(toMap(Pair::getKey, Pair::getValue));
    }

}
