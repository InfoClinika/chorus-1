package com.infoclinika.mssharing.model.internal.entity.restorable;

import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.write.AnalysisBounds;
import com.infoclinika.mssharing.model.write.ExperimentCategory;
import com.infoclinika.mssharing.platform.entity.ExperimentType;
import com.infoclinika.mssharing.platform.entity.Species;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentData;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.InstrumentRestriction;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Elena Kurilina, Stanislav Kurilin
 */
@Entity(name = "Experiment")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AbstractExperiment extends ExperimentTemplate<User, Lab, AbstractProject, Instrument, Factor, RawFile> {

    private AnalysisBounds bounds;

    @ManyToOne(/*, optional = false*/)
    @JoinColumn(name = "bill_laboratory")
    private Lab billLaboratory;

    @ElementCollection
    @CollectionTable(name = "experiment_lock_masses", joinColumns = {@JoinColumn(name = "experiment_id")})
    private List<LockMz> lockMasses = newArrayList();

    @Basic(optional = false)
    private int sampleTypesCount;

    @Basic
    @Column(name = "channelsCount", nullable = false)
    private int channelsCount;

    @Basic
    private String labelType;

    @Basic
    private String groupSpecificParametersType = "Standard";

    @Basic
    @Column(name = "reporterMassTol", nullable = false)
    private double reporterMassTol;

    @Basic
    @Column(name = "filterByPIFEnabled", nullable = false)
    private boolean filterByPIFEnabled;

    @Basic
    @Column(name = "minReporterPIF", nullable = false)
    private double minReporterPIF;

    @Basic
    @Column(name = "minBasePeakRatio", nullable = false)
    private double minBasePeakRatio;

    @Basic
    @Column(name = "minReporterFraction", nullable = false)
    private double minReporterFraction;

    @OneToOne
    public AnnotationAttachment annotationAttachment;

    @Basic(optional = false)
    @Enumerated(value = EnumType.STRING)
    private ExperimentCategory experimentCategory;

    @Embedded
    private NgsRelatedData ngsRelatedData;

    public AbstractExperiment(User creator,
                              AbstractProject project,
                              Lab lab,
                              String name,
                              ExperimentData experiment,
                              Date creation,
                              InstrumentRestriction<Instrument> instrumentRestriction,
                              ExperimentType experimentType,
                              Species specie,
                              AnalysisBounds bounds,
                              List<LockMz> lockMasses,
                              int sampleTypesCount,
                              int channelsCount,
                              String labelType,
                              String groupSpecificParametersType,
                              double reporterMassTol,
                              boolean filterByPIFEnabled,
                              double minReporterPIF,
                              double minBasePeakRatio,
                              double minReporterFraction,
                              ExperimentCategory experimentCategory,
                              NgsRelatedData ngsRelatedData
    ) {
        setName(name);
        setLab(lab);
        setCreator(creator);
        setProject(project);
        setExperiment(experiment);
        setSpecie(specie);
        this.lastModification = creation;
        setInstrumentRestriction(instrumentRestriction);
        this.experimentType = experimentType;
        this.downloadToken = null;
        this.bounds = bounds;
        this.lockMasses.addAll(lockMasses);
        this.sampleTypesCount = sampleTypesCount;
        this.channelsCount = channelsCount;
        this.labelType = labelType;
        this.groupSpecificParametersType = groupSpecificParametersType;
        this.reporterMassTol = reporterMassTol;
        this.filterByPIFEnabled = filterByPIFEnabled;
        this.minReporterPIF = minReporterPIF;
        this.minBasePeakRatio = minBasePeakRatio;
        this.minReporterFraction = minReporterFraction;
        this.experimentCategory = experimentCategory;
        this.ngsRelatedData = ngsRelatedData;
    }

    public void setLockMasses(List<LockMz> lockMasses) {
        this.lockMasses = lockMasses;
    }

    public void setDescription(String description) {
        this.getExperiment().setDescription(description);
    }

    public AbstractExperiment() {
    }

    @Transient
    public void changeProject(ActiveProject newProject) {
        this.setProject(newProject);
    }

    public AnalysisBounds getBounds() {
        if (bounds == null) {
            bounds = new AnalysisBounds();
        }
        return bounds;
    }

    public void setBounds(AnalysisBounds bounds) {
        this.bounds = bounds;
    }

    public List<LockMz> getLockMasses() {
        return lockMasses;
    }

    public Lab getBillLaboratory() {
        return billLaboratory;
    }

    public void setBillLaboratory(Lab billLaboratory) {
        this.billLaboratory = billLaboratory;
    }

    public int getSampleTypesCount() {
        return sampleTypesCount;
    }

    public void setSampleTypesCount(int mixedSamplesCount) {
        this.sampleTypesCount = mixedSamplesCount;
    }

    public int getChannelsCount() {
        return channelsCount;
    }

    public void setChannelsCount(int channelsCount) {
        this.channelsCount = channelsCount;
    }

    public String getLabelType() {
        return labelType;
    }

    public void setLabelType(String labelType) {
        this.labelType = labelType;
    }

    public String getGroupSpecificParametersType() {
        return groupSpecificParametersType;
    }

    public void setGroupSpecificParametersType(String groupSpecificParametersType) {
        this.groupSpecificParametersType = groupSpecificParametersType;
    }

    public double getReporterMassTol() {
        return reporterMassTol;
    }

    public void setReporterMassTol(double reporterMassTol) {
        this.reporterMassTol = reporterMassTol;
    }

    public boolean isFilterByPIFEnabled() {
        return filterByPIFEnabled;
    }

    public void setFilterByPIFEnabled(boolean filterByPIFEnabled) {
        this.filterByPIFEnabled = filterByPIFEnabled;
    }

    public double getMinReporterPIF() {
        return minReporterPIF;
    }

    public void setMinReporterPIF(double minReporterPIF) {
        this.minReporterPIF = minReporterPIF;
    }

    public double getMinBasePeakRatio() {
        return minBasePeakRatio;
    }

    public void setMinBasePeakRatio(double minBasePeakRatio) {
        this.minBasePeakRatio = minBasePeakRatio;
    }

    public double getMinReporterFraction() {
        return minReporterFraction;
    }

    public void setMinReporterFraction(double minReporterFraction) {
        this.minReporterFraction = minReporterFraction;
    }

    public ExperimentCategory getExperimentCategory() {
        return experimentCategory;
    }

    public void setExperimentCategory(ExperimentCategory experimentCategory) {
        this.experimentCategory = experimentCategory;
    }

    public AnnotationAttachment getAnnotationAttachment() {
        return annotationAttachment;
    }

    public void setAnnotationAttachment(AnnotationAttachment annotationAttachment) {
        this.annotationAttachment = annotationAttachment;
    }

    public NgsRelatedData getNgsRelatedData() {
        return ngsRelatedData;
    }

    public void setNgsRelatedData(NgsRelatedData ngsRelatedData) {
        this.ngsRelatedData = ngsRelatedData;
    }

    @Override
    public String toString() {
        return "AbstractExperiment{" +
                "bounds=" + bounds +
                ", billLaboratory=" + billLaboratory +
                ", lockMasses=" + lockMasses +
                ", sampleTypesCount=" + sampleTypesCount +
                ", channelsCount=" + channelsCount +
                ", labelType='" + labelType + '\'' +
                ", groupSpecificParametersType='" + groupSpecificParametersType + '\'' +
                ", reporterMassTol=" + reporterMassTol +
                ", filterByPIFEnabled=" + filterByPIFEnabled +
                ", minReporterPIF=" + minReporterPIF +
                ", minBasePeakRatio=" + minBasePeakRatio +
                ", minReporterFraction=" + minReporterFraction +
                ", annotationAttachment=" + annotationAttachment +
                ", experimentCategory=" + experimentCategory +
                ", ngsRelatedData=" + ngsRelatedData +
                "} " + super.toString();
    }
}
