package com.infoclinika.mssharing.model.internal.repository;

/**
 * @author Elena Kurilina
 */
public class FeatureUsageByUser {
    public final long totalAmount;
    private final long user;
    private final String userName;
    private final long filesCount;

    public FeatureUsageByUser(Number totalAmount, long user, String userName, Number filesCount) {
        this.totalAmount = totalAmount.longValue();
        this.filesCount = filesCount.longValue();
        this.user = user;
        this.userName = userName;
    }


    public long getUser() {
        return user;
    }

    public String getUserName() {
        return userName;
    }

    public long getFilesCount() {
        return filesCount;
    }
}
