package com.infoclinika.mssharing.model.test.study;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.infoclinika.mssharing.model.helper.ExperimentLabelsInfo;
import com.infoclinika.mssharing.model.helper.ExperimentPreparedSampleItem;
import com.infoclinika.mssharing.model.helper.ExperimentSampleItem;
import com.infoclinika.mssharing.model.read.ExtendedShortExperimentFileItem;
import com.infoclinika.mssharing.model.read.ExtendedShortExperimentFileItem.ExperimentShortSampleItem;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.model.write.AnalysisBounds;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ExperimentShortInfo;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ShortExperimentFileItem;
import com.infoclinika.mssharing.platform.model.read.Filter;
import org.testng.annotations.Test;

import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.model.helper.ExperimentSampleTypeItem.*;
import static com.infoclinika.mssharing.model.write.ExperimentInfo.Builder;
import static com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.MetaFactorTemplate;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author andrii.loboda
 */
public class ExperimentWithSamplesTest extends AbstractStudyTest {


    @Test
    public void createExperimentWithAllSampleTypesAndFactors() {

        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);

        final long experiment = createExperimentWithAllSampleTypesAndFactors(bob, project, file1, file2);

        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);
        final ExperimentShortInfo shortInfo = detailsReader.readExperimentShortInfo(bob, experiment);

        validateExperimentWithAllSamplesAndFactors(file1, file2, experimentItem);
        validateExperimentShortDetails(shortInfo, file1);
    }

    private long createExperimentWithAllSampleTypesAndFactors(long bob, long project, long file1, long file2) {
        final ExperimentSampleItem sampleLight1 =
            new ExperimentSampleItem("sample_Light_1", LIGHT, ImmutableList.of("sick", "5"));
        final ExperimentSampleItem sampleLight2 =
            new ExperimentSampleItem("sample_Light_2", LIGHT, ImmutableList.of("sick", "90"));

        final ExperimentSampleItem sampleMedium1 =
            new ExperimentSampleItem("sample_Medium_1", MEDIUM, ImmutableList.of("healthy", "5"));
        final ExperimentSampleItem sampleHeavy1 =
            new ExperimentSampleItem("sample_Heavy_1", HEAVY, ImmutableList.of("healthy", "10"));
        final ExperimentSampleItem sampleHeavy2 =
            new ExperimentSampleItem("sample_Heavy_2", HEAVY, ImmutableList.of("healthy", "5"));
        final ImmutableSet<ExperimentSampleItem> file1Samples =
            ImmutableSet.of(sampleLight1, sampleMedium1, sampleHeavy1);
        final ImmutableSet<ExperimentSampleItem> file2Samples =
            ImmutableSet.of(sampleLight2, sampleMedium1, sampleHeavy2);

        final List<MetaFactorTemplate> factors = newArrayList(
            new MetaFactorTemplate("treatment group", null, false, 0),
            new MetaFactorTemplate("temperature", "C", true, 0)
        );
        final ImmutableList<com.infoclinika.mssharing.model.write.FileItem> files = of(
            new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, file1Samples)),
            new com.infoclinika.mssharing.model.write.FileItem(file2, false, 0, preparedSample(file2, file2Samples))
        );

        final Builder builder = experiment(bob, project).factors(factors)
            .experimentType(experimentTypeLabeled())
            .files(files)
            .experimentLabels(new ExperimentLabelsInfo(
                ImmutableList.of(getExperimentLabelKAminoAcid()),
                ImmutableList.of(),
                ImmutableList.of(getExperimentLabelRAminoAcid())
            ))
            .sampleTypesCount(3);
        return studyManagement.createExperiment(bob, builder.build());
    }

    @Test
    public void removeExperimentWithAllSampleTypesAndFactorsToTrash() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);

        final long experiment = createExperimentWithAllSampleTypesAndFactors(bob, project, file1, file2);
        final long experimentInTrash = studyManagement.moveExperimentToTrash(bob, experiment);
        final long restoredEx = studyManagement.restoreExperiment(bob, experimentInTrash);

        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, restoredEx);

        validateExperimentWithAllSamplesAndFactors(file1, file2, experimentItem);
    }

    @Test
    public void removeForeverExperimentWithAllSampleTypesAndFactors() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);

        final long experiment = createExperimentWithAllSampleTypesAndFactors(bob, project, file1, file2);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 1);
        studyManagement.deleteExperiment(bob, experiment);
        assertTrue(dashboardReader.readExperiments(bob, Filter.MY).isEmpty());
    }

    @Test
    public void createExperimentWithCommonSamplesAndDifferentTypes() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);


        // prep.sample1: bio-sample-1 bio-sample-1
        // prep.sample2: bio-sample-2 bio-sample-1
        final long experiment = createExperimentWithIntersectedSamples(bob, project, file1, file2);

        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);

        validateCreatedExperimentWithCommonSamples(file1, file2, experimentItem);

    }


    @Test
    public void removeExperimentWithCommonSamplesAndDifferentTypesToTrash() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);

        // prep.sample1: bio-sample-1 bio-sample-1
        // prep.sample2: bio-sample-2 bio-sample-1
        final long experiment = createExperimentWithIntersectedSamples(bob, project, file1, file2);
        final long experimentInTrash = studyManagement.moveExperimentToTrash(bob, experiment);
        final long restoredExperiment = studyManagement.restoreExperiment(bob, experimentInTrash);

        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, restoredExperiment);
        validateCreatedExperimentWithCommonSamples(file1, file2, experimentItem);
    }

    @Test
    public void addFileToExperimentWithCommonSamplesAndDifferentTypes() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);
        final long newFile3 = uc.saveFile(bob);

        final long experiment = createExperimentWithIntersectedSamples(bob, project, file1, file2);

        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);
        final List<com.infoclinika.mssharing.model.write.FileItem> updatedFiles =
            asFileItems(experimentItem.files, null);
        final ImmutableMap<String, ExperimentSampleItem> nameToSample = extractSampleNameToSample(experimentItem);
        updatedFiles.add(new com.infoclinika.mssharing.model.write.FileItem(newFile3, false, 0,
            preparedSample(newFile3, ImmutableSet.of(
                nameToSample.get(composeSampleUniqueKey("bio-sample-1", LIGHT)),
                new ExperimentSampleItem("bio-sample-3", HEAVY, ImmutableList.of("healthy", "5"))
            ))
        ));

        final Builder infoBuilder = new Builder()
            .name(generateString())
            .description(generateString())
            .experimentType(experimentItem.experimentType)
            .specie(experimentItem.specie)
            .project(experimentItem.project)
            .billLab(experimentItem.billLab)
            .is2dLc(false)
            .restriction(restriction(bob))
            .factors(asFactorsTemplates(experimentItem.factors))
            .files(updatedFiles)
            .experimentLabels(new ExperimentLabelsInfo(
                experimentItem.labels.lightLabels,
                experimentItem.labels.mediumLabels,
                experimentItem.labels.heavyLabels
            ))
            .sampleTypesCount(experimentItem.sampleTypesCount)
            .lockMasses(experimentItem.lockMasses);
        //              L               H
        // prep.sample1: bio-sample-1 bio-sample-1
        // prep.sample2: bio-sample-2 bio-sample-1
        // prep.sample3: bio-sample-1 bio-sample-3
        studyManagement.updateExperiment(bob, experiment, infoBuilder.build());

        final ExperimentItem experimentItemUpdated = detailsReader.readExperiment(bob, experiment);

        assertEquals(experimentItemUpdated.factors.size(), 2);
        assertEquals(experimentItemUpdated.sampleTypesCount, 2);
        final HashMultimap<String, Long> samplesToFilesMap = extractSamplesToFileMap(experimentItemUpdated);
        assertEquals(samplesToFilesMap.keySet().size(), 3);
        assertTrue(samplesToFilesMap.get("bio-sample-1").containsAll(newArrayList(file1, file2, newFile3)));
        assertEquals(samplesToFilesMap.get("bio-sample-1").size(), 3);
        assertTrue(samplesToFilesMap.get("bio-sample-2").containsAll(newArrayList(file2)));
        assertEquals(samplesToFilesMap.get("bio-sample-2").size(), 1);
        assertTrue(samplesToFilesMap.get("bio-sample-3").containsAll(newArrayList(newFile3)));
        assertEquals(samplesToFilesMap.get("bio-sample-3").size(), 1);
        assertTrue(Arrays.deepEquals(experimentItemUpdated.factorValues, new String[][] {
            new String[] {"sick", "5"},
            new String[] {"sick", "90"},
            new String[] {"healthy", "5"}
        }));

        final Map<String, ExperimentPreparedSampleItem> preparedSamplesMap =
            extractPreparedSamplesMap(experimentItemUpdated);

        assertEquals(preparedSamplesMap.size(), 3);

        final List<String> prepSamplesNames = newArrayList(preparedSamplesMap.keySet());
        Collections.sort(prepSamplesNames);
        final List<ExperimentSampleItem> firstPrepSamples =
            newArrayList(preparedSamplesMap.get(prepSamplesNames.get(0)).samples);
        Collections.sort(firstPrepSamples, SORT_ASC);
        assertEquals(firstPrepSamples.size(), 2);
        assertEquals(firstPrepSamples.get(0).type, LIGHT);
        assertEquals(firstPrepSamples.get(0).name, "bio-sample-1");
        assertEquals(firstPrepSamples.get(1).type, HEAVY);
        assertEquals(firstPrepSamples.get(1).name, "bio-sample-1");


        final List<ExperimentSampleItem> secondPrepSamples =
            newArrayList(preparedSamplesMap.get(prepSamplesNames.get(1)).samples);
        Collections.sort(secondPrepSamples, SORT_ASC);
        assertEquals(secondPrepSamples.size(), 2);
        assertEquals(secondPrepSamples.get(0).type, HEAVY);
        assertEquals(secondPrepSamples.get(0).name, "bio-sample-1");
        assertEquals(secondPrepSamples.get(1).type, LIGHT);
        assertEquals(secondPrepSamples.get(1).name, "bio-sample-2");

        final List<ExperimentSampleItem> thirdPrepSamples =
            newArrayList(preparedSamplesMap.get(prepSamplesNames.get(2)).samples);
        Collections.sort(thirdPrepSamples, SORT_ASC);
        assertEquals(thirdPrepSamples.size(), 2);
        assertEquals(thirdPrepSamples.get(0).type, LIGHT);
        assertEquals(thirdPrepSamples.get(0).name, "bio-sample-1");
        assertEquals(thirdPrepSamples.get(1).type, HEAVY);
        assertEquals(thirdPrepSamples.get(1).name, "bio-sample-3");
    }


    @Test
    public void addFileToSampleInExperimentWithFactorsAndSamples() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);
        final long newFile3 = uc.saveFile(bob);

        final long experiment = createExperimentWithAllSampleTypesAndFactors(bob, project, file1, file2);
        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);
        final ImmutableMap<String, ExperimentSampleItem> nameToSample = extractSampleNameToSample(experimentItem);
        final List<com.infoclinika.mssharing.model.write.FileItem> updatedFiles =
            asFileItems(experimentItem.files, null);
        updatedFiles.add(new com.infoclinika.mssharing.model.write.FileItem(newFile3, false, 0,
            preparedSample(newFile3, ImmutableSet.of(
                nameToSample.get(composeSampleUniqueKey("sample_Light_1", LIGHT)),
                nameToSample.get(composeSampleUniqueKey("sample_Medium_1", MEDIUM)),
                nameToSample.get(composeSampleUniqueKey("sample_Heavy_1", HEAVY))
                )
            )
        ));

        final Builder infoBuilder = new Builder()
            .name(generateString())
            .description(generateString())
            .experimentType(experimentItem.experimentType)
            .specie(experimentItem.specie)
            .project(experimentItem.project)
            .billLab(experimentItem.billLab)
            .is2dLc(false)
            .restriction(restriction(bob))
            .factors(asFactorsTemplates(experimentItem.factors))
            .files(updatedFiles)
            .experimentLabels(new ExperimentLabelsInfo(
                experimentItem.labels.lightLabels,
                experimentItem.labels.mediumLabels,
                experimentItem.labels.heavyLabels
            ))
            .sampleTypesCount(experimentItem.sampleTypesCount)
            .lockMasses(experimentItem.lockMasses);

        studyManagement.updateExperiment(bob, experiment, infoBuilder.build());

        final ExperimentItem experimentItemUpdated = detailsReader.readExperiment(bob, experiment);


        assertEquals(experimentItemUpdated.factors.size(), 2);
        assertEquals(experimentItemUpdated.sampleTypesCount, 3);
        final HashMultimap<String, Long> samplesToFilesMap = extractSamplesToFileMap(experimentItemUpdated);
        assertEquals(samplesToFilesMap.keySet().size(), 5);
        assertTrue(samplesToFilesMap.get("sample_Medium_1").containsAll(newArrayList(file1, file2, newFile3)));
        assertEquals(samplesToFilesMap.get("sample_Medium_1").size(), 3);
        assertTrue(samplesToFilesMap.get("sample_Light_1").containsAll(newArrayList(file1, newFile3)));
        assertEquals(samplesToFilesMap.get("sample_Light_1").size(), 2);
        assertTrue(samplesToFilesMap.get("sample_Light_2").containsAll(newArrayList(file2)));
        assertEquals(samplesToFilesMap.get("sample_Light_2").size(), 1);
        assertTrue(samplesToFilesMap.get("sample_Heavy_1").containsAll(newArrayList(file1, newFile3)));
        assertEquals(samplesToFilesMap.get("sample_Heavy_1").size(), 2);
        assertTrue(samplesToFilesMap.get("sample_Heavy_2").containsAll(newArrayList(file2)));
        assertEquals(samplesToFilesMap.get("sample_Heavy_2").size(), 1);
        assertTrue(Arrays.deepEquals(experimentItem.factorValues, new String[][] {
            new String[] {"healthy", "10"},
            new String[] {"healthy", "5"},
            new String[] {"sick", "5"},
            new String[] {"sick", "90"},
            new String[] {"healthy", "5"}
        }));
    }


    @Test
    public void addFactorToExperimentWithSamplesAndFactors() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);

        final long experiment = createExperimentWithAllSampleTypesAndFactors(bob, project, file1, file2);
        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);
        final ImmutableMap<String, ExperimentSampleItem> nameToSample = extractSampleNameToSample(experimentItem);
        final Iterator<String> factorValuesForNewFactor = newArrayList("1", "3", "34", "44", "77").iterator();
        nameToSample.get(composeSampleUniqueKey(
            "sample_Heavy_1",
            HEAVY
        )).factorValues.add(factorValuesForNewFactor.next());
        nameToSample.get(composeSampleUniqueKey(
            "sample_Heavy_2",
            HEAVY
        )).factorValues.add(factorValuesForNewFactor.next());
        nameToSample.get(composeSampleUniqueKey(
            "sample_Light_1",
            LIGHT
        )).factorValues.add(factorValuesForNewFactor.next());
        nameToSample.get(composeSampleUniqueKey(
            "sample_Light_2",
            LIGHT
        )).factorValues.add(factorValuesForNewFactor.next());
        nameToSample.get(composeSampleUniqueKey(
            "sample_Medium_1",
            MEDIUM
        )).factorValues.add(factorValuesForNewFactor.next());
        final List<com.infoclinika.mssharing.model.write.FileItem> updatedFiles =
            asFileItems(experimentItem.files, null);

        final List<MetaFactorTemplate> updatedFactors =
            asFactorsTemplates(experimentItem.factors);
        updatedFactors.add(new MetaFactorTemplate("new factor", "KG", true, experiment));
        final Builder infoBuilder = new Builder()
            .name(generateString())
            .description(generateString())
            .experimentType(experimentItem.experimentType)
            .specie(experimentItem.specie)
            .project(experimentItem.project)
            .billLab(experimentItem.billLab)
            .is2dLc(false)
            .restriction(restriction(bob))
            .factors(updatedFactors)
            .files(updatedFiles)
            .experimentLabels(new ExperimentLabelsInfo(
                experimentItem.labels.lightLabels,
                experimentItem.labels.mediumLabels,
                experimentItem.labels.heavyLabels
            ))
            .sampleTypesCount(experimentItem.sampleTypesCount)
            .lockMasses(experimentItem.lockMasses);

        studyManagement.updateExperiment(bob, experiment, infoBuilder.build());

        final ExperimentItem experimentItemUpdated = detailsReader.readExperiment(bob, experiment);


        assertEquals(experimentItemUpdated.factors.size(), 3);
        assertEquals(experimentItemUpdated.sampleTypesCount, 3);
        final HashMultimap<String, Long> samplesToFilesMap = extractSamplesToFileMap(experimentItem);
        assertEquals(samplesToFilesMap.keySet().size(), 5);
        assertTrue(samplesToFilesMap.get("sample_Medium_1").containsAll(newArrayList(file1, file2)));
        assertEquals(samplesToFilesMap.get("sample_Medium_1").size(), 2);
        assertTrue(samplesToFilesMap.get("sample_Light_1").containsAll(newArrayList(file1)));
        assertEquals(samplesToFilesMap.get("sample_Light_1").size(), 1);
        assertTrue(samplesToFilesMap.get("sample_Light_2").containsAll(newArrayList(file2)));
        assertEquals(samplesToFilesMap.get("sample_Light_2").size(), 1);
        assertTrue(samplesToFilesMap.get("sample_Heavy_1").containsAll(newArrayList(file1)));
        assertEquals(samplesToFilesMap.get("sample_Heavy_1").size(), 1);
        assertTrue(samplesToFilesMap.get("sample_Heavy_2").containsAll(newArrayList(file2)));
        assertEquals(samplesToFilesMap.get("sample_Heavy_2").size(), 1);
        assertTrue(Arrays.deepEquals(experimentItemUpdated.factorValues, new String[][] {
            new String[] {"healthy", "10", "1"},
            new String[] {"healthy", "5", "3"},
            new String[] {"sick", "5", "34"},
            new String[] {"sick", "90", "44"},
            new String[] {"healthy", "5", "77"}
        }));
    }

    @Test
    public void addFileWithNewSampleToExperimentWithFactorsAndSamples() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);
        final long newFile3 = uc.saveFile(bob);

        final long experiment = createExperimentWithAllSampleTypesAndFactors(bob, project, file1, file2);
        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);
        final List<com.infoclinika.mssharing.model.write.FileItem> updatedFiles =
            asFileItems(experimentItem.files, null);
        updatedFiles.add(new com.infoclinika.mssharing.model.write.FileItem(
            newFile3,
            false,
            0,
            preparedSample(newFile3, ImmutableSet.of(
                new ExperimentSampleItem("new sample light", LIGHT, newArrayList("healthy", "200")),
                new ExperimentSampleItem("new sample medium", MEDIUM, newArrayList("sick", "90")),
                new ExperimentSampleItem("new sample heavy", HEAVY, newArrayList("healthy", "5"))
                )
            )
        ));

        final Builder infoBuilder = new Builder()
            .name(generateString())
            .description(generateString())
            .experimentType(experimentItem.experimentType)
            .specie(experimentItem.specie)
            .project(experimentItem.project)
            .billLab(experimentItem.billLab)
            .is2dLc(false)
            .restriction(restriction(bob))
            .factors(asFactorsTemplates(experimentItem.factors))
            .files(updatedFiles)
            .experimentLabels(new ExperimentLabelsInfo(
                experimentItem.labels.lightLabels,
                experimentItem.labels.mediumLabels,
                experimentItem.labels.heavyLabels
            ))
            .sampleTypesCount(experimentItem.sampleTypesCount)
            .lockMasses(experimentItem.lockMasses);

        studyManagement.updateExperiment(bob, experiment, infoBuilder.build());

        final ExperimentItem experimentItemUpdated = detailsReader.readExperiment(bob, experiment);


        assertEquals(experimentItemUpdated.factors.size(), 2);
        assertEquals(experimentItemUpdated.sampleTypesCount, 3);
        final HashMultimap<String, Long> samplesToFilesMap = extractSamplesToFileMap(experimentItemUpdated);
        assertEquals(samplesToFilesMap.keySet().size(), 8);
        assertTrue(samplesToFilesMap.get("sample_Medium_1").containsAll(newArrayList(file1, file2)));
        assertEquals(samplesToFilesMap.get("sample_Medium_1").size(), 2);
        assertTrue(samplesToFilesMap.get("sample_Light_1").containsAll(newArrayList(file1)));
        assertEquals(samplesToFilesMap.get("sample_Light_1").size(), 1);
        assertTrue(samplesToFilesMap.get("sample_Light_2").containsAll(newArrayList(file2)));
        assertEquals(samplesToFilesMap.get("sample_Light_2").size(), 1);
        assertTrue(samplesToFilesMap.get("sample_Heavy_1").containsAll(newArrayList(file1)));
        assertEquals(samplesToFilesMap.get("sample_Heavy_1").size(), 1);
        assertTrue(samplesToFilesMap.get("sample_Heavy_2").containsAll(newArrayList(file2)));
        assertEquals(samplesToFilesMap.get("sample_Heavy_2").size(), 1);
        assertTrue(samplesToFilesMap.get("new sample light").containsAll(newArrayList(newFile3)));
        assertEquals(samplesToFilesMap.get("new sample light").size(), 1);
        assertTrue(samplesToFilesMap.get("new sample medium").containsAll(newArrayList(newFile3)));
        assertEquals(samplesToFilesMap.get("new sample medium").size(), 1);
        assertTrue(samplesToFilesMap.get("new sample heavy").containsAll(newArrayList(newFile3)));
        assertEquals(samplesToFilesMap.get("new sample heavy").size(), 1);
        assertTrue(Arrays.deepEquals(experimentItemUpdated.factorValues, new String[][] {
            new String[] {"healthy", "5"},
            new String[] {"healthy", "200"},
            new String[] {"sick", "90"},
            new String[] {"healthy", "10"},
            new String[] {"healthy", "5"},
            new String[] {"sick", "5"},
            new String[] {"sick", "90"},
            new String[] {"healthy", "5"}
        }));
    }

    @Test
    public void addNewSampleTypeToExperimentWithFactorsAndSamples() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);

        final long experiment = createExperimentWithTwoSampleTypesAndFactors(bob, project, file1, file2);
        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);
        final List<com.infoclinika.mssharing.model.write.FileItem> updatedFiles =
            asFileItems(experimentItem.files, null);
        for (com.infoclinika.mssharing.model.write.FileItem updatedFile : updatedFiles) {
            if (updatedFile.id == file1) {
                updatedFile.preparedSample.samples.add(new ExperimentSampleItem(
                    "new heavy sample 1",
                    HEAVY,
                    newArrayList("healthy", "10")
                ));
            } else if (updatedFile.id == file2) {
                updatedFile.preparedSample.samples.add(new ExperimentSampleItem(
                    "new heavy sample 2",
                    HEAVY,
                    newArrayList("healthy", "5")
                ));
            }
        }

        final Builder infoBuilder = new Builder()
            .name(generateString())
            .description(generateString())
            .experimentType(experimentItem.experimentType)
            .specie(experimentItem.specie)
            .project(experimentItem.project)
            .billLab(experimentItem.billLab)
            .is2dLc(false)
            .restriction(restriction(bob))
            .factors(asFactorsTemplates(experimentItem.factors))
            .files(updatedFiles)
            .experimentLabels(new ExperimentLabelsInfo(
                experimentItem.labels.lightLabels,
                experimentItem.labels.mediumLabels,
                experimentItem.labels.heavyLabels
            ))
            .sampleTypesCount(3)
            .lockMasses(experimentItem.lockMasses);

        studyManagement.updateExperiment(bob, experiment, infoBuilder.build());

        final ExperimentItem experimentItemUpdated = detailsReader.readExperiment(bob, experiment);


        assertEquals(experimentItemUpdated.factors.size(), 2);
        assertEquals(experimentItemUpdated.sampleTypesCount, 3);
        final HashMultimap<String, Long> samplesToFilesMap = extractSamplesToFileMap(experimentItemUpdated);
        assertEquals(samplesToFilesMap.keySet().size(), 5);
        assertTrue(samplesToFilesMap.get("sample_Medium_1").containsAll(newArrayList(file1, file2)));
        assertEquals(samplesToFilesMap.get("sample_Medium_1").size(), 2);
        assertTrue(samplesToFilesMap.get("sample_Light_1").containsAll(newArrayList(file1)));
        assertEquals(samplesToFilesMap.get("sample_Light_1").size(), 1);
        assertTrue(samplesToFilesMap.get("sample_Light_2").containsAll(newArrayList(file2)));
        assertEquals(samplesToFilesMap.get("sample_Light_2").size(), 1);
        assertTrue(samplesToFilesMap.get("new heavy sample 1").containsAll(newArrayList(file1)));
        assertEquals(samplesToFilesMap.get("new heavy sample 1").size(), 1);
        assertTrue(samplesToFilesMap.get("new heavy sample 2").containsAll(newArrayList(file2)));
        assertEquals(samplesToFilesMap.get("new heavy sample 2").size(), 1);
        assertTrue(Arrays.deepEquals(experimentItemUpdated.factorValues, new String[][] {
            new String[] {"healthy", "10"},
            new String[] {"healthy", "5"},
            new String[] {"sick", "5"},
            new String[] {"sick", "90"},
            new String[] {"healthy", "5"}
        }));
    }

    @Test
    public void removeSampleTypeToExperimentWithFactorsAndSamples() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);

        final long experiment = createExperimentWithAllSampleTypesAndFactors(bob, project, file1, file2);

        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);
        final List<com.infoclinika.mssharing.model.write.FileItem> updatedFiles =
            asFileItems(
                experimentItem.files,
                sample -> !sample.name.equals("sample_Heavy_1") && !sample.name.equals("sample_Heavy_2")
            );

        final Builder infoBuilder = new Builder()
            .name(generateString())
            .description(generateString())
            .experimentType(experimentItem.experimentType)
            .specie(experimentItem.specie)
            .project(experimentItem.project)
            .billLab(experimentItem.billLab)
            .is2dLc(false)
            .restriction(restriction(bob))
            .factors(asFactorsTemplates(experimentItem.factors))
            .files(updatedFiles)
            .experimentLabels(new ExperimentLabelsInfo(
                experimentItem.labels.lightLabels,
                experimentItem.labels.mediumLabels,
                experimentItem.labels.heavyLabels
            ))
            .sampleTypesCount(2)
            .lockMasses(experimentItem.lockMasses);

        studyManagement.updateExperiment(bob, experiment, infoBuilder.build());

        final ExperimentItem experimentItemUpdated = detailsReader.readExperiment(bob, experiment);


        assertEquals(experimentItemUpdated.factors.size(), 2);
        assertEquals(experimentItemUpdated.sampleTypesCount, 2);
        final HashMultimap<String, Long> samplesToFilesMap = extractSamplesToFileMap(experimentItemUpdated);
        assertEquals(samplesToFilesMap.keySet().size(), 3);
        assertTrue(samplesToFilesMap.get("sample_Medium_1").containsAll(newArrayList(file1, file2)));
        assertEquals(samplesToFilesMap.get("sample_Medium_1").size(), 2);
        assertTrue(samplesToFilesMap.get("sample_Light_1").containsAll(newArrayList(file1)));
        assertEquals(samplesToFilesMap.get("sample_Light_1").size(), 1);
        assertTrue(samplesToFilesMap.get("sample_Light_2").containsAll(newArrayList(file2)));
        assertEquals(samplesToFilesMap.get("sample_Light_2").size(), 1);
        assertTrue(!samplesToFilesMap.containsKey("sample_Heavy_1"));
        assertTrue(!samplesToFilesMap.containsKey("sample_Heavy_2"));
        assertTrue(Arrays.deepEquals(experimentItemUpdated.factorValues, new String[][] {
            new String[] {"sick", "5"},
            new String[] {"sick", "90"},
            new String[] {"healthy", "5"},
        }));

    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void restrictCreationTheSameSampleWithinDifferentSampleTypes() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);
        final ExperimentSampleItem sampleLight1 =
            new ExperimentSampleItem("sample_Light_1", LIGHT, ImmutableList.of("sick", "5"));
        final ExperimentSampleItem sampleLight2 =
            new ExperimentSampleItem("sample_Light_2", LIGHT, ImmutableList.of("sick", "90"));

        final ExperimentSampleItem sampleMedium1 =
            new ExperimentSampleItem("sample_Medium_1", MEDIUM, ImmutableList.of("healthy", "5"));
        final ImmutableSet<ExperimentSampleItem> file1Samples = ImmutableSet.of(sampleLight1, sampleMedium1);
        final ImmutableSet<ExperimentSampleItem> file2Samples = ImmutableSet.of(sampleLight1, sampleLight2);

        final List<MetaFactorTemplate> factors = newArrayList(
            new MetaFactorTemplate("treatment group", null, false, 0),
            new MetaFactorTemplate("temperature", "C", true, 0)
        );
        final ImmutableList<com.infoclinika.mssharing.model.write.FileItem> files = of(
            new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, file1Samples)),
            new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, file2Samples))
        );

        final Builder builder = experiment(bob, project).factors(factors)
            .experimentType(experimentTypeLabeled())
            .files(files)
            .experimentLabels(new ExperimentLabelsInfo())
            .sampleTypesCount(2);
        studyManagement.createExperiment(bob, builder.build());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void restrictCreationExperimentWithoutSamples() {
        final long bob = uc.createLab3AndBob();
        final long file = uc.saveFile(bob);
        final ImmutableList<com.infoclinika.mssharing.model.write.FileItem> fileWithoutSamples =
            of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0,
                new ExperimentPreparedSampleItem(String.valueOf(file), ImmutableSet.of())
            ));

        final Builder builder = new Builder().name(generateString())
            .description("")
            .experimentType(anyExperimentType())
            .specie(unspecified())
            .experimentLabels(new ExperimentLabelsInfo())
            .project(uc.createProject(bob))
            .lab(uc.getLab3())
            .billLab(uc.getLab3())
            .is2dLc(false)
            .restriction(restriction(bob))
            .factors(NO_FACTORS)
            .files(fileWithoutSamples)
            .bounds(new AnalysisBounds())
            .lockMasses(NO_LOCK_MASSES)
            .sampleTypesCount(1);
        studyManagement.createExperiment(bob, builder.build());
    }

    @Test
    public void successfullyCreateExperimentWithoutLabels() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);

        final long file = uc.saveFile(bob);
        final Builder builder = experimentInfo()
            .project(project).lab(uc.getLab3()).billLab(uc.getLab3()).is2dLc(false)
            .restriction(restriction(bob, file)).bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES)
            .factors(NO_FACTORS)
            .experimentType(anyExperimentType())
            .files(noFactoredFile(file))
            .experimentLabels(new ExperimentLabelsInfo())
            .sampleTypesCount(0);


        studyManagement.createExperiment(bob, builder.build());

    }

    @Test
    public void createExperimentWithFilesOfOnePreparedSample() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);

        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);
        final String bioSample1Name = "bio-sample-1";
        final String bioSample2Name = "bio-sample-2";
        final ExperimentPreparedSampleItem preparedSample = preparedSample(42L, ImmutableSet.of(
            new ExperimentSampleItem(bioSample1Name, LIGHT, newArrayList("firstFactorValue1", "secondFactorValue1")),
            new ExperimentSampleItem(bioSample2Name, HEAVY, newArrayList("firstFactorValue2", "secondFactorValue2"))
        ));
        final MetaFactorTemplate factor1 =
            new MetaFactorTemplate("factor1", "", false, -1L);
        final MetaFactorTemplate factor2 =
            new MetaFactorTemplate("factor2", "", false, -1L);
        final Builder builder = experiment(bob, project)
            .factors(of(factor1, factor2))
            .experimentType(anyExperimentType())
            .files(
                of(
                    new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample),
                    new com.infoclinika.mssharing.model.write.FileItem(file2, false, 0, preparedSample)
                )
            )
            .experimentLabels(new ExperimentLabelsInfo())
            .sampleTypesCount(2);


        final long experiment = studyManagement.createExperiment(bob, builder.build());

        final ExperimentItem exDetails = detailsReader.readExperiment(bob, experiment);
        assertEquals(exDetails.files.size(), 2);
        final FileItem file1Item = (FileItem) exDetails.files.get(0);
        final FileItem file2Item = (FileItem) exDetails.files.get(1);
        assertEquals(file1Item.preparedSample.name, preparedSample.name);
        assertEquals(file2Item.preparedSample.name, preparedSample.name);
        assertEquals(file2Item.preparedSample.samples.size(), 2);
        final ExperimentSampleItem sample =
            Iterables.find(file2Item.preparedSample.samples, sampleItem -> sampleItem.name.equals(bioSample1Name));
        assertEquals(sample.name, bioSample1Name);
        assertEquals(sample.factorValues, newArrayList("firstFactorValue1", "secondFactorValue1"));
    }

    private static List<MetaFactorTemplate> asFactorsTemplates(
        Iterable<DetailsReaderTemplate.MetaFactorTemplate> factorsToTransform
    ) {
        List<MetaFactorTemplate> factors = newArrayList();
        for (DetailsReaderTemplate.MetaFactorTemplate factor : factorsToTransform) {
            factors.add(new MetaFactorTemplate(
                factor.name,
                factor.units,
                factor.isNumeric,
                factor.experimentId
            ));
        }
        return factors;
    }

    private static List<com.infoclinika.mssharing.model.write.FileItem> asFileItems(
        Iterable<DetailsReaderTemplate.FileItemTemplate> files,
        @Nullable Predicate<ExperimentSampleItem> samplePredicate
    ) {
        final List<com.infoclinika.mssharing.model.write.FileItem> updatedFiles = newArrayList();
        for (DetailsReaderTemplate.FileItemTemplate file : files) {
            final ExperimentPreparedSampleItem preparedSample = ((FileItem) file).preparedSample;
            final Set<ExperimentSampleItem> allSamples = preparedSample.samples;
            final Collection<ExperimentSampleItem> samplesFiltered =
                samplePredicate == null ? newArrayList(allSamples) : Collections2.filter(allSamples, samplePredicate);
            final ExperimentPreparedSampleItem prepSample =
                new ExperimentPreparedSampleItem(preparedSample.name, newHashSet(samplesFiltered));
            updatedFiles.add(new com.infoclinika.mssharing.model.write.FileItem(file.id, file.copy, 0, prepSample));
        }
        return updatedFiles;
    }

    private long createExperimentWithIntersectedSamples(long bob, long project, long file1, long file2) {
        // prep.sample1: bio-sample-1 bio-sample-1
        // prep.sample2: bio-sample-2 bio-sample-1
        final ExperimentSampleItem bioSample1Light =
            new ExperimentSampleItem("bio-sample-1", LIGHT, ImmutableList.of("sick", "5"));
        final ExperimentSampleItem bioSample1Heavy =
            new ExperimentSampleItem("bio-sample-1", HEAVY, ImmutableList.of("sick", "5"));
        final ExperimentSampleItem bioSample2Light =
            new ExperimentSampleItem("bio-sample-2", LIGHT, ImmutableList.of("sick", "90"));
        final ImmutableSet<ExperimentSampleItem> file1Samples = ImmutableSet.of(bioSample1Light, bioSample1Heavy);
        final ImmutableSet<ExperimentSampleItem> file2Samples = ImmutableSet.of(bioSample2Light, bioSample1Heavy);

        final List<MetaFactorTemplate> factors = newArrayList(
            new MetaFactorTemplate("treatment group", null, false, 0),
            new MetaFactorTemplate("temperature", "C", true, 0)
        );
        final ImmutableList<com.infoclinika.mssharing.model.write.FileItem> files = of(
            new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, file1Samples)),
            new com.infoclinika.mssharing.model.write.FileItem(file2, false, 0, preparedSample(file2, file2Samples))
        );

        final Builder builder = experiment(bob, project).factors(factors)
            .experimentType(experimentTypeLabeled())
            .files(files)
            .experimentLabels(new ExperimentLabelsInfo())
            .sampleTypesCount(2);
        return studyManagement.createExperiment(bob, builder.build());
    }

    private Builder experiment(long bob, long project) {
        return experimentInfo()
            .project(project).lab(uc.getLab3()).billLab(uc.getLab3()).is2dLc(false)
            .restriction(restriction(bob)).bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES);
    }

    private long createExperimentWithTwoSampleTypesAndFactors(long bob, long project, long file1, long file2) {
        final ExperimentSampleItem sampleLight1 =
            new ExperimentSampleItem("sample_Light_1", LIGHT, ImmutableList.of("sick", "5"));
        final ExperimentSampleItem sampleLight2 =
            new ExperimentSampleItem("sample_Light_2", LIGHT, ImmutableList.of("sick", "90"));

        final ExperimentSampleItem sampleMedium1 =
            new ExperimentSampleItem("sample_Medium_1", MEDIUM, ImmutableList.of("healthy", "5"));
        final ImmutableSet<ExperimentSampleItem> file1Samples = ImmutableSet.of(sampleLight1, sampleMedium1);
        final ImmutableSet<ExperimentSampleItem> file2Samples = ImmutableSet.of(sampleLight2, sampleMedium1);

        final List<MetaFactorTemplate> factors = newArrayList(
            new MetaFactorTemplate("treatment group", null, false, 0),
            new MetaFactorTemplate("temperature", "C", true, 0)
        );
        final ImmutableList<com.infoclinika.mssharing.model.write.FileItem> files = of(
            new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, file1Samples)),
            new com.infoclinika.mssharing.model.write.FileItem(file2, false, 0, preparedSample(file2, file2Samples))
        );

        final Builder builder = experiment(bob, project).factors(factors)
            .experimentType(experimentTypeLabeled())
            .files(files)
            .experimentLabels(new ExperimentLabelsInfo())
            .sampleTypesCount(2);
        return studyManagement.createExperiment(bob, builder.build());
    }

    private void validateCreatedExperimentWithCommonSamples(long file1, long file2, ExperimentItem experimentItem) {
        assertEquals(experimentItem.factors.size(), 2);
        assertEquals(experimentItem.sampleTypesCount, 2);
        final HashMultimap<String, Long> samplesToFilesMap = extractSamplesToFileMap(experimentItem);

        assertEquals(samplesToFilesMap.keySet().size(), 2);
        assertTrue(samplesToFilesMap.get("bio-sample-1").containsAll(newArrayList(file1, file2)));
        assertEquals(samplesToFilesMap.get("bio-sample-1").size(), 2);
        assertTrue(samplesToFilesMap.get("bio-sample-2").containsAll(newArrayList(file2)));
        assertEquals(samplesToFilesMap.get("bio-sample-2").size(), 1);
        assertTrue(Arrays.deepEquals(experimentItem.factorValues, new String[][] {
            new String[] {"sick", "5"},
            new String[] {"sick", "90"}
        }));

        final Map<String, ExperimentPreparedSampleItem> preparedSamplesMap = extractPreparedSamplesMap(experimentItem);

        assertEquals(preparedSamplesMap.size(), 2);

        final List<String> prepSamplesNames = newArrayList(preparedSamplesMap.keySet());
        Collections.sort(prepSamplesNames);
        final List<ExperimentSampleItem> firstPrepSamples =
            newArrayList(preparedSamplesMap.get(prepSamplesNames.get(0)).samples);
        Collections.sort(firstPrepSamples, SORT_ASC);
        assertEquals(firstPrepSamples.size(), 2);
        assertEquals(firstPrepSamples.get(0).type, LIGHT);
        assertEquals(firstPrepSamples.get(0).name, "bio-sample-1");
        assertEquals(firstPrepSamples.get(1).type, HEAVY);
        assertEquals(firstPrepSamples.get(1).name, "bio-sample-1");


        final List<ExperimentSampleItem> secondPrepSamples =
            newArrayList(preparedSamplesMap.get(prepSamplesNames.get(1)).samples);
        Collections.sort(secondPrepSamples, SORT_ASC);
        assertEquals(secondPrepSamples.size(), 2);
        assertEquals(secondPrepSamples.get(0).type, HEAVY);
        assertEquals(secondPrepSamples.get(0).name, "bio-sample-1");
        assertEquals(secondPrepSamples.get(1).type, LIGHT);
        assertEquals(secondPrepSamples.get(1).name, "bio-sample-2");
    }

    private static final Comparator<ExperimentSampleItem> SORT_ASC = new Comparator<ExperimentSampleItem>() {
        @Override
        public int compare(ExperimentSampleItem o1, ExperimentSampleItem o2) {
            final int result = o1.name.compareTo(o2.name);
            if (result == 0) {
                return o1.type.compareTo(o2.type);
            } else {
                return result;
            }

        }
    };

    private static void validateExperimentShortDetails(ExperimentShortInfo shortInfo, long file1) {

        final List<? extends ShortExperimentFileItem> files = shortInfo.files;
        assertEquals(files.size(), 2);

        final ExtendedShortExperimentFileItem fileItem1 =
            (ExtendedShortExperimentFileItem) files.stream().filter(file -> file.id == file1).findFirst().get();
        assertEquals(fileItem1.samples.size(), 3);

        final ImmutableMap<String, ExperimentShortSampleItem> sampleNameToSample =
            Maps.uniqueIndex(fileItem1.samples, input -> input.name);
        final ExperimentShortSampleItem sample1 = sampleNameToSample.get("sample_Heavy_1");
        assertEquals(sample1.condition.name, "treatment group:healthy, temperature:10(C)");

        final ExperimentShortSampleItem sample2 = sampleNameToSample.get("sample_Medium_1");
        assertEquals(sample2.condition.name, "treatment group:healthy, temperature:5(C)");
        assertEquals(sample2.name, "sample_Medium_1");

        final ExperimentShortSampleItem sample3 = sampleNameToSample.get("sample_Light_1");
        assertEquals(sample3.condition.name, "treatment group:sick, temperature:5(C)");
        assertEquals(sample3.name, "sample_Light_1");

        final Set<String> conditionNames = newHashSet();
        final Set<Long> conditionIds = newHashSet();
        for (ShortExperimentFileItem file : files) {
            final ExtendedShortExperimentFileItem fileItem = (ExtendedShortExperimentFileItem) file;
            for (ExperimentShortSampleItem sample : fileItem.samples) {
                conditionNames.add(sample.condition.name);
                conditionIds.add(sample.condition.id);
            }
        }
        assertEquals(conditionIds.size(), 4);
        assertEquals(conditionNames.size(), 4);
        final Set<String> expectedConditions = ImmutableSet.<String>builder()
            .add("treatment group:healthy, temperature:10(C)")
            .add("treatment group:healthy, temperature:5(C)")
            .add("treatment group:sick, temperature:5(C)")
            .add("treatment group:sick, temperature:90(C)")
            .build();
        assertEquals(conditionNames, expectedConditions);
    }

    private static void validateExperimentWithAllSamplesAndFactors(
        long file1,
        long file2,
        ExperimentItem experimentItem
    ) {
        assertEquals(experimentItem.factors.size(), 2);
        assertEquals(experimentItem.sampleTypesCount, 3);
        final HashMultimap<String, Long> samplesToFilesMap = extractSamplesToFileMap(experimentItem);
        assertEquals(samplesToFilesMap.keySet().size(), 5);
        assertTrue(samplesToFilesMap.get("sample_Medium_1").containsAll(newArrayList(file1, file2)));
        assertEquals(samplesToFilesMap.get("sample_Medium_1").size(), 2);
        assertTrue(samplesToFilesMap.get("sample_Light_1").containsAll(newArrayList(file1)));
        assertEquals(samplesToFilesMap.get("sample_Light_1").size(), 1);
        assertTrue(samplesToFilesMap.get("sample_Light_2").containsAll(newArrayList(file2)));
        assertEquals(samplesToFilesMap.get("sample_Light_2").size(), 1);
        assertTrue(samplesToFilesMap.get("sample_Heavy_1").containsAll(newArrayList(file1)));
        assertEquals(samplesToFilesMap.get("sample_Heavy_1").size(), 1);
        assertTrue(samplesToFilesMap.get("sample_Heavy_2").containsAll(newArrayList(file2)));
        assertEquals(samplesToFilesMap.get("sample_Heavy_2").size(), 1);
        assertTrue(Arrays.deepEquals(experimentItem.factorValues, new String[][] {
            new String[] {"healthy", "10"},
            new String[] {"healthy", "5"},
            new String[] {"sick", "5"},
            new String[] {"sick", "90"},
            new String[] {"healthy", "5"}
        }));
    }

    private static HashMultimap<String, Long> extractSamplesToFileMap(ExperimentItem experimentItemUpdated) {
        final HashMultimap<String, Long> samplesToFilesMap = HashMultimap.create();
        for (DetailsReaderTemplate.FileItemTemplate file : experimentItemUpdated.files) {
            for (ExperimentSampleItem sample : ((FileItem) file).preparedSample.samples) {
                samplesToFilesMap.put(sample.name, file.id);
            }
        }
        return samplesToFilesMap;
    }

    private Map<String, ExperimentPreparedSampleItem> extractPreparedSamplesMap(ExperimentItem experimentItemUpdated) {
        final Map<String, ExperimentPreparedSampleItem> preparedSamplesMap = newHashMap();
        for (DetailsReaderTemplate.FileItemTemplate file : experimentItemUpdated.files) {
            final ExperimentPreparedSampleItem preparedSample = ((FileItem) file).preparedSample;
            preparedSamplesMap.put(preparedSample.name, preparedSample);
        }
        return preparedSamplesMap;
    }


}
