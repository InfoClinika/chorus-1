package com.infoclinika.mssharing.helper;

import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.services.billing.persistence.repository.*;
import org.springframework.data.repository.CrudRepository;

import javax.inject.Inject;
import java.util.List;

/**
 * @author andrii.loboda
 */
public class BillingRepositories {
    @Inject
    private DailyAnalyseStorageUsageRepository usageRepository;
    @Inject
    private HourlyAnalyseStorageUsageRepository hourlyRepository;
    @Inject
    private DailyArchiveStorageUsageRepository dailyArchiveStorageUsageRepository;
    @Inject
    private HourlyArchiveStorageUsageRepository hourlyArchiveStorageUsageRepository;
    @Inject
    private MonthlySummaryRepository monthlySummaryRepository;
    @Inject
    private DownloadUsageRepository downloadUsageRepository;
    @Inject
    private DailySummaryRepository dailySummaryRepository;
    @Inject
    private PublicDownloadUsageRepository publicDownloadUsageRepository;

    public List<CrudRepository> get() {
        return ImmutableList.of(
            usageRepository,
            hourlyRepository,
            dailyArchiveStorageUsageRepository,
            hourlyArchiveStorageUsageRepository,
            downloadUsageRepository,
            publicDownloadUsageRepository,
            monthlySummaryRepository,
            dailySummaryRepository
        );
    }
}
