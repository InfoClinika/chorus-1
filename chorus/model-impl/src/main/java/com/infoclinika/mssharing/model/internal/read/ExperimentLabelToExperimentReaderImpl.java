package com.infoclinika.mssharing.model.internal.read;

import com.infoclinika.mssharing.model.internal.entity.ExperimentLabelToExperiment;
import com.infoclinika.mssharing.model.internal.entity.ExperimentSampleType;
import com.infoclinika.mssharing.model.internal.repository.ExperimentLabelToExperimentRepository;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentLabelsItem;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

/**
 * @author andrii.loboda
 */
@Service
public class ExperimentLabelToExperimentReaderImpl implements ExperimentLabelToExperimentReader {
    @Inject
    private ExperimentLabelToExperimentRepository experimentLabelToExperimentRepository;

    @Override
    public ExperimentLabelsItem readLabels(long experiment) {
        final List<ExperimentLabelToExperiment> labels =
            experimentLabelToExperimentRepository.findLabelsById(experiment);
        final List<Long> lightLabels = newLinkedList();
        final List<Long> mediumLabels = newLinkedList();
        final List<Long> heavyLabels = newLinkedList();
        final List<Long> specialLabels = newLinkedList();

        for (ExperimentLabelToExperiment label : labels) {
            final ExperimentSampleType mixType = label.getExperimentLabelMixType();

            switch (mixType) {
                case LIGHT:
                    lightLabels.add(label.getExperimentLabel().getId());
                    break;
                case MEDIUM:
                    mediumLabels.add(label.getExperimentLabel().getId());
                    break;
                case HEAVY:
                    heavyLabels.add(label.getExperimentLabel().getId());
                    break;
                case SPECIAL:
                    specialLabels.add(label.getExperimentLabel().getId());
                    break;
                default:
                    throw new IllegalStateException(
                        "Undefined experiment labels type: " + mixType + " for experiment: " + experiment
                    );
            }
        }

        return new ExperimentLabelsItem(lightLabels, mediumLabels, heavyLabels, specialLabels);
    }

    @Override
    public ExperimentLabelsDetails readLabelsDetails(long experiment) {
        final List<ExperimentLabelToExperiment> labels =
            experimentLabelToExperimentRepository.findLabelsById(experiment);
        final List<ExperimentLabelItem> lightLabels = newLinkedList();
        final List<ExperimentLabelItem> mediumLabels = newLinkedList();
        final List<ExperimentLabelItem> heavyLabels = newLinkedList();
        final List<ExperimentLabelItem> specialLabels = newLinkedList();

        for (ExperimentLabelToExperiment label : labels) {
            final ExperimentSampleType mixType = label.getExperimentLabelMixType();

            switch (mixType) {
                case LIGHT:
                    lightLabels.add(asLabelItem(label));
                    break;
                case MEDIUM:
                    mediumLabels.add(asLabelItem(label));
                    break;
                case HEAVY:
                    heavyLabels.add(asLabelItem(label));
                    break;
                case SPECIAL:
                    specialLabels.add(asLabelItem(label));
                    break;
                default:
                    throw new IllegalStateException(
                        "Undefined experiment labels type: " + mixType + " for experiment: " + experiment
                    );
            }
        }

        return new ExperimentLabelsDetails(lightLabels, mediumLabels, heavyLabels, specialLabels);
    }

    private ExperimentLabelItem asLabelItem(ExperimentLabelToExperiment label) {
        return new ExperimentLabelItem(label.getExperimentLabel().getId(), label.getExperimentLabel().getName());
    }
}
