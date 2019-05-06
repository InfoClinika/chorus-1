package com.infoclinika.mssharing.model.internal.entity.payment;

import com.infoclinika.mssharing.model.internal.entity.Lab;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Elena Kurilina
 */
@Entity
@Table(name = "billing_lab_payment_account")
public class LabPaymentAccount extends AbstractPersistable<Long> {

    public static final long DEFAULT_CREDIT_LIMIT = 10000;

    @OneToOne
    private Lab lab;
    @Column(name = "store_balance")
    private long storeBalance = 0;
    @Column(name = "calculation_date")
    private Date calculationDate = new Date();
    @Column(name = "pay_by_pay_pal")
    private long payByPayPal;
    @Column(name = "pay_by_store")
    private long payByStore;

    @Column(name = "scaled_to_pay_value")
    private long scaledToPayValue;

    @Column(name = "last_plan_update")
    private Date accountCreationDate;

    @Column(name = "credit_limit")
    private long creditLimit;

    @Embedded
    private AccountBillingData billingData;

    @Column(name = "type", nullable = false, columnDefinition = "varchar(255) default 'FREE'")
    @Enumerated(EnumType.STRING)
    private LabPaymentAccountType type = LabPaymentAccountType.FREE;

    @Column(name = "last_type_update")
    private Date lastTypeUpdateDate = new Date();

    public LabPaymentAccount() {
    }

    public LabPaymentAccount(Lab lab) {
        this.lab = lab;
    }

    public long getPayByPayPal() {
        return payByPayPal;
    }

    public void setPayByPayPal(long payByPayPal) {
        this.payByPayPal = payByPayPal;
    }

    public Date getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(Date calculationDate) {
        this.calculationDate = calculationDate;
    }

    public long getPayByStore() {
        return payByStore;
    }

    public void setPayByStore(long payByStore) {
        this.payByStore = payByStore;
    }

    public long getStoreBalance() {
        return storeBalance;
    }

    public void setStoreBalance(long storeBalance) {
        this.storeBalance = storeBalance;
    }

    public Lab getLab() {
        return lab;
    }

    public Date getAccountCreationDate() {
        return accountCreationDate;
    }

    public void setAccountCreationDate(Date lastPlanUpdate) {
        this.accountCreationDate = lastPlanUpdate;
    }

    public AccountBillingData getBillingData() {
        return billingData;
    }

    public void setBillingData(AccountBillingData billingPlanData) {
        this.billingData = billingPlanData;
    }

    public long getScaledToPayValue() {
        return scaledToPayValue;
    }

    public void setScaledToPayValue(long scaledToPayValue) {
        this.scaledToPayValue = scaledToPayValue;
    }

    public long getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(long creditLimit) {
        this.creditLimit = creditLimit;
    }

    public LabPaymentAccountType getType() {
        return type;
    }

    public void setType(LabPaymentAccountType type) {
        this.type = type;
    }

    public Date getLastTypeUpdateDate() {
        return lastTypeUpdateDate;
    }

    public void setLastTypeUpdateDate(Date lastTypeUpdateDate) {
        this.lastTypeUpdateDate = lastTypeUpdateDate;
    }

    @Transient
    public boolean isFree() {
        return getType().equals(LabPaymentAccountType.FREE);
    }

    @Transient
    public void addBalance(long amount) {
        storeBalance += amount;
    }

    public static enum LabPaymentAccountType {
        FREE("Free"), ENTERPRISE("Enterprise");

        String value;

        LabPaymentAccountType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}
