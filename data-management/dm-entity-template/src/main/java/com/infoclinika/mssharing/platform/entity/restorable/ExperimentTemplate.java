package com.infoclinika.mssharing.platform.entity.restorable;

import com.infoclinika.mssharing.platform.entity.*;
import org.hibernate.annotations.Index;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Herman Zamula
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class ExperimentTemplate<U extends UserTemplate<L>,
    L extends LabTemplate<U>,
    P extends ProjectTemplate<U, L, ?, P>,
    I extends InstrumentTemplate<U, L>,
    F extends FactorTemplate<?, ?>,
    EF extends ExperimentFileTemplate<?, ?, ?>> extends AbstractRestorable {

    @Embedded
    public RawFiles<F, EF> rawFiles = new RawFiles<>();
    @OneToMany(cascade = {CascadeType.REFRESH, CascadeType.DETACH, CascadeType.PERSIST, CascadeType.MERGE})
    public List<Attachment<U>> attachments = newArrayList();
    protected String downloadToken;
    @ManyToOne(optional = false)
    protected ExperimentType experimentType;
    @Index(name = "EXPERIMENT_NAME_IDX")
    private String name;
    @ManyToOne
    private U creator;
    @ManyToOne
    private L lab;
    @ManyToOne
    private P project;
    @ManyToOne
    private Species specie;
    @Embedded
    private ExperimentData experiment;
    @Basic
    private boolean failed = false;

    private InstrumentRestriction<I> instrumentRestriction;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public U getCreator() {
        return creator;
    }

    public void setCreator(U creator) {
        this.creator = creator;
    }

    @Nullable
    public L getLab() {
        return lab;
    }

    public void setLab(@Nullable L lab) {
        this.lab = lab;
    }

    public P getProject() {
        return project;
    }

    public void setProject(P project) {
        this.project = project;
    }

    public Species getSpecie() {
        return specie;
    }

    public void setSpecie(Species specie) {
        this.specie = specie;
    }

    public ExperimentData getExperiment() {
        return experiment;
    }

    public void setExperiment(ExperimentData experiment) {
        this.experiment = experiment;
    }

    public InstrumentRestriction<I> getInstrumentRestriction() {
        return instrumentRestriction;
    }

    public void setInstrumentRestriction(InstrumentRestriction<I> instrumentRestriction) {
        this.instrumentRestriction = instrumentRestriction;
    }

    public RawFiles<F, EF> getRawFiles() {
        return rawFiles;
    }

    public void setRawFiles(RawFiles<F, EF> rawFiles) {
        this.rawFiles = rawFiles;
    }

    public String getDownloadToken() {
        return downloadToken;
    }

    public void setDownloadToken(String downloadToken) {
        this.downloadToken = downloadToken;
    }

    public int getNumberOfFiles() {
        return rawFiles.numberOfFiles();
    }

    public ExperimentType getExperimentType() {
        return experimentType;
    }

    public void setExperimentType(ExperimentType experimentType) {
        this.experimentType = experimentType;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }
}
