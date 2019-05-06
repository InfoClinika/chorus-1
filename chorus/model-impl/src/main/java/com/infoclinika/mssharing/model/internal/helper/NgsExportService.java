package com.infoclinika.mssharing.model.internal.helper;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.infoclinika.mssharing.model.internal.entity.ExperimentSample;
import com.infoclinika.mssharing.model.internal.entity.RawFile;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractExperiment;
import com.infoclinika.mssharing.model.internal.entity.workflow.PrepToExperimentSample;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.internal.s3client.AWSConfigService;
import com.infoclinika.mssharing.platform.entity.AnnotationTemplate;
import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.propertiesprovider.AmazonPropertiesProvider;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by slava.nekhaienko on 5/12/17.
 */
@Service
@Transactional(readOnly = true)
public class NgsExportService {
    private final RuleValidator ruleValidator;
    private final ExperimentRepository experimentRepository;
    private final AWSConfigService awsConfigService;
    private final AmazonPropertiesProvider amazonPropertiesProvider;

    @Inject
    public NgsExportService(RuleValidator ruleValidator,
                            ExperimentRepository experimentRepository,
                            AWSConfigService awsConfigService, AmazonPropertiesProvider amazonPropertiesProvider) {
        this.ruleValidator = ruleValidator;
        this.experimentRepository = experimentRepository;
        this.awsConfigService = awsConfigService;
        this.amazonPropertiesProvider = amazonPropertiesProvider;
    }

    public String getCsvText(long userId, long experimentId) {

        AbstractExperiment experiment = getExperiment(userId, experimentId);

        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer);

        List<String> header = getItemNames();
        csvWriter.writeNext(header.toArray(new String[header.size()]), false);

        for (List<String> row : getRows(experiment, "")) {
            csvWriter.writeNext(row.toArray(new String[row.size()]), false);
        }

