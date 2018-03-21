package com.infoclinika.mssharing.platform.model.helper.read.requests;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.ProjectSharingRequestTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.helper.read.AbstractReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.ResultBuilder;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.ProjectSharingRequestRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static com.infoclinika.mssharing.platform.model.helper.read.ResultBuilder.builder;

/**
 * @author Herman Zamula
 */
@Component
@Scope(value = "prototype")
public class ProjectSharingInboxHelper<ENTITY extends ProjectSharingRequestTemplate, LINE extends RequestsReaderTemplate.ProjectSharingInfo>
        extends AbstractReaderHelper<ENTITY, LINE, RequestsReaderTemplate.ProjectSharingInfo> {

    @Inject
    private ProjectRepositoryTemplate projectRepository;
    @Inject
    private UserRepositoryTemplate userRepository;

    @Inject
    private ProjectSharingRequestRepositoryTemplate<ENTITY> projectSharingRequestRepository;

    @Override
    public Function<ENTITY, RequestsReaderTemplate.ProjectSharingInfo> getDefaultTransformer() {
        return new Function<ENTITY, RequestsReaderTemplate.ProjectSharingInfo>() {
            @Override
            public RequestsReaderTemplate.ProjectSharingInfo apply(ENTITY input) {
                final String projectName = ((ProjectTemplate) projectRepository.findOne(input.getProjectId())).getName();
                final String requesterName = ((UserTemplate) userRepository.findOne(input.getRequesterId())).getFullName();
                return new RequestsReaderTemplate.ProjectSharingInfo(input.getId(), requesterName,
                        input.getRequesterId(), projectName,
                        input.getProjectId(),
                        input.getRequestDate(),
                        input.getDownloadExperimentLinks());
            }
        };
    }

    public ResultBuilder<ENTITY, LINE> readProject(long actor) {
        return builder(projectSharingRequestRepository.findByProjectCreator(actor), activeTransformer);
    }
}
