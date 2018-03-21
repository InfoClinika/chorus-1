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
    private TranslationUsageRepository translationUsageRepository;
    @Inject
    private MonthlySummaryRepository monthlySummaryRepository;
    @Inject
    private DownloadUsageRepository downloadUsageRepository;
    @Inject
    private DailySummaryRepository dailySummaryRepository;
    @Inject
    private ProteinIDSearchUsageRepository proteinIDSearchUsageRepository;
    @Inject
    private PublicDownloadUsageRepository publicDownloadUsageRepository;

    public List<CrudRepository> get() {
        return ImmutableList.<CrudRepository>of(
                usageRepository,
                hourlyRepository,
                dailyArchiveStorageUsageRepository,
                hourlyArchiveStorageUsageRepository,
                translationUsageRepository,
                downloadUsageRepository,
                proteinIDSearchUsageRepository,
                publicDownloadUsageRepository,
                monthlySummaryRepository,
                dailySummaryRepository);
    }
}
