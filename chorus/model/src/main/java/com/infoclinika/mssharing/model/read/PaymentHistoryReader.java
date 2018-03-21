package com.infoclinika.mssharing.model.read;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.services.billing.rest.api.model.HistoryForMonthReference;

import java.util.Date;
import java.util.Optional;

/**
 * @author Elena Kurilina
 */
public interface PaymentHistoryReader {

    @Deprecated
    String creditCharge = "Charge from credit card";
    @Deprecated
    String storageCharge = "Charge from balance";
    String TOP_UP_BALANCE = "Top up balance";
    String FEATURES_USAGE = "Feature usage";

    HistoryForLab readAll(long actor, long lab);

    ImmutableSet<HistoryForMonth> readTopUpBalance(long actor, long lab);

    ImmutableSet<HistoryForMonth> readDailyUsage(long actor, long lab);

    HistoryForLab readNextHistory(long userId, long lab, long previousCount, long nextCount);

    Optional<HistoryForMonthReference> readMonthsReferences(long userId, long lab, Date month);

    class HistoryForLab {
        public final String labName;
        public final long storeBalance;
        public final ImmutableSortedSet<HistoryForMonth> months;
        public final boolean hasNext;

        public HistoryForLab(String labName, long storeBalance, ImmutableSortedSet<HistoryForMonth> months, boolean hasNext) {
            this.labName = labName;
            this.storeBalance = storeBalance;
            this.months = months;
            this.hasNext = hasNext;
        }
    }

    class HistoryForMonth {
        public final Date monthYear;
        public final ImmutableSortedSet<PaymentHistoryLine> lines;

        public HistoryForMonth(Date monthYear, ImmutableSortedSet<PaymentHistoryLine> lines) {
            this.monthYear = monthYear;
            this.lines = lines;
        }

        @Override
        @SuppressWarnings("all")
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HistoryForMonth that = (HistoryForMonth) o;

            if (lines != null ? !lines.equals(that.lines) : that.lines != null) return false;
            if (monthYear != null ? !monthYear.equals(that.monthYear) : that.monthYear != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = monthYear != null ? monthYear.hashCode() : 0;
            result = 31 * result + (lines != null ? lines.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "HistoryForMonth{" +
                    "monthYear=" + monthYear +
                    ", lines=" + lines +
                    '}';
        }
    }

    class PaymentHistoryLine {
        public final Date date;
        public final Date toDate;
        public final String serverDay;
        public final String description;
        public final Long amount;
        public final long balance;
        public final HistoryItemType type;
        public final String timeZoneId;
        public final boolean hasNext;

        public PaymentHistoryLine(Date date, Date toDate, String serverDay, String description, Long amount, long balance, HistoryItemType type, String timeZoneId, boolean hasNext) {
            this.date = date;
            this.toDate = toDate;
            this.serverDay = serverDay;
            this.description = description;
            this.amount = amount;
            this.balance = balance;
            this.type = type;
            this.timeZoneId = timeZoneId;
            this.hasNext = hasNext;
        }

        @Override
        @SuppressWarnings("all")
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PaymentHistoryLine that = (PaymentHistoryLine) o;

            if (balance != that.balance) return false;
            if (hasNext != that.hasNext) return false;
            if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
            if (date != null ? !date.equals(that.date) : that.date != null) return false;
            if (description != null ? !description.equals(that.description) : that.description != null) return false;
            if (serverDay != null ? !serverDay.equals(that.serverDay) : that.serverDay != null) return false;
            if (timeZoneId != null ? !timeZoneId.equals(that.timeZoneId) : that.timeZoneId != null) return false;
            if (toDate != null ? !toDate.equals(that.toDate) : that.toDate != null) return false;
            if (type != that.type) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = date != null ? date.hashCode() : 0;
            result = 31 * result + (toDate != null ? toDate.hashCode() : 0);
            result = 31 * result + (serverDay != null ? serverDay.hashCode() : 0);
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (amount != null ? amount.hashCode() : 0);
            result = 31 * result + (int) (balance ^ (balance >>> 32));
            result = 31 * result + (type != null ? type.hashCode() : 0);
            result = 31 * result + (timeZoneId != null ? timeZoneId.hashCode() : 0);
            result = 31 * result + (hasNext ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "PaymentHistoryLine{" +
                    "date=" + date +
                    ", toDate=" + toDate +
                    ", serverDay='" + serverDay + '\'' +
                    ", description='" + description + '\'' +
                    ", amount=" + amount +
                    ", balance=" + balance +
                    ", type=" + type +
                    ", timeZoneId='" + timeZoneId + '\'' +
                    ", hasNext=" + hasNext +
                    '}';
        }
    }

    enum HistoryItemType {
        FEATURE, STORE, PAYPAL, USAGE
    }


}
