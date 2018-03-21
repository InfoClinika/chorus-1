package com.infoclinika.mssharing.model.write.billing;

import com.infoclinika.mssharing.services.billing.rest.api.model.BillingChargeType;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;

import java.util.Date;

/**
 * @author andrii.loboda
 */
public interface BillingManagement {
    long createChargeableItem(int price, BillingFeature feature, int chargeValue, BillingChargeType type);
    void makeLabAccountEnterprise(long actor, long lab);
    void makeLabAccountFree(long actor, long lab);
    MakeAccountFreeCheckResult checkCanMakeAccountFree(long actor, long lab);

    void updateProcessingFeatureState(long actor, long lab, boolean processingIsActive, boolean prolongateAutomatically);

    void enableProcessingForLabAccount(long actor, long lab, boolean prolongateAutomatically);
    void disableProcessingForLabAccount(long actor, long lab);
    UploadLimitCheckResult checkUploadLimit(long actor, long lab);
    void updateLabAccountSubscriptionDetails(long actor, SubscriptionInfo subscriptionInfo);

    void topUpLabBalance(long admin, long lab, long amountCents);


    enum LabPaymentAccountType {

        FREE("Free"), ENTERPRISE("Enterprise");

        String value;

        LabPaymentAccountType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    enum LabPaymentAccountFeatureType {
        ARCHIVE_STORAGE, ANALYSE_STORAGE, TRANSLATION, DOWNLOAD, ANALYSIS, PUBLIC_DOWNLOAD, PROCESSING, STORAGE_VOLUMES
    }

    class MakeAccountFreeCheckResult {
        public boolean canChange;
        public long allowedAfterTimestamp;
        public long storageLimitExceededSize;
        public long archiveStorageLimitExceededSize;

        public MakeAccountFreeCheckResult(boolean canChange, long allowedAfterTimestamp, long storageLimitExceededSize, long archiveStorageLimitExceededSize) {
            this.canChange = canChange;
            this.allowedAfterTimestamp = allowedAfterTimestamp;
            this.storageLimitExceededSize = storageLimitExceededSize;
            this.archiveStorageLimitExceededSize = archiveStorageLimitExceededSize;
        }

        public MakeAccountFreeCheckResult() {
        }

        public static MakeAccountFreeCheckResult ok() {
            return new MakeAccountFreeCheckResult(true, new Date().getTime(), 0, 0);
        }
    }

    class UploadLimitCheckResult {

        public boolean isExceeded;
        public String message;

        public UploadLimitCheckResult() {
        }

        public UploadLimitCheckResult(boolean isExceeded, String message) {
            this.isExceeded = isExceeded;
            this.message = message;
        }
    }

    class SubscriptionInfo {

        public final long labId;
        public final int storageVolumesCount;
        public final boolean enableProcessing;
        public final boolean autoprolongateProcessing;
        public final LabPaymentAccountType accountType;

        public SubscriptionInfo(long labId, int storageVolumesCount, boolean enableProcessing, boolean autoprolongateProcessing, LabPaymentAccountType accountType) {
            this.labId = labId;
            this.storageVolumesCount = storageVolumesCount;
            this.enableProcessing = enableProcessing;
            this.autoprolongateProcessing = autoprolongateProcessing;
            this.accountType = accountType;
        }

        @Override
        public String toString() {
            return "SubscriptionInfo{" +
                    "labId=" + labId +
                    ", storageVolumesCount=" + storageVolumesCount +
                    ", enableProcessing=" + enableProcessing +
                    ", autoprolongateProcessing=" + autoprolongateProcessing +
                    ", accountType=" + accountType +
                    '}';
        }
    }
}
