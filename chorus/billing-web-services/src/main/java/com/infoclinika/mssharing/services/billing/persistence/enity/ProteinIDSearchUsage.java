package com.infoclinika.mssharing.services.billing.persistence.enity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Herman Zamula
 */
@Entity
@Table(name = "billling_protein_id_search_usage")
public class ProteinIDSearchUsage extends ChargeableItemUsage {

    private String experimentName;
    private Long experiment;
    @Column(name = "files_count", columnDefinition = "int(4) default 0")
    private int filesCount;

    public ProteinIDSearchUsage() {
    }

    public ProteinIDSearchUsage(long lab, long user, long experiment, String experimentName, long bytes, Date timestamp, String ownerName, long price, int filesCount) {
        super(lab, user, null, bytes, timestamp, ownerName, null, price, null);
        this.experiment = experiment;
        this.experimentName = experimentName;
        this.filesCount = filesCount;
    }

    public ProteinIDSearchUsage(long lab, long user, long bytes, long timestamp, String ownerName, long price, String experimentName, Long experiment, long balance) {
        super(lab, user, null, bytes, new Date(timestamp), ownerName, null, price, null);
        this.experimentName = experimentName;
        this.experiment = experiment;
        super.setBalance(balance);
    }

    public Long getExperiment() {
        return experiment;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public int getFilesCount() {
        return filesCount;
    }
}
