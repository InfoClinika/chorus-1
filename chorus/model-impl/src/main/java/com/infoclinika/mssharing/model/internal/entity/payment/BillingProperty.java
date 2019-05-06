package com.infoclinika.mssharing.model.internal.entity.payment;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

/**
 * @author : Alexander Serebriyan
 */
@Entity
@Table(name = "billing_property")
public class BillingProperty extends AbstractPersistable<Long> {

    @Column(name = "property", nullable = false)
    @Enumerated(EnumType.STRING)
    private BillingPropertyName name;

    @Column(name = "value", nullable = false)
    private String value;

    public BillingProperty() {
    }

    public BillingProperty(BillingPropertyName name, String value) {
        this.name = name;
        this.value = value;
    }

    public BillingPropertyName getName() {
        return name;
    }

    public void setName(BillingPropertyName name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public enum BillingPropertyName {
        FREE_ACCOUNT_STORAGE_LIMIT("Free account storage limit"),
        ENTERPRISE_ACCOUNT_STORAGE_VOLUME_SIZE("Enterprise account storage volume size"),
        ENTERPRISE_ACCOUNT_STORAGE_VOLUME_COST("Enterprise account storage volume cost"),
        FREE_ACCOUNT_ARCHIVE_STORAGE_LIMIT("Free account archive storage limit"),
        ENTERPRISE_ACCOUNT_ARCHIVE_STORAGE_VOLUME_COST("Enterprise account archive storage volume cost"),
        ENTERPRISE_ACCOUNT_ARCHIVE_STORAGE_VOLUME_SIZE("Enterprise account archive storage volume size"),
        PROCESSING_FEATURE_COST("Processing feature cost");

        private final String value;

        BillingPropertyName(String value) {
            this.value = value;
        }
    }
}
