package com.infoclinika.mssharing.platform.model.impl.helper;

import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.helper.MailSendingHelperTemplate;
import com.infoclinika.mssharing.platform.model.impl.DefaultTransformingTemplate;
import com.infoclinika.mssharing.platform.repository.*;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultMailSendingHelper<USER_DETAILS extends MailSendingHelperTemplate.UserDetails>
    implements MailSendingHelperTemplate, DefaultTransformingTemplate<UserTemplate, USER_DETAILS> {

    @Inject
    private UserRepositoryTemplate<UserTemplate> userRepository;
    @Inject
    private ProjectRepositoryTemplate<ProjectTemplate> projectRepository;
    @Inject
    private InstrumentRepositoryTemplate<InstrumentTemplate> instrumentRepository;
    @Inject
    private LabRepositoryTemplate<LabTemplate> labRepository;
    @Inject
    private ExperimentRepositoryTemplate<ExperimentTemplate> experimentRepository;

    protected UserDetails transformDefault(UserTemplate user) {
        return new UserDetails(user.getFullName(), user.getEmail());
    }

    @Override
    public UserDetails userDetails(long id) {
        final UserTemplate one = userRepository.findOne(id);
        return transform(one);
    }

    @Override
    public String projectName(long id) {
        return projectRepository.findOne(id).getName();
    }

    @Override
    public String instrumentName(long instrument) {
        return instrumentRepository.findOne(instrument).getName();
    }

    @Override
    public String labName(long lab) {
        return labRepository.findOne(lab).getName();
    }

    @Override
    public String experimentName(long experiment) {
        return experimentRepository.findOne(experiment).getName();
    }

    @Override
    public List<String> labMembersEmails(long labId) {
        return userRepository.findAllUsersByLab(labId)
            .stream()
            .filter(UserTemplate::isEmailVerified)
            .map(UserTemplate::getEmail)
            .collect(toList());
    }
}
