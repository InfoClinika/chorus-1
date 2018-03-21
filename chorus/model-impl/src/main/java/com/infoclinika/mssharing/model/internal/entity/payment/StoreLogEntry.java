package com.infoclinika.mssharing.model.internal.entity.payment;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Elena Kurilina
 */
@Entity
@Table(name = "billing_store_log_entry")
public class StoreLogEntry extends PaymentLogEntry {

    public enum Direction {IN, OUT}

    @Basic
    private Direction direction;

    public StoreLogEntry() {

    }

    public StoreLogEntry(long amount, Long user, Date timestamp, long totalToPay, Direction direction, long storeBalance, String transactionId) {
        super(amount, user, timestamp, totalToPay, storeBalance, transactionId);
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }


}
