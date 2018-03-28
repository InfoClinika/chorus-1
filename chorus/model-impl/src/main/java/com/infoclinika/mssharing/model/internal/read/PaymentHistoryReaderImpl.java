package com.infoclinika.mssharing.model.internal.read;

import com.google.common.collect.*;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.payment.FeatureLog;
import com.infoclinika.mssharing.model.internal.entity.payment.LabPaymentAccount;
import com.infoclinika.mssharing.model.internal.entity.payment.PayPalLogEntry;
import com.infoclinika.mssharing.model.internal.entity.payment.StoreLogEntry;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.read.PaymentHistoryReader;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import com.infoclinika.mssharing.services.billing.rest.api.model.DailyUsageLine;
import com.infoclinika.mssharing.services.billing.rest.api.model.HistoryForMonthReference;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.*;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.ImmutableSortedSet.copyOf;
import static com.google.common.collect.ImmutableSortedSet.orderedBy;
import static com.infoclinika.mssharing.model.internal.read.Transformers.HISTORY_LINES_BY_DATE_REVERSED;
import static com.infoclinika.mssharing.model.read.PaymentHistoryReader.HistoryItemType.USAGE;
import static java.util.Optional.ofNullable;
import static org.joda.time.DateTimeZone.forTimeZone;

/**
 * @author Elena Kurilina, Herman Zamula
 */
@Service
public class PaymentHistoryReaderImpl implements PaymentHistoryReader {

    private final Comparator<HistoryForMonth> historyForMonthComparator = new Comparator<HistoryForMonth>() {
        @Override
        public int compare(HistoryForMonth o1, HistoryForMonth o2) {
            return o1.monthYear.compareTo(o2.monthYear);
        }
    };
    @Inject
    private PayPalLogEntryRepository payPalLogEntryRepository;
    @Inject
    private StoreCreditLogEntryRepository storeCreditLogEntryRepository;
    @Inject
    private LabRepository labRepository;
    @Inject
    private LabPaymentAccountRepository labPaymentAccountRepository;
    @Inject
    private FeatureLogRepository featureLogRepository;
    @Resource(name = "billingRestService")
    private BillingService billingService;
    @Inject
    private Transformers transformers;
    @Inject
    private RuleValidator ruleValidator;

    private static final Logger LOG = Logger.getLogger(PaymentHistoryReaderImpl.class);

    @Override
    public HistoryForLab readAll(long actor, long lab) {
        if (!ruleValidator.canReadLabBilling(actor, lab)) {
            throw new AccessDenied(String.format("User {%d} has not permission on lab {%d}", actor, lab));
        }
        List<PayPalLogEntry> payPalLogEntryList = payPalLogEntryRepository.findByLab(lab);
        List<StoreLogEntry> storeLogEntries = storeCreditLogEntryRepository.findOutByLab(lab);
        List<StoreLogEntry> storeInLogEntries = storeCreditLogEntryRepository.findInByLab(lab);
        List<FeatureLog> featureLogs = featureLogRepository.findByLab(lab);
        Collection<PaymentHistoryLine> lines = new HashSet<>();
        lines.addAll(Collections2.transform(payPalLogEntryList, transformers.paypalHistoryLineTransformFunction));
        lines.addAll(Collections2.transform(storeLogEntries, transformers.storeHistoryLineTransformFunction));
        lines.addAll(Collections2.transform(storeInLogEntries, transformers.storeHistoryLineTransformFunction));
        lines.addAll(Collections2.transform(featureLogs, transformers.featureHistoryLineTransformFunction));
        lines.addAll(readHistory(lab));
        ImmutableSortedSet<HistoryForMonth> months = groupByMonth(copyOf(HISTORY_LINES_BY_DATE_REVERSED, lines));
        final LabPaymentAccount byLab = labPaymentAccountRepository.findByLab(lab);
        return new HistoryForLab(labRepository.findOne(lab).getName(),
                billingService.calculateRoundedPriceByUnscaled(byLab.getStoreBalance(), byLab.getScaledToPayValue()),
                months, false);
    }

    @Override
    public ImmutableSet<HistoryForMonth> readTopUpBalance(long actor, long lab) {
        if (!ruleValidator.canReadLabBilling(actor, lab)) {
            throw new AccessDenied(String.format("User {%d} has not permission on lab {%d}", actor, lab));
        }
        List<StoreLogEntry> storeLogEntries = storeCreditLogEntryRepository.findInByLab(lab);
        Collection<PaymentHistoryLine> lines = Collections2.transform(storeLogEntries, transformers.storeHistoryLineTransformFunction);
        return groupByMonth(copyOf(HISTORY_LINES_BY_DATE_REVERSED, lines));
    }

