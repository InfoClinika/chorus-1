package com.infoclinika.mssharing.services.billing.persistence.read.strategy;

import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import com.infoclinika.mssharing.services.billing.persistence.repository.PublicDownloadUsageRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Herman Zamula
 */
@Component
public class PublicDownloadLogFeatureStrategy extends AbstractCommonFeatureUsage {

    @Inject
    public PublicDownloadLogFeatureStrategy(PublicDownloadUsageRepository featureUsageRepository) {
        super(ChargeableItem.Feature.PUBLIC_DOWNLOAD, featureUsageRepository);
    }
}
