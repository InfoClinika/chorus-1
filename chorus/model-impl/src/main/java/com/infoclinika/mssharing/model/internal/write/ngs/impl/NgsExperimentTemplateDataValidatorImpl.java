package com.infoclinika.mssharing.model.internal.write.ngs.impl;

import com.infoclinika.mssharing.model.internal.write.ngs.api.NgsExperimentTemplateDataValidator;
import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentTemplateData;
import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentTemplateSampleData;
import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentTemplateValidationResult;
import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentTemplateValidationResult.ValidationError;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

import static com.infoclinika.mssharing.model.internal.write.ngs.impl.ExperimentTemplateSampleColumn.*;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author timofei.kasianov 8/3/18
 */
@Component
public class NgsExperimentTemplateDataValidatorImpl implements NgsExperimentTemplateDataValidator {
    private static final String MANDATORY_FIELD_NOT_SPECIFIED_MESSAGE_PREFIX = "Mandatory field ";
    private static final String MANDATORY_FIELD_NOT_SPECIFIED_MESSAGE_SUFFIX = " is not specified";

    @Override
    public NgsExperimentTemplateValidationResult validate(NgsExperimentTemplateData experimentTemplateData) {

        final NgsExperimentTemplateValidationResult result = new NgsExperimentTemplateValidationResult();
        final List<NgsExperimentTemplateSampleData> samples = experimentTemplateData.getSamples();

        if (samples.isEmpty()) {
            result.getErrors().add(new ValidationError("Experiment template doesn't contain any samples."));
            return result;
        }

        IntStream.range(0, samples.size()).forEach(ind -> {

            final NgsExperimentTemplateSampleData sampleData = samples.get(ind);
            final int sampleOrder = ind + 1;
            final String sampleDataStranded = sampleData.getStranded();

            addMandatoryErrorIfEmpty(result, sampleOrder, sampleData.getVendor(), VENDOR);
            addMandatoryErrorIfEmpty(result, sampleOrder, sampleData.getVendorId(), VENDOR_ID);
            addMandatoryErrorIfEmpty(result, sampleOrder, sampleData.getVendorProjectName(), VENDOR_PROJECT_NAME);
            addMandatoryErrorIfEmpty(result, sampleOrder, sampleData.getCelgeneId(), CELGENE_ID);
            addMandatoryErrorIfEmpty(result, sampleOrder, sampleData.getDaProjectId(), DA_PROJECT_ID);
            addMandatoryErrorIfEmpty(
                result,
                sampleOrder,
                sampleData.getCelgeneProjectDescription(),
                CELGENE_PROJECT_DESC
            );
            addMandatoryErrorIfEmpty(result, sampleOrder, sampleData.getExperiment(), EXPERIMENT);
            addMandatoryErrorIfEmpty(result, sampleOrder, sampleData.getDisplayName(), DISPLAY_NAME);
            addMandatoryErrorIfEmpty(result, sampleOrder, sampleData.getDisplayNameShort(), DISPLAY_NAME_SHORT);
            addMandatoryErrorIfEmpty(result, sampleOrder, sampleData.getExperimentType(), EXPERIMENT_TYPE);
            addMandatoryErrorIfEmpty(result, sampleOrder, sampleData.getTechnology(), TECHNOLOGY);
            addMandatoryErrorIfEmpty(result, sampleOrder, sampleData.getLibraryPrep(), LIBRARY_PREP);
            addMandatoryErrorIfEmpty(result, sampleOrder, sampleData.getReferenceGenome(), REFERENCE_GENOME);
            addMandatoryErrorIfEmpty(result, sampleOrder, sampleDataStranded, STRANDED);
            addMandatoryErrorIfEmpty(result, sampleOrder, sampleData.getPairedEnd(), PAIRED_END);
            addMandatoryErrorIfEmpty(result, sampleOrder, sampleData.getFilename(), FILENAME);

        });

        if (hasMultipleSpecies(samples)) {
            result.getErrors().add(new ValidationError("Experiment samples contain multiple species."));
        }

        if (hasMultipleExperimentTypes(samples)) {
            result.getErrors().add(new ValidationError("Experiment samples contain multiple experiment types."));
        }

        if (hasMultipleLibararyPreps(samples)) {
            result.getErrors().add(new ValidationError("Experiment samples contain multiple library preps."));
        }

        if (hasMultipleXenograftValues(samples)) {
            result.getErrors().add(new ValidationError("Experiment samples contain multiple xenograft values."));
        }

        if (hasMultipleRnaSelectionValues(samples)) {
            result.getErrors().add(new ValidationError("Experiment samples contain multiple rna selection values."));
        }

        if (hasMultipleNtExtractionValues(samples)) {
            result.getErrors().add(new ValidationError("Experiment samples contain multiple nt extraction values."));
        }

        if (hasMultiplePairedEndValues(samples)) {
            result.getErrors().add(new ValidationError("Experiment samples contain multiple paired end values."));
        }

        if (hasFilenameDuplicates(samples)) {
            result.getErrors().add(new ValidationError("Experiment samples contain filename duplicates"));
        }

        return result;
    }

