package com.infoclinika.mssharing.model.internal.helper.billing;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import com.infoclinika.mssharing.model.helper.BillingFeatureItem;
import com.infoclinika.mssharing.model.helper.BillingHelper;
import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.model.internal.repository.ChargeableItemRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Comparator;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.model.internal.read.Transformers.transformChargeType;
import static com.infoclinika.mssharing.model.internal.read.Transformers.transformFeature;

/**
 * @author Herman Zamula
 */
@Service
public class BillingHelperImpl implements BillingHelper {

    @Inject
    private ChargeableItemRepository chargeableItemRepository;


    @Override
    public ImmutableSortedSet<BillingFeatureItem> billingFeatures() {

        return from(chargeableItemRepository.findAll())
                .transform(new Function<ChargeableItem, BillingFeatureItem>() {
                    @Override
                    public BillingFeatureItem apply(ChargeableItem input) {
                        final BillingFeature feature = transformFeature(input.getFeature());
                        return new BillingFeatureItem(input.getPrice(), feature, feature.getValue(), transformChargeType(input.getChargeType()), input.getChargeValue());
                    }
                })
                .toSortedSet(new Comparator<BillingFeatureItem>() {
                    @Override
                    public int compare(BillingFeatureItem o1, BillingFeatureItem o2) {
                        return o1.name.compareTo(o2.name);
                    }
                });
    }
}
