package com.infoclinika.mssharing.services.billing.persistence.enity.storage;

import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * @author Herman Zamula
 */
@MappedSuperclass
public class ArchiveStorageUsage extends ChargeableItemUsage {

    @Column(name = "hours")
    public int hours = 1;
    //Field used for aggregation storage usages by hours and days.
    @Column(name = "compressed")
    public boolean compressed = false;

    public ArchiveStorageUsage() {
        super();
    }

    public ArchiveStorageUsage(long lab, long user, long file1, long bytes, Date timestamp, String ownerName, String instrument,
                               long price, String file) {
        super(lab, user, file1, bytes, timestamp, ownerName, instrument, price, file);
    }

    public ArchiveStorageUsage(long lab, long user, Long file, long bytes, long timestamp, String ownerName, String instrument, long price, String fileName, int hours, long balance) {
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

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }
}
