package com.infoclinika.mssharing.model.internal.entity.payment;

import javax.persistence.*;
import java.util.Set;

import static org.hibernate.validator.ap.util.CollectionHelper.newHashSet;

/**
 * @author Herman Zamula
 */
@Embeddable
public class AccountBillingData {

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "account")
    private Set<AccountChargeableItemData> featuresData = newHashSet();

    public AccountBillingData(Set<AccountChargeableItemData> features) {
        this.featuresData.addAll(features);
    }

    protected AccountBillingData() {
    }

    public Set<AccountChargeableItemData> getFeaturesData() {
        return featuresData;
    }
}
