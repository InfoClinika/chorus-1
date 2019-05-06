package com.infoclinika.mssharing.model.write;

import java.util.Date;
import java.util.List;

public interface StorageUsageStatisticsService {

    /**
     * Returns storage usage statistics per laboratory within the wanted period.
     * It reads the first day of the month from the application.properties file and composes date range to search by.
     * For example, if the first day of the month is 6th, wanted month is September and wanted year is 2019
     * then composed date range will be next: from 6th September 2019 to 6th October 2019.
     * In case if wanted month is December, the date range will look like this:
     * from 6th December 2019 to 6th January 2020
     *
     * @param deadline - the end of the period to search by
     * @return - List of StorageUsageStatisticsDTO containing storage usage statistics per laboratory within wanted
     *           period. Returns empty list if there are no records within wanted range.
     */
    List<StorageUsageStatisticsDTO> findByDeadline(Date deadline);

    /**
     * Returns storage usage statistics per laboratory within the whole period.
     *
     * @return List of StorageUsageStatisticsDTO containing storage usage statistics per laboratory within the whole
     *         period. Returns an empty list if there are no records.
     */
    List<StatisticsSearchDTO> findAllStatisticsSearches();

    /**
     * Aggregates storage usage statistics and saves it into a DB specifying date range.
     *
     * @param from - date to specify the start of the range to aggregate statistics.
     * @param to   - date to specify the end of the range to aggregate statistics.
     * @return - List of StorageUsageStatisticsDTO
     */
    List<StorageUsageStatisticsDTO> aggregateStatisticsAndSave(Date from, Date to);

    /**
     * Aggregates storage usage statistics in automatic mode. Date range is next: from 1 January 2010
     * to now.
     */
    void aggregateStatisticsMonthly();

    class StorageUsageStatisticsDTO {
        public long labId;
        public String labName;
        public int rawFilesCount;
        public long rawFilesSize;
        public int otherFilesCount;
        public long otherFilesSize;
        public long totalFilesSize;

        public StorageUsageStatisticsDTO(long labId,
                                         String labName,
                                         int rawFilesCount,
                                         long rawFilesSize,
                                         int otherFilesCount,
                                         long otherFilesSize,
                                         long totalFilesSize) {
            this.labId = labId;
            this.labName = labName;
            this.rawFilesCount = rawFilesCount;
            this.rawFilesSize = rawFilesSize;
            this.otherFilesCount = otherFilesCount;
            this.otherFilesSize = otherFilesSize;
            this.totalFilesSize = totalFilesSize;
        }
    }

    class StatisticsSearchDTO {
        public String deadline;

        public StatisticsSearchDTO(String deadline) {
            this.deadline = deadline;
        }
    }
}
