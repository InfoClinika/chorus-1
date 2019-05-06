package com.infoclinika.mssharing.model.internal.stats;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.infoclinika.mssharing.model.internal.cloud.CloudStorageClientsProvider;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.LabRepository;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Throwables.getStackTraceAsString;
import static java.util.Collections.synchronizedMap;

@Service
public class DefaultS3StatisticsAggregator implements StatisticsAggregator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultS3StatisticsAggregator.class);

    @Inject
    private FileMetaDataRepository fileMetaDataRepository;

    @Inject
    private AmazonPropertiesProvider amazonPropertiesProvider;

    @Inject
    private ChorusPropertiesProvider chorusPropertiesProvider;

    @Inject
    private CloudStorageClientsProvider cloudStorageClientsProvider;

    @Inject
    private LabRepository labRepository;

    @Override
    public Map<Long, LabDataStatisticsSummary> aggregateStatisticsPerLab(Date from, Date to) {
        final List<DateRange> dateRanges = splitOnSubrangesNoLongerThanYear(from, to);

        final Map<Long, LabDataStatisticsSummary> labDataStatisticsSummaryMap = synchronizedMap(new HashMap<>());
        for (DateRange dateRange : dateRanges) {
            final Map<Long, LabDataStatisticsSummary> summaryMap = aggregateStatisticsPerLab(dateRange);

            for (Long labId : summaryMap.keySet()) {
                final LabDataStatisticsSummary statisticsSummary = summaryMap.get(labId);

                if (labDataStatisticsSummaryMap.containsKey(labId)) {
                    final LabDataStatisticsSummary masterStatisticsSummary = labDataStatisticsSummaryMap.get(labId);

                    masterStatisticsSummary.summarize(statisticsSummary);
                } else {
                    labDataStatisticsSummaryMap.put(labId, statisticsSummary);
                }
            }
        }

        return labDataStatisticsSummaryMap;
    }

    private Map<Long, LabDataStatisticsSummary> aggregateStatisticsPerLab(DateRange dateRange) {
        if (dateRange == null) {
            throw new RuntimeException("DateRange must not be null");
        }

        final long startTime = System.currentTimeMillis();

        final Date from = dateRange.getFrom();
        final Date to = dateRange.getTo();

        final int threads = chorusPropertiesProvider.getAggregateStatisticsNumberOfThreads();
        final ExecutorService executorService = Executors.newFixedThreadPool(threads);
        final List<Lab> laboratories = labRepository.findAll();
        final Map<Long, LabDataStatistics> statsByLab = synchronizedMap(new HashMap<>());
        final LinkedList<Callable<Object>> callables = new LinkedList<>();

        for (Lab laboratory : laboratories) {
            final long labId = laboratory.getId();
            final String laboratoryName = laboratory.getName();

            LOGGER.info("Composing storage usage statistics search for lab {}", laboratoryName);

            final List<S3ObjectProjection> rawFiles = fileMetaDataRepository.getAllRawFilesPerLab(
                labId,
                from,
                to
            );

            if (rawFiles.isEmpty()) {
                LOGGER.info("No files found for the lab {}. Skipping it...", laboratoryName);
                continue;
            }

            statsByLab.put(labId, new LabDataStatistics(laboratoryName));

            callables.add(() -> {
                LOGGER.info("Doing search for lab {}", laboratoryName);
                final AmazonS3Client s3Client = (AmazonS3Client) cloudStorageClientsProvider.getAmazonS3Client();

                final LabDataStatistics labDataStats = statsByLab.get(labId);

                LOGGER.info(
                    "Calculating size of raw files {} for lab {} from {} to {}",
                    rawFiles.size(),
                    laboratoryName,
                    from,
                    to
                );
                for (S3ObjectProjection s3ObjectProjection : rawFiles) {
                    long size = calculateFolderSize(s3Client, s3ObjectProjection.getPath());
                    labDataStats.getFileIds().add(s3ObjectProjection.getId());
                    labDataStats.getTotalRawFileSizes().add(size);
                }

                return null;
            });
        }

        try {
            LOGGER.info("Calling all the jobs. Total jobs: {}", callables.size());
            executorService.invokeAll(callables);
        } catch (InterruptedException e) {
            LOGGER.error(getStackTraceAsString(e));
            throw new RuntimeException("Error while aggregating statistics", e);
        } finally {
            executorService.shutdownNow();
        }

        final Map<Long, LabDataStatisticsSummary> labDataStatisticsSummaryMap = new HashMap<>();
        for (Long labId : statsByLab.keySet()) {
            final LabDataStatistics labData = statsByLab.get(labId);

            long totalRawDataSize = 0;
            final List<Long> rawDataFileSizes = labData.getTotalRawFileSizes();
            for (Long rawDataFileSize : rawDataFileSizes) {
                totalRawDataSize += rawDataFileSize;
            }

            labDataStatisticsSummaryMap.put(labId, new LabDataStatisticsSummary(
                labId,
                labData.getLabName(),
                labData.getTotalRawFileSizes().size(),
                totalRawDataSize,
                labData.getOtherFilesSizes().size(),
                0
            ));
        }

        LOGGER.info("Statistics collected.");
        LOGGER.info("Time taken: " + ((double) (System.currentTimeMillis() - startTime) / (60 * 1000)) + " min.");

        return labDataStatisticsSummaryMap;
    }

    private List<DateRange> splitOnSubrangesNoLongerThanYear(Date from, Date to) {
        final Calendar fromCalIninitial = Calendar.getInstance();
        fromCalIninitial.setTime(from);
        fromCalIninitial.set(Calendar.HOUR, 0);
        fromCalIninitial.set(Calendar.MINUTE, 0);
        fromCalIninitial.set(Calendar.SECOND, 0);
        fromCalIninitial.set(Calendar.MILLISECOND, 0);

        final Calendar toCalInitial = Calendar.getInstance();
        toCalInitial.setTime(to);
        toCalInitial.set(Calendar.HOUR, 0);
        toCalInitial.set(Calendar.MINUTE, 0);
        toCalInitial.set(Calendar.SECOND, 0);
        toCalInitial.set(Calendar.MILLISECOND, 0);

        final int fromYear = fromCalIninitial.get(Calendar.YEAR);
        final int toYear = toCalInitial.get(Calendar.YEAR);

        final List<DateRange> subRanges = new ArrayList<>();
        if (toYear - fromYear > 1) {
            final Calendar fromCal = (Calendar) fromCalIninitial.clone();
            final Calendar toCal = (Calendar) fromCalIninitial.clone();

            for (int i = 0; i < toYear - fromYear - 1; i++) {
                toCal.add(Calendar.YEAR, 1);
                subRanges.add(new DateRange(fromCal.getTime(), toCal.getTime()));

                fromCal.add(Calendar.YEAR, 1);
            }

            subRanges.add(new DateRange(fromCal.getTime(), toCalInitial.getTime()));
        } else {
            subRanges.add(new DateRange(from, to));
        }

        return subRanges;
    }

    private long calculateFolderSize(AmazonS3Client s3Client, String rawPath) {
        if (rawPath.startsWith("\"")) {
            rawPath = rawPath.substring(1);
        }

        if (rawPath.endsWith("\"")) {
            rawPath = rawPath.substring(0, rawPath.length() - 1);
        }

        final ObjectListing objectListing = s3Client.listObjects(amazonPropertiesProvider.getActiveBucket(), rawPath);
        final List<S3ObjectSummary> summaries = objectListing.getObjectSummaries();

        long totalSize = 0;
        for (S3ObjectSummary summary : summaries) {
            totalSize += summary.getSize();
        }

        return totalSize;
    }
}
