package com.infoclinika.mssharing.model.write.dto;

/**
 * @author timofei.kasianov 8/16/18
 */
public class UploadLimitCheckResult {

    public boolean exceeded;
    public String message;

    private UploadLimitCheckResult(boolean exceeded, String message) {
        this.exceeded = exceeded;
        this.message = message;
    }

    public static UploadLimitCheckResult uploadAvailable() {
        return new UploadLimitCheckResult(false, null);
    }

    public static UploadLimitCheckResult uploadLimitExceeded(String message) {
        return new UploadLimitCheckResult(true, message);
    }
}
