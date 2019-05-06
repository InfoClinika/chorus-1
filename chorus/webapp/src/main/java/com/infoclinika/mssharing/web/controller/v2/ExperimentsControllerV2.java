package com.infoclinika.mssharing.web.controller.v2;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.infoclinika.mssharing.model.internal.entity.restorable.ExperimentPrepMethod;
import com.infoclinika.mssharing.model.internal.entity.restorable.NgsExperimentType;
import com.infoclinika.mssharing.model.internal.helper.NgsExportService;
import com.infoclinika.mssharing.model.internal.repository.ExperimentPrepMethodRepository;
import com.infoclinika.mssharing.model.internal.repository.NgsExperimentTypeRepository;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import com.infoclinika.mssharing.web.controller.v2.service.ExperimentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by slava on 6/15/17.
 */
@RestController
@RequestMapping(value = "/v2/experiments")
@Api(description = "Experiment controllers", tags = {"experiments"})
public class ExperimentsControllerV2 {

    @Inject
    private NgsExperimentTypeRepository ngsExperimentTypeRepository;
    @Inject
    private ExperimentPrepMethodRepository experimentPrepMethodRepository;
    @Inject
    private ExperimentService experimentService;

    private static MappingJacksonValue serializeOnlyFields(IgnoredFields objToFilter, String fields) {
        return doSerialization(objToFilter, SimpleBeanPropertyFilter.filterOutAllExcept(fields.split(",")));
    }

    private static MappingJacksonValue serializeAll(IgnoredFields serializedObject) {
        return doSerialization(serializedObject, SimpleBeanPropertyFilter.serializeAll());
    }

    private static MappingJacksonValue doSerialization(IgnoredFields objToFilter, SimpleBeanPropertyFilter filter) {
        MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(objToFilter);
        SimpleFilterProvider filters = new SimpleFilterProvider().addFilter("FieldsFilter", filter);
        filters.addFilter("ExcludeFieldsFilter", objToFilter.getIgnoredFields().isEmpty()
            ? SimpleBeanPropertyFilter.serializeAll()
            : SimpleBeanPropertyFilter.serializeAllExcept(objToFilter.getIgnoredFields().split(",")));

        mappingJacksonValue.setFilters(filters);

        return mappingJacksonValue;
    }

    private static Collection<SampleFileDTO> toSampleFiles(
        Collection<NgsExportService.FileInfo> files,
        boolean isPairedEnd
    ) {
        return files.stream()
            .map(v -> isPairedEnd ? toSampleFileExtDTO((NgsExportService.FileInfoPaired) (v)) : toSampleFileDTO(v))
            .collect(Collectors.toCollection(() -> new ArrayList<>(files.size())));

    }

    private static SampleFileDTO toSampleFileDTO(NgsExportService.FileInfo fi) {
        return new SampleFileDTO(fi.getFilePath(), fi.getSampleName());
    }

