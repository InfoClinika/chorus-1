package com.infoclinika.mssharing.model.internal.helper;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.model.helper.ExperimentDownloadHelper;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.helper.items.ChorusExperimentDownloadData;
import com.infoclinika.mssharing.model.helper.items.ChorusFileData;
import com.infoclinika.mssharing.model.helper.items.LockMzData;
import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.PrepToExperimentSample;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate;
import com.infoclinika.mssharing.platform.model.impl.helper.DefaultExperimentDownloadHelper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

@Service("experimentDownloadHelper")
public class ExperimentDownloadHelperImpl extends DefaultExperimentDownloadHelper<ExperimentDownloadHelperTemplate.ExperimentItemTemplate, ChorusExperimentDownloadData, ChorusFileData> implements ExperimentDownloadHelper {

    public static final Function<ExperimentFileTemplate, Long> META_ID_FROM_RAW = new Function<ExperimentFileTemplate, Long>() {
        @Override
        public Long apply(ExperimentFileTemplate input) {
            return input.getFileMetaData().getId();
        }
    };

    private static final Function<LockMz, LockMzData> LOCK_MZ_FUNCTION = new Function<LockMz, LockMzData>() {
        @Override
        public LockMzData apply(LockMz input) {
            return new LockMzData(input.getMass(), input.getCharge());
        }
    };

    private Function<Attachment<User>, AttachmentDataTemplate> attachmentFn(final long creator) {
        return new Function<Attachment<User>, AttachmentDataTemplate>() {
            @Override
            public AttachmentDataTemplate apply(Attachment<User> input) {
                return new AttachmentDataTemplate(input.getId(), input.getName(), storedObjectPaths.experimentAttachmentPath(creator, input.getId()).getPath());
            }
        };
    }

    private final Function<ActiveFileMetaData, ChorusFileData> FILE_FUNCTION = new Function<ActiveFileMetaData, ChorusFileData>() {
        @Override
        public ChorusFileData apply(ActiveFileMetaData input) {
            final Sharing.Type sharingType = fromNullable(fileMetaDataRepository.getSharingTypeThroughExperiment(input.getId())).or(Sharing.Type.PRIVATE);
            return new ChorusFileData(input.getContentId(), input.getArchiveId(), input.getName(), input.isInvalid(), null,
                    input.getInstrument().getLab().getId(), input.getId(),
                    fromNullable(input.getBillLab()).transform(EntityUtil.ENTITY_TO_ID), Transformers.fromSharingType(sharingType));
        }
    };

    @Inject
    Transformers transformers;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private StoredObjectPaths storedObjectPaths;


    @Override
    protected String getPublicDownloadLink(ExperimentTemplate experimentTemplate) {
        return transformers.getPublicDownloadLink((ActiveExperiment) experimentTemplate);
    }

    @Override
    protected String getPrivateDownloadLink(ExperimentTemplate experimentTemplate) {
        return transformers.getPrivateDownloadLink((ActiveExperiment) experimentTemplate);
    }

    @Override
    protected ChorusFileData transformFileData(FileMetaDataTemplate metaDataTemplate) {
        return FILE_FUNCTION.apply((ActiveFileMetaData) metaDataTemplate);
    }

    @Override
    protected ChorusExperimentDownloadData transformExperimentDownloadData(ExperimentTemplate experimentTemplate) {
        final ActiveExperiment experiment = (ActiveExperiment) experimentTemplate;
        final long creatorId = experiment.getCreator().getId();
        final String experimentName = experiment.getName();
        final String experimentDescription = experiment.getExperiment().getDescription();
        final String projectName = experiment.getProject().getName();
        final String specie = experiment.getSpecie().getName();

        final ExperimentType exType = experiment.getExperimentType();

        final String experimentType = exType != null ? exType.getName() : null;
        final boolean allow2dLc = exType != null && exType.allowed2dLC;

        final Instrument instrument = experiment.getInstrumentRestriction().getInstrument();
        final String instrumentName = instrument != null ? instrument.getName() : null;

        final List<LockMzData> lockMasses = newArrayList();
        lockMasses.addAll(Lists.transform(experiment.getLockMasses(), LOCK_MZ_FUNCTION));

        final List<AttachmentDataTemplate> attachments = newArrayList();
        attachments.addAll(Lists.transform(experiment.attachments, attachmentFn(experiment.getCreator().getId())));
        final Sharing sharing = experiment.getProject().getSharing();

        final ImmutableList<FileDataTemplate> files = from(experiment.getRawFiles().getData()).transform(new Function<ExperimentFileTemplate, FileDataTemplate>() {
            @Override
            public ChorusFileData apply(ExperimentFileTemplate input) {

                //noinspection unchecked
                final Set<PrepToExperimentSample> samples = ((RawFile) input).getPreparedSample().getSamples();
                final Set<SampleCondition> fileConditions = newHashSet();
                for (PrepToExperimentSample prepToExperimentSample : samples) {
                    fileConditions.addAll(prepToExperimentSample.getExperimentSample().getSampleConditions());
                }
                final ImmutableList<ConditionDataTemplate> conditions = from(fileConditions).transform(new Function<SampleCondition, ConditionDataTemplate>() {
                    @Override
                    public ConditionDataTemplate apply(SampleCondition input) {
                        return new ConditionDataTemplate(input.getId(), input.getName(), experimentName);
                    }
                }).toList();

                final AbstractFileMetaData fileMetaData = (AbstractFileMetaData) input.getFileMetaData();

                return new ChorusFileData(
                        fileMetaData.getContentId(),
                        fileMetaData.getArchiveId(), fileMetaData.getName(),
                        fileMetaData.isInvalid(),
                        conditions,
                        fileMetaData.getInstrument().getLab().getId(), fileMetaData.getId(), fromNullable(fileMetaData.getBillLab()).transform(EntityUtil.ENTITY_TO_ID),
                        Transformers.fromSharingType(sharing.getType()));
            }
        }).toList();


        return new ChorusExperimentDownloadData(
                creatorId,
                experimentName,
                experimentDescription,
                projectName,
                specie,
                experimentType,
                allow2dLc,
                instrumentName,
                lockMasses,
                attachments,
                files,
                Transformers.fromSharingType(sharing.getType()),
                fromNullable(experiment.getLab() == null ? experiment.getBillLaboratory() : experiment.getLab()).transform(EntityUtil.ENTITY_TO_ID));
    }

    @Override
    protected ExperimentItemTemplate transformExperimentItem(ExperimentTemplate experiment) {

        //noinspection unchecked
        return new ExperimentItemTemplate(experiment.getCreator().getId(), experiment.getId()
                ,from(experiment.getRawFiles().getData())
                .transform(META_ID_FROM_RAW).toSet());
    }


}
