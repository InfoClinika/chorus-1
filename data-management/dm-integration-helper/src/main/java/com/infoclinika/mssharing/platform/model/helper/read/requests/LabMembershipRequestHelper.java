package com.infoclinika.mssharing.platform.model.helper.read.requests;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.UserLabMembershipRequestTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.AbstractReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.ResultBuilder;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate.LabMembershipRequest;
import com.infoclinika.mssharing.platform.repository.UserLabMembershipRequestRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static com.infoclinika.mssharing.platform.model.helper.read.ResultBuilder.builder;

/**
 * @author Herman Zamula
 */
@Component
@Scope(value = "prototype")
public class LabMembershipRequestHelper<ENTITY extends UserLabMembershipRequestTemplate, LINE extends LabMembershipRequest>
        extends AbstractReaderHelper<ENTITY, LINE, LabMembershipRequest> {

    @Inject
    private UserLabMembershipRequestRepositoryTemplate<ENTITY> userLabMemebershipRequestRepository;

    @Override
    public Function<ENTITY, LabMembershipRequest> getDefaultTransformer() {
        return new Function<ENTITY, LabMembershipRequest>() {
            @Override
            public LabMembershipRequest apply(ENTITY input) {
                return new LabMembershipRequest(input.getId(),
                        input.getLab().getId(),
                        input.getSent(),
                        input.getUser().getEmail(),
                        input.getUser().getFullName(),
                        input.getLab().getName());
            }
        };
    }

    public ResultBuilder<ENTITY, LINE> readInbox(long actor) {
        return builder(userLabMemebershipRequestRepository.findPendingForHead(actor), activeTransformer);
    }

    public ResultBuilder<ENTITY, LINE> readOutbox(long actor) {
        return builder(userLabMemebershipRequestRepository.findPendingByUser(actor), activeTransformer);
    }

}
