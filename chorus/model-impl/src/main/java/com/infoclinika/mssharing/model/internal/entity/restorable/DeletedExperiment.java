package com.infoclinika.mssharing.model.internal.entity.restorable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import java.util.Date;

/**
 * @author Elena Kurilina
 */
@Entity
public class DeletedExperiment extends AbstractExperiment {

    @Basic
    private Date deletionDate = new Date();

    protected DeletedExperiment() {
    }

    public DeletedExperiment(ActiveExperiment e) {
        super(
                e.getCreator(),
                e.getProject(),
                e.getLab(),
                e.getName(),
                e.getExperiment(),
                e.getLastModification(),
                e.getInstrumentRestriction(),
                e.getExperimentType(),
                e.getSpecie(),
                e.getBounds(),
                e.getLockMasses(),
                e.getSampleTypesCount(),
                e.getChannelsCount(),
                e.getLabelType(),
                e.getGroupSpecificParametersType(),
                e.getReporterMassTol(),
                e.isFilterByPIFEnabled(),
                e.getMinReporterPIF(),
                e.getMinBasePeakRatio(),
                e.getMinReporterFraction(),
                e.getExperimentCategory(),
                e.getNgsRelatedData()
        );
        this.setDownloadToken(e.getDownloadToken());
        this.setRawFiles(e.getRawFiles());
        this.setLastModification(new Date());
        this.attachments = e.attachments;
        this.deletionDate = new Date();
        setDeleted(true);
    }

    public Date getDeletionDate() {
        return deletionDate;
    }
}
