package com.infoclinika.mssharing.platform.model.impl.helper;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.Sharing;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.NotifierTemplate;
import com.infoclinika.mssharing.platform.model.ObjectNotFoundException;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate;
import com.infoclinika.mssharing.platform.repository.ExperimentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.FileRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultExperimentDownloadHelper<
    EXPERIMENT_ITEM extends ExperimentDownloadHelperTemplate.ExperimentItemTemplate,
    EXPERIMENT_DOWNLOAD_DATA extends ExperimentDownloadHelperTemplate.ExperimentDownloadDataTemplate,
    FILE_DATA extends ExperimentDownloadHelperTemplate.FileDataTemplate>
    implements ExperimentDownloadHelperTemplate<EXPERIMENT_ITEM, EXPERIMENT_DOWNLOAD_DATA, FILE_DATA> {

    @Inject
    private ExperimentRepositoryTemplate<ExperimentTemplate> experimentRepository;
    @Inject
    private UserRepositoryTemplate<UserTemplate> userRepository;
    @Inject
    private FileRepositoryTemplate<FileMetaDataTemplate> fileMetaDataRepository;

    @Inject
    private NotifierTemplate notifier;

    @Inject
    private RuleValidator ruleValidator;

    @Override
    public boolean isDownloadTokenAvailable(String token) {
        checkNotNull(token);
        return experimentRepository.findOneByToken(token) != null;
    }


    @Override
    public EXPERIMENT_ITEM getExperimentByDownloadToken(String token) {
        ExperimentTemplate experiment = checkNotNull(experimentRepository.findOneByToken(token));
        return transformExperimentItem(experiment);
    }

    @Override
    public void sendDownloadExperimentLinkEmail(long actor, long experimentId, String email) {
        final ExperimentTemplate experiment = checkNotNull(experimentRepository.findOne(experimentId));
        if (experiment.getProject().getSharing().getType() == Sharing.Type.PUBLIC) {
            notifier.sendExperimentPublicDownloadLink(
                actor,
                experiment.getId(),
                email,
                getPublicDownloadLink(experiment)
            );
        } else {
            UserTemplate user = userRepository.findByEmail(email);
            if (user == null) {
                throw new RuntimeException("Can't find user with specified email: " + email);
            }
            if (ruleValidator.isUserCanReadExperiment(actor, experimentId)) {
                notifier.sendExperimentPrivateDownloadLink(
                    actor,
                    experiment.getId(),
                    email,
                    getPrivateDownloadLink(experiment)
                );
            } else {
                throw new AccessDenied("User haven't right to read experiment");
            }
        }

    }

    @Override
    public EXPERIMENT_DOWNLOAD_DATA readExperimentDownloadData(long userId, long experimentId) {

        final ExperimentTemplate experiment = experimentRepository.findOne(experimentId);

        if (experiment == null) {
            throw new ObjectNotFoundException("Experiment not found");
        }

        if (!ruleValidator.isUserCanReadExperiment(userId, experimentId)) {
            throw new AccessDenied("Can't read experiment");
        }

        return transformExperimentDownloadData(experiment);
    }


    @Override
    public List<FILE_DATA> readFilesDownloadData(long userId, Set<Long> fileIds) {

        final List<FileMetaDataTemplate> files = fileMetaDataRepository.findAllByIds(fileIds);
        final boolean hasPermissions = ruleValidator.userHasReadPermissionsOnFiles(userId, fileIds);

        if (!hasPermissions) {
            throw new AccessDenied("User can't read all files");
        }

        return newArrayList(from(files).transform(input -> transformFileData(input)).toList());

    }

    protected abstract String getPublicDownloadLink(ExperimentTemplate experimentTemplate);

    protected abstract String getPrivateDownloadLink(ExperimentTemplate experimentTemplate);

    protected abstract FILE_DATA transformFileData(FileMetaDataTemplate metaDataTemplate);

    protected abstract EXPERIMENT_DOWNLOAD_DATA transformExperimentDownloadData(ExperimentTemplate experiment);

    protected abstract EXPERIMENT_ITEM transformExperimentItem(ExperimentTemplate experimentTemplate);

}