        return writer.toString();
    }

    public String getCsvFileName(long userId, long experimentId) {
        return replaceForbiddenPathCharacters(getExperiment(userId, experimentId).getName());
    }

    public List<Map<String, String>> getRowsForJson(Long userId, Long experimentId) {
        AbstractExperiment experiment = getExperiment(userId, experimentId);
        List<String> names = getItemNames();
        List<List<String>> rows = getRows(experiment, null);

        return rows.stream().map(
            v -> IntStream.range(0, names.size()).boxed().collect(
                LinkedHashMap<String, String>::new,
                (mm, vv) -> mm.put(names.get(vv), v.get(vv)),
                LinkedHashMap<String, String>::putAll
            ))
            .collect(Collectors.toList());
    }

    private List<String> getItemNames() {
        return Arrays.asList(
            "vendor",
            "vendor_id",
            "vendor_project_name",
            "celgene_id",
            "DA_project_id",
            "celgene_project_desc",
            "experiment",
            "display_name",
            "cell_type",
            "cell_line",
            "tissue",
            "condition",
            "condition1",
            "xenograft",
            "time_treatment",
            "response_desc",
            "response_desc1",
            "response",
            "response1",
            "compound",
            "compound1",
            "dose",
            "dose1",
            "biological_replicates_group",
            "technical_replicates_group",
            "technology",
            "library_prep",
            "rna_selection",
            "antibody",
            "antibody_target",
            "reference_genome",
            "host_genome",
            "stranded",
            "paired_end",
            "filename",
            "file_id"
        );
    }

    private List<List<String>> getRows(AbstractExperiment experiment, String emptyValue) {
        Map<ExperimentSample, Collection<RawFile>> sampleFilesMap = mapFilesToSamples(experiment);

        List<List<String>> result = new ArrayList<>();

        for (Map.Entry<ExperimentSample, Collection<RawFile>> entrySet : sampleFilesMap.entrySet()) {
            ExperimentSample experimentSample = entrySet.getKey();
            result.add(Arrays.asList(
                Optional.ofNullable(experiment.getLab()).map(LabTemplate::getName).orElseGet(() -> emptyValue),
                // "vendor"
                findAnnotation(experimentSample, "vendor_id", emptyValue),
                experiment.getProject().getName(),
                String.valueOf(entrySet.getKey().getName()),
                emptyValue,
                experiment.getProject().getDescription(),
                experiment.getName(),
                findAnnotation(experimentSample, "display_name", emptyValue),
                findFactor(experimentSample, "cell_type", emptyValue),
                findAnnotation(experimentSample, "cell_line", emptyValue),
                findFactor(experimentSample, "tissue", emptyValue),
                findAnnotation(experimentSample, "condition", emptyValue),
                findAnnotation(experimentSample, "condition1", emptyValue),
                getXwnograf(experiment),
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                emptyValue,
                getS3Urls(entrySet.getValue().stream().map(v -> v.getFileMetaData()).collect(Collectors.toList())),
                getIds(entrySet.getValue().stream().map(v -> v.getFileMetaData().getId()).collect(Collectors.toList()))
            ));
        }

        return result;
    }

    public List<FileInfo> mapFilesToSamples(long userId, long experimentId) {
        AbstractExperiment experiment = getExperiment(userId, experimentId);

        List<RawFile> experimentFiles = experiment.getRawFiles().getData();
        return experimentFiles.stream().map(v -> isPairEnd(experiment) ? toFileInfoExt(v) : toFileInfo(v))
            .collect(Collectors.toCollection(() -> new ArrayList<>(experimentFiles.size())));
    }

    private Map<ExperimentSample, Collection<RawFile>> mapFilesToSamples(AbstractExperiment experiment) {
        Map<ExperimentSample, Collection<RawFile>> result = new HashMap<>();

        List<RawFile> experimentFiles = experiment.getRawFiles().getData();

        for (RawFile r : experimentFiles) {
            for (PrepToExperimentSample prepToExperimentSample : r.getPreparedSample().getSamples()) {
                ExperimentSample experimentSample = prepToExperimentSample.getExperimentSample();
                Collection<RawFile> files = result.computeIfAbsent(experimentSample, k -> new ArrayList<>());
                files.add(r);
            }
        }

        return result;
    }

    private boolean isPairEnd(AbstractExperiment experiment) {
        return experiment.getNgsRelatedData().getPairedEnd() == 2;

    }

    private FileInfo toFileInfo(RawFile rawFile) {
        return new FileInfo(rawFile.getFileMetaData().getContentId(), rawFile.getPreparedSample().getName());
    }

    private FileInfoPaired toFileInfoExt(RawFile rawFile) {
        return new FileInfoPaired(
            rawFile.getFileMetaData().getContentId(),
            rawFile.getPreparedSample().getName(),
            rawFile.getPairedEnd(),
            rawFile.getFractionNumber()
        );
    }

    public Map<String, Collection<Annotation>> getAnnotationsBySample(long userId, long experimentId) {
        AbstractExperiment experiment = getExperiment(userId, experimentId);

        Set<ExperimentSample> sampleFileMap = getAllSamples(experiment);

        return sampleFileMap.stream().collect(Collectors.toMap(
            v -> v.getName(),
            v -> v.getAnnotationValues()
                .stream()
                .map(annt -> new Annotation(annt.getName(), annt.getType(), annt.getUnits(), annt.getValue()))
                .collect(Collectors.toList())
        ));
    }

    private Set<ExperimentSample> getAllSamples(AbstractExperiment experiment) {
        Set<ExperimentSample> result = new HashSet<>();

        List<RawFile> experimentFiles = experiment.getRawFiles().getData();

        for (RawFile r : experimentFiles) {
            for (PrepToExperimentSample prepToExperimentSample : r.getPreparedSample().getSamples()) {
                ExperimentSample experimentSample = prepToExperimentSample.getExperimentSample();
                result.add(experimentSample);
            }
        }

        return result;
    }

    private String findAnnotation(ExperimentSample experimentSample, String annotation, String emptyValue) {
        return experimentSample.getAnnotationValues().stream()
            .filter(v -> annotation.compareToIgnoreCase(v.getName()) == 0)
            .map(AnnotationTemplate::getValue)
            .findAny()
            .orElseGet(() -> emptyValue);
    }

    private String findFactor(ExperimentSample experimentSample, String factor, String emptyValue) {
        char keyValueSeparator = ':';
        int skipKey = (factor + keyValueSeparator).length();

        return experimentSample.getSampleConditions().stream()
            .flatMap(v -> Stream.of(v.getName().split(", "))
                .filter(sp -> StringUtils.startsWithIgnoreCase(sp, factor + keyValueSeparator)))
            .map(v -> v.substring(skipKey, v.length()))
            .findAny()
            .orElseGet(() -> emptyValue);
    }

    private String getXwnograf(AbstractExperiment experiment) {
        return Optional.ofNullable(experiment.getNgsRelatedData().isXenograft()).orElse(false) ? "YES" : "NO";
    }

    private String getS3Urls(Collection<FileMetaDataTemplate> files) {
        return files.stream()
            .map(f -> getS3TemporaryUrl(getBucketOf(f), f.getContentId()))
            .collect(Collectors.joining(","));
    }

    private String getBucketOf(FileMetaDataTemplate file) {
        return StringUtils.isEmpty(file.getBucket()) ? amazonPropertiesProvider.getActiveBucket() : file.getBucket();
    }

    private String getIds(Collection<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private String getS3TemporaryUrl(String bucketName, String objectKey) {
        Date expiration = new Date();
        long msec = expiration.getTime();
        msec += 24_000 * 60 * 60;
        expiration.setTime(msec);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
            new GeneratePresignedUrlRequest(bucketName, objectKey);
        generatePresignedUrlRequest.setMethod(HttpMethod.GET);
        generatePresignedUrlRequest.setExpiration(expiration);

        URL s = awsConfigService.s3Client().generatePresignedUrl(generatePresignedUrlRequest);
        return s.toString();
    }

    private String replaceForbiddenPathCharacters(final String str) {
        return str.replaceAll("[/,*?<>|\":\\\\]+", "_");
    }

    private AbstractExperiment getExperiment(long userId, long experimentId) {
        AbstractExperiment experiment = experimentRepository.findOne(experimentId);
        if (experiment == null) {
            throw new NoSuchElementException("Failed to find experiment {" + experimentId + '}');
        }

        if (!ruleValidator.isUserCanReadExperiment(userId, experimentId)) {
            throw new AccessDenied("User {" + userId + "} doesn't have access to experiment" + experimentId + '}');
        }

        return experiment;
    }

    public class Annotation {
        private String name;
        private AnnotationTemplate.Type type;
        private String units;
        private String value;

        public Annotation() {
        }

        public Annotation(
            String name,
            AnnotationTemplate.Type type,
            String units,
            String value
        ) {
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

        public AnnotationTemplate.Type getType() {
            return type;
        }

        public void setType(AnnotationTemplate.Type type) {
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

    public class FileInfo {
        private String filePath;
        private String sampleName;

        public FileInfo() {
        }

        public FileInfo(String filePath, String sampleName) {
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

    public class FileInfoPaired extends FileInfo {
        private Integer pairEnd;
        private Integer lane;

        public FileInfoPaired(String filePath, String sampleName, Integer pairEnd, Integer lane) {
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
}
