package com.infoclinika.mssharing.services.billing.persistence.enity.storage;

import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import java.beans.Transient;
import java.util.Date;

/**
 * @author Elena Kurilina
 */
@MappedSuperclass
public class AnalyzableStorageUsage extends ChargeableItemUsage {

    @Column(name = "hours")
    private int hours = 1;
    @Column(name = "translated_bytes")
    private long translatedBytes = 0;
    @Column(name = "translated_charge")
    private long translatedCharge = 0;
    @Column(name = "compressed")
    private boolean compressed = false;

    public AnalyzableStorageUsage() {
    }

    public AnalyzableStorageUsage(long lab, long user, long file1, long bytes, Date timestamp, String ownerName, String instrument,
                                  long price, String file) {
        super(lab, user, file1, bytes, timestamp, ownerName, instrument, price, file);
    }

    public AnalyzableStorageUsage(long lab, long user, Long file, long bytes, long timestamp, String ownerName, String instrument, long price, String fileName, int hours, long balance) {
        super(lab, user, file, bytes, new Date(timestamp), ownerName, instrument, price, fileName);
        this.hours = hours;
        super.setBalance(balance);
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public long getTranslatedBytes() {
        return translatedBytes;
    }

    public void setTranslatedBytes(long translatedBytes) {
        this.translatedBytes = translatedBytes;
    }

    public long getTranslatedCharge() {
        return translatedCharge;
    }

    public void setTranslatedCharge(long translatedPrice) {
        this.translatedCharge = translatedPrice;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    @Transient
    public long getTotalPrice() {
        return getCharge() + getTranslatedCharge();
    }

}
