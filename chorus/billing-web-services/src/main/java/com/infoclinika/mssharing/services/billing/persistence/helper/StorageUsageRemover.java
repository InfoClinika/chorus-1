package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.infoclinika.mssharing.services.billing.persistence.repository.MonthlySummaryRepository;
import com.infoclinika.mssharing.services.billing.persistence.repository.StorageFeatureUsageRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Herman Zamula
 */
@Component
public class StorageUsageRemover {

    private final TimeZone timeZone;
    @Inject
    private MonthlySummaryRepository monthlySummaryRepository;
    @Inject
    private Collection<StorageFeatureUsageRepository> usageRepositories;

    @Inject
    protected StorageUsageRemover(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public void removeTillDate(Date month) {
        //TODO:2015-11-23:herman.zamula: Validation. Check months to remove are present in monthly repository
        usageRepositories.stream().forEach(repo -> repo.deleteLogsBefore(month.getTime()));
    }


}
