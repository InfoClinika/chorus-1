package com.infoclinika.mssharing.model.helper;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Herman Zamula
 */
@Transactional
public interface BillingFeaturesHelper {

    boolean isFeatureEnabled(long lab, BillingFeature feature);

    ImmutableSet<BillingFeature> enabledBillingFeatures(long lab);
}
