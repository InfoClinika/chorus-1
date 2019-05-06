package com.infoclinika.mssharing.services.billing.persistence.enity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Date;

/**
 * @author Herman Zamula
 */
@Entity
@Table(name = "daily_usage_summary",
    uniqueConstraints = {@UniqueConstraint(name = "lab_day", columnNames = {"lab_id", "server_day_formatted"})}
)
public class DailySummary extends AbstractPersistable<Long> {

    private Date date;

    @Column(name = "lab_id", nullable = false)
    private long labId;

    @Column(name = "server_day_formatted")
    private String serverDayFormatted;

    @Column(name = "time_zone_id")
    private String timeZoneId;

    @Basic
    private Long balance;

    @Basic(optional = false)
    private long amount;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getLabId() {
        return labId;
    }

    public void setLabId(long labId) {
        this.labId = labId;
    }

    public String getServerDayFormatted() {
        return serverDayFormatted;
    }

    public void setServerDayFormatted(String serverDayFormatted) {
        this.serverDayFormatted = serverDayFormatted;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    @Nullable
    public Long getBalance() {
        return balance;
    }

    public void setBalance(@Nullable Long balance) {
        this.balance = balance;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
