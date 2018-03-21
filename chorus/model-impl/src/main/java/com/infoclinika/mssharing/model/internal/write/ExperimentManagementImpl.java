package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.ExperimentLabelsInfo;
import com.infoclinika.mssharing.model.helper.ExperimentSampleItem;
import com.infoclinika.mssharing.model.helper.ExperimentSampleTypeItem;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.NgsExperimentType;
import com.infoclinika.mssharing.model.internal.entity.restorable.NgsRelatedData;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.FeaturesRepository;
import com.infoclinika.mssharing.model.internal.repository.LabRepository;
import com.infoclinika.mssharing.model.write.AttachmentManagement;
import com.infoclinika.mssharing.model.write.ExperimentInfo;
import com.infoclinika.mssharing.model.write.FileItem;
import com.infoclinika.mssharing.model.write.NgsRelatedExperimentInfo;
import com.infoclinika.mssharing.platform.entity.ExperimentFileTemplate;
import com.infoclinika.mssharing.platform.entity.FactorTemplate;
import com.infoclinika.mssharing.platform.entity.RawFiles;
import com.infoclinika.mssharing.platform.model.InvalidFactorException;
import com.infoclinika.mssharing.platform.model.impl.write.DefaultExperimentManagement;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState.ENABLED;
import static com.infoclinika.mssharing.model.internal.read.Transformers.LOCK_MZ_FUNCTION;

/**
 * @author : Alexander Serebriyan
 */
@Component
public class ExperimentManagementImpl extends DefaultExperimentManagement<ActiveExperiment, ExperimentInfo> {

    private static final Logger LOG = Logger.getLogger(ExperimentManagementImpl.class);
    @Inject
    private FeaturesRepository featuresRepository;
    @Inject
    private LabRepository labRepository;
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private AttachmentManagement attachmentManagement;
    @Inject
    private ExperimentLabelToExperimentManagement experimentLabelToExperimentManagement;
    @Inject
    private ExperimentManagerWithSamplesSupport<ExperimentInfo, ActiveExperiment> experimentManagerWithSamplesSupport;

    @Override
    protected ActiveExperiment onCreateExperiment(long actor, ExperimentInfo experimentInfo) {
        ActiveExperiment experiment = experimentManager.createWithoutSaving(actor, experimentInfo);
        return experimentManager.saveExperiment(createSetExperimentPropsFn(experimentInfo).apply(experiment));
    }

    @Override
    protected void beforeCreateExperiment(long actor, ExperimentInfo experimentInfo) {
        super.beforeCreateExperiment(actor, experimentInfo);

        if (featuresRepository.get().get(ApplicationFeature.BILLING.getFeatureName()).getEnabledState().equals(ENABLED) && experimentInfo.lab == null && experimentInfo.billLab != null) {
            checkArgument(labRepository.findOne(experimentInfo.billLab) != null, "Unknown billing laboratory. Requested bill lab id: " + experimentInfo.billLab);
        }

        checkExperimentLabels(experimentInfo.experimentType, experimentInfo.experimentLabels);
        checkExperimentSamplesAndItsFactors(experimentInfo);

    }

    //    assigning samples to experiment, assigning factors to samples
    @Override
    protected ActiveExperiment updateExperimentFilesOnCreateExperiment(ExperimentInfo info, ActiveExperiment experiment, Function<ExperimentFileTemplate, ExperimentFileTemplate> setFilePropsFn, Function<FactorTemplate, FactorTemplate> setFactorPropsFn) {
        experimentManagerWithSamplesSupport.updateExperimentFilesWithFactorsAndSamples(info, experiment);
        return experiment;
    }

    private static void checkExperimentSamplesAndItsFactors(ExperimentInfo experimentInfo) {

        Set<ExperimentSampleItem> samples = newHashSet();
        for (FileItem file : experimentInfo.files) {
            final Set<ExperimentSampleTypeItem> sampleTypesForFile = newHashSet();
            for (ExperimentSampleItem sample : file.preparedSample.samples) {
                sampleTypesForFile.add(sample.type);
            }
            if (experimentInfo.channelsCount == 0 && experimentInfo.sampleTypesCount != sampleTypesForFile.size()) {//handle case for no labeled experiment but with single sample
                checkArgument(experimentInfo.sampleTypesCount == 0 && sampleTypesForFile.size() == 1,
                        "Sample groups specified with labels doesn't correlate with specified in files, should be different types: %s",
                        experimentInfo.sampleTypesCount);
            }
            samples.addAll(file.preparedSample.samples);
        }
        checkArgument(!samples.isEmpty(), "Can't create experiment without samples.");

        final List<String> firstSamplesFactors = samples.iterator().next().factorValues;
        checkArgument(firstSamplesFactors.size() == experimentInfo.factors.size(), "Factor values array differs from factors used in experiment");
        for (MetaFactorTemplate factor : experimentInfo.factors) {
            if (Strings.isNullOrEmpty(factor.name) || factor.name.trim().isEmpty()) {
                throw new InvalidFactorException("No name was specified for meta factor");
            }
        }


        final HashMultimap<ExperimentSampleTypeItem, String> sampleNamesBySampleType = HashMultimap.create();
        final Set<ExperimentSampleTypeItem> sampleTypes = newHashSet();
        for (ExperimentSampleItem sample : samples) {
            sampleTypes.add(sample.type);
            sampleNamesBySampleType.put(sample.type, sample.name);
            checkArgument(firstSamplesFactors.size() == sample.factorValues.size(), "Factor values array differs from sample to sample.");
            //factors values shouldn't be empty
            for (String factorValue : sample.factorValues) {
                checkArgument(!Strings.isNullOrEmpty(factorValue), "Factor value can't be null");
            }
        }
        if (experimentInfo.channelsCount == 0 && experimentInfo.sampleTypesCount != sampleTypes.size()) {
            checkArgument(experimentInfo.sampleTypesCount == 0 && sampleTypes.size() == 1,
                    "Sample groups specified with labels doesn't correlate with specified in samples");
        }

        checkArgument(sampleNamesBySampleType.values().size() == samples.size(), "Samples can't intersect across groups");

    }


