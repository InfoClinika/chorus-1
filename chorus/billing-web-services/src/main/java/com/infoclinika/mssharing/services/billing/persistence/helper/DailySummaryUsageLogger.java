package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.LabPaymentAccountRepository;
import com.infoclinika.mssharing.services.billing.persistence.enity.DailySummary;
import com.infoclinika.mssharing.services.billing.persistence.repository.DailySummaryRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.abs;

/**
 * @author Herman Zamula
 */
@Component
public class DailySummaryUsageLogger {

    private static final Logger LOG = LoggerFactory.getLogger(DailySummaryUsageLogger.class);

    private final TimeZone timeZone;
    @Inject
    private PaymentCalculationsHelper calculationsHelper;
    @Inject
    private LabPaymentAccountRepository accountRepository;

    @Inject
    private DailySummaryRepository dailySummaryRepository;

    @Inject
    private Transformers transformers;

    @Inject
    protected DailySummaryUsageLogger(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public boolean saveDay(Date day) {

        if (dailySummaryRepository.isDayExists(transformers.historyLineDateFormat
            .format(new DateTime(day, DateTimeZone.forTimeZone(timeZone)).toDate()))) {
            LOG.warn("Day is already logged. Skipping day: {}", day);
            return false;
        }


        LOG.debug("Start logging usages for the date day: {}", day);

        final List<LabPaymentAccount> labs = newArrayList(accountRepository.findAll());

        LOG.debug("Found {} accounts", labs.size());

        final DateTime dateTime = new DateTime(day, DateTimeZone.forTimeZone(timeZone));
        final Date startOfDay = dateTime.millisOfDay().withMinimumValue().toDate();
        final Date endOfDay = dateTime.millisOfDay().withMaximumValue().toDate();

        final Set<DailySummary> dailySummaries = labs.stream()
            .map(account -> createDailySummary(day, startOfDay, endOfDay, account))
            .collect(Collectors.toSet());

        LOG.debug("Saving total daily usages. Count {}", dailySummaries.size());

        dailySummaryRepository.save(dailySummaries);

        LOG.debug("Completed logging usages for the date day: {}", day);

        return true;

    }

    private DailySummary createDailySummary(Date day, Date startOfDay, Date endOfDay, LabPaymentAccount account) {
        final Long id = account.getLab().getId();
        final DailySummary dailySummary = new DailySummary();
        dailySummary.setLabId(id);
        dailySummary
            .setBalance(calculationsHelper.calculateStoreBalanceForDay(id, middleDate(startOfDay, endOfDay)).orNull());
        dailySummary
            .setAmount(abs(calculationsHelper.calculateTotalToPayForLabForDay(id, middleDate(startOfDay, endOfDay))));
        dailySummary.setDate(day);
        dailySummary.setServerDayFormatted(transformers.historyLineDateFormat.format(startOfDay));
        dailySummary.setTimeZoneId(timeZone.getID());
        return dailySummary;
    }

    private Date middleDate(Date first, Date second) {
        if (second.before(first)) {
            return new Date((second.getTime() + (first.getTime() - second.getTime()) / 2));
        }
        return new Date((first.getTime() + (second.getTime() - first.getTime()) / 2));
    }
}
