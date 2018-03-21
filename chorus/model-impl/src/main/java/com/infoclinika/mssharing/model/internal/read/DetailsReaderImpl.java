/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.infoclinika.mssharing.model.helper.LockMzItem;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.AnnotationAttachment;
import com.infoclinika.mssharing.model.internal.entity.ExperimentSample;
import com.infoclinika.mssharing.model.internal.entity.Factor;
import com.infoclinika.mssharing.model.internal.entity.Group;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.PrepToExperimentSample;
import com.infoclinika.mssharing.model.internal.entity.RawFile;
import com.infoclinika.mssharing.model.internal.entity.SampleCondition;
import com.infoclinika.mssharing.model.internal.entity.Util;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.model.internal.entity.restorable.NgsRelatedData;
import com.infoclinika.mssharing.model.internal.repository.AnnotationAttachmentRepository;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.RawFilesRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.ExtendedShortExperimentFileItem;
import com.infoclinika.mssharing.model.read.ExtendedShortExperimentFileItem.ExperimentShortSampleItem;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.model.read.dto.details.InstrumentItem;
import com.infoclinika.mssharing.model.read.dto.details.ProjectItem;
import com.infoclinika.mssharing.model.read.dto.details.ShortFileWithConditions;
import com.infoclinika.mssharing.model.write.ExperimentCategory;
import com.infoclinika.mssharing.model.write.NgsRelatedExperimentInfo;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.RawFiles;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.helper.read.SingleResultBuilder;
import com.infoclinika.mssharing.platform.model.helper.read.details.DetailsTransformersTemplate;
import com.infoclinika.mssharing.platform.model.impl.read.DefaultDetailsReader;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.RequestsDetailsReaderTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newTreeSet;
import static com.infoclinika.mssharing.model.internal.read.Transformers.LOCK_MZ_ITEM_FUNCTION;
import static com.infoclinika.mssharing.model.internal.read.Transformers.transformStorageStatus;
import static com.infoclinika.mssharing.platform.entity.EntityUtil.ENTITY_TO_ID;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkAccess;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;
import static java.lang.String.format;

/**
 * @author Stanislav Kurilin
 */
