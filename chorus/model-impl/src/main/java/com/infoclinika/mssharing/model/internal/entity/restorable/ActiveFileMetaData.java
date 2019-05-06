package com.infoclinika.mssharing.model.internal.entity.restorable;

import com.google.common.base.Objects;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.platform.entity.Species;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Stanislav Kurilin, Elena Kurilina
 */
@Entity
@DynamicUpdate
//@Indexed
@Table(name = "FileMetaData")
@SecondaryTable(name = "file_upload_bucket")
public class ActiveFileMetaData extends AbstractFileMetaData {

    @Column(table = "file_upload_bucket", name = "bucket")
    private String fileUploadBucket;

    @Column(name = "billing_last_charging_date")
    private Date chargingDate;

    @Column(name = "billing_last_sum_date")
    private Date lastChargingSumDate;

    @Column(name = "to_replace", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean toReplace = false;

    /**
     * File is indicated as `corrupted` in case of failed upload to S3 bucket.
     * File can't be marked as `corrupted` if it is not marked as `toReplace`.
     * `corrupted` flag doesn't affect logic to reload already existing files.
     * `corrupted` files can be reloaded or removed.
     */
    @Column(name = "corrupted", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean corrupted = false;


    public ActiveFileMetaData() {
    }

    public ActiveFileMetaData(
        User owner,
        String name,
        Date uploadDate,
        Instrument instrument,
        long sizeInBytes,
        String labels,
        Species specie,
        boolean archive
    ) {
        super(owner, name, uploadDate, instrument, sizeInBytes, labels, specie, archive);
    }

    public ActiveFileMetaData(Long id) {
        super(id);
    }


    public String getFileUploadBucket() {
        return fileUploadBucket;
    }

    public void setFileUploadBucket(String fileUploadBucket) {
        this.fileUploadBucket = fileUploadBucket;
    }

    public Date getLastChargingSumDate() {
        return lastChargingSumDate;
    }

    public void setLastChargingSumDate(Date lastChargingSumDate) {
        this.lastChargingSumDate = lastChargingSumDate;
    }

    public Date getChargingDate() {
        return chargingDate;
    }

    public void setChargingDate(Date chargingStart) {
        this.chargingDate = chargingStart;
    }

    public boolean isToReplace() {
        return toReplace;
    }

    public void setToReplace(boolean toReplace) {
        this.toReplace = toReplace;
    }

    public boolean isCorrupted() {
        return corrupted;
    }

    public void setCorrupted(boolean corrupted) {
        this.corrupted = corrupted;
    }

    @Override
    public String toString() {
        return "ActiveFileMetaData{" +
            "fileUploadBucket='" + fileUploadBucket + '\'' +
            ", chargingDate=" + chargingDate +
            ", lastChargingSumDate=" + lastChargingSumDate +
            ", toReplace=" + toReplace +
            ", corrupted=" + corrupted +
            "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final ActiveFileMetaData that = (ActiveFileMetaData) o;

        return isToReplace() == that.isToReplace() &&
            isCorrupted() == that.isCorrupted() &&
            Objects.equal(getFileUploadBucket(), that.getFileUploadBucket()) &&
            Objects.equal(getChargingDate(), that.getChargingDate()) &&
            Objects.equal(getLastChargingSumDate(), that.getLastChargingSumDate());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
            super.hashCode(),
            getFileUploadBucket(),
            getChargingDate(),
            getLastChargingSumDate(),
            isToReplace(),
            isCorrupted()
        );
    }
}

