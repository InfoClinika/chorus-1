package com.infoclinika.mssharing.model.internal.read;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Should not be used inside production code.
 *
 * @author andrii.loboda
 */
@Transactional(readOnly = true)
public interface ExperimentLabelReader {
    List<ExperimentLabelItem> readLabels(long type);

    class ExperimentLabelItem {
        public final long id;
        public final String acid;
        public final String name;

        public ExperimentLabelItem(long id, String acid, String name) {
            this.id = id;
            this.acid = acid;
            this.name = name;
        }
    }
}
