package com.infoclinika.mssharing.platform.model.helper.write;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.write.AttachmentManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.ProjectManagementTemplate;
import com.infoclinika.mssharing.platform.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.transform;

/**
 * @author Herman Zamula
 */
@Component
@Transactional
@SuppressWarnings("unchecked")
public class ProjectManager<PROJECT extends ProjectTemplate,
    PROJECT_INFO extends ProjectManagementTemplate.ProjectInfoTemplate> {

    @Inject
    private ProjectRepositoryTemplate<PROJECT> projectRepository;
    @Inject
    private UserRepositoryTemplate<UserTemplate> userRepository;
    @Inject
    private LabRepositoryTemplate<LabTemplate> labRepository;
    @Inject
    private AttachmentRepositoryTemplate<Attachment> attachmentRepository;
    @Inject
    private ExperimentRepositoryTemplate<ExperimentTemplate> experimentRepository;
    @Inject
    private ExperimentFileRepositoryTemplate<ExperimentFileTemplate> experimentFileRepository;
    @Inject
    private FactorRepositoryTemplate<FactorTemplate> factorRepository;
    @Inject
    private Provider<Date> current;
    @Inject
    private EntityFactories factories;
    @Inject
    private CopyManager copyManager;
    @Inject
    private AttachmentManagementTemplate attachmentManagement;

    private Function<Long, LabTemplate> labFromID = new Function<Long, LabTemplate>() {
        @Override
        public LabTemplate apply(Long input) {
            return labRepository.findOne(input);
        }
    };


    public PROJECT createProject(Long creator, PROJECT_INFO projectInfo) {
        final UserTemplate creatorEntity = userRepository.findOne(creator);
        //noinspection unchecked
        final PROJECT project = (PROJECT) factories.project.get();
        final LabTemplate lab = projectInfo.lab.transform(labFromID).orNull();

        project.setDescription(projectInfo.description);
        project.setName(projectInfo.name);
        project.setCreator(creatorEntity);
        project.setLab(lab);
        project.setAreaOfResearch(projectInfo.areaOfResearch);
        projectRepository.save(project);
        return project;
    }

    public PROJECT updateProject(Long projectId, PROJECT_INFO projectInfo) {
        PROJECT project = projectRepository.findOne(projectId);
        project.setName(projectInfo.name);
        project.setDescription(projectInfo.description);
        project.setAreaOfResearch(projectInfo.areaOfResearch);
        return projectRepository.save(project);
    }

    public PROJECT saveProject(PROJECT project) {
        project.setLastModification(current.get());
        return projectRepository.save(project);
    }


    public PROJECT copyProject(long projectId, long newOwnerId) {
        final PROJECT original = checkNotNull(projectRepository.findOne(projectId));

        final UserTemplate newOwner = userRepository.findOne(newOwnerId);

        final String copyProjName = copyManager.createCopyName(
            original.getName(),
            projectRepository.findOneByName(newOwner.getId(), original.getName()) != null
        );

        return saveProject(copyProjectFor(newOwner, original, copyProjName));
    }

    public ExperimentTemplate copyProjectExperiment(UserTemplate newOwner,
                                                    PROJECT copyOfProject,
                                                    ExperimentTemplate originalExperiment) {
        final boolean isNameUsed =
            experimentRepository.findOneByName(newOwner.getId(), originalExperiment.getName()) != null;
        String experimentCopyName = copyManager.createCopyName(originalExperiment.getName(), isNameUsed);
        ExperimentTemplate copy =
            saveExperiment(copyExperimentData(copyOfProject, originalExperiment, experimentCopyName));
        setDownloadToken(projectRepository.findOne(copyOfProject.getId()).getSharing().getType(), copy);
        return copy;
    }

    public ExperimentTemplate copyExperimentData(PROJECT copyOfProject,
                                                 ExperimentTemplate originalExperiment,
                                                 String experimentCopyName) {
        final ExperimentTemplate copy = factories.experiment.get();
        final UserTemplate creator = copyOfProject.getCreator();
        copy.setCreator(creator);
        copy.setProject(copyOfProject);
        copy.setLab(originalExperiment.getLab());
        copy.setName(experimentCopyName);
        copy.setInstrumentRestriction(originalExperiment.getInstrumentRestriction());
        copy.setSpecie(originalExperiment.getSpecie());
        copy.setLastModification(new Date());
        copy.setExperimentType(originalExperiment.getExperimentType());
        copy.setExperiment(originalExperiment.getExperiment());

        copy.attachments.addAll(copyAttachments(creator, originalExperiment.attachments, false));

        return copy;
    }

    private ImmutableSet<Attachment> copyAttachments(final UserTemplate creator, List<Attachment> attachments,
                                                     final boolean isProject) {
        return from(attachments)
            .transform(attachment -> attachmentRepository
                .findOne(attachmentManagement.copyAttachment(attachment.getId(), creator.getId(), isProject))).toSet();
    }

    public void setDownloadToken(Sharing.Type type, ExperimentTemplate experiment) {
        switch (type) {
            case PUBLIC:
                experiment.setDownloadToken(String.valueOf(new Random().nextLong()));
                break;
            case SHARED:
            case PRIVATE:
                experiment.setDownloadToken(null);
                break;
            default:
                throw new IllegalArgumentException("Unrecognized sharing type: " + type);
        }
    }

    private ExperimentTemplate saveExperiment(ExperimentTemplate experiment) {
        experiment.setLastModification(current.get());
        return experimentRepository.save(experiment);
    }

    public void copyExperimentRawFiles(ExperimentTemplate from, ExperimentTemplate to,
                                       final Function<FileMetaDataTemplate, FileMetaDataTemplate> copyMetaFn) {

        final List<ExperimentFileTemplate> copiedFiles =
            transform(from.rawFiles.getData(), copyExperimentFileFn(copyMetaFn));
        final List<FactorTemplate> factors = transform(from.rawFiles.getFactors(), copyFactorFn());

        to.rawFiles.getData().clear();
        ;
        to.rawFiles.getFactors().clear();

        to.rawFiles.getData().addAll(copiedFiles);
        to.rawFiles.getFactors().addAll(factors);

        addConditionsToLevelsAndRawFiles(to);

    }

    protected Function<ExperimentFileTemplate, ExperimentFileTemplate> copyExperimentFileFn(
        final Function<FileMetaDataTemplate, FileMetaDataTemplate> copyMetaFn) {
        return input -> {
            final ExperimentFileTemplate apply = copyManager.copyExperimentFileData(copyMetaFn).apply(input);
            return experimentFileRepository.save(apply);
        };
    }

    private void addConditionsToLevelsAndRawFiles(ExperimentTemplate ex) {
        new ConditionsFactory(ex, ex.rawFiles.getFactors(), ex.rawFiles.getData()).create();
    }

    private Function<FactorTemplate, FactorTemplate> copyFactorFn() {
        return origin -> {
            final Function<FactorTemplate, FactorTemplate> copyFactorData = copyManager.copyFactorData();
            final FactorTemplate factor = copyFactorData.apply(origin);
            final Function<LevelTemplate, LevelTemplate> copyLevelData = copyManager.copyLevelData(factor);
            factor.getLevels().addAll(from(origin.getLevels()).transform(copyLevelData).toSet());
            return factorRepository.save(factor);
        };
    }


    private PROJECT copyProjectFor(UserTemplate newOwner, PROJECT origin, String copyName) {

        //noinspection unchecked
        final PROJECT copyProject = (PROJECT) factories.project.get();

        copyProject.setCreator(newOwner);
        copyProject.setName(copyName);
        copyProject.setDescription(origin.getDescription());
        copyProject.setAreaOfResearch(origin.getAreaOfResearch());

        copyProject.getAttachments().addAll(copyAttachments(newOwner, origin.getAttachments(), true));

        return copyProject;
    }

}
