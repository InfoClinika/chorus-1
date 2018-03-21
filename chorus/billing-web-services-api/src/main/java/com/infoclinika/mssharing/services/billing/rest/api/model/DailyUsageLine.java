package com.infoclinika.mssharing.services.billing.rest.api.model;

import javax.annotation.Nullable;
import java.util.Date;

/**
 * @author Herman Zamula
 */
public class DailyUsageLine {
    public long labId;
    public Date date;
    public String serverDayFormatted;
    public String timeZoneId;
    @Nullable
    public Long balance;
    public long amount;

    public DailyUsageLine(long labId, Date date, String serverDayFormatted, String timeZoneId, @Nullable Long balance, long amount) {

        this.labId = labId;
        this.date = date;
        this.serverDayFormatted = serverDayFormatted;
        this.timeZoneId = timeZoneId;
        this.balance = balance;
        this.amount = amount;
    }

    public DailyUsageLine() {

    }

}