    private static SampleFileDTO toSampleFileExtDTO(NgsExportService.FileInfoPaired fiExt) {
        return new SampleFilePairedDTO(fiExt.getFilePath(), fiExt.getSampleName(), fiExt.getPairEnd(), fiExt.getLane());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleAccessDeniedException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(
            ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> anyUnknownException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(
            ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ApiOperation(
        value = "Retrieve experiment information by id, response can contain additional experiment specific fields",
        notes = "Fields can be filtered by parameter fields, like fields=id,name",
        response = ExperimentInfoFilterFieldsDTO.class,
        tags = {"experiments"})
    @RequestMapping(value = "/{experimentId}", method = RequestMethod.GET)
    public MappingJacksonValue getExperimentInfoCertainFields(@PathVariable long experimentId, String fields) {
        ExperimentInfoDTO experimentInfoDTO =
            toExperimentInfoFieldsDTO(experimentService.getExperiment(RichUser.getCurrentUserId(), experimentId));

        return StringUtils.isEmpty(fields) ? serializeAll(experimentInfoDTO)
            : serializeOnlyFields(experimentInfoDTO, fields);
    }

    private NgsExperimentInfoDTO mapExperimentInfoDTO(
        ExperimentService.ExperimentDetails experimentDetails,
        NgsExperimentInfoDTO destination
    ) {
        ExperimentItem experimentItemSource = experimentDetails.getExperimentItem();
        destination.getGeneralInfo().setId(experimentItemSource.id);
        destination.getGeneralInfo().setName(experimentItemSource.name);
        destination.getGeneralInfo().setProjectId(experimentItemSource.project);
        destination.getGeneralInfo().setProjectName(experimentDetails.getExperimentShortInfo().projectName);
        destination.getGeneralInfo().setLaboratory(experimentDetails.getExperimentShortInfo().labName);
        destination.getGeneralInfo().setInstrumentVendor(experimentItemSource.instrumentVendor);
        destination.getGeneralInfo().setTechnologyType(experimentDetails.getStudyTypeName());
        destination.getGeneralInfo().setDescription(experimentItemSource.description);
        destination.getGeneralInfo().setSpecies(experimentDetails.getExperimentShortInfo().species);
        destination.getGeneralInfo().setInstrumentModel(experimentDetails.getInstrumentModelName());
        destination.getGeneralInfo().setInstrument(experimentItemSource.instrumentName);

        destination.getTechnology().setXenograf(experimentItemSource.ngsRelatedInfo.xenograft);
        destination.getTechnology().setMultiplexing(experimentItemSource.ngsRelatedInfo.multiplexing);
        boolean isPairedEnd = experimentItemSource.ngsRelatedInfo.pairedEnd == 2;
        destination.getTechnology().setPairedEnd(isPairedEnd);
        final int experimentType = experimentItemSource.ngsRelatedInfo.experimentType;
        final NgsExperimentType ngsExperimentType = ngsExperimentTypeRepository.findOne(experimentType);
        destination.getTechnology().setNgsExperimentType(ngsExperimentType.getTitle());
        if (experimentItemSource.ngsRelatedInfo.experimentPrepMethod == null) {
            destination.setIgnoredFields("experimentPrepMethod");
        } else {
            ExperimentPrepMethod experimentPrepMethod =
                experimentPrepMethodRepository.findOne(experimentItemSource.ngsRelatedInfo.experimentPrepMethod);
            destination.getTechnology().setExperimentPrepMethod(experimentPrepMethod.getTitle());
        }
        destination.getTechnology().setLibraryPrep(experimentItemSource.ngsRelatedInfo.libraryPrep);

        destination.setFilesToSamples(toSampleFiles(experimentDetails.getSamplesToFiles(), isPairedEnd));

        destination.setSampleToAnnotation(experimentDetails.getSamplesToAnnotations().entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey(),
                e -> e.getValue()
                    .stream()
                    .map(v -> new AnnotationDTO(v.getName(), v.getType().toString(), v.getUnits(), v.getValue()))
                    .collect(Collectors.toList())
                )
            ));
        return destination;
    }

    public NgsExperimentInfoDTO toExperimentInfoFieldsDTO(ExperimentService.ExperimentDetails experimentItemSource) {
        return mapExperimentInfoDTO(experimentItemSource, new NgsExperimentInfoFilterFieldsDTO());
    }

    private interface IgnoredFields {
        String getIgnoredFields();
    }

    @JsonFilter("FieldsFilter")
    public static class ExperimentInfoFilterFieldsDTO extends ExperimentInfoDTO {
    }

    @JsonPropertyOrder(alphabetic = true)
    public static class ExperimentInfoDTO implements IgnoredFields {
        private GeneralInfo generalInfo = new GeneralInfo();
        private Collection<SampleFileDTO> filesToSamples;
        private Map<String, Collection<AnnotationDTO>> sampleToAnnotation;

        @JsonIgnore
        private String ignoredFields = "";

        public ExperimentInfoDTO() {
        }

        public ExperimentInfoDTO(
            GeneralInfo generalInfo,
            Collection<SampleFileDTO> filesToSamples,
            Map<String, Collection<AnnotationDTO>> sampleToAnnotation,
            String ignoredFields
        ) {
            this.generalInfo = generalInfo;
            this.filesToSamples = filesToSamples;
            this.sampleToAnnotation = sampleToAnnotation;
            this.ignoredFields = ignoredFields;
        }

        public GeneralInfo getGeneralInfo() {
            return generalInfo;
        }

        public void setGeneralInfo(GeneralInfo generalInfo) {
            this.generalInfo = generalInfo;
        }

        public Collection<SampleFileDTO> getFilesToSamples() {
            return filesToSamples;
        }

        public void setFilesToSamples(Collection<SampleFileDTO> filesToSamples) {
            this.filesToSamples = filesToSamples;
        }

        public Map<String, Collection<AnnotationDTO>> getSampleToAnnotation() {
            return sampleToAnnotation;
        }

        public void setSampleToAnnotation(Map<String, Collection<AnnotationDTO>> sampleToAnnotation) {
            this.sampleToAnnotation = sampleToAnnotation;
        }

        @Override
        public String getIgnoredFields() {
            return ignoredFields;
        }

        public void setIgnoredFields(String ignoredFields) {
            this.ignoredFields = ignoredFields;
        }

        @JsonPropertyOrder(alphabetic = true)
        private static class GeneralInfo {
            private Long id;
            private String name;
            private Long projectId;
            private String projectName;
            private String laboratory;
            private String instrumentVendor;
            private String technologyType;
            private String description;
            private String species;
            private String instrumentModel;
            private String instrument;

            public GeneralInfo() {
            }

            public GeneralInfo(Long id,
                               String name,
                               Long projectId,
                               String projectName,
                               String laboratory,
                               String instrumentVendor,
                               String technologyType,
                               String description,
                               String species,
                               String instrumentModel,
                               String instrument) {
                this.id = id;
                this.name = name;
                this.projectId = projectId;
                this.projectName = projectName;
                this.laboratory = laboratory;
                this.instrumentVendor = instrumentVendor;
                this.technologyType = technologyType;
                this.description = description;
                this.species = species;
                this.instrumentModel = instrumentModel;
                this.instrument = instrument;
            }

            public Long getId() {
                return id;
            }

            public void setId(Long id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public Long getProjectId() {
                return projectId;
            }

            public void setProjectId(Long projectId) {
                this.projectId = projectId;
            }

            public String getProjectName() {
                return projectName;
            }

            public void setProjectName(String projectName) {
                this.projectName = projectName;
            }

            public String getLaboratory() {
                return laboratory;
            }

            public void setLaboratory(String laboratory) {
                this.laboratory = laboratory;
            }

            public String getInstrumentVendor() {
                return instrumentVendor;
            }

            public void setInstrumentVendor(String instrumentVendor) {
                this.instrumentVendor = instrumentVendor;
            }

            public String getTechnologyType() {
                return technologyType;
            }

            public void setTechnologyType(String technologyType) {
                this.technologyType = technologyType;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public String getSpecies() {
                return species;
            }

            public void setSpecies(String species) {
                this.species = species;
            }

            public String getInstrumentModel() {
                return instrumentModel;
            }

            public void setInstrumentModel(String instrumentModel) {
                this.instrumentModel = instrumentModel;
            }

            public String getInstrument() {
                return instrument;
            }

            public void setInstrument(String instrument) {
                this.instrument = instrument;
            }
        }
    }

    public static class NgsExperimentInfoDTO extends ExperimentInfoDTO {
        private Technology technology = new Technology();

        public NgsExperimentInfoDTO() {
        }

        public NgsExperimentInfoDTO(Technology technology) {
            this.technology = technology;
        }

        public Technology getTechnology() {
            return technology;
        }

        public void setTechnology(Technology technology) {
            this.technology = technology;
        }

        @JsonFilter("ExcludeFieldsFilter")
        @JsonPropertyOrder(alphabetic = true)
        private static class Technology {
            private Boolean xenograf;
            private Boolean multiplexing;
            private Boolean pairedEnd;
            private String ngsExperimentType;
            private String experimentPrepMethod;
            private String libraryPrep;

            public Technology() {
            }

            public Technology(Boolean xenograf,
                              Boolean multiplexing,
                              Boolean pairedEnd,
                              String ngsExperimentType,
                              String experimentPrepMethod,
                              String libraryPrep) {
                this.xenograf = xenograf;
                this.multiplexing = multiplexing;
                this.pairedEnd = pairedEnd;
                this.ngsExperimentType = ngsExperimentType;
                this.experimentPrepMethod = experimentPrepMethod;
                this.libraryPrep = libraryPrep;
            }

            public Boolean getXenograf() {
                return xenograf;
            }

            public void setXenograf(Boolean xenograf) {
                this.xenograf = xenograf;
            }

            public Boolean getMultiplexing() {
                return multiplexing;
            }

            public void setMultiplexing(Boolean multiplexing) {
                this.multiplexing = multiplexing;
            }

            public Boolean getPairedEnd() {
                return pairedEnd;
            }

            public void setPairedEnd(Boolean pairedEnd) {
                this.pairedEnd = pairedEnd;
            }

            public String getNgsExperimentType() {
                return ngsExperimentType;
            }

            public void setNgsExperimentType(String ngsExperimentType) {
                this.ngsExperimentType = ngsExperimentType;
            }

            public String getExperimentPrepMethod() {
                return experimentPrepMethod;
            }

            public void setExperimentPrepMethod(String experimentPrepMethod) {
                this.experimentPrepMethod = experimentPrepMethod;
            }

            public String getLibraryPrep() {
                return libraryPrep;
            }

            public void setLibraryPrep(String libraryPrep) {
                this.libraryPrep = libraryPrep;
            }
        }
    }

    @JsonPropertyOrder(alphabetic = true)
    public static class AnnotationDTO {
        private String name;
        private String type;
        private String units;
        private String value;

        public AnnotationDTO() {
        }

        public AnnotationDTO(String name, String type, String units, String value) {
            this.name = name;
            this.type = type;
            this.units = units;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class SampleFileDTO {
        private String filePath;
        private String sampleName;

        public SampleFileDTO() {
        }

        public SampleFileDTO(String filePath, String sampleName) {
            this.filePath = filePath;
            this.sampleName = sampleName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getSampleName() {
            return sampleName;
        }

        public void setSampleName(String sampleName) {
            this.sampleName = sampleName;
        }
    }

    public static class SampleFilePairedDTO extends SampleFileDTO {
        private Integer pairEnd;
        private Integer lane;

        public SampleFilePairedDTO(
            String filePath,
            String sampleName,
            Integer pairEnd,
            Integer lane
        ) {
            super(filePath, sampleName);
            this.pairEnd = pairEnd;
            this.lane = lane;
        }

        public Integer getPairEnd() {
            return pairEnd;
        }

        public void setPairEnd(Integer pairEnd) {
            this.pairEnd = pairEnd;
        }

        public Integer getLane() {
            return lane;
        }

        public void setLane(Integer lane) {
            this.lane = lane;
        }
    }

    @JsonFilter("FieldsFilter")
    @JsonPropertyOrder( {"generalInfo", "technology", "filesToSamples", "sampleToAnnotation"})
    public static class NgsExperimentInfoFilterFieldsDTO extends NgsExperimentInfoDTO {
    }

}
