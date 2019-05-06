package com.infoclinika.mssharing.services.billing.persistence.write;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Elena Kurilina
 */
@Transactional
public interface PaymentManagement {

    void depositStoreCredit(Map<String, String> paramsMap);

    void depositStoreCredit(long admin, long lab, long amount);

    @Async
    void logDownloadUsage(long actor, long file, long lab);

    @Async
    void logPublicDownload(@Nullable Long actor, long file);

    @Async
    void logStorageVolumeUsage(long actor, long lab, int volumes, long time);

    @Async
    void logArchiveStorageVolumeUsage(long actor, long lab, int volumes, long time);

    @Async
    void logLabBecomeEnterprise(long actor, long lab, long time);

    @Async
    void logLabBecomeFree(long actor, long lab, long time);
}
