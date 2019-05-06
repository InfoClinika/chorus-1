package com.infoclinika.mssharing.model.internal.read;

import com.infoclinika.mssharing.model.read.dto.details.ExperimentLabelsItem;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author andrii.loboda
 */
@Transactional(readOnly = true)
public interface ExperimentLabelToExperimentReader {
    /**
     * @return object with light, medium, heavy label IDS of experiment
     *     Please note, that those IDs obtained from common ExperimentLabel, not assigned to experiment
     */
    ExperimentLabelsItem readLabels(long experiment);

    ExperimentLabelsDetails readLabelsDetails(long experiment);

    final class ExperimentLabelsDetails {
        public final List<ExperimentLabelItem> lightLabels;
        public final List<ExperimentLabelItem> mediumLabels;
        public final List<ExperimentLabelItem> heavyLabels;
        public final List<ExperimentLabelItem> specialLabels;

        public ExperimentLabelsDetails(
            List<ExperimentLabelItem> lightLabels,
            List<ExperimentLabelItem> mediumLabels,
            List<ExperimentLabelItem> heavyLabels,
            List<ExperimentLabelItem> specialLabels
        ) {
            this.lightLabels = lightLabels;
            this.mediumLabels = mediumLabels;
            this.heavyLabels = heavyLabels;
            this.specialLabels = specialLabels;
        }
    }

    final class ExperimentLabelItem {
        public final long id;
        public final String name;

        public ExperimentLabelItem(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

}
