package com.infoclinika.mssharing.services.billing.persistence.helper;

import com.infoclinika.mssharing.model.internal.repository.LabPaymentAccountRepository;
import com.infoclinika.mssharing.model.read.BillingInfoReader;
import com.infoclinika.mssharing.model.write.billing.BillingManagement;
import com.infoclinika.mssharing.propertiesprovider.BillingPropertiesProvider;
import com.infoclinika.mssharing.services.billing.persistence.enity.ArchiveStorageVolumeUsage;
import com.infoclinika.mssharing.services.billing.persistence.enity.StorageVolumeUsage;
import com.infoclinika.mssharing.services.billing.persistence.read.ChargeableItemUsageReader;
import com.infoclinika.mssharing.services.billing.persistence.repository.ArchiveStorageVolumeUsageRepository;
import com.infoclinika.mssharing.services.billing.persistence.repository.StorageVolumeUsageRepository;
import com.infoclinika.mssharing.services.billing.persistence.write.PaymentManagement;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import com.infoclinika.mssharing.services.billing.rest.api.model.LabAccountFeatureInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

/**
 * @author : Alexander Serebriyan
 */
@Component
public class StorageAndProcessingFeaturesUsageAnalyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageAndProcessingFeaturesUsageAnalyser.class);

    private TimeZone timeZone;

    @Inject
    private PaymentManagement paymentManagement;

    @Inject
    private LabPaymentAccountRepository labPaymentAccountRepository;

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

    @Inject
    private BillingInfoReader billingInfoReader;

    @Inject
    private BillingPropertiesProvider billingPropertiesProvider;

    @PostConstruct
    private void init() {
        timeZone = TimeZone.getTimeZone(ZoneId.of(billingPropertiesProvider.getTimeZoneId()));
    }

    public void analyseStorageVolumeUsage(long currentTime) {
        LOGGER.info("analyseStorageVolumeUsage");
        labPaymentAccountRepository.findAll().forEach(account -> {

            final Long labId = account.getLab().getId();
            final StorageVolumeUsage lastUsage = storageVolumeUsageRepository.findLast(labId);

            if (lastUsage != null) {

                final ZonedDateTime now =
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime), timeZone.toZoneId());
                final ZonedDateTime lastUsageTime =
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastUsage.getTimestamp()), timeZone.toZoneId());

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

    public void analyseArchiveStorageVolumeUsage(long currentTime) {
        LOGGER.info("archiveStorageVolumeUsage");
        labPaymentAccountRepository.findAll().forEach(account -> {

            final Long labId = account.getLab().getId();
            final ArchiveStorageVolumeUsage lastUsage = archiveStorageVolumeUsageRepository.findLast(labId);

            if (lastUsage != null) {

                final ZonedDateTime now =
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTime), timeZone.toZoneId());
                final ZonedDateTime lastUsageTime =
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastUsage.getTimestamp()), timeZone.toZoneId());

                if (oneMonthPassed(lastUsageTime, now)) {

                    final ZonedDateTime lastUsageTimePlusMonth = lastUsageTime.plusMonths(1);
                    final long maximumStorageUsage = paymentCalculationsHelper.calculateMaximumArchiveStorageUsage(
                        labId,
                        new Date(lastUsageTime.toInstant().toEpochMilli()),
                        new Date(lastUsageTimePlusMonth.toInstant().toEpochMilli())
                    );
                    final int storageVolumes =
                        paymentCalculationsHelper.calculateArchiveStorageVolumes(maximumStorageUsage);

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
