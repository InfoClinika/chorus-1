package com.infoclinika.mssharing.model.internal.entity.payment;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Date;

/**
 * @author : Alexander Serebriyan
 */

@Entity
@Table(name = "billing_lab_account_feature_usage")
public class LabAccountFeatureUsage extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "account")
    private LabPaymentAccount account;

    @ManyToOne(optional = false)
    @JoinColumn(name = "feature_id")
    private ChargeableItem chargeableItem;

    @Column(name = "change_date")
    private Date changeDate = new Date();

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "quantity", nullable = false)
    private int quantity = 1;


    public LabAccountFeatureUsage() {
    }

    public LabPaymentAccount getAccount() {
        return account;
    }

    public void setAccount(LabPaymentAccount account) {
        this.account = account;
    }

    public ChargeableItem getChargeableItem() {
        return chargeableItem;
    }

    public void setChargeableItem(ChargeableItem chargeableItem) {
        this.chargeableItem = chargeableItem;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
