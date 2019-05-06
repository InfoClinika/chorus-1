package com.infoclinika.mssharing.model.internal.helper.billing;

import com.infoclinika.mssharing.model.internal.entity.payment.BillingProperty;
import com.infoclinika.mssharing.model.internal.entity.payment.BillingProperty.BillingPropertyName;
import com.infoclinika.mssharing.model.internal.repository.BillingPropertyRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static com.infoclinika.mssharing.model.internal.entity.payment.BillingProperty.BillingPropertyName.*;

/**
 * @author : Alexander Serebriyan
 */
@Service("databaseBillingPropertiesProvider")
public class DatabaseBillingPropertiesProvider {

    @Inject
    private BillingPropertyRepository billingPropertyRepository;

    public Long getFreeAccountStorageLimit() {
        return Long.valueOf(getPropValue(FREE_ACCOUNT_STORAGE_LIMIT));
    }

    public Long getEnterpriseAccountVolumeSize() {
        return Long.valueOf(getPropValue(ENTERPRISE_ACCOUNT_STORAGE_VOLUME_SIZE));
    }

    public Long getEnterpriseAccountVolumeCost() {
        return Long.valueOf(getPropValue(ENTERPRISE_ACCOUNT_STORAGE_VOLUME_COST));
    }

    public Long getFreeAccountArchiveStorageLimit() {
        return Long.valueOf(getPropValue(FREE_ACCOUNT_ARCHIVE_STORAGE_LIMIT));
    }

    public Long getEnterpriseAccountArchiveVolumeSize() {
        return Long.valueOf(getPropValue(ENTERPRISE_ACCOUNT_ARCHIVE_STORAGE_VOLUME_SIZE));
    }

    public Long getEnterpriseAccountArchiveVolumeCost() {
        return Long.valueOf(getPropValue(ENTERPRISE_ACCOUNT_ARCHIVE_STORAGE_VOLUME_COST));
    }

    public Long getProcessingFeatureCost() {
        return Long.valueOf(getPropValue(PROCESSING_FEATURE_COST));
    }

    public String getPropValue(BillingPropertyName propertyName) {
        final BillingProperty property = billingPropertyRepository.findByName(propertyName);
        return property.getValue();
    }
}
