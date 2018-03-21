package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.infoclinika.mssharing.model.internal.repository.LabPaymentAccountRepository;
import com.infoclinika.mssharing.model.read.BillingInfoReader;
import com.infoclinika.mssharing.model.write.billing.BillingManagement;
import com.infoclinika.mssharing.services.billing.persistence.enity.ArchiveStorageVolumeUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.ProcessingUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.StorageVolumeUsage;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.persistence.repository.*;
import com.infoclinika.mssharing.services.billing.persistence.write.PaymentManagement;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import com.infoclinika.mssharing.services.billing.rest.api.model.LabAccountFeatureInfo;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.*;
import java.util.*;

/**
 * @author : Alexander Serebriyan
 */
@Component
public class StorageAndProcessingFeaturesUsageAnalyser {
    private static final Logger LOGGER = Logger.getLogger(StorageAndProcessingFeaturesUsageAnalyser.class);

    @Inject
    private PaymentManagement paymentManagement;
    @Inject
    private LabPaymentAccountRepository labPaymentAccountRepository;
    @Inject
    private ProcessingUsageRepository processingUsageRepository;
    @Inject
    private StorageVolumeUsageRepository storageVolumeUsageRepository;
    @Inject
    private ArchiveStorageVolumeUsageRepository archiveStorageVolumeUsageRepository;
    @Inject
    private ChargeableItemUsageReader chargeableItemUsageReader;
    @Inject
    private BillingManagement billingManagement;
    @Inject
    private PaymentCalculationsHelper paymentCalculationsHelper;
    private TimeZone timeZone;
    @Value("${billing.server.timezone}")
    private String timeZoneId;

    @Inject
    private BillingInfoReader billingInfoReader;

    @PostConstruct
    private void init() {
        timeZone = TimeZone.getTimeZone(ZoneId.of(timeZoneId));
    }

    public void analyseStorageVolumeUsage(long currentTime) {
        LOGGER.info("analyseStorageVolumeUsage");
        labPaymentAccountRepository.findAll().forEach(account -> {

            final Long labId = account.getLab().getId();
            final StorageVolumeUsage lastUsage = storageVolumeUsageRepository.findLast(labId);

            if (lastUsage != null) {

                final ZonedDateTime now = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime), timeZone.toZoneId());
                final ZonedDateTime lastUsageTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastUsage.getTimestamp()), timeZone.toZoneId());

                if (oneMonthPassed(lastUsageTime, now)) {

                    final ZonedDateTime lastUsageTimePlusMonth = lastUsageTime.plusMonths(1);
                    final long maximumStorageUsage = paymentCalculationsHelper.calculateMaximumStorageUsage(
                            labId,
                            new Date(lastUsageTime.toInstant().toEpochMilli()),
                            new Date(lastUsageTimePlusMonth.toInstant().toEpochMilli())
                    );
                    final int storageVolumes = paymentCalculationsHelper.calculateStorageVolumes(maximumStorageUsage);

                    paymentManagement.logStorageVolumeUsage(
                            lastUsage.getUser(),
                            lastUsage.getLab(),
                            storageVolumes,
                            lastUsageTimePlusMonth.toInstant().toEpochMilli()
                    );
                }
            }
        });
    }

    public void analyseProcessingUsage(long currentTime) {
        LOGGER.info("analyseProcessingUsage");
        labPaymentAccountRepository.findAll().forEach(account -> {

            final Long labId = account.getLab().getId();
            final ProcessingUsage lastUsage = processingUsageRepository.findLast(labId);

            if (lastUsage != null) {

                final ZonedDateTime now = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime), timeZone.toZoneId());
                final ZonedDateTime lastUsageTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastUsage.getTimestamp()), timeZone.toZoneId());

                if (oneMonthPassed(lastUsageTime, now)) {

                    if (!autoprolongateFeature(labId, BillingFeature.PROCESSING)) {
                        billingManagement.disableProcessingForLabAccount(lastUsage.getUser(), labId);
                    } else {
                        paymentManagement.logProcessingUsage(
                                lastUsage.getUser(),
                                lastUsage.getLab(),
                                lastUsageTime.plusMonths(1).toInstant().toEpochMilli()
                        );
                    }
                }
            }
        });
    }

    public void analyseArchiveStorageVolumeUsage(long currentTime) {
        LOGGER.info("archiveStorageVolumeUsage");
        labPaymentAccountRepository.findAll().forEach(account -> {

            final Long labId = account.getLab().getId();
            final ArchiveStorageVolumeUsage lastUsage = archiveStorageVolumeUsageRepository.findLast(labId);

            if (lastUsage != null) {

                final ZonedDateTime now = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime), timeZone.toZoneId());
                final ZonedDateTime lastUsageTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastUsage.getTimestamp()), timeZone.toZoneId());

                if (oneMonthPassed(lastUsageTime, now)) {

                    final ZonedDateTime lastUsageTimePlusMonth = lastUsageTime.plusMonths(1);
                    final long maximumStorageUsage = paymentCalculationsHelper.calculateMaximumArchiveStorageUsage(
                            labId,
                            new Date(lastUsageTime.toInstant().toEpochMilli()),
                            new Date(lastUsageTimePlusMonth.toInstant().toEpochMilli())
                    );
                    final int storageVolumes = paymentCalculationsHelper.calculateArchiveStorageVolumes(maximumStorageUsage);

                    paymentManagement.logArchiveStorageVolumeUsage(
                            lastUsage.getUser(),
                            lastUsage.getLab(),
                            storageVolumes,
                            lastUsageTimePlusMonth.toInstant().toEpochMilli()
                    );
                }
            }
        });
    }

    private boolean oneMonthPassed(ZonedDateTime from, ZonedDateTime to) {
        return !from.plusMonths(1).isAfter(to);
    }

    private boolean autoprolongateFeature(long lab, BillingFeature billingFeature) {
        final Set<LabAccountFeatureInfo> labAccountFeatureInfos = billingInfoReader.readLabAccountFeatures(lab);
        final Optional<LabAccountFeatureInfo> storageVolumesFeature = labAccountFeatureInfos
                .stream()
                .filter(feature -> feature.name.equals(billingFeature.name()))
                .findFirst();
        return storageVolumesFeature.isPresent() && storageVolumesFeature.get().autoProlongate;
    }
}
