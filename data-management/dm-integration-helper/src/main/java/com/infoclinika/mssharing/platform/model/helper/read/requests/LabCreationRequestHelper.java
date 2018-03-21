package com.infoclinika.mssharing.platform.model.helper.read.requests;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.LabCreationRequestTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.AbstractReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.ResultBuilder;
import com.infoclinika.mssharing.platform.repository.LabCreationRequestRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static com.infoclinika.mssharing.platform.model.helper.read.ResultBuilder.builder;
import static com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate.LabRequest;

/**
 * @author Herman Zamula
 */
@Component
@Scope(value = "prototype")
public class LabCreationRequestHelper
        <ENTITY extends LabCreationRequestTemplate, LINE extends LabRequest>
        extends AbstractReaderHelper<ENTITY, LINE, LabRequest> {

    @Inject
    private LabCreationRequestRepositoryTemplate<ENTITY> labCreationRequestRepository;


    @Override
    public Function<ENTITY, LabRequest> getDefaultTransformer() {
        return new Function<ENTITY, LabRequest>() {
            @Override
            public LabRequest apply(ENTITY input) {
                return new LabRequest(input.getContactEmail(), input.getLabName(), input.getRequestDate(), input.getId());
            }
        };
    }

    public ResultBuilder<ENTITY, LINE> readInbox() {
        return builder(labCreationRequestRepository.findAll(), activeTransformer);
    }
}
