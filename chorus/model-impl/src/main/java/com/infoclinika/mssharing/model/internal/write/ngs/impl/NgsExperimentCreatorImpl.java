package com.infoclinika.mssharing.model.internal.write.ngs.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.infoclinika.mssharing.model.helper.*;
import com.infoclinika.mssharing.model.internal.entity.Instrument;
import com.infoclinika.mssharing.model.internal.entity.restorable.ExperimentPrepMethod;
import com.infoclinika.mssharing.model.internal.entity.restorable.LibraryPrep;
import com.infoclinika.mssharing.model.internal.entity.restorable.NgsExperimentType;
import com.infoclinika.mssharing.model.internal.entity.restorable.NtExtractionMethod;
import com.infoclinika.mssharing.model.internal.repository.ExperimentPrepMethodRepository;
import com.infoclinika.mssharing.model.internal.repository.InstrumentRepository;
import com.infoclinika.mssharing.model.internal.repository.NgsExperimentTypeRepository;
import com.infoclinika.mssharing.model.internal.repository.NtExtractionMethodRepository;
import com.infoclinika.mssharing.model.internal.write.ngs.api.NgsExperimentCreator;
import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentCreateRequest;
import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentTemplateData;
import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentTemplateSampleData;
import com.infoclinika.mssharing.model.write.*;
import com.infoclinika.mssharing.model.write.ngs.NgsExperimentImportException;
import com.infoclinika.mssharing.platform.entity.Dictionary;
import com.infoclinika.mssharing.platform.entity.Species;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.MetaFactorTemplate;
import com.infoclinika.mssharing.platform.repository.ExperimentTypeRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.SpeciesRepositoryTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.model.internal.write.ngs.impl.ExperimentTemplateSampleColumn.*;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author timofei.kasianov 8/6/18
 */
@Component
public class NgsExperimentCreatorImpl implements NgsExperimentCreator {

    private static final String OTHER_EXPERIMENT_TYPE_NAME = "Other";
    private static final String FACTOR_DEFAULT_STRING_VALUE = "Not Set";
    private static final String FACTOR_DEFAULT_LONG_VALUE = "-1";
    private static final boolean DEFAULT_MULTIPLEXING_VALUE = false;
    private static final int DEFAULT_SAMPLES_COUNT_VALUE = 1;

    @Inject
    private StudyManagement studyManagement;
    @Inject
    private SpeciesRepositoryTemplate<Species> speciesRepository;
    @Inject
    private InstrumentRepository instrumentRepository;
    @Inject
    private InstrumentManagement instrumentManagement;
    @Inject
    private NgsExperimentTypeRepository ngsExperimentTypeRepository;
    @Inject
    private ExperimentPrepMethodRepository experimentPrepMethodRepository;
    @Inject
    private NtExtractionMethodRepository ntExtractionMethodRepository;
    @Inject
    private ExperimentTypeRepositoryTemplate experimentTypeRepository;
    @Inject
    private StoredObjectPaths storedObjectPaths;

    @Override
    public long create(long actor, NgsExperimentCreateRequest request) {
        final ExperimentInfo experimentInfo = composeExperimentInfo(actor, request);
        return studyManagement.createExperiment(actor, experimentInfo);
    }

    private ExperimentInfo composeExperimentInfo(long actor, NgsExperimentCreateRequest request) {

        final NgsExperimentTemplateData data = request.getData();
        final long speciesId = obtainSpecies(data);
        final Instrument instrument = instrumentRepository.findOne(request.getInstrumentId());
        final NgsExperimentTemplateSampleData firstSample = data.getSamples().get(0);
        final List<MetaFactorTemplate> factors = obtainFactors(firstSample);
        final List<FileItem> files = createFiles(actor, speciesId, request);
        final NgsRelatedExperimentInfo ngsRelatedExperimentInfo = obtainNgsRelatedInfo(data.getSamples());
        final long otherExperimentTypeId = getOtherExperimentTypeId();

        return new ExperimentInfo.Builder()
            .name(request.getExperimentName())
            .project(request.getProjectId())
            .lab(request.getLabId())
            .lockMasses(new ArrayList<>())
            .experimentCategory(ExperimentCategory.NGS)
            .restriction(new ExperimentManagementTemplate.Restriction(
                instrument.getModel().getStudyType().getId(),
                instrument.getModel().getVendor().getId(),
                instrument.getModel().getType().getId(),
                instrument.getModel().getId(),
                Optional.of(instrument.getId())
            ))
            .factors(factors)
            .files(files)
            .ngsRelatedInfo(ngsRelatedExperimentInfo)
            .bounds(new AnalysisBounds())
            .experimentType(otherExperimentTypeId)
            .specie(speciesId)
            .experimentLabels(new ExperimentLabelsInfo())
            .build();
    }

