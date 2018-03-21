package com.infoclinika.mssharing.model.write;

import com.google.common.base.MoreObjects;
import com.infoclinika.mssharing.model.helper.ExperimentLabelsInfo;
import com.infoclinika.mssharing.model.helper.LockMzItem;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;

import java.util.List;

/**
 * @author Herman Zamula
 */
public class ExperimentInfo extends ExperimentManagementTemplate.ExperimentInfoTemplate<ExperimentManagementTemplate.MetaFactorTemplate, FileItem> {
    public final AnalysisBounds bounds;
    public final List<LockMzItem> lockMasses;
    public final Long billLab;
    public final ExperimentLabelsInfo experimentLabels;
    public final int sampleTypesCount;
    public final int channelsCount;
    public final String labelType;
    public final String groupSpecificParametersType;
    public final double reporterMassTol;
    public final boolean filterByPIFEnabled;
    public final double minReporterPIF;
    public final double minBasePeakRatio;
    public final double minReporterFraction;
    public final ExperimentCategory experimentCategory;
    public final NgsRelatedExperimentInfo ngsRelatedExperimentInfo;

    //For internal use. TODO: refactor json mapper
    /*package*/ ExperimentInfo(String name, String description, long experimentType, long specie) {
        super(null, name, description, 0, null, null, specie, false, null, experimentType);
        bounds = null;
        lockMasses = null;
        billLab = null;
        experimentLabels = null;
        sampleTypesCount = 0;
        channelsCount = 0;
        labelType = null;
        groupSpecificParametersType = "Standard";
        reporterMassTol = 0;
        filterByPIFEnabled = false;
        minReporterPIF = 0;
        minBasePeakRatio = 0;
        minReporterFraction = 0;
        experimentCategory = ExperimentCategory.PROTEOMICS;
        ngsRelatedExperimentInfo = null;
    }

    public ExperimentInfo(
            String name,
            String description,
            long experimentType,
            long specie,
            long project,
            Long lab,
            Long billLab,
            List<ExperimentManagementTemplate.MetaFactorTemplate> factors,
            List<FileItem> files,
            boolean is2dLc,
            StudyManagement.Restriction restriction,
            AnalysisBounds bounds,
            List<LockMzItem> lockMasses,
            ExperimentLabelsInfo experimentLabels,
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
            NgsRelatedExperimentInfo ngsRelatedExperimentInfo
    ) {

        super(lab, name, description, project, factors, files, specie, is2dLc, restriction, experimentType);
        this.billLab = billLab;
        this.bounds = bounds;
        this.lockMasses = lockMasses;
        this.experimentLabels = experimentLabels;
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
        this.ngsRelatedExperimentInfo = ngsRelatedExperimentInfo;
    }

    public static class Builder {
        private String name;
        private String description;
        private long experimentType;
        private long specie;
        private long project;
        private Long lab;
        private Long billLab;
        private List<ExperimentManagementTemplate.MetaFactorTemplate> factors;
        private List<FileItem> files;
        private boolean is2dLc;
        private StudyManagement.Restriction restriction;
        private AnalysisBounds bounds;
        private List<LockMzItem> lockMasses;
        private ExperimentLabelsInfo experimentLabels;
        private int sampleTypesCount;
        private int channelsCount;
        private String labelType;
        private String groupSpecificParametersType;
        private double reporterMassTol;
        private boolean filterByPIFEnabled;
        private double minReporterPIF;
        private double minBasePeakRatio;
        private double minReporterFraction;
        private ExperimentCategory experimentCategory = ExperimentCategory.PROTEOMICS;
        private NgsRelatedExperimentInfo ngsRelatedExperimentInfo;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder experimentType(long experimentType) {
            this.experimentType = experimentType;
            return this;
        }

        public Builder specie(long specie) {
            this.specie = specie;
            return this;
        }

        public Builder project(Long project) {
            this.project = project;
            return this;
        }

        public Builder lab(Long lab) {
            this.lab = lab;
            return this;
        }

