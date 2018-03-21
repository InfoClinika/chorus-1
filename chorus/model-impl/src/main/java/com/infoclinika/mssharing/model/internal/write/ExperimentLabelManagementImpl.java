package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.internal.entity.ExperimentLabel;
import com.infoclinika.mssharing.model.internal.entity.ExperimentLabelType;
import com.infoclinika.mssharing.model.internal.repository.ExperimentLabelRepository;
import com.infoclinika.mssharing.model.internal.repository.ExperimentLabelTypeRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author andrii.loboda
 */
@Service
public class ExperimentLabelManagementImpl implements ExperimentLabelManagement {
    @Inject
    private ExperimentLabelTypeRepository experimentLabelTypeRepository;
    @Inject
    private ExperimentLabelRepository experimentLabelRepository;

    @Override
    public long createLabelType(ExperimentTypeInfo typeToCreate) {
        checkNotNull(typeToCreate);
        checkNotNull(typeToCreate.name);
        final ExperimentLabelType type = new ExperimentLabelType(typeToCreate.name, typeToCreate.maxSamples);
        return experimentLabelTypeRepository.save(type).getId();
    }

    @Override
    public long createLabel(ExperimentLabelInfo labelToCreate) {
        checkNotNull(labelToCreate.aminoAcid);
        checkNotNull(labelToCreate.name);
        final ExperimentLabelType type = experimentLabelTypeRepository.findOne(labelToCreate.type);
        checkNotNull(type);
        return experimentLabelRepository.save(new ExperimentLabel(labelToCreate.aminoAcid, labelToCreate.name, type)).getId();
    }
}