    private long getOtherExperimentTypeId() {
        final Dictionary otherExperimentType = experimentTypeRepository.findByName(OTHER_EXPERIMENT_TYPE_NAME);
        if (otherExperimentType == null) {
            throw new NgsExperimentImportException("Couldn't get experiment type 'Other'.");
        }
        return otherExperimentType.getId();
    }

    private NgsRelatedExperimentInfo obtainNgsRelatedInfo(List<NgsExperimentTemplateSampleData> samples) {

        final String ngsExperimentTypeName = samples.get(0).getExperimentType();
        final NgsExperimentType ngsExperimentType = ngsExperimentTypeRepository.findByTitle(ngsExperimentTypeName);

        if (ngsExperimentType == null) {
            throw new NgsExperimentImportException(
                "Couldn't get NGS experiment type by name: " + ngsExperimentTypeName
            );
        }

        // the value is not given
        final boolean multiplexing = DEFAULT_MULTIPLEXING_VALUE;
        // the value is not given
        final int samplesCount = DEFAULT_SAMPLES_COUNT_VALUE;

        final Boolean pairedEndFromTemplate = samples.get(0).getPairedEnd();
        // if paired end in a template is not specified on 'false' -> 1, otherwise -> 0
        final int pairedEnd = pairedEndFromTemplate == null || !pairedEndFromTemplate ? 1 : 0;

        final String rnaSelection = getRnaSelection(samples);
        final Integer expPrepMethod;
        if (rnaSelection == null) {
            expPrepMethod = null;
        } else {
            final ExperimentPrepMethod foundPrepMethod = experimentPrepMethodRepository.findByTitle(rnaSelection);
            expPrepMethod = foundPrepMethod == null ?
                null :
                foundPrepMethod.getId();
        }

        final Boolean xenograftFromTemplate = samples.get(0).getXenograft();
        final boolean xenograft = xenograftFromTemplate != null && xenograftFromTemplate;

        final String libraryPrepTitle = samples.get(0).getLibraryPrep();
        final LibraryPrep libraryPrep = LibraryPrep.getByTitle(libraryPrepTitle);

        final String ntExtractionTitle = getNtExtraction(samples);
        final Long ntExtractionId;
        if (ntExtractionTitle == null) {
            ntExtractionId = null;
        } else {
            final NtExtractionMethod foundNtExtraction = ntExtractionMethodRepository.findByTitle(ntExtractionTitle);
            ntExtractionId = foundNtExtraction == null ?
                null :
                foundNtExtraction.getId();
        }

        return new NgsRelatedExperimentInfo(
            ngsExperimentType.getId(),
            multiplexing,
            samplesCount,
            pairedEnd,
            expPrepMethod,
            xenograft,
            libraryPrep.name(),
            ntExtractionId
        );
    }

    private String getNtExtraction(List<NgsExperimentTemplateSampleData> samples) {
        return samples
            .stream()
            .filter(s -> isNotEmpty(s.getNtExtraction()))
            .map(NgsExperimentTemplateSampleData::getNtExtraction)
            .findFirst()
            .orElse(null);
    }

    private String getRnaSelection(List<NgsExperimentTemplateSampleData> samples) {
        return samples
            .stream()
            .filter(s -> isNotEmpty(s.getRnaSelection()))
            .map(NgsExperimentTemplateSampleData::getRnaSelection)
            .findFirst()
            .orElse(null);
    }

