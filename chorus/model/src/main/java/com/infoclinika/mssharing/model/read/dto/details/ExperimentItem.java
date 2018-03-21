package com.infoclinika.mssharing.model.read.dto.details;

import com.infoclinika.mssharing.model.helper.LockMzItem;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.write.AnalysisBounds;
import com.infoclinika.mssharing.model.write.NgsRelatedExperimentInfo;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ExperimentItemTemplate;

import java.util.Date;
import java.util.List;

/**
 * @author Herman Zamula
 */
public class ExperimentItem extends DetailsReaderTemplate.ExperimentItemTemplate {
    public final Long billLab;
    public final AnalysisBounds bounds;
    public final List<LockMzItem> lockMasses;
    public final String msChartsLink;
    public final boolean is2dLc;
    public final ExperimentLabelsItem labels;
    public final int sampleTypesCount;
    public final List<String> samplesInOrder;
    public final String[][] factorValues;
    public final int channelsCount;
    public final String labelType;
    public final String groupSpecificParametersType;
    public final double reporterMassTol;
    public final boolean filterByPIFEnabled;
    public final double minReporterPIF;
    public final double minBasePeakRatio;
    public final double minReporterFraction;
    public final NgsRelatedExperimentInfo ngsRelatedInfo;

    public ExperimentItem(
            ExperimentItemTemplate other,
            Long billLab,
            AnalysisBounds bounds,
            List<LockMzItem> lockMasses,
            String msChartsLink,
            boolean is2dLc,
            ExperimentLabelsItem labels,
            int sampleTypesCount,
            List<String> samplesInOrder,
            String[][] factorValues,
            int channelsCount,
            String labelType,
            String groupSpecificParametersType,
            double reporterMassTol,
            boolean filterByPIFEnabled,
            double minReporterPIF,
            double minBasePeakRatio,
            double minReporterFraction,
            NgsRelatedExperimentInfo ngsRelatedInfo
    ) {
        super(other);
        this.billLab = billLab;
        this.bounds = bounds;
        this.lockMasses = lockMasses;
        this.msChartsLink = msChartsLink;
        this.is2dLc = is2dLc;
        this.labels = labels;
        this.sampleTypesCount = sampleTypesCount;
        this.samplesInOrder = samplesInOrder;
        this.factorValues = factorValues;
        this.channelsCount = channelsCount;
        this.labelType = labelType;
        this.groupSpecificParametersType = groupSpecificParametersType;
        this.reporterMassTol = reporterMassTol;
        this.filterByPIFEnabled = filterByPIFEnabled;
        this.minReporterPIF = minReporterPIF;
        this.minBasePeakRatio = minBasePeakRatio;
        this.minReporterFraction = minReporterFraction;
        this.ngsRelatedInfo = ngsRelatedInfo;
    }
}
