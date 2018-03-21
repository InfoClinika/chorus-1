package com.infoclinika.mssharing.model.internal.entity.payment;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * @author Elena Kurilina
 */

@MappedSuperclass
public  abstract class Log extends AbstractPersistable<Long> {

    @Basic(optional = false)
    private Long lab;
    @Basic(optional = false)
    private Date timestamp;
    @Column(name = "total_to_pay")
    private long totalToPay;
    @Column(name = "store_balance")
    private long storeBalance;

    public Long getLab() {
        return lab;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public long getTotalToPay() {
        return totalToPay;
    }

    public long getStoreBalance() {
        return storeBalance;
    }

    protected Log() {
    }

    protected Log(Long lab, Date timestamp, long totalToPay, long storeBalance) {
        this.lab = lab;
        this.timestamp = timestamp;
        this.totalToPay = totalToPay;
        this.storeBalance = storeBalance;
    }
}