    private void checkExperimentLabels(long experimentType, ExperimentLabelsInfo experimentLabels) {
        if (experimentLabels.lightLabels.isEmpty() && experimentLabels.mediumLabels.isEmpty() && experimentLabels.heavyLabels.isEmpty()) {
            return;
        }
        checkArgument(experimentTypeRepository.findOne(experimentType).labelsAllowed,
                "Can't create an experiment with labels with such experiment type %s", experimentType);
    }

    protected Predicate<AnnotationTemplate> isAnnotationPresent(final String fractionNumber) {
        return new Predicate<AnnotationTemplate>() {
            @Override
            public boolean apply(AnnotationTemplate input) {
                return input.name.equals(fractionNumber) && !isNullOrEmpty(input.value);
            }
        };
    }

    private Function<ActiveExperiment, ActiveExperiment> createSetExperimentPropsFn(final ExperimentInfo info) {
        return new Function<ActiveExperiment, ActiveExperiment>() {
            @Override
            public ActiveExperiment apply(ActiveExperiment experiment) {

                experiment.setBounds(info.bounds);
                experiment.setSampleTypesCount(info.sampleTypesCount);
                experiment.setChannelsCount(info.channelsCount);
                experiment.setLabelType(info.labelType);
                experiment.setNgsRelatedData(extractNgsRelatedData(info));
                experiment.setGroupSpecificParametersType(info.groupSpecificParametersType);
                experiment.setReporterMassTol(info.reporterMassTol);
                experiment.setFilterByPIFEnabled(info.filterByPIFEnabled);
                experiment.setMinReporterPIF(info.minReporterPIF);
                experiment.setMinBasePeakRatio(info.minBasePeakRatio);
                experiment.setMinReporterFraction(info.minReporterFraction);
                experiment.setExperimentCategory(info.experimentCategory);
                experiment.getLockMasses().clear();
                experiment.getLockMasses().addAll(transform(info.lockMasses, LOCK_MZ_FUNCTION));
                final Feature.FeatureState featureState = featuresRepository.get().get(ApplicationFeature.BILLING.getFeatureName()).getEnabledState();
                switch (featureState) {
                    case DISABLED:
                        return experiment;
                    case ENABLED:
                    case ENABLED_PER_LAB:
                        if (info.billLab != null) {
                            experiment.setBillLaboratory(labRepository.findOne(info.billLab));
                        } else {
                            return experiment; //In case if user is not a member of any Lab
                        }
                }

                experiment.setExperimentCategory(info.experimentCategory);
                return experiment;
            }
        };
    }

    private static NgsRelatedData extractNgsRelatedData(final ExperimentInfo info) {
        final NgsRelatedExperimentInfo ngsRelatedInfo = info.ngsRelatedExperimentInfo;
        final String experimentTypeString = ngsRelatedInfo != null ? ngsRelatedInfo.experimentType : "";
        final NgsExperimentType ngsExperimentType = StringUtils.isEmpty(experimentTypeString) ? null : NgsExperimentType.valueOf(experimentTypeString);
        final boolean multiplexing = ngsRelatedInfo != null && ngsRelatedInfo.multiplexing;
        return new NgsRelatedData(multiplexing, ngsExperimentType);
    }


    @Override
    protected void beforeDeleteExperiment(long actor, long experiment) {

        super.beforeDeleteExperiment(actor, experiment);

        attachmentManagement.updateExperimentAnnotationAttachment(actor, experiment, null);//remove annotation attachment

        experimentLabelToExperimentManagement.deleteExperimentLabels(experiment);
    }

    @Override
    protected ActiveExperiment onUpdateExperiment(long experimentId, ExperimentInfo experimentInfo) {
        ActiveExperiment updatedExperiment = experimentManager.updateExperiment(experimentId, experimentInfo);
        return experimentManager.saveExperiment(createSetExperimentPropsFn(experimentInfo).apply(updatedExperiment));
    }