@Service("detailsReader")
@Transactional(readOnly = true)
public class DetailsReaderImpl extends DefaultDetailsReader<ActiveFileMetaData, ActiveProject, ActiveExperiment, Instrument, Lab, Group,
        FileItem, ExperimentItem, ProjectItem, InstrumentItem, DetailsReaderTemplate.LabItemTemplateDetailed,
        DetailsReaderTemplate.GroupItemTemplate>
        implements DetailsReader {

    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private RuleValidator ruleValidator;

    @Inject
    private Transformers transformers;
    @Inject
    private AnnotationAttachmentRepository annotationAttachmentRepository;
    @Inject
    private DetailsTransformersTemplate detailsTransformers;
    @Inject
    private RawFilesRepository filesRepository;
    @Inject
    @Named("requestDetailsReaderImpl")
    private RequestsDetailsReaderTemplate<InstrumentCreationItem, LabItemTemplate> requestsDetailsReader;
    @Inject
    private UserRepository userRepository;
    @Inject
    private ExperimentLabelToExperimentReader experimentLabelToExperimentReader;

    @Override
    public ExperimentItem transformExperiment(ActiveExperiment experiment) {
        return experimentHelper.getTransformer().apply(experiment);
    }

    @Override
    protected ExperimentItem afterReadExperiment(final long actor, SingleResultBuilder<ActiveExperiment, ExperimentItem> resultBuilder) {

        return resultBuilder.transform(new Function<ActiveExperiment, ExperimentItem>() {

            @Override
            public ExperimentItem apply(ActiveExperiment experiment) {

                final ExperimentItemTemplate byDefault = experimentHelper.getDefaultTransformer().apply(experiment);
                final String msChartsLink = transformers.getChartsLink(experiment);
                final Set<ExperimentSample> samples = newTreeSet(new Ordering<ExperimentSample>() {
                    @Override
                    public int compare(ExperimentSample o1, ExperimentSample o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                for (RawFile rawFile : experiment.getRawFiles().getData()) {
                    for (PrepToExperimentSample prepToExperimentSample : rawFile.getPreparedSample().getSamples()) {
                        samples.add(prepToExperimentSample.getExperimentSample());
                    }

                }

                final String[][] factorValues = new String[samples.size()][];
                int sampleCounter = 0;
                for (ExperimentSample experimentSample : samples) {
                    factorValues[sampleCounter] = experimentSample.getFactorValues().toArray(new String[experimentSample.getFactorValues().size()]);
                    sampleCounter++;
                }
                final List<String> samplesInOrder = newArrayList(Collections2.transform(samples, new Function<ExperimentSample, String>() {
                    @Override
                    public String apply(ExperimentSample sample) {
                        return sample.getName();
                    }
                }));

                final NgsRelatedData ngsRelatedData = experiment.getNgsRelatedData();
                final NgsRelatedExperimentInfo ngsRelatedInfo = ngsRelatedData != null ?
                        new NgsRelatedExperimentInfo(
                                ngsRelatedData.getNgsExperimentTypeName(),
                                ngsRelatedData.isMultiplexing(),
                                samples.size()
                        ) : null;

                return new ExperimentItem(
                        byDefault,
                        fromNullable(experiment.getBillLaboratory()).transform(ENTITY_TO_ID).orNull(),
                        experiment.getBounds(),
                        newArrayList(transform(experiment.getLockMasses(), LOCK_MZ_ITEM_FUNCTION)),
                        msChartsLink,
                        experiment.getExperiment().is2dLc(),
                        experimentLabelToExperimentReader.readLabels(experiment.getId()),
                        experiment.getSampleTypesCount(),
                        samplesInOrder,
                        factorValues,
                        experiment.getChannelsCount(),
                        experiment.getLabelType(),
                        experiment.getGroupSpecificParametersType(),
                        experiment.getReporterMassTol(),
                        experiment.isFilterByPIFEnabled(),
                        experiment.getMinReporterPIF(),
                        experiment.getMinBasePeakRatio(),
                        experiment.getMinReporterFraction(),
                        ngsRelatedInfo
                );
            }
        });
    }

    @Override
    public LabItemTemplate readLabRequestDetails(long actor, long labCreationRequestId) {
        return requestsDetailsReader.readLabRequestDetails(actor, labCreationRequestId);
    }

    @Override
    public FileItem readFileDetailsWithConditions(long actor, final long fileId, long experimentId) {

        checkAccess(ruleValidator.userHasReadPermissionsOnFilePredicate(actor).apply(Util.FILE_FROM_ID.apply(fileId)),
                "User has no permissions to read file");
        final RawFile rawFile = checkPresence(filesRepository.findByFileAndExperiment(fileId, experimentId),
                format("RawFile not found for given file {%d} and experiment {%d}", fileId, experimentId));

        return (FileItem) detailsTransformers.experimentFileTransformer().apply(rawFile);
    }

    @Override
    public List<ShortFileWithConditions> readFilesAndConditionsForExperiment(long actor, long experimentId) {
        //todo[tymchenko]: security
        final ActiveExperiment experiment = experimentRepository.findOne(experimentId);

        final RawFiles<Factor, RawFile> files = experiment.getRawFiles();
        final List<RawFile> filesData = files.getData();

        final List<ShortFileWithConditions> result = new ArrayList<>(filesData.size());

        for (RawFile rawFile : filesData) {
            final Set<SampleCondition> fileConditions = newHashSet();
            for (PrepToExperimentSample prepToExperimentSample : rawFile.getPreparedSample().getSamples()) {
                fileConditions.addAll(prepToExperimentSample.getExperimentSample().getSampleConditions());
            }
            final ImmutableList<ConditionItem> conditions = from(fileConditions).filter(new Predicate<SampleCondition>() {
                @Override
                public boolean apply(SampleCondition condition) {
                    return condition.getExperiment().getId().equals(experiment.getId());
                }
            }).transform(new Function<SampleCondition, ConditionItem>() {
                @Override
                public ConditionItem apply(SampleCondition input) {
                    return new ConditionItem(input.getId(), input.getName(), experiment.getName());
                }
            }).toList();

            final Set<String> conditionNames = new HashSet<String>();
            for (ConditionItem item : conditions) {
                conditionNames.addAll(Arrays.asList(item.name.split(", ")));
            }

            final String joinedConditions = Joiner.on(", ").join(conditionNames);
            final AbstractFileMetaData fileMetaData = rawFile.getFileMetaData();
            final ShortFileWithConditions shortFileWithConditions = new ShortFileWithConditions(fileMetaData.getId(), fileMetaData.getName(), joinedConditions);
            result.add(shortFileWithConditions);
        }
        return result;
    }

    @Override
    protected Function<ExperimentFileTemplate, ? extends ShortExperimentFileItem> shortInfoFileTransformer(ActiveExperiment experiment) {
        return new Function<ExperimentFileTemplate, ExtendedShortExperimentFileItem>() {
            @Override
            public ExtendedShortExperimentFileItem apply(ExperimentFileTemplate input) {
                final ImmutableList.Builder<ExperimentShortSampleItem> samples = ImmutableList.builder();
                final Function<SampleCondition, ConditionItem> conditionItemTransformer = sampleConditionTransformer(experiment);
                for (PrepToExperimentSample prepToExperimentSample : ((RawFile) input).getPreparedSample().getSamples()) {
                    final ExperimentSample sample = prepToExperimentSample.getExperimentSample();
                    final SampleCondition sampleCondition = sample.getSampleConditions().isEmpty() ? SampleCondition.createUndefinedCondition(
                            experiment, ImmutableList.of(sample)) : sample.getSampleConditions().iterator().next();
                    samples.add(new ExperimentShortSampleItem(sample.getId(), sample.getName(), prepToExperimentSample.getType().name(), conditionItemTransformer.apply(sampleCondition)));
                }
                final FileMetaDataTemplate fileMetaData = input.getFileMetaData();
                return new ExtendedShortExperimentFileItem(fileMetaData.getId(), fileMetaData.getName(), samples.build());
            }

            private Function<SampleCondition, ConditionItem> sampleConditionTransformer(final ActiveExperiment experiment) {
                return new Function<SampleCondition, ConditionItem>() {
                    @Override
                    public ConditionItem apply(SampleCondition input) {
                        return new ConditionItem(input.getId(), input.getName(), experiment.getName());
                    }
                };
            }
        };
    }

    @Override
    public List<ShortExperimentFileItem> readFilesInOtherExperiments(long actor, long experiment) {

        return from(experimentRepository.filesInOtherExperiments(experiment))
                .transform(new Function<ActiveFileMetaData, ShortExperimentFileItem>() {
                    @Override
                    public ShortExperimentFileItem apply(ActiveFileMetaData input) {
                        return new ShortExperimentFileItem(input.getId(), input.getName(), ImmutableList.<ConditionItem>of(), ImmutableList.<AnnotationItem>of());
                    }
                })
                .toList();
    }

    @Override
    public AttachmentsReaderTemplate.AttachmentItem readExperimentAnnotationAttachment(long actor, long experimentID) {
        if (!ruleValidator.isUserCanReadExperiment(actor, experimentID))
            throw new AccessDenied("Can't read experiment");
        final AnnotationAttachment annotationAttachment = annotationAttachmentRepository.findByExperiment(experimentID);
        if (annotationAttachment == null) return null;
        return new AttachmentsReaderTemplate.AttachmentItem(annotationAttachment.getId(), annotationAttachment.getName(), annotationAttachment.getSizeInBytes(), annotationAttachment.getUploadDate(), annotationAttachment.getOwner().getId());
    }

    @Override
    public AttachmentsReaderTemplate.AttachmentItem readAnnotationAttachmentDetails(long actor, long annotationAttachmentId) {
        final AnnotationAttachment annotationAttachment = checkPresence(annotationAttachmentRepository.findOne(annotationAttachmentId));
        return new AttachmentsReaderTemplate.AttachmentItem(annotationAttachment.getId(), annotationAttachment.getName(), annotationAttachment.getSizeInBytes(), annotationAttachment.getUploadDate(), annotationAttachment.getOwner().getId());
    }

    @Override
    protected InstrumentItem afterReadInstrument(long actor, SingleResultBuilder<InstrumentRepositoryTemplate.AccessedInstrument<Instrument>, InstrumentItem> instrumentItemBuilder) {

        final InstrumentItem transformed = instrumentItemBuilder.transform();

        return new InstrumentItem(transformed,
                transformed.lockMasses,
                transformed.hplc
        );

    }

    @Override
    public InstrumentCreationItem readInstrumentCreation(long actor, long request) {

        return requestsDetailsReader.readInstrumentCreation(actor, request);

    }

    @Override
    public FileItem transformFile(ActiveFileMetaData activeFileMetaData) {
        final FileItemTemplate defaultTransformed = fileHelper.getDefaultTransformer().apply(activeFileMetaData);
        final DashboardReader.StorageStatus storageStatus = transformStorageStatus(activeFileMetaData.getStorageData().getStorageStatus(),
                activeFileMetaData.getStorageData().isArchivedDownloadOnly());
        return new FileItem(defaultTransformed, activeFileMetaData.getArchiveId(), storageStatus, 0, null);
    }

    @Override
    public ProjectItem transformProject(ActiveProject activeProject) {
        final ProjectItemTemplate defaultTransformed = projectHelper.getDefaultTransformer().apply(activeProject);
        return new ProjectItem(defaultTransformed, activeProject.isBlogEnabled());
    }

    @Override
    public InstrumentItem transformInstrument(InstrumentRepositoryTemplate.AccessedInstrument<Instrument> accessedInstrument) {

        final Instrument instrument = accessedInstrument.instrument;
        final InstrumentItemTemplate instrumentItemTemplate = instrumentHelper.getDefaultTransformer().apply(accessedInstrument);
        final ImmutableList<LockMzItem> lockMasses = from(transform(instrument.getLockMasses(), LOCK_MZ_ITEM_FUNCTION)).toList();

        return new InstrumentItem(instrumentItemTemplate, lockMasses, instrument.getHplc());
    }

    @Override
    public LabItemTemplateDetailed transformLab(Lab lab) {
        return labHelper.getDefaultTransformer().apply(lab);
    }

    @Override
    public GroupItemTemplate transformGroup(Group file) {
        return groupHelper.getDefaultTransformer().apply(file);
    }

    @Override
    public ExperimentShortInfoDetailed readExperimentShortInfo(long actor, long experimentId) {
        final ExperimentShortInfo experimentShortInfo = super.readExperimentShortInfo(actor, experimentId);
        final ExperimentCategory experimentCategory = experimentRepository.findOne(experimentId).getExperimentCategory();
        return new ExperimentShortInfoDetailed(experimentShortInfo, experimentCategory);
    }


}
