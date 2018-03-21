package com.infoclinika.mssharing.model.internal.entity.payment;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Pavel Kaplin
 */
@Entity
@Table(name = "billing_pay_pal_log_entry")
public class PayPalLogEntry extends PaymentLogEntry {

    public PayPalLogEntry() {
    }

    public PayPalLogEntry(long amount, Long lab, Date timestamp, long totalToPay, long storeBalance, String transactionId) {
        super(amount, lab, timestamp, totalToPay, storeBalance, transactionId);
    }


}
