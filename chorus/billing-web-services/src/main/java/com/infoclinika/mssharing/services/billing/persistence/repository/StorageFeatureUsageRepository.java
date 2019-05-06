package com.infoclinika.mssharing.services.billing.persistence.repository;

import com.infoclinika.mssharing.services.billing.persistence.enity.ChargeableItemUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.dto.GroupedStorageUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author Herman Zamula
 */
public interface StorageFeatureUsageRepository<T extends ChargeableItemUsage> extends FeatureUsageRepository<T> {

    @Modifying
    @Transactional("billingLoggingTransactionManager")
    void deleteLogsForFilesOfDay(Iterable<Long> files, long dayToLog);

    @Transactional(readOnly = true)
    Page<GroupedStorageUsage<T>> groupedNotCompressedUsagesByFile(long lab, long dayToLog, Pageable pageable);

    /**
     * Use to retrieve days where by some reasons logs compressing wasn't executed
     *
     * @param daySinceEpoch day since epoch search to (exclusive)
     * @return dates where logs were missed except last 2 days (today and yesterday)
     */
    @Transactional(readOnly = true)
    List<Date> datesWhereSumLogsWereMissed(long daySinceEpoch);

    @Modifying(clearAutomatically = true)
    @Transactional("billingLoggingTransactionManager")
    @Query("DELETE FROM #{#entityName} s WHERE s.timestamp<:date ")
    void deleteLogsBefore(@Param("date") long date);


}
