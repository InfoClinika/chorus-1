package com.infoclinika.mssharing.model.test.processing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.helper.ExperimentLabelsInfo;
import com.infoclinika.mssharing.model.helper.ExperimentSampleItem;
import com.infoclinika.mssharing.model.internal.entity.ExperimentSample;
import com.infoclinika.mssharing.model.internal.entity.ProcessingFile;
import com.infoclinika.mssharing.model.internal.read.ProcessingRunReader;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.write.AnalysisBounds;
import com.infoclinika.mssharing.model.write.ExperimentInfo;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.infoclinika.mssharing.model.helper.ExperimentSampleTypeItem.HEAVY;
import static com.infoclinika.mssharing.model.helper.ExperimentSampleTypeItem.LIGHT;
import static com.infoclinika.mssharing.model.helper.ExperimentSampleTypeItem.MEDIUM;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


public class ManagingProcessingRunTest extends AbstractProcessingTest{

    @Test
    public void testUpdateProcessingRunAfterFirstCreation(){
        final long user = uc.createLab3AndBob();
        final long experiment = createExperimentWithOneRawFile(user, uc.getLab3());

        final long instrument = instrumentFromExperimentFile(user, experiment);
        final long file0 = uc.saveFile(user, instrument);
        final long file1 = uc.saveFile(user, instrument);

        final ExperimentSampleItem sample0 = sampleWithFactors(file0, of("1"));
        final ExperimentSampleItem sample1 = sampleWithFactors(file1, of("2"));
        addFilesToExperiment(user, experiment, of(factor(experiment)), of(
                new com.infoclinika.mssharing.model.write.FileItem(file0, false, 0, preparedSample(file0, ImmutableSet.of(sample0))),
                new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, ImmutableSet.of(sample1)))), of("3"));

        final ExperimentItem experimentItem = detailsReader.readExperiment(user, experiment);
        List<Long> processingFilesList = createMultiProcessingFiles(experimentItem);
        Map<String, Collection<String>> map = createFileToFileMap(experimentItem, processingFilesList);

        processingFileManagement.associateProcessingFileWithRawFile(map, new HashMap<>(),experiment, user, "UpdateProcessingRun");
        ProcessingRunReader.ProcessingRunInfo processingRunInfo = processingRunReader.readProcessingRunByNameAndExperiment(experiment, "UpdateProcessingRun");

        assertEquals(processingFilesList.size(), processingRunInfo.processingFiles.size());
        assertEquals("UpdateProcessingRun", processingRunInfo.name);

        generateFilesToExperiment(user, instrument, experiment);

        final ExperimentItem experimentItems = detailsReader.readExperiment(user, experiment);
        List<Long> newProcessingFiles = createMultiProcessingFiles(experimentItems);
        Map<String, Collection<String>> newAssociationFilesMap = createFileToFileMap(experimentItems, newProcessingFiles);

        processingFileManagement.associateProcessingFileWithRawFile(newAssociationFilesMap, new HashMap<>(),experiment, user, "UpdateProcessingRun");
        ProcessingRunReader.ProcessingRunInfo processingRun = processingRunReader.readProcessingRunByNameAndExperiment(experiment,"UpdateProcessingRun");

        assertTrue(processingRun.processingFiles.size() == 8);
        assertEquals(processingRunInfo.id, processingRun.id);
    }


    @Test
    public void createProcessingRunWithAssociateFiles(){

        final long user = uc.createLab3AndBob();
        final long experiment = createExperimentWithOneRawFile(user, uc.getLab3());

        final long instrument = instrumentFromExperimentFile(user, experiment);
        final long file0 = uc.saveFile(user, instrument);
        final long file1 = uc.saveFile(user, instrument);

        final ExperimentSampleItem sample0 = sampleWithFactors(file0, of("1"));
        final ExperimentSampleItem sample1 = sampleWithFactors(file1, of("2"));
        addFilesToExperiment(user, experiment, of(factor(experiment)), of(
                new com.infoclinika.mssharing.model.write.FileItem(file0, false, 0, preparedSample(file0, ImmutableSet.of(sample0))),
                new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, ImmutableSet.of(sample1)))), of("3"));

        final ExperimentItem experimentItem = detailsReader.readExperiment(user, experiment);
        List<Long> processingFilesList = createMultiProcessingFiles(experimentItem);
        Map<String, Collection<String>> fileToFileMap = createFileToFileMap(experimentItem, processingFilesList);

        processingFileManagement.associateProcessingFileWithRawFile(fileToFileMap, new HashMap<>(),experiment, user, "ProcessingRun");

        ProcessingRunReader.ProcessingRunInfo processingRunInfo = processingRunReader.readProcessingRunByNameAndExperiment(experiment, "ProcessingRun");

        final long processingRunExperiment = processingRunInfo.abstractExperiment.getId();

        assertEquals(experiment, processingRunExperiment);
        assertEquals(processingFilesList.size(), processingRunInfo.processingFiles.size());
        assertEquals("ProcessingRun", processingRunInfo.name);
    }


    @Test
    public void createNewSamplesFileMapInProcessingRun(){

        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);

        final long experiment = createExperimentWithAllSampleTypesAndFactors(bob, project, file1, file2);
        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);

        DetailsReaderTemplate.ExperimentShortInfo shortInfo = detailsReader.readExperimentShortInfo(bob, experiment);
        List<Long> processingFilesList = createMultiProcessingFiles(experimentItem);
        Map<String, Collection<String>> fileToFileMap = createFileToFileMap(experimentItem, processingFilesList);

        Map<String, Collection<String>> sampleFileMap = createSamplesToFileMap(shortInfo, fileToFileMap);

        processingFileManagement.associateProcessingFileWithRawFile(fileToFileMap, sampleFileMap ,experiment, bob, "ProcessingRunWithSample");

        ProcessingRunReader.ProcessingRunInfo processingRunInfo = processingRunReader.readProcessingRunByNameAndExperiment(experiment, "ProcessingRunWithSample");

        validateSamplesToProcessedFiles(processingRunInfo, sampleFileMap);

    }

    private void validateSamplesToProcessedFiles(ProcessingRunReader.ProcessingRunInfo processingRunInfo, Map<String, Collection<String>> sampleToFile){
        HashMultimap<String, String> sample = extractSampleToFile(processingRunInfo);

        assertEquals(sample.get("sample_Medium_1").size(), 1);
        assertEquals(sample.get("sample_Heavy_1").size(), 1);
        assertTrue(sample.get("sample_Medium_1").contains(sampleToFile.get("sample_Medium_1").iterator().next()));
        assertTrue(sample.get("sample_Heavy_1").contains(sampleToFile.get("sample_Heavy_1").iterator().next()));
    }


    private static HashMultimap<String, String> extractSampleToFile(ProcessingRunReader.ProcessingRunInfo processingRunInfo){

        HashMultimap<String, String> sampleToFiles = HashMultimap.create();

        for(ProcessingFile file : processingRunInfo.processingFiles){
            for(ExperimentSample experimentSample : file.getExperimentSamples()){
                sampleToFiles.put(experimentSample.getName(), file.getName());
            }
        }
        return sampleToFiles;
    }


    private void generateFilesToExperiment(long user, long instrument, long experiment){

        final long file3 = uc.saveFile(user, instrument);
        final long file4 = uc.saveFile(user, instrument);

        final ExperimentSampleItem sample3 = sampleWithFactors(file3, of("sample_one"));
        final ExperimentSampleItem sample4 = sampleWithFactors(file4, of("sample_two"));

        addFilesToExperiment(user, experiment, of(factor(experiment)), of(
                new com.infoclinika.mssharing.model.write.FileItem(file3, false, 0, preparedSample(file3, ImmutableSet.of(sample3))),
                new com.infoclinika.mssharing.model.write.FileItem(file4, false, 0, preparedSample(file4, ImmutableSet.of(sample4)))), of("3"));

    }

    protected ExperimentManagementTemplate.MetaFactorTemplate factor(long experimentId) {
        return new ExperimentManagementTemplate.MetaFactorTemplate(generateString(), "", false, experimentId);
    }




    private long createExperimentWithAllSampleTypesAndFactors(long bob, long project, long file1, long file2) {
        final ExperimentSampleItem sampleLight1 = new ExperimentSampleItem("sample_Light_1", LIGHT, ImmutableList.of("sick", "5"));
        final ExperimentSampleItem sampleLight2 = new ExperimentSampleItem("sample_Light_2", LIGHT, ImmutableList.of("sick", "90"));

        final ExperimentSampleItem sampleMedium1 = new ExperimentSampleItem("sample_Medium_1", MEDIUM, ImmutableList.of("healthy", "5"));
        final ExperimentSampleItem sampleHeavy1 = new ExperimentSampleItem("sample_Heavy_1", HEAVY, ImmutableList.of("healthy", "10"));
        final ExperimentSampleItem sampleHeavy2 = new ExperimentSampleItem("sample_Heavy_2", HEAVY, ImmutableList.of("healthy", "5"));
        final ImmutableSet<ExperimentSampleItem> file1Samples = ImmutableSet.of(sampleLight1, sampleMedium1, sampleHeavy1);
        final ImmutableSet<ExperimentSampleItem> file2Samples = ImmutableSet.of(sampleLight2, sampleMedium1, sampleHeavy2);

        final List<ExperimentManagementTemplate.MetaFactorTemplate> factors = newArrayList(
                new ExperimentManagementTemplate.MetaFactorTemplate("treatment group", null, false, 0),
                new ExperimentManagementTemplate.MetaFactorTemplate("temperature", "C", true, 0));
        final ImmutableList<com.infoclinika.mssharing.model.write.FileItem> files = of(
                new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, file1Samples)),
                new com.infoclinika.mssharing.model.write.FileItem(file2, false, 0, preparedSample(file2, file2Samples))
        );

        final ExperimentInfo.Builder builder = experiment(bob, project).factors(factors)
                .experimentType(experimentTypeLabeled())
                .files(files)
                .experimentLabels(new ExperimentLabelsInfo(ImmutableList.of(getExperimentLabelKAminoAcid()), ImmutableList.of(), ImmutableList.of(getExperimentLabelRAminoAcid())))
                .sampleTypesCount(3);
        return studyManagement.createExperiment(bob, builder.build());
    }

    private ExperimentInfo.Builder experiment(long bob, long project) {
        return experimentInfo()
                .project(project).lab(uc.getLab3()).billLab(uc.getLab3()).is2dLc(false)
                .restriction(restriction(bob)).bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES);
    }
}
