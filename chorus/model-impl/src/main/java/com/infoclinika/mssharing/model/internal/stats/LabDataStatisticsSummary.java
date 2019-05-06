package com.infoclinika.mssharing.model.internal.stats;

public class LabDataStatisticsSummary {
    private long labId;
    private String labName;
    private int rawFilesCount;
    private long totalRawDataSize;
    private int otherFilesCount;
    private long otherFilesSize;

    public LabDataStatisticsSummary(long labId,
                                    String labName,
                                    int rawFilesCount,
                                    long totalRawDataSize,
                                    int otherFilesCount,
                                    long otherFilesSize) {
        this.labId = labId;
        this.labName = labName;
        this.rawFilesCount = rawFilesCount;
        this.totalRawDataSize = totalRawDataSize;
        this.otherFilesCount = otherFilesCount;
        this.otherFilesSize = otherFilesSize;
    }

    public void summarize(LabDataStatisticsSummary that) {
        this.rawFilesCount += that.rawFilesCount;
        this.totalRawDataSize += that.totalRawDataSize;
        this.otherFilesCount += that.otherFilesCount;
        this.otherFilesSize += that.otherFilesSize;
    }

    public long getLabId() {
        return labId;
    }

    public String getLabName() {
        return labName;
    }

    public int getRawFilesCount() {
        return rawFilesCount;
    }

    public long getTotalRawDataSize() {
        return totalRawDataSize;
    }

    public int getOtherFilesCount() {
        return otherFilesCount;
    }

    public long getOtherFilesSize() {
        return otherFilesSize;
    }

    @Override
    public String toString() {
        return "LabDataStatisticsSummary{" +
            "labId=" + labId +
            ", labName='" + labName + '\'' +
            ", rawFilesCount=" + rawFilesCount +
            ", totalRawDataSize=" + totalRawDataSize +
            ", otherFilesCount=" + otherFilesCount +
            ", otherFilesSize=" + otherFilesSize +
            '}';
    }
}
