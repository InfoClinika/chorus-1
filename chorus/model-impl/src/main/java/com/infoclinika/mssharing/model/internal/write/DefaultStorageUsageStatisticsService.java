package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.StorageUsageStatistics;
import com.infoclinika.mssharing.model.internal.repository.LabRepository;
import com.infoclinika.mssharing.model.internal.repository.StorageUsageStatisticsRepository;
import com.infoclinika.mssharing.model.internal.stats.LabDataStatisticsSummary;
import com.infoclinika.mssharing.model.internal.stats.StatisticsAggregator;
import com.infoclinika.mssharing.model.write.StorageUsageStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.LocalDate.of;
import static java.time.Month.JANUARY;

@Service
public class DefaultStorageUsageStatisticsService implements StorageUsageStatisticsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStorageUsageStatisticsService.class);

    private final StorageUsageStatisticsRepository storageUsageStatisticsRepository;
    private final LabRepository labRepository;
    private final StatisticsAggregator statisticsAggregator;

    @Inject
    public DefaultStorageUsageStatisticsService(StorageUsageStatisticsRepository storageUsageStatisticsRepository,
                                                LabRepository labRepository,
                                                StatisticsAggregator statisticsAggregator) {
        this.storageUsageStatisticsRepository = storageUsageStatisticsRepository;
        this.labRepository = labRepository;
        this.statisticsAggregator = statisticsAggregator;
    }

    @Override
    public List<StorageUsageStatisticsDTO> findByDeadline(Date deadline) {
        final LocalDate from = of(2010, JANUARY, 1);
        LOGGER.info("Searching for storage usage statistics for the date range from: {} to: {}", from, deadline);

        final List<StorageUsageStatistics> statisticsList = storageUsageStatisticsRepository.findByDeadline(
            deadline
        );

        LOGGER.info("Found {} records", statisticsList.size());

        return statisticsList
            .stream()
            .map(sus -> new StorageUsageStatisticsDTO(
                sus.getLab().getId(),
                sus.getLab().getName(),
                sus.getRawFilesCount(),
                sus.getRawFilesSize(),
                sus.getOtherFilesCount(),
                sus.getOtherFilesSize(),
                sus.getTotalFilesSize()
            ))
            .collect(Collectors.toList());
    }

    @Override
    public List<StatisticsSearchDTO> findAllStatisticsSearches() {
        final List<Date> statisticsSearches = storageUsageStatisticsRepository.findAllStatisticsSearches();
        final DateFormat dateFormat = new SimpleDateFormat();

        return statisticsSearches.stream()
            .map(d -> new StatisticsSearchDTO(dateFormat.format(d)))
            .collect(Collectors.toList());
    }

    @Override
    public List<StorageUsageStatisticsDTO> aggregateStatisticsAndSave(Date from, Date to) {
        LOGGER.info("Aggregating new statistics. Date range from: {} to: {}", from, to);
        final Map<Long, LabDataStatisticsSummary> statisticsSummaryMap = statisticsAggregator.aggregateStatisticsPerLab(
            from,
            to
        );

        final List<StorageUsageStatisticsDTO> records = getStorageUsageStatisticsDTOS(statisticsSummaryMap);

        LOGGER.info("Saving {} records into DB. Date range from: {} to: {}", records.size(), from, to);

        save(records, from, to);

        return records;
    }

    @Transactional
    public void save(List<StorageUsageStatisticsDTO> records, Date from, Date to) {
        try {
            for (StorageUsageStatisticsDTO record : records) {
                final Lab lab = labRepository.findOne(record.labId);
                storageUsageStatisticsRepository.save(
                    new StorageUsageStatistics(
                        lab,
                        from,
                        to,
                        record.rawFilesCount,
                        record.rawFilesSize,
                        record.otherFilesCount,
                        record.otherFilesSize,
                        record.totalFilesSize
                    )
                );
            }
        } catch (Exception e) {
            LOGGER.error("Error while saving storage usage statistics into DB", e);
            throw new RuntimeException(e);
        }
    }

    private List<StorageUsageStatisticsDTO> getStorageUsageStatisticsDTOS(
        Map<Long, LabDataStatisticsSummary> statisticsSummaryMap
    ) {
        final List<StorageUsageStatisticsDTO> records = new ArrayList<>();
        for (Long labId : statisticsSummaryMap.keySet()) {
            final LabDataStatisticsSummary statisticsSummary = statisticsSummaryMap.get(labId);

            records.add(new StorageUsageStatisticsDTO(
                statisticsSummary.getLabId(),
                statisticsSummary.getLabName(),
                statisticsSummary.getRawFilesCount(),
                statisticsSummary.getTotalRawDataSize(),
                statisticsSummary.getOtherFilesCount(),
                statisticsSummary.getOtherFilesSize(),
                statisticsSummary.getTotalRawDataSize()
                    + statisticsSummary.getOtherFilesSize()
            ));
        }
        return records;
    }

    @Override
    @Scheduled(cron = "${aggregate.storage.usage.statistics.cron.expression:0 0 1 6 * ?}")
    public void aggregateStatisticsMonthly() {
        final Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.set(2010, Calendar.JANUARY, 1);

        final Calendar toCalendar = Calendar.getInstance();
        toCalendar.set(Calendar.HOUR, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);

        final Date from = fromCalendar.getTime();
        final Date to = toCalendar.getTime();

        aggregateStatisticsAndSave(from, to);
    }
}
