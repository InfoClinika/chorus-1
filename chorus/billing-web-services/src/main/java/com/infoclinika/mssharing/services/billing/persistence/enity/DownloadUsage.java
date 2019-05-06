package com.infoclinika.mssharing.services.billing.persistence.enity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Herman Zamula
 */

@Entity
@Table(name = "billing_download_usage")
public class DownloadUsage extends ChargeableItemUsage {

    public DownloadUsage() {
    }

    public DownloadUsage(long lab, long user, long file1, long bytes, Date timestamp, String ownerName,
                         String instrument, long price, String file) {
        super(lab, user, file1, bytes, timestamp, ownerName, instrument, price, file);
    }

    public DownloadUsage(long lab, long user, long file1, long bytes, long timestamp, String ownerName,
                         String instrument, long price, String file, long balance) {
        super(lab, user, file1, bytes, new Date(timestamp), ownerName, instrument, price, file);
        super.setBalance(balance);
    }
}
