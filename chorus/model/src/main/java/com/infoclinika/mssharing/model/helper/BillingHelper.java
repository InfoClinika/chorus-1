package com.infoclinika.mssharing.model.helper;

import com.google.common.collect.ImmutableSortedSet;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public interface BillingHelper {

    ImmutableSortedSet<BillingFeatureItem> billingFeatures();
}
