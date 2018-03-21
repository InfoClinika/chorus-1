package com.infoclinika.mssharing.services.billing.persistence.enity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Herman Zamula
 */
@Entity
@Table(name = "billing_download_usage_public")
public class PublicDownloadUsage extends ChargeableItemUsage {

    @Column(name = "on_archive")
    private boolean onArchive;

    public PublicDownloadUsage(Long lab, Long user, Long file, long bytes,
                               Date timestamp, String ownerName, String instrument,
                               long charge, String fileName, boolean onArchive) {
        super(lab, user, file, bytes, timestamp, ownerName, instrument, charge, fileName);
        this.onArchive = onArchive;
    }

    public PublicDownloadUsage(long lab, Long user, long file1, long bytes, long timestamp, String ownerName, String instrument,
                         long price, String file, long balance) {
        super(lab, user, file1, bytes, new Date(timestamp), ownerName, instrument, price, file);
        super.setBalance(balance);
    }

    public PublicDownloadUsage() {
    }

    public boolean isOnArchive() {
        return onArchive;
    }

    public void setOnArchive(boolean onArchive) {
        this.onArchive = onArchive;
    }
}
