package com.infoclinika.mssharing.web.transform;

import com.infoclinika.mssharing.model.helper.ExperimentLabelsInfo;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentLabelsItem;
import com.infoclinika.mssharing.model.write.ExperimentInfo;
import com.infoclinika.mssharing.model.write.FileItem;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.web.controller.request.ExperimentDetails;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : Alexander Serebriyan
 */
public class ExperimentTransformer {

    public static final Function<ExperimentDetails, ExperimentInfo> TO_EXPERIMENT_INFO = experiment -> {
            return new ExperimentInfo.Builder().name(experiment.info.name).description(experiment.info.description)
                    .specie(experiment.info.specie)
                    .experimentType(experiment.info.experimentType)
                    .experimentLabels(experiment.experimentLabels)
                    .project(experiment.project).lab(experiment.lab).billLab(experiment.billLab).is2dLc(experiment.is2dLc)
                    .restriction(experiment.restriction).factors(experiment.factors).files(experiment.files).bounds(experiment.bounds)
                    .sampleTypesCount(experiment.mixedSamplesCount)
                    .channelsCount(experiment.channelsCount)
                    .labelType(experiment.labelType)
                    .groupSpecificParametersType(experiment.groupSpecificParametersType)
                    .reporterMassTol(experiment.reporterMassTol)
                    .filterByPIFEnabled(experiment.filterByPIFEnabled)
                    .minReporterPIF(experiment.minReporterPIF)
                    .minBasePeakRatio(experiment.minBasePeakRatio)
                    .minReporterFraction(experiment.minReporterFraction)
                    .lockMasses(experiment.lockMasses)
                    .ngsRelatedInfo(experiment.ngsRelatedInfo)
                    .build();
    };

    public static final Function<ExperimentItem, ExperimentDetails> TO_EXPERIMENT_DETAILS = experiment -> {
        final long id = experiment.id;
        ExperimentDetails result = new ExperimentDetails();
        result.info = new ExperimentInfo.Builder().name(experiment.name).description(experiment.description).specie(experiment.specie).experimentType(experiment.experimentType).build();
        result.restriction = new ExperimentManagementTemplate.Restriction(experiment.technologyType, experiment.instrumentVendorId, experiment.instrumentType, experiment.instrumentModel, experiment.instrument);
        result.project = experiment.project;
        result.is2dLc = experiment.is2dLc;
        result.ownerEmail = experiment.ownerEmail;
        result.lab = experiment.lab;
        result.labHead = experiment.labHead;
        result.accessLevel = experiment.accessLevel;
        result.billLab = experiment.billLab;

        result.factors = experiment.factors.stream()
                .map(input -> new ExperimentManagementTemplate.MetaFactorTemplate(input.name, input.units, input.isNumeric, id))
                .collect(Collectors.toList());

        result.files = experiment.files.stream()
                .map(input -> {
                    final com.infoclinika.mssharing.model.read.dto.details.FileItem fileItem = (com.infoclinika.mssharing.model.read.dto.details.FileItem) input;
                    return new FileItem(input.id, input.copy, fileItem.fractionNumber, fileItem.preparedSample);
                })
                .collect(Collectors.toList());

        result.type = experiment.experimentType;
        result.id = id;
        result.bounds = experiment.bounds;
        result.lockMasses = experiment.lockMasses;
        result.labName = experiment.labName;

        final ExperimentLabelsItem labels = experiment.labels;
        result.experimentLabels = new ExperimentLabelsInfo(
                labels.lightLabels,
                labels.mediumLabels,
                labels.heavyLabels,
                labels.specialLabels
        );

        result.mixedSamplesCount = experiment.sampleTypesCount;
        result.channelsCount = experiment.channelsCount;
        result.labelType = experiment.labelType;
        result.groupSpecificParametersType = experiment.groupSpecificParametersType;
        result.reporterMassTol = experiment.reporterMassTol;
        result.filterByPIFEnabled = experiment.filterByPIFEnabled;
        result.minReporterPIF = experiment.minReporterPIF;
        result.minBasePeakRatio = experiment.minBasePeakRatio;
        result.minReporterFraction = experiment.minReporterFraction;
        result.ngsRelatedInfo = experiment.ngsRelatedInfo;
        return result;
    };

    private ExperimentTransformer() {
    }

}
