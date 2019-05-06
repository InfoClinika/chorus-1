package com.infoclinika.mssharing.services.billing.persistence.enity.storage;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Herman Zamula
 */
@Entity
@Table(name = "billing_archived_by_day")
public class DailyArchiveStorageUsage extends ArchiveStorageUsage {

    public DailyArchiveStorageUsage(long lab, long user, long file1, long bytes, Date timestamp, String ownerName,
                                    String instrument, long price, String file) {
        super(lab, user, file1, bytes, timestamp, ownerName, instrument, price, file);
    }

    public DailyArchiveStorageUsage() {
    }
}
