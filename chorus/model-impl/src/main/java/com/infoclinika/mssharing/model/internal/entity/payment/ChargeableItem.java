package com.infoclinika.mssharing.model.internal.entity.payment;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.EnumSet;

/**
 * @author Elena Kurilina, Herman Zamula
 */
@Entity
@Table(name = "billing_chargeable_item")
public class ChargeableItem extends AbstractPersistable<Long> {

    @Basic(optional = false)
    private int price;

    @Column(name = "charge_value")
    private int chargeValue;
    @Column(name = "charge_type")
    @Enumerated(EnumType.STRING)
    private ChargeType chargeType;

    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private Feature feature;

    public ChargeableItem() {
    }

    public ChargeableItem(int price, Feature feature, int chargeValue, ChargeType chargeType) {
        this.price = price;
        this.chargeValue = chargeValue;
        this.chargeType = chargeType;
        this.feature = feature;
    }

    public ChargeableItem(Long id) {
        super.setId(id);
    }

    public int getPrice() {
        return price;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public int getChargeValue() {
        return chargeValue;
    }

    public void setChargeValue(int chargeValue) {
        this.chargeValue = chargeValue;
    }

    public ChargeType getChargeType() {
        return chargeType;
    }

    public void setChargeType(ChargeType chargeType) {
        this.chargeType = chargeType;
    }

    public static enum Feature {
        ARCHIVE_STORAGE, ANALYSE_STORAGE, DOWNLOAD, ANALYSIS, PUBLIC_DOWNLOAD, PROCESSING, STORAGE_VOLUMES, ARCHIVE_STORAGE_VOLUMES;

        public static Feature[] getPerFileMembers() {
            return new Feature[] {ARCHIVE_STORAGE, ANALYSE_STORAGE, DOWNLOAD, ANALYSIS, PUBLIC_DOWNLOAD};
        }
    }

    public static enum ChargeType {
        GB,
    }
}
