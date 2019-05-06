package com.infoclinika.mssharing.model.internal.helper.isaexport;

import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.isatabmapping.api.ISATabTemplate;
import com.infoclinika.mssharing.isatabmapping.api.dto.*;
import com.infoclinika.mssharing.isatabmapping.api.dto.experiment.ExperimentDTO;
import com.infoclinika.mssharing.isatabmapping.api.dto.experiment.MsExperimentDTO;
import com.infoclinika.mssharing.isatabmapping.api.dto.experiment.NgsExperimentDTO;
import com.infoclinika.mssharing.model.internal.entity.ExperimentSampleType;
import com.infoclinika.mssharing.model.internal.entity.RawFile;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.model.internal.entity.workflow.PrepToExperimentSample;
import com.infoclinika.mssharing.model.internal.helper.isaexport.TmtLabelsMappingProvider.LabelMapping;
import com.infoclinika.mssharing.model.internal.read.ExperimentLabelToExperimentReader;
import com.infoclinika.mssharing.model.internal.read.ExperimentLabelToExperimentReader.ExperimentLabelsDetails;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.ProjectRepository;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.ExtendedShortExperimentFileItem;
import com.infoclinika.mssharing.model.read.ExtendedShortExperimentFileItem.ExperimentShortSampleItem;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.RawFiles;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.InstrumentRestriction;
import com.infoclinika.mssharing.platform.model.InstrumentsDefaults;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ConditionItem;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ExperimentShortInfo;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ShortExperimentFileItem;
import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sergii.ivanov
 */
@Service
public class ISATabTemplateConverter {
    private static final long ADMIN_ID = 1L;
    private static final String FACTOR_VALUE_SEPARATOR = ":";
    private static final Map<ExperimentSampleType, LabelMapping> tmtLabelsMapping = TmtLabelsMappingProvider.get();

    @Inject
    private ProjectRepository projectRepository;

    @Inject
    private ExperimentRepository experimentRepository;

    @Inject
    private DetailsReader detailsReader;

    @Inject
    private ExperimentLabelToExperimentReader experimentLabelToExperimentReader;

    @Inject
    private ChorusPropertiesProvider chorusPropertiesProvider;

    @Transactional
    public ISATabTemplate convertToISATab(final long experimentId) {
        ActiveExperiment experiment = experimentRepository.findOne(experimentId);
        ActiveProject project = projectRepository.findOne(experiment.getProject().getId());
        return new ISATabTemplate.ISATabTemplateBuilder()
            .setProject(convertProjectData(project))
            .setCreator(convertCreator(project))
            .setExperiments(convertExperiments(experiment))
            .build();
    }

    private List<ExperimentDTO> convertExperiments(final ActiveExperiment experiment) {
        List<ExperimentDTO> result = new ArrayList<>();
        result.add(convertExperiment(experiment));
        return result;
    }

    private ExperimentDTO convertExperiment(final ActiveExperiment experiment) {
        final InstrumentRestriction instrumentRestriction = experiment.getInstrumentRestriction();
        final InstrumentModel instrumentModel = instrumentRestriction.getInstrumentModel();
        final String technologyType = instrumentModel.getStudyType().getName();
        final String vendor = instrumentModel.getVendor().getName();
        final String instrument =
            Optional.ofNullable(instrumentRestriction.getInstrument()).map(InstrumentTemplate::getName).orElse("All");
        final boolean is2dls = experiment.getExperiment().is2dLc();

        final int channelsCount = experiment.getChannelsCount();
        final int mixedSamplesCount = experiment.getSampleTypesCount();
        final String groupSpecificParametersType = experiment.getGroupSpecificParametersType();
        final String labelType = experiment.getLabelType();
        final ExperimentLabelsDetails experimentLabelsItem =
            experimentLabelToExperimentReader.readLabelsDetails(experiment.getId());
        final List<SampleDTO> samples = convertSamples(experiment);
        final List<FileDTO> files = convertFiles(experiment);
        final List<String> lightLabels =
            experimentLabelsItem.lightLabels.stream().map(l -> l.name).collect(Collectors.toList());
        final List<String> mediumLabels =
            experimentLabelsItem.mediumLabels.stream().map(l -> l.name).collect(Collectors.toList());
        final List<String> heavyLabels =
            experimentLabelsItem.heavyLabels.stream().map(l -> l.name).collect(Collectors.toList());
        final String downloadLink = getDownloadLinkForExperiment(experiment.getId());
        final String type = experiment.getInstrumentRestriction().getInstrumentModel().getStudyType().getName();

        switch (type) {
            case InstrumentsDefaults.NG_TECHNOLOGY_TYPE:
                return NgsExperimentDTO.builder()
                    .name(experiment.getName())
                    .projectId(experiment.getProject().getId().toString())
                    .description(experiment.getExperiment().getDescription())
                    .technologyType(technologyType)
                    .vendor(vendor)
                    .instrumentModel(instrument)
                    .species(experiment.getSpecie().getName())
                    .samples(samples)
                    .files(files)
                    .downloadLink(downloadLink)
                    .type(type)
                    .ngsExperimentType(experiment.getNgsRelatedData().getNgsExperimentType().toString())
                    .multiplexing(experiment.getNgsRelatedData().isMultiplexing())
                    .pairedEnd(experiment.getNgsRelatedData().getPairedEnd())
                    .rnaSelection(experiment.getNgsRelatedData().getExperimentPrepMethod().toString())
                    .xenograft(experiment.getNgsRelatedData().isXenograft())
                    .libraryPrep(experiment.getNgsRelatedData().getLibraryPrep().toString())
                    .build();
            case InstrumentsDefaults.MS_TECHNOLOGY_TYPE:
                return MsExperimentDTO.builder()
                    .name(experiment.getName())
                    .projectId(experiment.getProject().getId().toString())
                    .description(experiment.getExperiment().getDescription())
                    .technologyType(technologyType)
                    .vendor(vendor)
                    .instrumentModel(instrument)
                    .species(experiment.getSpecie().getName())
                    .samples(samples)
                    .files(files)
                    .downloadLink(downloadLink)
                    .type(type)
                    .experimentType(experiment.getExperimentType().getName())
                    .is2dlc(is2dls)
                    .isLabeled(mixedSamplesCount > 0)
                    .groupSpecificParametersType(groupSpecificParametersType)
                    .labelType(labelType)
                    .mixedSamples(mixedSamplesCount)
                    .lightLabels(lightLabels)
                    .intermediateLabels(mediumLabels)
                    .heavyLabels(heavyLabels)
                    .channels(channelsCount)
                    .build();
            default:
                throw new RuntimeException("Unsupported experiment type {" + type + "}");
        }
    }

