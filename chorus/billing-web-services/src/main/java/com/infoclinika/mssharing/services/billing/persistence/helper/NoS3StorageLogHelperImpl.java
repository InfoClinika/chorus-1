package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.LabRepository;
import com.infoclinika.mssharing.services.billing.persistence.enity.dto.GroupedStorageUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.storage.*;
import com.infoclinika.mssharing.services.billing.persistence.repository.DailyAnalyseStorageUsageRepository;
import com.infoclinika.mssharing.services.billing.persistence.repository.DailyArchiveStorageUsageRepository;
import com.infoclinika.mssharing.services.billing.persistence.repository.HourlyAnalyseStorageUsageRepository;
import com.infoclinika.mssharing.services.billing.persistence.repository.HourlyArchiveStorageUsageRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author Elena Kurilina
 */

@Transactional
public class NoS3StorageLogHelperImpl implements StorageLogHelper {


    private static final Logger LOGGER = Logger.getLogger(NoS3StorageLogHelperImpl.class);
    @Inject
    private FileMetaDataRepository fileRepository;
    @Inject
    private PaymentCalculationsHelper paymentCalculations;
    @Inject
    private HourlyAnalyseStorageUsageRepository hourlyUsageRepository;
    @Inject
    private HourlyArchiveStorageUsageRepository hourlyArchiveStorageUsageRepository;
    @Inject
    private DailyAnalyseStorageUsageRepository dailyUsageRepository;
    @Inject
    private DailyArchiveStorageUsageRepository dailyArchiveStorageUsageRepository;
    @Inject
    private LabRepository labRepository;

    public NoS3StorageLogHelperImpl() {
    }

    @Override
    public void log(long timestamp) {
        Iterable<ActiveFileMetaData> fileMetaDatas = fileRepository.findAll();
        for (ActiveFileMetaData data : fileMetaDatas) {
            try {
                logFile(data, new Date(timestamp));
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    @Override
    @Transactional("billingLoggingTransactionManager")
    public void sumLogs(Date dayToLog) {
        sumLogsAnalyse(dayToLog);
        sumLogsArchive(dayToLog);
    }

    private void sumLogsAnalyse(Date dateToLog) {
        Iterable<Lab> labs = labRepository.findAll();
        for (Lab lab : labs) {
            final Page<GroupedStorageUsage<HourlyAnalyzableStorageUsage>> analyzableStorageUsages =
                    hourlyUsageRepository.groupedNotCompressedUsagesByFile(
                            lab.getId(),
                            paymentCalculations.calculationDaySinceEpoch(dateToLog),
                            new PageRequest(0, Integer.MAX_VALUE)
                    );
            for (GroupedStorageUsage<HourlyAnalyzableStorageUsage> storageUsage : analyzableStorageUsages) {
                final HourlyAnalyzableStorageUsage itemUsage = storageUsage.itemUsage;
                itemUsage.setHours(storageUsage.hours.intValue());
                itemUsage.setCharge(storageUsage.summedCharge);
                final int day = paymentCalculations.calculationDaySinceEpoch(itemUsage.getTimestampDate());
                itemUsage.setTranslatedCharge(storageUsage.summedTranslatedCharge);
                itemUsage.setDay(day);

                hourlyUsageRepository.deleteLogsForFilesOfDay(ImmutableSet.of(itemUsage.getFile()), day);

                final DailyAnalyzableStorageUsage entity = new DailyAnalyzableStorageUsage(
                        itemUsage.getLab(),
                        itemUsage.getUser(),
                        itemUsage.getFile(),
                        itemUsage.getBytes(),
                        itemUsage.getTimestamp(),
                        itemUsage.getUsedBy(),
                        itemUsage.getInstrument(),
                        itemUsage.getTotalPrice(),
                        itemUsage.getFileName(),
                        itemUsage.getHours(),
                        itemUsage.getBalance()
                );

                entity.setDay(day);
                entity.setCharge(storageUsage.summedCharge);
                entity.setTranslatedCharge(storageUsage.summedTranslatedCharge);
                entity.setCompressed(true);
                dailyUsageRepository.save(entity);
            }
        }
    }

    private void sumLogsArchive(Date dateToLog) {
        Iterable<Lab> labs = labRepository.findAll();
        for (Lab lab : labs) {
            final Page<GroupedStorageUsage<HourlyArchiveStorageUsage>> analyzableStorageUsages =
                    hourlyArchiveStorageUsageRepository.groupedNotCompressedUsagesByFile(
                            lab.getId(),
                            paymentCalculations.calculationDaySinceEpoch(dateToLog),
                            new PageRequest(0, Integer.MAX_VALUE)
                    );
            for (GroupedStorageUsage<HourlyArchiveStorageUsage> storageUsage : analyzableStorageUsages) {
                final HourlyArchiveStorageUsage itemUsage = storageUsage.itemUsage;
                itemUsage.setHours(storageUsage.hours.intValue());
                itemUsage.setCharge(storageUsage.summedCharge);
                final int day = paymentCalculations.calculationDaySinceEpoch(itemUsage.getTimestampDate());
                itemUsage.setDay(day);

                hourlyArchiveStorageUsageRepository.deleteLogsForFilesOfDay(ImmutableSet.of(itemUsage.getFile()), day);

                final DailyArchiveStorageUsage entity = new DailyArchiveStorageUsage(
                        itemUsage.getLab(),
                        itemUsage.getUser(),
                        itemUsage.getFile(),
                        itemUsage.getBytes(),
                        new Date(itemUsage.getTimestamp()),
                        itemUsage.getUsedBy(),
                        itemUsage.getInstrument(),
                        itemUsage.getCharge(),
                        itemUsage.getFileName()
                );

                entity.setDay(day);
                entity.setCharge(storageUsage.summedCharge);
                entity.setCompressed(true);
                dailyArchiveStorageUsageRepository.save(entity);
            }
        }
    }

    private void logFile(ActiveFileMetaData file, Date date) {

        final Long lab = file.getInstrument().getLab().getId();
        final long fileSize = file.getSizeInBytes();
        final Long ownerId = file.getOwner().getId();
        final Long fileId = file.getId();
        final String ownerFullName = file.getOwner().getFullName();
        final String instrumentName = file.getInstrument().getName();
        final String fileName = file.getName();

        if(StringUtils.isNotEmpty(file.getContentId())) {

            final HourlyAnalyzableStorageUsage entry = new HourlyAnalyzableStorageUsage(
                    lab,
                    ownerId,
                    fileId,
                    fileSize,
                    date,
                    ownerFullName, instrumentName,
                    0, fileName);
            entry.setDay(paymentCalculations.calculationDaySinceEpoch(date));
            hourlyUsageRepository.save(entry);

        } else if(StringUtils.isNotEmpty(file.getArchiveId())) {

            final HourlyArchiveStorageUsage entry = new HourlyArchiveStorageUsage(
                    lab,
                    ownerId,
                    fileId,
                    fileSize,
                    date,
                    ownerFullName, instrumentName,
                    0, fileName);
            entry.setDay(paymentCalculations.calculationDaySinceEpoch(date));
            hourlyArchiveStorageUsageRepository.save(entry);

        } else {
            throw new RuntimeException("File doesn't have content ID.");
        }
    }

}
