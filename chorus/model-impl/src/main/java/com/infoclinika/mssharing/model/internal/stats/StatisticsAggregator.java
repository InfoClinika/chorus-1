package com.infoclinika.mssharing.model.internal.stats;

import java.util.Date;
import java.util.Map;

public interface StatisticsAggregator {

    Map<Long, LabDataStatisticsSummary> aggregateStatisticsPerLab(Date from, Date to);
}