    private List<MetaFactorTemplate> obtainFactors(NgsExperimentTemplateSampleData sampleData) {

        final List<MetaFactorTemplate> factors = new LinkedList<>();

        factors.add(new MetaFactorTemplate(CELL_TYPE.getName()));
        factors.add(new MetaFactorTemplate(TISSUE.getName()));
        factors.add(new MetaFactorTemplate(TIME_TREATMENT.getName()));

        final List<Integer> compoundFactorIndexes = Lists.newArrayList(sampleData.getCompounds().keySet());
        Collections.sort(compoundFactorIndexes);
        compoundFactorIndexes.forEach(ind -> {
            final String compoundFactorName = ind == 0 ?
                COMPOUND.getName() :
                COMPOUND.getName() + ind;
            factors.add(new MetaFactorTemplate(compoundFactorName));
        });

        final ArrayList<Integer> doseFactorIndexes = Lists.newArrayList(sampleData.getDoses().keySet());
        Collections.sort(doseFactorIndexes);
        doseFactorIndexes.forEach(ind -> {
            final String doseFactorName = ind == 0 ?
                DOSE.getName() :
                DOSE.getName() + ind;
            factors.add(new MetaFactorTemplate(doseFactorName));
        });

        factors.add(new MetaFactorTemplate(BIOLOGICAL_REPLICATES_GROUP.getName(), "", true, 0));
        factors.add(new MetaFactorTemplate(TECHNICAL_REPLICATES_GROUP.getName(), "", true, 0));

        return factors;
    }

    private long obtainSpecies(NgsExperimentTemplateData data) {
        final String referenceGenome = data.getSamples().get(0).getReferenceGenome();
        final Species species = speciesRepository.findByName(referenceGenome);
        return species != null ?
            species.getId() :
            speciesRepository.getUnspecified().getId();
    }

    private List<FileItem> createFiles(long actor, long speciesId, NgsExperimentCreateRequest request) {

        final List<NgsExperimentTemplateSampleData> samples = request.getData().getSamples();

        return samples
            .stream()
            .map(sample -> {

                final String filename = sample.getFilename();
                String s3Path = storedObjectPaths.rawFilePath(actor, request.getInstrumentId(), filename).getPath();
                final String rawFilesBucket = storedObjectPaths.getRawFilesBucket();
                final FileMetaDataInfo fileMetaDataInfo = new FileMetaDataInfo(
                    filename,
                    0L,
                    null,
                    null,
                    speciesId,
                    false,
                    rawFilesBucket,
                    s3Path,
                    false
                );
                final long fileId = instrumentManagement.createFile(actor, request.getInstrumentId(), fileMetaDataInfo);
                final List<String> sampleFactorValues = getSampleFactorValues(sample);
                final List<AnnotationItem> sampleAnnotationValues = getSampleAnnotationValues(sample);

                return new FileItem(
                    fileId,
                    new ExperimentPreparedSampleItem(
                        filename,
                        Sets.newHashSet(
                            new ExperimentSampleItem(
                                filename,
                                ExperimentSampleTypeItem.LIGHT,
                                sampleFactorValues,
                                sampleAnnotationValues
                            )
                        )
                    )

                );

            })
            .collect(Collectors.toList());
    }