    private boolean hasMultipleNtExtractionValues(List<NgsExperimentTemplateSampleData> samples) {
        return samples.stream()
            .filter(l -> !isEmpty(l.getNtExtraction()))
            .map(l -> l.getNtExtraction().toLowerCase())
            .collect(toSet())
            .size() > 1;
    }

    private boolean hasMultipleRnaSelectionValues(List<NgsExperimentTemplateSampleData> samples) {
        return samples.stream()
            .filter(l -> !isEmpty(l.getRnaSelection()))
            .map(l -> l.getRnaSelection().toLowerCase())
            .collect(toSet())
            .size() > 1;
    }

    private boolean hasMultiplePairedEndValues(List<NgsExperimentTemplateSampleData> samples) {
        return samples.stream().map(s -> s.getPairedEnd() != null && s.getPairedEnd()).collect(toSet()).size() > 1;
    }

    private boolean hasMultipleXenograftValues(List<NgsExperimentTemplateSampleData> samples) {
        return samples.stream().map(s -> s.getXenograft() != null && s.getXenograft()).collect(toSet()).size() > 1;
    }

    private boolean hasMultipleSpecies(List<NgsExperimentTemplateSampleData> samples) {
        return samples.stream()
            .filter(l -> !isEmpty(l.getReferenceGenome()))
            .map(l -> l.getReferenceGenome().toLowerCase())
            .collect(toSet())
            .size() > 1;
    }

    private boolean hasMultipleExperimentTypes(List<NgsExperimentTemplateSampleData> samples) {
        return samples.stream()
            .filter(l -> !isEmpty(l.getExperimentType()))
            .map(l -> l.getExperimentType().toLowerCase())
            .collect(toSet())
            .size() > 1;
    }

    private boolean hasMultipleLibararyPreps(List<NgsExperimentTemplateSampleData> samples) {
        return samples.stream()
            .filter(l -> !isEmpty(l.getLibraryPrep()))
            .map(l -> l.getLibraryPrep().toLowerCase())
            .collect(toSet())
            .size() > 1;
    }

    private boolean hasFilenameDuplicates(List<NgsExperimentTemplateSampleData> samples) {
        return samples.stream().map(NgsExperimentTemplateSampleData::getFilename).collect(toSet()).size() !=
            samples.size();
    }

    private void addMandatoryErrorIfEmpty(
        NgsExperimentTemplateValidationResult result,
        int sampleOrder,
        String value,
        ExperimentTemplateSampleColumn column
    ) {
        if (isEmpty(value)) {
            addMandatoryColumnError(result, sampleOrder, column);
        }
    }

    private void addMandatoryErrorIfEmpty(
        NgsExperimentTemplateValidationResult result,
        int sampleOrder,
        Boolean value,
        ExperimentTemplateSampleColumn column
    ) {
        if (value == null) {
            addMandatoryColumnError(result, sampleOrder, column);
        }
    }

    private void addMandatoryColumnError(
        NgsExperimentTemplateValidationResult result,
        int sampleOrder,
        ExperimentTemplateSampleColumn column
    ) {
        final String errorMessage =
            MANDATORY_FIELD_NOT_SPECIFIED_MESSAGE_PREFIX
                + "\""
                + column.getName()
                + "\""
                + MANDATORY_FIELD_NOT_SPECIFIED_MESSAGE_SUFFIX;
        result.getErrors().add(new ValidationError(sampleOrder, errorMessage));
    }


}
