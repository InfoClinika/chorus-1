package com.infoclinika.mssharing.services.billing.persistence.enity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Elena Kurilina
 */
@Entity
@Table(name = "billing_translation_usage")
public class TranslationUsage extends ChargeableItemUsage {

    public TranslationUsage() {
    }

    public TranslationUsage(long lab, long user, long file1, long bytes, Date timestamp, String ownerName,
                            String instrument, long price, String file) {
        super(lab, user, file1, bytes, timestamp, ownerName, instrument, price, file);
    }

    public TranslationUsage(long lab, long user, Long file, long bytes, long timestamp, String ownerName, String instrument, long price, String fileName, long balance) {
        super(lab, user, file, bytes, new Date(timestamp), ownerName, instrument, price, fileName);
        super.setBalance(balance);
    }
}
