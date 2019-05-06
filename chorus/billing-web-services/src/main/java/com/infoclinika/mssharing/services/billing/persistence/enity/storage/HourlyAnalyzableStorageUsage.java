package com.infoclinika.mssharing.services.billing.persistence.enity.storage;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Herman Zamula
 */
@Entity
@Table(name = "billing_analyzable_by_hour")
public class HourlyAnalyzableStorageUsage extends AnalyzableStorageUsage {

    public HourlyAnalyzableStorageUsage(long lab, long user, long file1, long bytes, Date timestamp, String ownerName,
                                        String instrument, long price, String file) {
        super(lab, user, file1, bytes, timestamp, ownerName, instrument, price, file);
    }

    public HourlyAnalyzableStorageUsage(long lab, long user, Long file, long bytes, long timestamp, String ownerName,
                                        String instrument, long price, String fileName, int hours, long balance) {
        super(lab, user, file, bytes, timestamp, ownerName, instrument, price, fileName, hours, balance);
    }

    public HourlyAnalyzableStorageUsage() {
    }
}