    private List<FileDTO> convertFiles(final ActiveExperiment experiment) {
        final RawFiles rawFiles = experiment.getRawFiles();
        final List<FileDTO> files = new LinkedList<>();
        for (Object experimentFile : rawFiles.getData()) {
            final RawFile rawFile = (RawFile) experimentFile;
            final AbstractFileMetaData fileMetaData = rawFile.getFileMetaData();
            final String downloadLinkForFile = getDownloadLinkForFile(fileMetaData);
            final String fileName = fileMetaData.getName();

            final Set<PrepToExperimentSample> samples = rawFile.getPreparedSample().getSamples();
            final int fractionNumber = rawFile.getFractionNumber();

            // add a separate file record for each sample
            for (PrepToExperimentSample sample : samples) {

                final String label =
                    transformSampleLabel(sample.getType(), experiment.getLabelType(), experiment.getChannelsCount());

                files.add(new FileDTO.Builder()
                    .setName(fileName)
                    .setSampleName(sample.getExperimentSample().getName())
                    .setSampleLabel(label)
                    .setFractionNumber(fractionNumber)
                    .setDownloadLink(downloadLinkForFile)
                    .build());
            }
        }
        return files;
    }

    private String getDownloadLinkForFile(final FileMetaDataTemplate file) {
        return chorusPropertiesProvider.getBaseUrl() + "/download/directDownload?file=" + file.getId();
    }

    private String getDownloadLinkForExperiment(final long experimentId) {
        return chorusPropertiesProvider.getBaseUrl() + "/download/bulk?experiment=" + experimentId;
    }

    private static String transformSampleLabel(ExperimentSampleType label, String labelType, int channelsCount) {
        if (tmtLabelsMapping.containsKey(label)) {

            final LabelMapping labelMapping = tmtLabelsMapping.get(label);
            if ("ITRAQ".equals(labelType.toUpperCase())) {
                return labelMapping.getItraq().get(String.valueOf(channelsCount));
            }

            return labelMapping.getTmt().get(String.valueOf(channelsCount));

        } else {
            return label.name();
        }
    }

    private List<SampleDTO> convertSamples(final ActiveExperiment experiment) {
        final ExperimentShortInfo experimentShortInfo =
            detailsReader.readExperimentShortInfo(ADMIN_ID, experiment.getId());

        final Set<ExperimentShortSampleItem> sampleToConditionMap = new HashSet<>();
        for (ShortExperimentFileItem file : experimentShortInfo.files) {
            final ExtendedShortExperimentFileItem extendedFile = (ExtendedShortExperimentFileItem) file;
            final ImmutableList<ExperimentShortSampleItem> samples = extendedFile.samples;
            for (ExperimentShortSampleItem sample : samples) {
                sampleToConditionMap.add(sample);
            }
        }

        final LinkedList<SampleDTO> samples = new LinkedList<>();
        for (ExperimentShortSampleItem sample : sampleToConditionMap) {
            samples.add(new SampleDTO.Builder().setName(sample.name)
                .setFactors(parseFactors(sample.condition))
                .setAnnotations(convertAnnotations(sample.annotations))
                .build());
        }

        return samples;
    }

    private ArrayList<AnnotationDTO> convertAnnotations(Collection<DetailsReaderTemplate.AnnotationItem> annotations) {
        return annotations.stream()
            .map(v -> AnnotationDTO.builder().name(v.name).value(v.value).build())
            .collect(Collectors.toCollection(() -> new ArrayList<>(annotations.size())));
    }

    private List<FactorDTO> parseFactors(final ConditionItem condition) {
        final LinkedList<FactorDTO> factors = new LinkedList<>();
        final String[] factorValuePairs = condition.name.split(",");
        for (String factorValuePair : factorValuePairs) {
            if (factorValuePair.contains(FACTOR_VALUE_SEPARATOR)) {
                final String[] nameValue = factorValuePair.split(FACTOR_VALUE_SEPARATOR);
                factors.add(new FactorDTO.Builder().setName(nameValue[0]).setValue(nameValue[1]).build());
            }
        }

        factors.sort(Comparator.comparing(FactorDTO::getName));

        return factors;
    }

    private ContactDTO convertCreator(final ActiveProject project) {

        final UserTemplate creator = project.getCreator();
        if (creator != null) {
            return new ContactDTO.Builder()
                .setFirstName(creator.getFirstName())
                .setLastName(creator.getLastName())
                .setEmail(creator.getEmail())
                .build();

        } else {
            return new ContactDTO.Builder().build();
        }
    }

    private ProjectDTO convertProjectData(final ActiveProject project) {
        return new ProjectDTO.Builder()
            .setName(project.getName())
            .setDescription(project.getDescription())
            .setAreaOfResearch(project.getAreaOfResearch())
            .build();
    }
}
