package com.infoclinika.mssharing.platform.model.helper.write;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Herman Zamula
 *         <p>
 *         TODO: Refactor, use this helper with default implementation
 */
@Component
public class CopyManager {

    @Inject
    private EntityFactories factories;
    @Inject
    private StorageService fileStorageService;
    @Inject
    private Provider<Date> current;

    public String createCopyName(String oldName, boolean isNameUsed) {
        if (oldName.lastIndexOf(".") != -1) {
            final String extension = oldName.substring(oldName.lastIndexOf("."), oldName.length());
            final String clearOldName = oldName.substring(0, oldName.lastIndexOf("."));
            return isNameUsed ? clearOldName + " Copy " + currentDateFormatted() + extension : clearOldName + extension;
        }
        //For files without extension
        return isNameUsed ? oldName + " Copy " + currentDateFormatted() : oldName;
    }

    private String currentDateFormatted() {
        final SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy hh:mm:ss", Locale.CANADA.ENGLISH);
        return format.format(current.get());
    }

    public Function<ProjectTemplate, ProjectTemplate> copyProjectData(UserTemplate newOwner) {
        return new Function<ProjectTemplate, ProjectTemplate>() {
            @Override
            public ProjectTemplate apply(ProjectTemplate origin) {
                return factories.project.get();
            }
        };
    }

    public Function<ExperimentTemplate, ExperimentTemplate> copyExperimentData(final ProjectTemplate copiedProject, final String copyName) {
        return new Function<ExperimentTemplate, ExperimentTemplate>() {
            @Override
            public ExperimentTemplate apply(ExperimentTemplate origin) {
                //noinspection unchecked
                final ExperimentTemplate copy = factories.experiment.get();
                copy.setCreator(copiedProject.getCreator());
                copy.setProject(copiedProject);
                copy.setLab(origin.getLab());
                copy.setName(copyName);
                copy.setInstrumentRestriction(origin.getInstrumentRestriction());
                copy.setSpecie(origin.getSpecie());
                copy.setLastModification(new Date());
                return copy;
            }
        };
    }

    public Function<FileMetaDataTemplate, FileMetaDataTemplate> copyFileMetaData(final UserTemplate newOwner) {
        return new Function<FileMetaDataTemplate, FileMetaDataTemplate>() {
            @Override
            public FileMetaDataTemplate apply(FileMetaDataTemplate origin) {
                final FileMetaDataTemplate f = factories.fileMetaData.get();
                f.setName(origin.getName());
                f.setDestinationPath(origin.getDestinationPath());
                f.setUploadId(origin.getUploadId());
                f.setUploadDate(origin.getUploadDate());
                f.setSizeInBytes(origin.getSizeInBytes());
                f.setLabels(origin.getLabels());
                f.setLastModification(new Date());
                f.setOwner(newOwner);
                f.setCopy(true);
                return f;
            }
        };
    }

    public FileMetaDataTemplate copyFileMetaData(FileMetaDataTemplate from, UserTemplate owner) {
        final String copyName = createCopyName(from.getName(), true);
        return from.copy(copyName, owner);
    }

    @SuppressWarnings("unchecked")
    public Function<ExperimentFileTemplate, ExperimentFileTemplate> copyExperimentFileData(final Function<FileMetaDataTemplate, FileMetaDataTemplate> copyMetaFn) {
        return new Function<ExperimentFileTemplate, ExperimentFileTemplate>() {
            @Override
            public ExperimentFileTemplate apply(ExperimentFileTemplate origin) {
                final ExperimentFileTemplate fileTemplate = factories.rawFile.get();
                fileTemplate.setFileMetaData(copyMetaFn.apply(origin.getFileMetaData()));
                fileTemplate.getFactorValues().addAll(newArrayList(origin.getFactorValues()));
                fileTemplate.getAnnotationList().addAll(from(origin.getAnnotationList()).transform(copyAnnotationFn()).toList());
                fileTemplate.setCopy(true);
                return fileTemplate;
            }
        };
    }

    protected Function<AnnotationTemplate, AnnotationTemplate> copyAnnotationFn() {
        return new Function<AnnotationTemplate, AnnotationTemplate>() {
            @Override
            public AnnotationTemplate apply(AnnotationTemplate input) {
                final AnnotationTemplate template = factories.annotation.get();
                template.setType(input.getType());
                template.setName(input.getName());
                template.setValue(input.getValue());
                template.setUnits(input.getUnits());
                return template;
            }
        };
    }

    public Function<FactorTemplate, FactorTemplate> copyFactorData() {
        return new Function<FactorTemplate, FactorTemplate>() {
            @Override
            public FactorTemplate apply(FactorTemplate origin) {
                final FactorTemplate factorTemplate = factories.factor.get();
                factorTemplate.setbDefault(origin.isbDefault());
                factorTemplate.setExperiment(origin.getExperiment());
                factorTemplate.setName(origin.getName());
                factorTemplate.setUnits(origin.getUnits());
                factorTemplate.setType(origin.getType());
                return factorTemplate;
            }
        };
    }

    public Function<LevelTemplate, LevelTemplate> copyLevelData(final FactorTemplate factor) {
        return new Function<LevelTemplate, LevelTemplate>() {
            @Override
            public LevelTemplate apply(LevelTemplate origin) {
                final LevelTemplate levelTemplate = factories.level.get();
                levelTemplate.setName(origin.getName());
                levelTemplate.setFactor(factor);
                return levelTemplate;
            }
        };
    }

    public Function<Attachment, Attachment> copyAttachment(final Function<Attachment<?>, NodePath> attachmentPathFn) {
        return new Function<Attachment, Attachment>() {
            @Override
            public Attachment apply(Attachment origin) {

                final Attachment copy = factories.attachment.get();
                copy.setOwner(origin.getOwner());
                copy.setName(origin.getName());
                copy.setUploadDate(new Date());
                copy.setSizeInBytes(origin.getSizeInBytes());

                final NodePath originPath = attachmentPathFn.apply(origin);
                final NodePath copyPath = attachmentPathFn.apply(copy);

                fileStorageService.put(copyPath, fileStorageService.get(originPath));

                return copy;
            }
        };
    }

}
