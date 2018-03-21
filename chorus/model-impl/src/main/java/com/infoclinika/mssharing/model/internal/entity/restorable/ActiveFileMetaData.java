package com.infoclinika.mssharing.model.internal.entity.restorable;

import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.platform.entity.Species;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
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

    public ActiveFileMetaData() {
    }

    public ActiveFileMetaData(User owner, String name, Date uploadDate, Instrument instrument, long sizeInBytes, String labels, Species specie, boolean archive) {
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
}
