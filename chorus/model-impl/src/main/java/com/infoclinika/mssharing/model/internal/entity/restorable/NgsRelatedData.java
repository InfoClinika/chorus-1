package com.infoclinika.mssharing.model.internal.entity.restorable;

import javax.persistence.*;
import java.util.Objects;
import java.util.Optional;

/**
 * @author : Alexander Serebriyan
 */
@Embeddable
public class NgsRelatedData {

    @Basic(optional = true)
    private Boolean multiplexing;

    @Basic(optional = true)
    private Integer pairedEnd;

    @OneToOne
    private NgsExperimentType ngsExperimentType;

    @OneToOne
    private ExperimentPrepMethod experimentPrepMethod;

    @OneToOne
    private NtExtractionMethod ntExtractionMethod;

    @Basic
    private Boolean xenograft;

    @Basic
    @Enumerated(value = EnumType.STRING)
    private LibraryPrep libraryPrep;

    public NgsRelatedData() {
    }

    public NgsRelatedData(Boolean multiplexing, NgsExperimentType ngsExperimentType, Integer pairedEnd,
                          ExperimentPrepMethod experimentPrepMethod, Boolean xenograft, LibraryPrep libraryPrep,
                          NtExtractionMethod ntExtractionMethod) {
        this.multiplexing = multiplexing;
        this.ngsExperimentType = ngsExperimentType;
        this.pairedEnd = pairedEnd;
        this.experimentPrepMethod = experimentPrepMethod;
        this.xenograft = xenograft;
        this.libraryPrep = libraryPrep;
        this.ntExtractionMethod = ntExtractionMethod;
    }

    public Boolean isMultiplexing() {
        return multiplexing;
    }

    public void setMultiplexing(Boolean multiplexing) {
        this.multiplexing = multiplexing;
    }

    public Integer getPairedEnd() {
        return pairedEnd;
    }

    public void setPairedEnd(Integer pairedEnd) {
        this.pairedEnd = pairedEnd;
    }

    public NgsExperimentType getNgsExperimentType() {
        return ngsExperimentType;
    }

    public void setNgsExperimentType(NgsExperimentType ngsExperimentType) {
        this.ngsExperimentType = ngsExperimentType;
    }

    public ExperimentPrepMethod getExperimentPrepMethod() {
        return experimentPrepMethod;
    }

    public void setExperimentPrepMethod(ExperimentPrepMethod experimentPrepMethod) {
        this.experimentPrepMethod = experimentPrepMethod;
    }

    public Boolean isXenograft() {
        return xenograft;
    }

    public void setXenograft(Boolean xenograft) {
        this.xenograft = xenograft;
    }

    public LibraryPrep getLibraryPrep() {
        return libraryPrep;
    }

    public void setLibraryPrep(LibraryPrep libraryPrep) {
        this.libraryPrep = libraryPrep;
    }

    public NtExtractionMethod getNtExtractionMethod() {
        return ntExtractionMethod;
    }

    public NgsRelatedData setNtExtractionMethod(NtExtractionMethod ntExtractionMethod) {
        this.ntExtractionMethod = ntExtractionMethod;
        return this;
    }

    @Transient
    public Integer getNgsExperimentTypeId() {
        return Optional.ofNullable(ngsExperimentType)
            .map(NgsExperimentType::getId)
            .orElse(null);
    }

    @Transient
    public String getNgsExperimentTypeName() {
        return Optional.ofNullable(ngsExperimentType)
            .map(NgsExperimentType::getTitle)
            .orElse(null);
    }

    @Transient
    public Integer getExperimentPrepMethodId() {
        return Optional.ofNullable(experimentPrepMethod)
            .map(ExperimentPrepMethod::getId)
            .orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NgsRelatedData that = (NgsRelatedData) o;
        return Objects.equals(multiplexing, that.multiplexing) &&
            Objects.equals(pairedEnd, that.pairedEnd) &&
            Objects.equals(ngsExperimentType, that.ngsExperimentType) &&
            Objects.equals(experimentPrepMethod, that.experimentPrepMethod) &&
            Objects.equals(ntExtractionMethod, that.ntExtractionMethod) &&
            Objects.equals(xenograft, that.xenograft) &&
            libraryPrep == that.libraryPrep;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            multiplexing,
            pairedEnd,
            ngsExperimentType,
            experimentPrepMethod,
            ntExtractionMethod,
            xenograft,
            libraryPrep
        );
    }

    @Override
    public String toString() {
        return "NgsRelatedData{" +
            "multiplexing=" + multiplexing +
            ", pairedEnd=" + pairedEnd +
            ", ngsExperimentType=" + ngsExperimentType +
            ", experimentPrepMethod=" + experimentPrepMethod +
            ", ntExtractionMethod=" + ntExtractionMethod +
            ", xenograft=" + xenograft +
            ", libraryPrep=" + libraryPrep +
            '}';
    }
}