    @Override
    protected void updateExperimentFilesOnUpdateExperiment(ExperimentInfo experimentInfo, ActiveExperiment activeExperiment, Function<FactorTemplate, FactorTemplate> setFactorsPropsFn, Function<ExperimentFileTemplate, ExperimentFileTemplate> setRawFilesPropsFn) {

        experimentManagerWithSamplesSupport.updateExperimentFilesWithFactorsAndSamples(experimentInfo, activeExperiment);
    }

    @Override
    protected void afterUpdateExperiment(long actor, ExperimentInfo experimentInfo, ActiveExperiment activeExperiment) {
        experimentLabelToExperimentManagement.persistExperimentLabels(activeExperiment.getId(), experimentInfo.experimentLabels);
    }

    @Override
    protected ActiveExperiment afterCreateExperiment(ActiveExperiment activeExperiment, ExperimentInfo experimentInfo) {

        final ActiveExperiment result = super.afterCreateExperiment(activeExperiment, experimentInfo);

        experimentLabelToExperimentManagement.persistExperimentLabels(result.getId(), experimentInfo.experimentLabels);
        return result;
    }

    @Override
    protected void beforeUpdateExperiment(long actor, long experimentId, ExperimentInfo experimentInfo) {
        super.beforeUpdateExperiment(actor, experimentId, experimentInfo);
        checkExperimentLabels(experimentInfo.experimentType, experimentInfo.experimentLabels);
        checkExperimentSamplesAndItsFactors(experimentInfo);
    }

    @Override
    protected boolean wereExperimentFilesChanged(long experimentId, long newProject, Restriction restriction, List<MetaFactorTemplate> newFactors, List<FileItemTemplate> newFiles) {
        final boolean result = super.wereExperimentFilesChanged(experimentId, newProject, restriction, newFactors, newFiles);
        if (result) {
            return result;
        }
        boolean samplesOrFractionsChanged = false;

        final HashMultimap<Long, ExperimentSampleItem> newFileToSamples = HashMultimap.create();
        for (FileItemTemplate newFile : newFiles) {
            for (ExperimentSampleItem sample : ((FileItem) newFile).preparedSample.samples) {
                newFileToSamples.put(newFile.id, sample);
            }
        }
        final ImmutableMap<Long, FileItemTemplate> fileIdToNewFile = Maps.uniqueIndex(newFiles, new Function<FileItemTemplate, Long>() {
            @Override
            public Long apply(FileItemTemplate flie) {
                return flie.id;
            }
        });
        final ActiveExperiment experiment = experimentRepository.findOne(experimentId);
        for (RawFile file : experiment.getRawFiles().getData()) {
            if (samplesOrFractionsChanged) {
                break;
            }
            final FileItem fileItem = (FileItem) fileIdToNewFile.get(file.getFileMetaData().getId());
            if (fileItem == null || fileItem.fractionNumber != file.getFractionNumber() || !fileItem.preparedSample.name.equals(file.getPreparedSample().getName())) {
                samplesOrFractionsChanged = true;
            }
            final Set<ExperimentSampleItem> newSamples = newFileToSamples.get(file.getFileMetaData().getId());
            if (newSamples.size() != file.getPreparedSample().getSamples().size()) {
                samplesOrFractionsChanged = true;
            }
            final ImmutableMap<String, ExperimentSampleItem> newSampleNameWithTypeToSamples = Maps.uniqueIndex(newSamples, new Function<ExperimentSampleItem, String>() {
                @Override
                public String apply(ExperimentSampleItem sample) {
                    return composeSampleUniqueKey(sample.name, Transformers.AS_SAMPLE_TYPE.apply(sample.type));
                }
            });

            for (PrepToExperimentSample prepToExperimentSample : file.getPreparedSample().getSamples()) {
                final ExperimentSample sample = prepToExperimentSample.getExperimentSample();
                final ExperimentSampleItem sampleToCompare = newSampleNameWithTypeToSamples.get(composeSampleUniqueKey(sample.getName(), prepToExperimentSample.getType()));
                if (!isSampleEquals(prepToExperimentSample, sampleToCompare)) {
                    samplesOrFractionsChanged = true;
                }

            }
        }
        return samplesOrFractionsChanged;
    }

    private static String composeSampleUniqueKey(String sampleName, ExperimentSampleType type) {
        return sampleName + type;
    }

    private static boolean isSampleEquals(PrepToExperimentSample prepToExperimentSample, ExperimentSampleItem sampleToCompare) {
        if (sampleToCompare == null) {
            return false;
        } else {
            final ExperimentSample sample = prepToExperimentSample.getExperimentSample();
            final String[] oldFactorValues = sample.getFactorValues().toArray(new String[sample.getFactorValues().size()]);
            final String[] newFactorValues = sampleToCompare.factorValues.toArray(new String[sampleToCompare.factorValues.size()]);
            if (Transformers.AS_SAMPLE_TYPE.apply(sampleToCompare.type) != prepToExperimentSample.getType()
                    || !Arrays.equals(oldFactorValues, newFactorValues)) {
                return false;
            }
        }
        return true;
    }

}
