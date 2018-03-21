package com.infoclinika.mssharing.services.billing.persistence.enity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

/**
 * @author timofey 21.03.16.
 */
@Entity
@Table(name = "billing_processing_usage", indexes = {
        @Index(columnList = "timestamp"),
        @Index(columnList = "user"),
        @Index(columnList = "lab")
})
public class ProcessingUsage extends AbstractPersistable<Long> implements BalanceEntry {

    private long user;
    private long lab;
    private long timestamp;
    private long charge;
    private long balance;
    private long scaledToPayValue;
    private long day;

    public ProcessingUsage(long user, long lab, long timestamp, long charge, long balance, long scaledToPayValue, long day) {
        this.user = user;
        this.lab = lab;
        this.timestamp = timestamp;
        this.charge = charge;
        this.balance = balance;
        this.scaledToPayValue = scaledToPayValue;
        this.day = day;
    }

    public ProcessingUsage() {}

    public long getUser() {
        return user;
    }

    public void setUser(long user) {
        this.user = user;
    }

    public long getLab() {
        return lab;
    }

    public void setLab(long lab) {
        this.lab = lab;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getCharge() {
        return charge;
    }

    public void setCharge(long charge) {
        this.charge = charge;
    }

    @Override
    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    @Override
    public long getScaledToPayValue() {
        return scaledToPayValue;
    }

    public void setScaledToPayValue(long scaledToPayValue) {
        this.scaledToPayValue = scaledToPayValue;
    }

    public long getDay() {
        return day;
    }

    public void setDay(long day) {
        this.day = day;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ProcessingUsage that = (ProcessingUsage) o;

        if (user != that.user) return false;
        if (lab != that.lab) return false;
        if (timestamp != that.timestamp) return false;
        if (charge != that.charge) return false;
        if (balance != that.balance) return false;
        if (scaledToPayValue != that.scaledToPayValue) return false;
        return day == that.day;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (user ^ (user >>> 32));
        result = 31 * result + (int) (lab ^ (lab >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (charge ^ (charge >>> 32));
        result = 31 * result + (int) (balance ^ (balance >>> 32));
        result = 31 * result + (int) (scaledToPayValue ^ (scaledToPayValue >>> 32));
        result = 31 * result + (int) (day ^ (day >>> 32));
        return result;
    }
}
