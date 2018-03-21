package com.infoclinika.mssharing.platform.model.impl.write;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.ProjectSharingRequestTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.NotifierTemplate;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.helper.write.CopyManager;
import com.infoclinika.mssharing.platform.model.helper.write.ProjectManager;
import com.infoclinika.mssharing.platform.model.write.ProjectManagementTemplate;
import com.infoclinika.mssharing.platform.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkAccess;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author : Alexander Serebriyan
 */
@Component
@Transactional
public class DefaultProjectManagement<PROJECT extends ProjectTemplate, PROJECT_INFO extends ProjectManagementTemplate.ProjectInfoTemplate> implements ProjectManagementTemplate<PROJECT_INFO> {

    @Inject
    protected ProjectManager<PROJECT, PROJECT_INFO> projectManager;
    @Inject
    protected ProjectRepositoryTemplate<PROJECT> projectRepository;
    @Inject
    protected ExperimentRepositoryTemplate<ExperimentTemplate> experimentRepository;
    @Inject
    protected UserRepositoryTemplate<UserTemplate> userRepository;
    @Inject
    protected RuleValidator ruleValidator;
    @Inject
    protected NotifierTemplate notifier;
    @Inject
    protected CopyManager copyManager;
    @Inject
    protected ExperimentFileRepositoryTemplate experimentFileRepository;
    @Inject
    protected FileRepositoryTemplate<FileMetaDataTemplate> fileMetaDataRepository;
    @Inject
    protected ProjectSharingRequestRepositoryTemplate<ProjectSharingRequestTemplate> projectSharingRequestRepository;

    @Override
    public long createProject(long creator, PROJECT_INFO projectInfo) {
        beforeCreateProject(creator, projectInfo);
        final PROJECT project = projectManager.createProject(creator, projectInfo);
        return project.getId();
    }

    protected void beforeCreateProject(long creator, PROJECT_INFO projectInfo) {
        if (!ruleValidator.canUserCreateProjectWithTitle(creator, projectInfo.name)) {
            throw new IllegalArgumentException("Can not create project with name: \"" + projectInfo.name + "\"");
        }
        if (isEmpty(projectInfo.areaOfResearch.trim())) {
            throw new IllegalArgumentException("Project's area of research must be specified");
        }
    }

    @Override
    public void removeProject(long actor, long projectId) {
        beforeRemoveProject(actor, projectId);
        for (ExperimentTemplate e : experimentRepository.findByProject(projectId)) {
            onRemoveExperiment(e);
        }
        projectRepository.delete(projectId);

        afterRemoveProject(projectId);
    }

    protected void beforeRemoveProject(long actor, long projectId) {

        checkAccess(ruleValidator.canRemoveProject(actor, projectId), "Couldn't remove project");

    }

    protected void afterRemoveProject(long projectId) {
        removeProjectSharingRequests(projectId);
    }

    protected void onRemoveExperiment(ExperimentTemplate e) {
        removeCopiedFiles(e);
        experimentRepository.delete(e.getId());
    }

    private void removeCopiedFiles(ExperimentTemplate e) {
        List<ExperimentFileTemplate> rawFiles = e.rawFiles.getData();
        for (ExperimentFileTemplate file : rawFiles) {
            if (file.getFileMetaData().isCopy() && experimentFileRepository.findByMetaData(file.getFileMetaData()).size() == 1) {
                experimentFileRepository.delete(file);
                fileMetaDataRepository.delete(file.getFileMetaData());
            }
        }
    }

    public void removeProjectSharingRequests(long projectId) {
        final List<ProjectSharingRequestTemplate> projectSharingRequests = projectSharingRequestRepository.findByProject(projectId);
        projectSharingRequestRepository.delete(projectSharingRequests);
    }

    @Override
    public void updateProject(long actor, long projectId, PROJECT_INFO projectInfo) {
        if (!ruleValidator.hasWriteAccessOnProject(actor, projectId)) throw new AccessDenied("Couldn't update");
        if (!ruleValidator.canUserUpdateProjectWithTitle(actor, projectId, projectInfo.name)) {
            throw new IllegalArgumentException("User already has project with this name: \"" + projectInfo.name + "\"");
        }
        if (isEmpty(projectInfo.areaOfResearch.trim())) {
            throw new IllegalArgumentException("Project's area of research must be specified");
        }
        projectManager.updateProject(projectId, projectInfo);
    }

    @Override
    public long copyProject(long actor, CopyProjectInfoTemplate copyInfo) {
        beforeCopyProject(actor, copyInfo);

        PROJECT originalProject = projectRepository.findOne(copyInfo.getProject());
        PROJECT copyOfProject = projectManager.copyProject(copyInfo.getProject(), copyInfo.getNewOwner());
        List<ExperimentTemplate> originalExperiments = experimentRepository.findByProject(copyInfo.getProject());
        UserTemplate newOwner = userRepository.findOne(copyInfo.getNewOwner());
        for (ExperimentTemplate originalExperiment : originalExperiments) {
            if (originalProject.getCreator().getEmail().contentEquals(originalExperiment.getCreator().getEmail())) {
                ExperimentTemplate experimentCopy = onCopyExperiment(copyOfProject, newOwner, originalExperiment, copyInfo);
                onCopyExperimentRawFiles(newOwner, originalExperiment, experimentCopy, copyInfo);
            }
        }

        if (copyInfo.isNotification()) {
            notifier.projectCopied(copyInfo.getNewOwner(), copyInfo.getOwner(), copyOfProject.getId());
        }
        return copyOfProject.getId();
    }

    protected void beforeCopyProject(long actor, CopyProjectInfoTemplate copyInfo) {
        checkAccess(ruleValidator.hasWriteAccessOnProject(copyInfo.getOwner(), copyInfo.getProject()), "Couldn't create a copy");
    }

    protected void onCopyExperimentRawFiles(UserTemplate newOwner, ExperimentTemplate originalExperiment, ExperimentTemplate experimentCopy, CopyProjectInfoTemplate copyInfo) {
        projectManager.copyExperimentRawFiles(originalExperiment, experimentCopy, createCopyMetaDataFn(newOwner));
    }

    protected ExperimentTemplate onCopyExperiment(PROJECT copyOfProject, UserTemplate newOwner, ExperimentTemplate originalProjectExperiment, CopyProjectInfoTemplate copyInfo) {
        return projectManager.copyProjectExperiment(newOwner, copyOfProject, originalProjectExperiment);
    }

    private Function<FileMetaDataTemplate, FileMetaDataTemplate> createCopyMetaDataFn(final UserTemplate newOwner) {

        final Map<FileMetaDataTemplate, FileMetaDataTemplate> originalCopyMap = newHashMap();

        return new Function<FileMetaDataTemplate, FileMetaDataTemplate>() {
            @Override
            public FileMetaDataTemplate apply(FileMetaDataTemplate originalMeta) {
                if (originalCopyMap.containsKey(originalMeta)) {
                    return originalCopyMap.get(originalMeta);
                }
                final FileMetaDataTemplate copied = fileMetaDataRepository.save(copyManager.copyFileMetaData(originalMeta, newOwner));
                originalCopyMap.put(originalMeta, copied);
                return copied;
            }
        };
    }


}
