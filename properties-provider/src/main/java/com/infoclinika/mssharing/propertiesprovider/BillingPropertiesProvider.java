package com.infoclinika.mssharing.propertiesprovider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BillingPropertiesProvider extends AbstractPropertiesProvider {

    @Value("${amazon.billing.prefix}")
    private String billingPrefix;

    @Value("${billing.enabled}")
    private boolean billingEnabled;

    @Value("${billing.logging.analyzable.threads.daily:10}")
    private int dailyMaxAnalyzableThreads;

    @Value("${billing.logging.analyzable.threads.hourly:150}")
    private int hourlyMaxAnalyzableThreads;

    @Value("${billing.logging.archive.threads.daily:10}")
    private int dailyMaxArchiveThreads;

    @Value("${billing.logging.archive.threads.hourly:150}")
    private int hourlyMaxArchiveThreads;

    @Value("${billing.planChangeDurationMonths:1}")
    private int planChangeDurationMonths;

    @Value("${billing.planChangeDuration}")
    private String planChangeDuration;

    @Value("${billing.server.timezone}")
    private String timeZoneId;

    @Value("${billing.storage.archive.download.restore.expiration:0}")
    private int unarchivedForDownloadMaxDays;

    @Value("${billing.storage.archive.restore.expiration:0}")
    private int unarchiveExpirationInDays;

    public String getBillingPrefix() {
        return billingPrefix;
    }

    public boolean isBillingEnabled() {
        return billingEnabled;
    }

    public int getDailyMaxAnalyzableThreads() {
        return dailyMaxAnalyzableThreads;
    }

    public int getHourlyMaxAnalyzableThreads() {
        return hourlyMaxAnalyzableThreads;
    }

    public int getDailyMaxArchiveThreads() {
        return dailyMaxArchiveThreads;
    }

    public int getHourlyMaxArchiveThreads() {
        return hourlyMaxArchiveThreads;
    }

    public int getPlanChangeDurationMonths() {
        return planChangeDurationMonths;
    }

    public String getPlanChangeDuration() {
        return planChangeDuration;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public int getUnarchivedForDownloadMaxDays() {
        return unarchivedForDownloadMaxDays;
    }

    public int getUnarchiveExpirationInDays() {
        return unarchiveExpirationInDays;
    }
}
