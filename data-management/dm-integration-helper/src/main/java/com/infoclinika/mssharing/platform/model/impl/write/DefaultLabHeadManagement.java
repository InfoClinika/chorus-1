package com.infoclinika.mssharing.platform.model.impl.write;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.infoclinika.mssharing.platform.entity.EntityUtil;
import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.read.ExperimentReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.FileReaderHelper;
import com.infoclinika.mssharing.platform.model.helper.read.ProjectReaderHelper;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate.ExperimentLineTemplate;
import com.infoclinika.mssharing.platform.model.read.FileReaderTemplate.FileLineTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.read.ProjectReaderTemplate.ProjectLineTemplate;
import com.infoclinika.mssharing.platform.model.write.InstrumentManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.LabHeadManagementTemplate;
import com.infoclinika.mssharing.platform.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

import static com.infoclinika.mssharing.platform.entity.EntityUtil.ENTITY_TO_ID;

/**
 * @author : Alexander Serebriyan
 */
@Transactional
@Component
public class DefaultLabHeadManagement implements LabHeadManagementTemplate {
    @Inject
    private UserRepositoryTemplate<UserTemplate> userRepository;
    @Inject
    private LabRepositoryTemplate<LabTemplate> labRepository;
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private InstrumentRepositoryTemplate<InstrumentTemplate> instrumentRepository;
    @Inject
    private InstrumentManagementTemplate instrumentManagement;
    @Inject
    private FileReaderHelper<?, ?> fileReaderHelper;
    @Inject
    private ExperimentReaderHelper<?, ?> experimentReaderHelper;
    @Inject
    private ProjectReaderHelper<?, ?> projectReaderHelper;

    @Inject
    private ExperimentRepositoryTemplate experimentRepository;
    @Inject
    private FileRepositoryTemplate fileMetaDataRepository;
    @Inject
    private ProjectRepositoryTemplate projectRepository;

    @Override
    public boolean isLabHead(long actor) {
        final List<LabTemplate> labList = labRepository.findByHeadEmail(userRepository.findOne(actor).getEmail());
        return !labList.isEmpty();
    }

    @Override
    public Collection<Long> findLabsForLabHead(long actor) {
        return Collections2.transform(
            labRepository.findByHeadEmail(userRepository.findOne(actor).getEmail()),
            EntityUtil.ENTITY_TO_ID
        );
    }

    @Override
    public void removeUserFromLab(long labHead, long labId, long userId) {
        if (!ruleValidator.userHasPermissionsToRemoveUserFromLab(labHead, labId, userId)) {
            throw new AccessDenied("User isn't permitted to removed user from laboratory");
        }

        final LabTemplate lab = labRepository.findOne(labId);
        final UserTemplate user = userRepository.findOne(userId);

        lab.removeUser(user);
        labRepository.save(lab);

        afterRemoveUserFromLab(labHead, labId, userId, user, lab);
    }

    protected void afterRemoveUserFromLab(long labHead, long labId, long userId, UserTemplate user, LabTemplate lab) {
        final Iterable<Long> experimentIds = getExperimentIdsToChangeOwner(userId, lab);
        final Iterable<Long> projectIds = getProjectIdsToChangeOwner(userId, lab);
        final Iterable<Long> fileIds = getFileIdsToChangeOwner(userId, lab);
        changeExperimentsOwnerToLabHead(lab, experimentIds);
        changeProjectsOwnerToLabHead(lab, projectIds);
        changeFilesOwnerToLabHead(lab, fileIds);

    }

    private Iterable<Long> getFileIdsToChangeOwner(long userId, final LabTemplate lab) {
        List<InstrumentTemplate> byLab = instrumentRepository.findByLab(lab.getId());
        final List<Long> instrumentIds = FluentIterable.from(byLab).transform(ENTITY_TO_ID).toList();
        return fileReaderHelper.filesByFilter(userId, Filter.MY)
            .transform()
            .filter((Predicate<FileLineTemplate>) input -> instrumentIds.contains(input.instrumentId))
            .transform((Function<FileLineTemplate, Long>) input -> input.id);
    }

    private void changeFilesOwnerToLabHead(LabTemplate lab, Iterable<Long> fileIds) {
        for (Long id : fileIds) {
            final FileMetaDataTemplate fileMetaData = (FileMetaDataTemplate) fileMetaDataRepository.findOne(id);
            fileMetaData.setOwner(lab.getHead());
            fileMetaDataRepository.save(fileMetaData);
        }
    }

    private void changeExperimentsOwnerToLabHead(final LabTemplate lab, Iterable<Long> experimentIds) {
        for (Long id : experimentIds) {
            final ExperimentTemplate experiment = (ExperimentTemplate) experimentRepository.findOne(id);
            experiment.setCreator(lab.getHead());
            experimentRepository.save(experiment);
        }
    }

    private Iterable<Long> getExperimentIdsToChangeOwner(long userId, final LabTemplate lab) {
        return experimentReaderHelper.byFilter(userId, Filter.MY).transform()
            .filter((Predicate<ExperimentLineTemplate>) input -> input.lab != null && lab.getId().equals(input.lab.id))
            .transform((Function<ExperimentLineTemplate, Long>) input -> input.id);
    }

    private void changeProjectsOwnerToLabHead(final LabTemplate lab, Iterable<Long> projectIds) {
        for (Long id : projectIds) {
            final ProjectTemplate project = (ProjectTemplate) projectRepository.findOne(id);
            project.setCreator(lab.getHead());
            projectRepository.save(project);
        }
    }

    private Iterable<Long> getProjectIdsToChangeOwner(long userId, final LabTemplate lab) {

        return projectReaderHelper.readProjectsFiltered(userId, Filter.MY)
            .transform()
            .filter((Predicate<ProjectLineTemplate>) input -> input.lab != null && lab.getId().equals(input.lab.id))
            .transform((Function<ProjectLineTemplate, Long>) input -> input.id);
    }

}