    @Override
    public ImmutableSet<HistoryForMonth> readDailyUsage(long actor, long lab) {
        if (!ruleValidator.canReadLabBilling(actor, lab)) {
            throw new AccessDenied(String.format("User {%d} has not permission on lab {%d}", actor, lab));
        }
        ImmutableSortedSet<PaymentHistoryLine> dailyUsageHistoryLines = readHistory(lab);
        return groupByMonth(dailyUsageHistoryLines);
    }

    @Override
    public HistoryForLab readNextHistory(long userId, long lab, long previousCount, long nextCount) {
        if (!ruleValidator.canReadLabBilling(userId, lab)) {
            throw new AccessDenied(String.format("User {%d} has not permission on lab {%d}", userId, lab));
        }

        final LabPaymentAccount byLab = labPaymentAccountRepository.findByLab(lab);
        final Date calculationDate = new Date();

        final Date fromTime = getPrevDay(calculationDate, (int) (previousCount + nextCount));
        final Date toTime = getPrevDay(calculationDate, (int) (previousCount));
        List<StoreLogEntry> storeInLogEntries = storeCreditLogEntryRepository.findInByLab(lab, fromTime, toTime);
        Collection<PaymentHistoryLine> lines = new HashSet<>();
        lines.addAll(Collections2.transform(storeInLogEntries, transformers.storeHistoryLineTransformFunction));
        final Collection<? extends PaymentHistoryLine> paymentHistoryLines = readHistoryReversed(lab, previousCount, nextCount);
        lines.addAll(paymentHistoryLines);

        ImmutableSortedSet<HistoryForMonth> months = groupByMonth(copyOf(HISTORY_LINES_BY_DATE_REVERSED, lines));

        return new HistoryForLab(labRepository.findOne(lab).getName(),
                billingService.calculateRoundedPriceByUnscaled(byLab.getStoreBalance(), byLab.getScaledToPayValue()),
                months, Iterables.getLast(paymentHistoryLines).hasNext);
    }

    @Override
    public Optional<HistoryForMonthReference> readMonthsReferences(long userId, long lab, Date month) {
        return Optional.ofNullable(billingService.readMonthsReferences(userId, lab, month.getTime()));
    }

    private Collection<? extends PaymentHistoryLine> readHistoryReversed(long lab, long previousCount, long nextCount) {
        ImmutableSortedSet.Builder<PaymentHistoryLine> invoices = orderedBy(HISTORY_LINES_BY_DATE_REVERSED);

        LOG.debug(String.format("Reading history for lab {%d}. Previous count: {%d}, next: {%d}", lab, previousCount, nextCount));
        final LabPaymentAccount account = labPaymentAccountRepository.findByLab(lab);

        Date current = getPrevDay(new Date(), (int) previousCount);
        final Date endLoggingDate = new DateTime(forTimeZone(transformers.serverTimezone)).minusMonths(1).dayOfMonth().withMinimumValue().millisOfDay().withMinimumValue().toDate();
        final Date endDate = getPrevDay(new Date(), (int) (previousCount + nextCount));
        long balance = 0;

        do {
            final Date prevDay = getPrevDay(current);
            LOG.info(String.format("Reading daily history from {%s} to {%s}", prevDay, current));

            final long fBalance = balance;
            final Date fCurrent = current;

            final long dayInMls = middleDate(prevDay, fCurrent).getTime();
            LOG.info("Going to get daily usage line for " + dayInMls);
            final DailyUsageLine dailyUsageLine = nullIfEmpty(billingService.getDailyUsageLine(lab, dayInMls));
            LOG.info("Got result: " + dailyUsageLine);
            final PaymentHistoryLine paymentHistoryLine = ofNullable(dailyUsageLine)
                    .map(line -> createPaymentHistoryLine(line, fCurrent, prevDay, fBalance, endLoggingDate.before(endDate)))
                    .orElseGet(() -> createPaymentHistoryLine(fCurrent, prevDay, lab, fBalance, endLoggingDate.before(endDate)));

            invoices.add(paymentHistoryLine);

            balance = paymentHistoryLine.balance;
            current = prevDay;

            LOG.info("current:"  + current);
            LOG.info("endDate:"  + endDate);
            LOG.info("endLoggingDate:"  + endLoggingDate);
            LOG.info("account.getAccountCreationDate():"  + account.getAccountCreationDate());
        }
        while (current.after(endDate) && current.after(endLoggingDate) && current.after(account.getAccountCreationDate()));

        return invoices.build();
    }

