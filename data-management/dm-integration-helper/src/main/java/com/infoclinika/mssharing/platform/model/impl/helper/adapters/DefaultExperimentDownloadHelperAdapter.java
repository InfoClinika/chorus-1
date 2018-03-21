package com.infoclinika.mssharing.platform.model.impl.helper.adapters;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.fileserver.StoredObjectPathsTemplate;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate;
import com.infoclinika.mssharing.platform.model.impl.helper.DefaultExperimentDownloadHelper;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;


/**
 * @author Herman Zamula
 */
@Component
public class DefaultExperimentDownloadHelperAdapter extends DefaultExperimentDownloadHelper<ExperimentDownloadHelperTemplate.ExperimentItemTemplate,
        ExperimentDownloadHelperTemplate.ExperimentDownloadDataTemplate, ExperimentDownloadHelperTemplate.FileDataTemplate> {

    @Inject
    private StoredObjectPathsTemplate storedObjectPaths;

    @Override
    protected String getPublicDownloadLink(ExperimentTemplate experimentTemplate) {
        return "/download?experiment=" + experimentTemplate.getId();
    }

    @Override
    protected String getPrivateDownloadLink(ExperimentTemplate experimentTemplate) {
        return "/download/private?experiment=" + experimentTemplate.getId();
    }

    @Override
    protected FileDataTemplate transformFileData(FileMetaDataTemplate metaDataTemplate) {
        return transformFileDataWithConditions(metaDataTemplate, Collections.<ConditionDataTemplate>emptyList());
    }


    private FileDataTemplate transformFileDataWithConditions(FileMetaDataTemplate metaDataTemplate, List<ConditionDataTemplate> conditions) {
        return new FileDataTemplate(metaDataTemplate.getId(), metaDataTemplate.getContentId(), metaDataTemplate.getName(),
                metaDataTemplate.isInvalid(), conditions,
                metaDataTemplate.getInstrument().getLab().getId());

    }

    @Override
    protected ExperimentDownloadDataTemplate transformExperimentDownloadData(final ExperimentTemplate experiment) {
        final long creatorId = experiment.getCreator().getId();
        final String experimentName = experiment.getName();
        final String experimentDescription = experiment.getExperiment().getDescription();
        final String projectName = experiment.getProject().getName();
        final String specie = experiment.getSpecie().getName();

        final ExperimentType exType = experiment.getExperimentType();

        final String experimentType = exType != null ? exType.getName() : null;
        final boolean allow2dLc = exType != null && exType.allowed2dLC;

        final InstrumentTemplate instrument = experiment.getInstrumentRestriction().getInstrument();
        final String instrumentName = instrument != null ? instrument.getName() : null;


        final List<AttachmentDataTemplate> attachments = newArrayList();
        //noinspection unchecked
        attachments.addAll(Lists.transform(experiment.attachments, new Function<Attachment, AttachmentDataTemplate>() {
            @Override
            public AttachmentDataTemplate apply(Attachment input) {
                return new AttachmentDataTemplate(input.getId(),
                        input.getName(),
                        storedObjectPaths.experimentAttachmentPath(experiment.getCreator().getId(), input.getId()).getPath()
                );
            }
        }));

        //noinspection unchecked
        final ImmutableList<FileDataTemplate> files = from(experiment.getRawFiles().getData()).transform(new Function<ExperimentFileTemplate, FileDataTemplate>() {
            @Override
            public FileDataTemplate apply(ExperimentFileTemplate input) {
                //noinspection unchecked
                final ImmutableList<ConditionDataTemplate> conditions = from(input.getConditions()).transform(new Function<Condition, ConditionDataTemplate>() {
                    @Override
                    public ConditionDataTemplate apply(Condition input) {
                        return new ConditionDataTemplate(input.getId(), input.getName(), experimentName);
                    }
                }).toList();
                return transformFileDataWithConditions(input.getFileMetaData(), conditions);
            }
        }).toList();


        return new ExperimentDownloadDataTemplate(
                creatorId,
                experimentName,
                experimentDescription,
                projectName,
                specie,
                experimentType,
                allow2dLc,
                instrumentName,
                attachments,
                files
        );

    }


    @Override
    protected ExperimentItemTemplate transformExperimentItem(ExperimentTemplate experimentTemplate) {
        //noinspection unchecked
        return new ExperimentItemTemplate(experimentTemplate.getCreator().getId(), experimentTemplate.getId()
                , from(experimentTemplate.getRawFiles().getData())
                .transform(new Function<ExperimentFileTemplate, Long>() {
                    @Override
                    public Long apply(ExperimentFileTemplate input) {
                        return input.getFileMetaData().getId();
                    }
                }).toSet());
    }
}