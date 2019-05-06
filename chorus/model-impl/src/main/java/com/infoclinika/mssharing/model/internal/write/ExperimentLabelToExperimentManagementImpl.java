package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.infoclinika.mssharing.model.helper.ExperimentLabelsInfo;
import com.infoclinika.mssharing.model.internal.entity.ExperimentLabel;
import com.infoclinika.mssharing.model.internal.entity.ExperimentLabelToExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.repository.ExperimentLabelRepository;
import com.infoclinika.mssharing.model.internal.repository.ExperimentLabelToExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static com.infoclinika.mssharing.model.internal.entity.ExperimentSampleType.*;

/**
 * @author andrii.loboda
 */
@Service
public class ExperimentLabelToExperimentManagementImpl implements ExperimentLabelToExperimentManagement {

    @Inject
    private ExperimentLabelToExperimentRepository experimentLabelToExperimentRepository;
    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private ExperimentLabelRepository experimentLabelRepository;

    public void persistExperimentLabels(long experiment, ExperimentLabelsInfo experimentLabels) {
        final List<ExperimentLabelToExperiment> experimentLabelsToPersist =
            createExperimentLabels(experiment, experimentLabels);
        experimentLabelToExperimentRepository.deleteAllByExperiment(experiment);
        experimentLabelToExperimentRepository.save(experimentLabelsToPersist);
    }

    private List<ExperimentLabelToExperiment> createExperimentLabels(
        long experimentId,
        ExperimentLabelsInfo experimentLabelItems
    ) {
        final ActiveExperiment experiment = experimentRepository.findOne(experimentId);
        final ImmutableSet<Long> labelsToBuild = ImmutableSet.<Long>builder()
            .addAll(experimentLabelItems.heavyLabels)
            .addAll(experimentLabelItems.mediumLabels)
            .addAll(experimentLabelItems.lightLabels)
            .addAll(experimentLabelItems.specialLabels)
            .build();

        final ImmutableMap<Long, ExperimentLabel> idToLabelMap =
            Maps.uniqueIndex(experimentLabelRepository.findAll(labelsToBuild), new Function<ExperimentLabel, Long>() {
                @Override
                public Long apply(ExperimentLabel label) {
                    return label.getId();
                }
            });

        final List<ExperimentLabelToExperiment> labels = newLinkedList();

        for (long heavyLabel : experimentLabelItems.heavyLabels) {
            labels.add(new ExperimentLabelToExperiment(experiment, idToLabelMap.get(heavyLabel), HEAVY));
        }
        for (long mediumLabel : experimentLabelItems.mediumLabels) {
            labels.add(new ExperimentLabelToExperiment(experiment, idToLabelMap.get(mediumLabel), MEDIUM));
        }
        for (long lightLabel : experimentLabelItems.lightLabels) {
            labels.add(new ExperimentLabelToExperiment(experiment, idToLabelMap.get(lightLabel), LIGHT));
        }
        for (long specialLabels : experimentLabelItems.specialLabels) {
            labels.add(new ExperimentLabelToExperiment(experiment, idToLabelMap.get(specialLabels), SPECIAL));
        }

        return labels;
    }

    @Override
    public void copyExperimentLabels(long experimentFrom, long experimentTo) {
        final ActiveExperiment exTo = experimentRepository.findOne(experimentTo);
        final List<ExperimentLabelToExperiment> labels =
            experimentLabelToExperimentRepository.findLabelsById(experimentFrom);
        final List<ExperimentLabelToExperiment> newLabels = newLinkedList();
        for (ExperimentLabelToExperiment label : labels) {
            newLabels.add(new ExperimentLabelToExperiment(
                exTo,
                label.getExperimentLabel(),
                label.getExperimentLabelMixType()
            ));
        }
        experimentLabelToExperimentRepository.save(labels);
    }

    @Override
    public void deleteExperimentLabels(long experiment) {
        final List<ExperimentLabelToExperiment> labels =
            experimentLabelToExperimentRepository.findLabelsById(experiment);
        for (ExperimentLabelToExperiment label : labels) {
            experimentLabelToExperimentRepository.delete(label);
        }
    }
}
