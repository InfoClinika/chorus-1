package com.infoclinika.mssharing.model.internal.entity.payment;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Herman Zamula
 */
@Entity
@Table(name = "billing_account_chargeable_feature")
public class AccountChargeableItemData extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "feature_id")
    private ChargeableItem chargeableItem;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private LabPaymentAccount account;

    @Column(name = "change_date")
    private Date changeDate = new Date();

    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "auto_prolongate", nullable = false)
    private boolean autoProlongate;


    public AccountChargeableItemData(boolean isActive, ChargeableItem chargeableItem, LabPaymentAccount account) {
        this.isActive = isActive;
        this.chargeableItem = chargeableItem;
        this.account = account;
    }

    public AccountChargeableItemData(
        ChargeableItem chargeableItem,
        LabPaymentAccount account,
        int quantity,
        boolean isActive,
        boolean autoProlongate
    ) {
        this.chargeableItem = chargeableItem;
        this.account = account;
        this.quantity = quantity;
        this.isActive = isActive;
        this.autoProlongate = autoProlongate;
    }

    protected AccountChargeableItemData() {
    }

    public ChargeableItem getChargeableItem() {
        return chargeableItem;
    }

    public void setChargeableItem(ChargeableItem feature) {
        this.chargeableItem = feature;
    }


    public static enum AccountFeatureState {
        BLOCKED, AVAILABLE
    }

    public LabPaymentAccount getAccount() {
        return account;
    }

    public void setAccount(LabPaymentAccount account) {
        this.account = account;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isAutoProlongate() {
        return autoProlongate;
    }

    public void setAutoProlongate(boolean autoProlongate) {
        this.autoProlongate = autoProlongate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountChargeableItemData)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        AccountChargeableItemData that = (AccountChargeableItemData) o;

        if (autoProlongate != that.autoProlongate) {
            return false;
        }
        if (isActive != that.isActive) {
            return false;
        }
        if (quantity != that.quantity) {
            return false;
        }
        if (account != null ? !account.equals(that.account) : that.account != null) {
            return false;
        }
        if (changeDate != null ? !changeDate.equals(that.changeDate) : that.changeDate != null) {
            return false;
        }
        if (chargeableItem != null ? !chargeableItem.equals(that.chargeableItem) : that.chargeableItem != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (chargeableItem != null ? chargeableItem.hashCode() : 0);
        result = 31 * result + (account != null ? account.hashCode() : 0);
        result = 31 * result + (changeDate != null ? changeDate.hashCode() : 0);
        result = 31 * result + quantity;
        result = 31 * result + (isActive ? 1 : 0);
        result = 31 * result + (autoProlongate ? 1 : 0);
        return result;
    }
}