    private Date middleDate(Date first, Date second) {
        if (second.before(first)) {
            return new Date((second.getTime() + (first.getTime() - second.getTime()) / 2));
        }
        return new Date((first.getTime() + (second.getTime() - first.getTime()) / 2));
    }

    private ImmutableSortedSet<PaymentHistoryLine> readHistory(final long lab) {

        ImmutableSortedSet.Builder<PaymentHistoryLine> invoices = orderedBy(HISTORY_LINES_BY_DATE_REVERSED);
        final LabPaymentAccount account = labPaymentAccountRepository.findByLab(lab);

        Date current = account.getAccountCreationDate();
        final Date today = new Date();
        long balance = 0;

        do {
            final Date nextDay = getNextDay(current);

            final long fBalance = balance;
            final Date fCurrent = current;

            final DailyUsageLine dailyUsageLine = nullIfEmpty(billingService.getDailyUsageLine(lab, current.getTime()));
            final PaymentHistoryLine paymentHistoryLine = ofNullable(dailyUsageLine)
                    .map(line -> createPaymentHistoryLine(line, fCurrent, nextDay, fBalance, true))
                    .orElseGet(() -> createPaymentHistoryLine(fCurrent, nextDay, lab, fBalance, true));

            invoices.add(paymentHistoryLine);

            balance = paymentHistoryLine.balance;
            current = nextDay;
        }
        while (current.before(today));

        return invoices.build();
    }

    private PaymentHistoryLine createPaymentHistoryLine(DailyUsageLine line, Date fCurrent, Date nextDay, long fBalance, boolean hasNext) {
        return new PaymentHistoryLine(
                fCurrent,
                nextDay,
                transformers.historyLineDateFormat.format(fCurrent),
                FEATURES_USAGE,
                line.amount,
                fromNullable(line.balance).or(fBalance),
                USAGE,
                line.timeZoneId,
                hasNext
        );
    }

    private PaymentHistoryLine createPaymentHistoryLine(Date fCurrent, Date nextDay, long lab, long fBalance, boolean hasNext) {
        final long dayInMls = middleDate(fCurrent, nextDay).getTime();
        LOG.info("Going to calculate total pay for lab for day: " + dayInMls);
        final long toPay = billingService.calculateTotalToPayForLabForDay(lab, dayInMls);
        final Long currentBalance = fromNullable(billingService.calculateStoreBalanceForDay(lab, dayInMls)).or(fBalance);

        return new PaymentHistoryLine(
                fCurrent,
                nextDay,
                transformers.historyLineDateFormat.format(fCurrent),
                FEATURES_USAGE,
                toPay,
                currentBalance,
                USAGE,
                transformers.historyLineDateFormat.getTimeZone().getID(),
                hasNext);
    }

    private Date getNextDay(Date date) {
        return new DateTime(date)
                .withZone(forTimeZone(transformers.serverTimezone))
                .plusDays(1)
                .withTimeAtStartOfDay()
                .toDate();
    }

    private Date getPrevDay(Date date) {
        return getPrevDay(date, 1);
    }

    private Date getPrevDay(Date date, int count) {
        return new DateTime(date)
                .withZone(forTimeZone(transformers.serverTimezone))
                .minusDays(count)
                .withTime(23, 59, 59, 999) // End of day
                .toDate();
    }

    private ImmutableSortedSet<HistoryForMonth> groupByMonth(ImmutableSortedSet<PaymentHistoryLine> lines) {

        final ImmutableSortedSet.Builder<HistoryForMonth> historyForMonths = ImmutableSortedSet.orderedBy(historyForMonthComparator);

        final ImmutableListMultimap<Integer, PaymentHistoryLine> groupedByMonth = Multimaps.index(lines, line -> {
            return getServerTime(line.date).getMonthOfYear();
        });

        for (Map.Entry<Integer, Collection<PaymentHistoryLine>> linesEntries : groupedByMonth.asMap().entrySet()) {

            final Collection<PaymentHistoryLine> paymentHistoryLines = linesEntries.getValue();
            final DateTime date = getServerTime(paymentHistoryLines.iterator().next().date);

            historyForMonths.add(new HistoryForMonth(date.toLocalDate().toDate(),
                    ImmutableSortedSet.copyOf(HISTORY_LINES_BY_DATE_REVERSED, paymentHistoryLines)));

        }

        return historyForMonths.build().descendingSet();
    }

    private DateTime getServerTime(Date date) {
        return new DateTime(date, forTimeZone(transformers.serverTimezone));
    }

    @Nullable
    private static DailyUsageLine nullIfEmpty(DailyUsageLine dailyUsageLine) {
        if (dailyUsageLine == null || dailyUsageLine.labId < 1){
            return null;
        }
        return dailyUsageLine;
    }

}
