package com.infoclinika.mssharing.web.controller.v2.service;

import com.infoclinika.mssharing.model.internal.helper.NgsExportService;
import com.infoclinika.mssharing.model.internal.helper.NgsExportService.Annotation;
import com.infoclinika.mssharing.model.internal.helper.NgsExportService.FileInfo;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.InstrumentModelReaderTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate.*;
import static com.infoclinika.mssharing.platform.model.read.InstrumentModelReaderTemplate.InstrumentModelLineTemplate;

/**
 * Created by slava on 6/16/17.
 */
@Service
public class ExperimentService {

    @Inject
    private InstrumentCreationHelperTemplate<PotentialOperator> instrumentCreationHelperAdapter;
    @Inject
    private DetailsReader detailsReader;
    @Inject
    private NgsExportService ngsExportService;

    @Inject
    @Named("defaultInstrumentModelsReaderAdapter")
    private InstrumentModelReaderTemplate<InstrumentModelLineTemplate> instrumentModelReader;

    public ExperimentDetails getExperiment(long userId, long experimentId) {

        final ExperimentItem experimentItem = detailsReader.readExperiment(userId, experimentId);
        return new ExperimentDetails(
            experimentItem,
            detailsReader.readExperimentShortInfo(userId, experimentId),
            instrumentModelReader.readById(userId, experimentItem.instrumentModel).name,
            instrumentCreationHelperAdapter.studyTypes()
                .stream()
                .filter(i -> i.id == experimentItem.technologyType)
                .findAny()
                .get().name,
            ngsExportService.mapFilesToSamples(userId, experimentId),
            ngsExportService.getAnnotationsBySample(userId, experimentId)
        );
    }

    public static class ExperimentDetails {
        private ExperimentItem experimentItem;
        private DetailsReaderTemplate.ExperimentShortInfo experimentShortInfo;
        private String instrumentModelName;
        private String studyTypeName;
        private Collection<FileInfo> samplesToFiles;
        private Map<String, Collection<Annotation>> samplesToAnnotations;

        public ExperimentDetails(
            ExperimentItem experimentItem,
            DetailsReaderTemplate.ExperimentShortInfo experimentShortInfo,
            String instrumentModelName,
            String studyTypeName,
            Collection<FileInfo> samplesToFiles,
            Map<String, Collection<Annotation>> samplesToAnnotations
        ) {
            this.experimentItem = experimentItem;
            this.experimentShortInfo = experimentShortInfo;
            this.instrumentModelName = instrumentModelName;
            this.studyTypeName = studyTypeName;
            this.samplesToFiles = samplesToFiles;
            this.samplesToAnnotations = samplesToAnnotations;
        }

        public ExperimentItem getExperimentItem() {
            return experimentItem;
        }

        public void setExperimentItem(ExperimentItem experimentItem) {
            this.experimentItem = experimentItem;
        }

        public DetailsReaderTemplate.ExperimentShortInfo getExperimentShortInfo() {
            return experimentShortInfo;
        }

        public void setExperimentShortInfo(DetailsReaderTemplate.ExperimentShortInfo experimentShortInfo) {
            this.experimentShortInfo = experimentShortInfo;
        }

        public String getInstrumentModelName() {
            return instrumentModelName;
        }

        public void setInstrumentModelName(String instrumentModelName) {
            this.instrumentModelName = instrumentModelName;
        }

        public String getStudyTypeName() {
            return studyTypeName;
        }

        public void setStudyTypeName(String studyTypeName) {
            this.studyTypeName = studyTypeName;
        }

        public Collection<FileInfo> getSamplesToFiles() {
            return samplesToFiles;
        }

        public void setSamplesToFiles(Collection<FileInfo> samplesToFiles) {
            this.samplesToFiles = samplesToFiles;
        }

        public Map<String, Collection<Annotation>> getSamplesToAnnotations() {
            return samplesToAnnotations;
        }

        public void setSamplesToAnnotations(Map<String, Collection<Annotation>> samplesToAnnotations) {
            this.samplesToAnnotations = samplesToAnnotations;
        }
    }
}
