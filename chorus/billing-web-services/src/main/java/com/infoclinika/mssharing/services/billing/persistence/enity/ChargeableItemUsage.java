package com.infoclinika.mssharing.services.billing.persistence.enity;

import org.hibernate.annotations.Index;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.annotation.Nullable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.beans.Transient;
import java.util.Date;

/**
 * @author Elena Kurilina
 */
@MappedSuperclass
public abstract class ChargeableItemUsage extends AbstractPersistable<Long> implements BalanceEntry {

    /**
     * The value which the price will be multiplied on usage log. Used for price scaling.
     */
    public static final long PRICE_SCALE_VALUE = 100000;
    public static final int PRICE_PRECISION = 16;

    @Basic
    @Index(name = "LAB_IDX")
    private Long lab;
    @Basic
    private Long user;
    @Index(name = "FILE_IDX")
    @Basic
    private Long file;
    @Basic
    private long bytes = 0;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "used_by")
    private String usedBy;
    @Basic
    private String instrument;
    @Basic
    private long charge;
    @Basic
    @Index(name = "TIMESTAMP_IDX")
    private long timestamp = new Date().getTime();
    @Column
    private long balance;
    @Column(name = "scaled_to_pay_value")
    private long scaledToPayValue;
    @Index(name = "DAY_IDX")
    @Basic
    private long day;

    public ChargeableItemUsage() {
    }

    public ChargeableItemUsage(Long lab, Long user, Long file, long bytes, Date timestamp, String ownerName,
                               String instrument, long charge, String fileName) {
        this.setLab(lab);
        this.setUser(user);
        this.setFile(file);
        this.setBytes(bytes);
        this.setTimestamp(timestamp.getTime());
        this.setUsedBy(ownerName);
        this.setInstrument(instrument);
        this.setCharge(charge);
        this.setFileName(fileName);
    }

    public Long getLab() {
        return lab;
    }

    public long getBytes() {
        return bytes;
    }

    @Transient
    public Date getTimestampDate() {
        return new Date(timestamp);
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public String getUsedBy() {
        return usedBy;
    }

    public String getInstrument() {
        return instrument;
    }

    public long getCharge() {
        return charge;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getUser() {
        return user;
    }

    @Nullable
    public Long getFile() {
        return file;
    }

    @Override
    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void setLab(Long lab) {
        this.lab = lab;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public void setFile(@Nullable Long file) {
        this.file = file;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setUsedBy(String usedBy) {
        this.usedBy = usedBy;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public void setCharge(long price) {
        this.charge = price;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Transient
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp.getTime();
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
}
