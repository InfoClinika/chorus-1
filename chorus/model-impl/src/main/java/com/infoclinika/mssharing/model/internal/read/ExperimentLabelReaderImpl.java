package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.internal.entity.ExperimentLabel;
import com.infoclinika.mssharing.model.internal.entity.ExperimentLabelType;
import com.infoclinika.mssharing.model.internal.repository.ExperimentLabelRepository;
import com.infoclinika.mssharing.model.internal.repository.ExperimentLabelTypeRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

/**
 * @author andrii.loboda
 */
@Service
public class ExperimentLabelReaderImpl implements ExperimentLabelReader {
    @Inject
    private ExperimentLabelRepository experimentLabelRepository;
    @Inject
    private ExperimentLabelTypeRepository experimentLabelTypeRepository;

    @Override
    public List<ExperimentLabelItem> readLabels(long type) {
        final ExperimentLabelType experimentLabelType = experimentLabelTypeRepository.findOne(type);
        checkNotNull(experimentLabelType);

        return newArrayList(transform(
            experimentLabelRepository.findByType(experimentLabelType),
            new Function<ExperimentLabel, ExperimentLabelItem>() {
                @Override
                public ExperimentLabelItem apply(ExperimentLabel label) {
                    return new ExperimentLabelItem(label.getId(), label.getAcid(), label.getName());
                }
            }
        ));
    }
}
