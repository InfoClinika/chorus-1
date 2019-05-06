package com.infoclinika.mssharing.model.internal.entity;

import com.infoclinika.mssharing.platform.entity.AbstractPersistable;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "storage_usage_statistics")
public class StorageUsageStatistics extends AbstractPersistable {

    @ManyToOne(optional = false)
    @JoinColumn(name = "lab_id")
    @Fetch(FetchMode.JOIN)
    private Lab lab;

    @Column(name = "from_date")
    private Date from;

    @Column(name = "to_date")
    private Date to;

    @Column(name = "raw_files_count")
    private int rawFilesCount;

    @Column(name = "raw_files_size")
    private long rawFilesSize;

    @Column(name = "other_files_count")
    private int otherFilesCount;

    @Column(name = "other_files_size")
    private long otherFilesSize;

    @Column(name = "total_files_size")
    private long totalFilesSize;

    public StorageUsageStatistics() {
    }

    public StorageUsageStatistics(Lab lab,
                                  Date from,
                                  Date to,
                                  int rawFilesCount,
                                  long rawFilesSize,
                                  int otherFilesCount,
                                  long otherFilesSize,
                                  long totalFilesSize) {
        this.lab = lab;
        this.from = from;
        this.to = to;
        this.rawFilesCount = rawFilesCount;
        this.rawFilesSize = rawFilesSize;
        this.otherFilesCount = otherFilesCount;
        this.otherFilesSize = otherFilesSize;
        this.totalFilesSize = totalFilesSize;
    }

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public int getRawFilesCount() {
        return rawFilesCount;
    }

    public void setRawFilesCount(int rawFilesCount) {
        this.rawFilesCount = rawFilesCount;
    }

    public long getRawFilesSize() {
        return rawFilesSize;
    }

    public void setRawFilesSize(long rawFilesSize) {
        this.rawFilesSize = rawFilesSize;
    }

    public int getOtherFilesCount() {
        return otherFilesCount;
    }

    public void setOtherFilesCount(int otherFilesCount) {
        this.otherFilesCount = otherFilesCount;
    }

    public long getOtherFilesSize() {
        return otherFilesSize;
    }

    public void setOtherFilesSize(long otherFilesSize) {
        this.otherFilesSize = otherFilesSize;
    }

    public long getTotalFilesSize() {
        return totalFilesSize;
    }

    public void setTotalFilesSize(long totalFilesSize) {
        this.totalFilesSize = totalFilesSize;
    }


    @Override
    public String toString() {
        return "StorageUsageStatistics{" +
            "lab=" + lab +
            ", from=" + from +
            ", to=" + to +
            ", rawFilesCount=" + rawFilesCount +
            ", rawFilesSize=" + rawFilesSize +
            ", otherFilesCount=" + otherFilesCount +
            ", otherFilesSize=" + otherFilesSize +
            ", totalFilesSize=" + totalFilesSize +
            '}';
    }
}
