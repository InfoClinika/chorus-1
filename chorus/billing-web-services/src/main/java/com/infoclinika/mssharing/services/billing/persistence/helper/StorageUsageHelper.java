package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.infoclinika.mssharing.services.billing.persistence.repository.DailyAnalyseStorageUsageRepository;
import com.infoclinika.mssharing.services.billing.persistence.repository.DailyArchiveStorageUsageRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author timofei.kasianov 5/23/16
 */
@Component
public class StorageUsageHelper {

    @Inject
    private DailyAnalyseStorageUsageRepository dailyAnalyseStorageUsageRepository;
    @Inject
    private DailyArchiveStorageUsageRepository dailyArchiveStorageUsageRepository;

    public long getRawFilesSize(long lab, Long daySinceEpoch) {
        if (daySinceEpoch != null) {
            return retrieveValue(dailyAnalyseStorageUsageRepository.getRawStorageUsageForDay(lab, daySinceEpoch));
        } else {
            final Long lastProcessedDaySinceEpoch =
                dailyAnalyseStorageUsageRepository.getLastProcessedDaySinceEpoch(lab);
            if (lastProcessedDaySinceEpoch == null) {
                return 0;
            }
            return retrieveValue(
                dailyAnalyseStorageUsageRepository.getRawStorageUsageForDay(lab, lastProcessedDaySinceEpoch)
            );
        }
    }

    public long getArchivedFilesSize(long lab, Long daySinceEpoch) {
        if (daySinceEpoch != null) {
            return retrieveValue(dailyArchiveStorageUsageRepository.getStorageUsageForDay(lab, daySinceEpoch));
        } else {
            final Long lastProcessedDaySinceEpoch =
                dailyArchiveStorageUsageRepository.getLastProcessedDaySinceEpoch(lab);
            if (lastProcessedDaySinceEpoch == null) {
                return 0;
            }
            return retrieveValue(
                dailyArchiveStorageUsageRepository.getStorageUsageForDay(lab, lastProcessedDaySinceEpoch)
            );
        }
    }

    public long getSearchResultsFilesSize(long lab) {
        return 0;
    }

    private long retrieveValue(Long value) {
        return value != null ? value : 0;
    }

}
