package com.infoclinika.mssharing.platform.model.helper.read.details;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.LabCreationRequestTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.AbstractReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.LabItemTemplate;
import com.infoclinika.mssharing.platform.repository.LabCreationRequestRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Herman Zamula
 */
@Component
@Scope("prototype")
public class LabRequestDetailsReaderHelper<LAB_CREATION_ENTITY extends LabCreationRequestTemplate,
    LAB_CREATION extends LabItemTemplate>
    extends AbstractReaderHelper<LAB_CREATION_ENTITY, LAB_CREATION, LabItemTemplate> {

    @Inject
    private LabCreationRequestRepositoryTemplate<LAB_CREATION_ENTITY> labCreationRequestRepository;

    @Override
    public Function<LAB_CREATION_ENTITY, LabItemTemplate> getDefaultTransformer() {
        return request -> new LabItemTemplate(
            request.getId(),
            request.getLabName(),
            request.getInstitutionUrl(),
            request.getHeadData().getFirstName(),
            request.getHeadData().getLastName(),
            request.getHeadData().getEmail(),
            request.getContactEmail(),
            request.getLastModification()
        );
    }

    public SingleResultBuilder<LAB_CREATION_ENTITY, LAB_CREATION> readRequest(long request) {
        return SingleResultBuilder.builder(labCreationRequestRepository.findOne(request), activeTransformer);
    }
}