        public Builder billLab(Long billLab) {
            this.billLab = billLab;
            return this;
        }

        public Builder factors(List<ExperimentManagementTemplate.MetaFactorTemplate> factors) {
            this.factors = factors;
            return this;
        }

        public Builder files(List<FileItem> files) {
            this.files = files;
            return this;
        }

        public Builder is2dLc(boolean is2dLc) {
            this.is2dLc = is2dLc;
            return this;
        }

        public Builder restriction(StudyManagement.Restriction restriction) {
            this.restriction = restriction;
            return this;
        }

        public Builder bounds(AnalysisBounds bounds) {
            this.bounds = bounds;
            return this;
        }

        public Builder lockMasses(List<LockMzItem> lockMasses) {
            this.lockMasses = lockMasses;
            return this;
        }

        public Builder experimentLabels(ExperimentLabelsInfo experimentLabels) {
            this.experimentLabels = experimentLabels;
            return this;
        }

        public Builder sampleTypesCount(int sampleTypesCount) {
            this.sampleTypesCount = sampleTypesCount;
            return this;
        }

        public Builder channelsCount(int channelsCount) {
            this.channelsCount = channelsCount;
            return this;
        }

        public Builder labelType(String labelType) {
            this.labelType = labelType;
            return this;
        }

        public Builder groupSpecificParametersType(String groupSpecificParametersType) {
            this.groupSpecificParametersType = groupSpecificParametersType;
            return this;
        }

        public Builder reporterMassTol(double reporterMassTol) {
            this.reporterMassTol = reporterMassTol;
            return this;
        }

        public Builder filterByPIFEnabled(boolean filterByPIFEnabled) {
            this.filterByPIFEnabled = filterByPIFEnabled;
            return this;
        }

        public Builder minReporterPIF(double minReporterPIF) {
            this.minReporterPIF = minReporterPIF;
            return this;
        }

        public Builder minBasePeakRatio(double minBasePeakRatio) {
            this.minBasePeakRatio = minBasePeakRatio;
            return this;
        }

        public Builder minReporterFraction(double minReporterFraction) {
            this.minReporterFraction = minReporterFraction;
            return this;
        }

        public Builder experimentCategory(ExperimentCategory experimentCategory) {
            this.experimentCategory = experimentCategory;
            return this;
        }

        public Builder ngsRelatedInfo(NgsRelatedExperimentInfo ngsRelatedExperimentInfo) {
            this.ngsRelatedExperimentInfo = ngsRelatedExperimentInfo;
            return this;
        }

        public ExperimentInfo build() {
            return new ExperimentInfo(
                    name,
                    description,
                    experimentType,
                    specie,
                    project,
                    lab,
                    billLab,
                    factors,
                    files,
                    is2dLc,
                    restriction,
                    bounds,
                    lockMasses,
                    experimentLabels,
                    sampleTypesCount,
                    channelsCount,
                    labelType,
                    groupSpecificParametersType,
                    reporterMassTol,
                    filterByPIFEnabled,
                    minReporterPIF,
                    minBasePeakRatio,
                    minReporterFraction,
                    experimentCategory,
                    ngsRelatedExperimentInfo
            );
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("bounds", bounds)
                .add("lockMasses", lockMasses)
                .add("billLab", billLab)
                .add("experimentLabels", experimentLabels)
                .add("sampleTypesCount", sampleTypesCount)
                .add("channelsCount", channelsCount)
                .add("labelType", labelType)
                .add("groupSpecificParametersType", groupSpecificParametersType)
                .add("reporterMassTol", reporterMassTol)
                .add("filterByPIFEnabled", filterByPIFEnabled)
                .add("minReporterPIF", minReporterPIF)
                .add("minBasePeakRatio", minBasePeakRatio)
                .add("minReporterFraction", minReporterFraction)
                .add("experimentCategory", experimentCategory)
                .add("ngsRelatedInfo", ngsRelatedExperimentInfo)
                .toString();
    }
}