    private List<AnnotationItem> getSampleAnnotationValues(NgsExperimentTemplateSampleData sample) {

        final List<AnnotationItem> annotationValues = new LinkedList<>();

        annotationValues.add(new AnnotationItem(VENDOR.getName(), sample.getVendor(), "", false));
        annotationValues.add(new AnnotationItem(VENDOR_ID.getName(), sample.getVendorId(), "", false));
        annotationValues.add(new AnnotationItem(
            VENDOR_PROJECT_NAME.getName(),
            sample.getVendorProjectName(),
            "",
            false
        ));
        annotationValues.add(new AnnotationItem(CELGENE_ID.getName(), sample.getCelgeneId(), "", false));
        annotationValues.add(new AnnotationItem(DA_PROJECT_ID.getName(), sample.getDaProjectId(), "", false));
        annotationValues.add(new AnnotationItem(
            CELGENE_PROJECT_DESC.getName(),
            sample.getCelgeneProjectDescription(),
            "",
            false
        ));
        annotationValues.add(new AnnotationItem(EXPERIMENT.getName(), sample.getExperiment(), "", false));
        annotationValues.add(new AnnotationItem(DISPLAY_NAME.getName(), sample.getDisplayName(), "", false));
        annotationValues.add(new AnnotationItem(DISPLAY_NAME_SHORT.getName(), sample.getDisplayNameShort(), "", false));
        annotationValues.add(new AnnotationItem(CELL_LINE.getName(), sample.getCellLine(), "", false));

        final ArrayList<Integer> conditionIndexes = Lists.newArrayList(sample.getConditions().keySet());
        Collections.sort(conditionIndexes);
        conditionIndexes.forEach(ind -> {
            final String annotationName = ind == 0 ?
                CONDITION.getName() :
                CONDITION.getName() + ind;
            annotationValues.add(new AnnotationItem(annotationName, sample.getConditions().get(ind), "", false));
        });

        final ArrayList<Integer> responseDescIndexes = Lists.newArrayList(sample.getResponseDescriptions().keySet());
        Collections.sort(responseDescIndexes);
        responseDescIndexes.forEach(ind -> {
            final String annotationName = ind == 0 ?
                RESPONSE_DESC.getName() :
                RESPONSE_DESC.getName() + ind;
            annotationValues.add(new AnnotationItem(
                annotationName,
                sample.getResponseDescriptions().get(ind),
                "",
                false
            ));
        });

        final ArrayList<Integer> responseIndexes = Lists.newArrayList(sample.getResponses().keySet());
        Collections.sort(responseIndexes);
        responseIndexes.forEach(ind -> {
            final String annotationName = ind == 0 ?
                RESPONSE.getName() :
                RESPONSE.getName() + ind;
            annotationValues.add(new AnnotationItem(
                annotationName,
                sample.getResponseDescriptions().get(ind),
                "",
                false
            ));
        });

        annotationValues.add(new AnnotationItem(TECHNOLOGY.getName(), sample.getTechnology(), "", false));
        annotationValues.add(new AnnotationItem(EXOME_BAIT_SET.getName(), sample.getExomeBaitSet(), "", false));
        annotationValues.add(new AnnotationItem(ANTIBODY_TARGET.getName(), sample.getAntibodyTarget(), "", false));
        annotationValues.add(new AnnotationItem(HOST_GENOME.getName(), sample.getHostGenome(), "", false));
        annotationValues.add(new AnnotationItem(STRANDED.getName(), sample.getStranded(), "", false));

        return annotationValues;
    }

    private List<String> getSampleFactorValues(NgsExperimentTemplateSampleData sample) {

        final List<String> factorValues = new LinkedList<>();

        factorValues.add(getValueOrDefaultString(sample.getCellType()));
        factorValues.add(getValueOrDefaultString(sample.getTissue()));
        factorValues.add(getValueOrDefaultString(sample.getTimeTreatment()));

        final ArrayList<Integer> compoundIndexes = Lists.newArrayList(sample.getCompounds().keySet());
        Collections.sort(compoundIndexes);
        compoundIndexes.forEach(ind -> factorValues.add(getValueOrDefaultString(sample.getCompounds().get(ind))));

        final ArrayList<Integer> doseIndexes = Lists.newArrayList(sample.getDoses().keySet());
        Collections.sort(doseIndexes);
        doseIndexes.forEach(ind -> factorValues.add(getValueOrDefaultString(sample.getDoses().get(ind))));

        factorValues.add(getValueOrDefaultLong(sample.getBiologicalReplicatesGroup()));
        factorValues.add(getValueOrDefaultLong(sample.getTechnicalReplicatesGroup()));

        return factorValues;
    }

    private String getValueOrDefaultString(String value) {
        return isNotEmpty(value) ?
            value :
            FACTOR_DEFAULT_STRING_VALUE;
    }

    private String getValueOrDefaultLong(Long value) {
        return value == null ?
            FACTOR_DEFAULT_LONG_VALUE :
            Long.toString(value);
    }
}
