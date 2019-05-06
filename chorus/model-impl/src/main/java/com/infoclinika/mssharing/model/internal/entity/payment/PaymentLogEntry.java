package com.infoclinika.mssharing.model.internal.entity.payment;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Elena Kurilina
 */
@MappedSuperclass
public abstract class PaymentLogEntry extends Log {

    @Basic(optional = false)
    private long amount;
    @OneToOne(optional = true)
    @JoinColumn(name = "transaction_details")
    public TransactionDetails transactionDetails;
    @Column(name = "transaction_id")
    private String transactionId;

    protected PaymentLogEntry() {
    }

    protected PaymentLogEntry(
        long amount,
        Long lab,
        Date timestamp,
        long totalToPay,
        long storeBalance,
        String transactionId
    ) {
        super(lab, timestamp, totalToPay, storeBalance);
        this.amount = amount;
        this.transactionId = transactionId;
    }

    public long getAmount() {
        return amount;
    }

}
