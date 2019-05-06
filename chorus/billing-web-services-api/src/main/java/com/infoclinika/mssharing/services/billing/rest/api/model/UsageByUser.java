package com.infoclinika.mssharing.services.billing.rest.api.model;

import java.util.Date;
import java.util.Set;

/**
 * @author andrii.loboda
 */
public class UsageByUser {

    public String userName;
    public long userId;
    public long totalUsedFeatureValue;
    public long totalPrice;
    public Set<? extends UsageLine> usageLines;
    public long filesCount;
    public long balance;

    public UsageByUser() {
    }

    public UsageByUser(String userName, long userId, long totalUsedFeatureValue, long totalPrice,
                       Set<? extends UsageLine> usageLines, long filesCount, long balance) {
        this.userName = userName;
        this.userId = userId;
        this.totalUsedFeatureValue = totalUsedFeatureValue;
        this.totalPrice = totalPrice;
        this.usageLines = usageLines;
        this.filesCount = filesCount;
        this.balance = balance;
    }

    public abstract static class UsageLine {
        public long usedFeatureValue;
        public long price;
        public long loggedPrice;
        public long balance;
        public Date timestamp;

        public UsageLine() {
        }

        public UsageLine(long usedFeatureValue, long price, long loggedPrice, long balance, Date timestamp) {
            this.usedFeatureValue = usedFeatureValue;
            this.price = price;
            this.loggedPrice = loggedPrice;
            this.balance = balance;
            this.timestamp = timestamp;
        }
    }

    public static class ExperimentUsageLine extends UsageLine {
        public long experimentId;
        public String experimentName;

        public ExperimentUsageLine(long usedFeatureValue, long price, long loggedPrice, long balance, long experimentId,
                                   String experimentName, Date date) {
            super(usedFeatureValue, price, loggedPrice, balance, date);
            this.experimentId = experimentId;
            this.experimentName = experimentName;
        }

        public ExperimentUsageLine() {
        }
    }

    public static class FileUsageLine extends UsageLine {
        public long fileId;
        public String instrument;
        public String fileName;
        public int hours;

        public FileUsageLine(long fileId, long usedFeatureValue,
                             String instrument, long price, long loggedPrice, long balance, int days, String fileName,
                             Date date) {
            super(usedFeatureValue, price, loggedPrice, balance, date);
            this.fileId = fileId;
            this.instrument = instrument;
            this.hours = days;
            this.fileName = fileName;
        }

        public FileUsageLine() {
        }
    }
}
