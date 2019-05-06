package com.infoclinika.mssharing.services.billing.persistence.read.strategy;

import com.infoclinika.mssharing.services.billing.persistence.repository.DownloadUsageRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem.Feature.DOWNLOAD;

/**
 * @author Herman Zamula
 */
@Component
public class DownloadLogFeature extends AbstractCommonFeatureUsage {

    @Inject
    public DownloadLogFeature(DownloadUsageRepository usageRepository) {
        super(DOWNLOAD, usageRepository);
    }

}
