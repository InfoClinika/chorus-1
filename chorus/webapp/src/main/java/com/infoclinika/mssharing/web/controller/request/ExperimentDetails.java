package com.infoclinika.mssharing.web.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.infoclinika.mssharing.model.helper.ExperimentLabelsInfo;
import com.infoclinika.mssharing.model.helper.LockMzItem;
import com.infoclinika.mssharing.model.write.*;
import com.infoclinika.mssharing.platform.model.read.AccessLevel;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;

import java.util.Collections;
import java.util.List;

/**
 * @author Pavel Kaplin
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperimentDetails {
    public Long id;
    public Long lab;
    public Long labHead;
    public Long billLab;
    public ExperimentInfo info;
    public long project;
    public List<FileItem> files;
    public List<ExperimentManagementTemplate.MetaFactorTemplate> factors = Collections.emptyList();
    public boolean is2dLc;
    public StudyManagement.Restriction restriction;
    public long type;
    public String ownerEmail;
    public AnalysisBounds bounds;
    public List<LockMzItem> lockMasses;
    public AccessLevel accessLevel;
    public ExperimentLabelsInfo experimentLabels;
    public int mixedSamplesCount;
    public String labName;
    public int channelsCount;
    public String labelType;
    public String groupSpecificParametersType;
    public double reporterMassTol;
    public boolean filterByPIFEnabled;
    public double minReporterPIF;
    public double minBasePeakRatio;
    public double minReporterFraction;
    public NgsRelatedExperimentInfo ngsRelatedInfo;

    public ExperimentDetails() {
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("id", id)
            .add("lab", lab)
            .add("labHead", labHead)
            .add("billLab", billLab)
            .add("info", info)
            .add("project", project)
            .add("files", files)
            .add("factors", factors)
            .add("is2dLc", is2dLc)
            .add("restriction", restriction)
            .add("type", type)
            .add("ownerEmail", ownerEmail)
            .add("bounds", bounds)
            .add("lockMasses", lockMasses)
            .add("accessLevel", accessLevel)
            .add("experimentLabels", experimentLabels)
            .add("mixedSamplesCount", mixedSamplesCount)
            .add("labName", labName)
            .add("channelsCount", channelsCount)
            .add("labelType", labelType)
            .add("groupSpecificParametersType", groupSpecificParametersType)
            .add("reporterMassTol", reporterMassTol)
            .add("filterByPIFEnabled", filterByPIFEnabled)
            .add("minReporterPIF", minReporterPIF)
            .add("minBasePeakRatio", minBasePeakRatio)
            .add("minReporterFraction", minReporterFraction)
            .add("ngsRelatedInfo", ngsRelatedInfo)
            .toString();
    }
}
