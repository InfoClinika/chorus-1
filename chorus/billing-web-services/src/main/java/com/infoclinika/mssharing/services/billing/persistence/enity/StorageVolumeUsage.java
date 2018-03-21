package com.infoclinika.mssharing.services.billing.persistence.enity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

/**
 * @author timofey 21.03.16.
 */
@Entity
@Table(name = "billing_storage_volume_usage", indexes = {
        @Index(columnList = "timestamp"),
        @Index(columnList = "user"),
        @Index(columnList = "lab")
})
public class StorageVolumeUsage extends AbstractPersistable<Long> implements BalanceEntry {

    private long user;
    private long lab;
    private long timestamp;
    private int volumesCount;
    private long charge;
    private long balance;
    private long scaledToPayValue;
    private long day;

    public StorageVolumeUsage(long user, long lab, long timestamp, int volumesCount, long charge, long balance, long scaledToPayValue, long day) {
        this.user = user;
        this.lab = lab;
        this.timestamp = timestamp;
        this.volumesCount = volumesCount;
        this.charge = charge;
        this.balance = balance;
        this.scaledToPayValue = scaledToPayValue;
        this.day = day;
    }

    public StorageVolumeUsage() {}

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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getVolumesCount() {
        return volumesCount;
    }

    public void setVolumesCount(int volumesCount) {
        this.volumesCount = volumesCount;
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

        StorageVolumeUsage that = (StorageVolumeUsage) o;

        if (user != that.user) return false;
        if (lab != that.lab) return false;
        if (timestamp != that.timestamp) return false;
        if (volumesCount != that.volumesCount) return false;
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
        result = 31 * result + volumesCount;
        result = 31 * result + (int) (charge ^ (charge >>> 32));
        result = 31 * result + (int) (balance ^ (balance >>> 32));
        result = 31 * result + (int) (scaledToPayValue ^ (scaledToPayValue >>> 32));
        result = 31 * result + (int) (day ^ (day >>> 32));
        return result;
    }
}
