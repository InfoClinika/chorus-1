package com.infoclinika.mssharing.model.internal.entity.payment;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Herman Zamula
 */
@Embeddable
public class AccountBillingData {

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "account")
    private Set<AccountChargeableItemData> featuresData = new HashSet<>();

    public AccountBillingData(Set<AccountChargeableItemData> features) {
        this.featuresData.addAll(features);
    }

    protected AccountBillingData() {
    }

    public Set<AccountChargeableItemData> getFeaturesData() {
        return featuresData;
    }
}
