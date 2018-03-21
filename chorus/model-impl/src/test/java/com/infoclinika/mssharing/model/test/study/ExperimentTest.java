/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.test.study;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.model.helper.Data;
import com.infoclinika.mssharing.model.helper.ExperimentDownloadHelper;
import com.infoclinika.mssharing.model.helper.ExperimentLabelsInfo;
import com.infoclinika.mssharing.model.helper.ExperimentSampleItem;
import com.infoclinika.mssharing.model.internal.read.ExperimentLabelReader;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.ExperimentLine;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.model.write.AnalysisBounds;
import com.infoclinika.mssharing.model.write.ExperimentInfo;
import com.infoclinika.mssharing.model.write.FileMetaDataInfo;
import com.infoclinika.mssharing.model.write.ProjectInfo;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.InvalidFactorException;
import com.infoclinika.mssharing.platform.model.ObjectNotFoundException;
import com.infoclinika.mssharing.platform.model.common.items.NamedItem;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.infoclinika.mssharing.model.helper.Data.PROJECT_TITLE;
import static com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate.AttachmentType.EXPERIMENT;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Stanislav Kurilin
 */
public class ExperimentTest extends AbstractStudyTest {
    @Inject
    private AttachmentsReaderTemplate attachmentsReader;
    //experiment creation

    @Inject
    private ExperimentDownloadHelper experimentDownloadHelper;
    @Inject
    private ExperimentLabelReader experimentLabelReader;

    @Test
    public void testFilesAreAvailableOnTypeRestriction() {
        final long bob = uc.createLab3AndBob();
        final long model = anyInstrumentModel();
        final long instrument = createInstrumentBySpecifiedInstrumentModel(bob, uc.getLab3(), model);
        uc.saveFile(bob, instrument);
        assertAvailableFilesByInstrumentModel(bob, unspecifiedSpecie(), model, 1);
    }

    @Test
    public void testReadExperimentShortItems() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob, uc.getLab3());
        final long experiment = createExperiment(bob, project);
        final Set<NamedItem> ownedExperiments = experimentCreationHelper.ownedExperiments(bob);
        assertThat(ownedExperiments.size(), is(1));
        assertThat(ownedExperiments.iterator().next().id, is(experiment));
    }

    @Test
    public void testFilesAreFilteredByInstrumentModelAndSpecie() {
        final long bob = uc.createLab3AndBob();
        final long model = anyInstrumentModel();
        long specie = anySpecies();

        creteFileWithInstrumentModel(bob, uc.getLab3(), model, specie);
        creteFileWithInstrumentModel(bob, uc.getLab3(), model, unspecifiedSpecie());

        assertAvailableFilesByInstrumentModel(bob, specie, model, 2);
        assertAvailableFilesByInstrumentModel(bob, anotherSpecie(specie), model, 1);
        assertAvailableFilesByInstrumentModel(bob, unspecifiedSpecie(), model, 2);
    }

    @Test
    public void testCreateExperimentForUserWithoutLab() {
        final long john = uc.createJohnWithoutLab();
        final long bob = uc.createLab3AndBob();
        final long project = createPublicProject(bob);

        final long file1 = uc.saveFile(bob);
        final ExperimentInfo.Builder builder = experimentInfo().project(project).lab(uc.getLab3()).billLab(uc.getLab3()).experimentLabels(new ExperimentLabelsInfo())
                .is2dLc(false).restriction(restriction(bob)).factors(NO_FACTORS).files(noFactoredFile(file1))
                .sampleTypesCount(1).bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES);

        final long experiment = studyManagement.createExperiment(bob, builder.build());

        final long file2 = uc.saveFile(bob);
        final String factorName = generateString();
        final String factorValue = "2";
        final ExperimentSampleItem sampleWithFactors = sampleWithFactors(file2, of(factorValue));
        addFilesToExperiment(bob, experiment,
                of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
                of(new com.infoclinika.mssharing.model.write.FileItem(file2, false, 0, preparedSample(file2, ImmutableSet.of(sampleWithFactors)))), of("3"));

        final long project2 = createPublicProject(john);
        createExperiment(john, project2);
        final Iterable<ExperimentLine> experiments = dashboardReader.readExperiments(john, Filter.MY);
        assertEquals(Iterables.size(experiments), 1);
    }

    @Test
    public void testFilesAreFilteredByInstrumentAndSpecie() {
        final long bob = uc.createLab3AndBob();
        final long model = anyInstrumentModel();
        final long instrument = createInstrumentBySpecifiedInstrumentModel(bob, uc.getLab3(), model);
        long specie = anySpecies();

        createFileWithInstrument(bob, instrument, specie);
        createFileWithInstrument(bob, instrument, unspecifiedSpecie());

        assertEquals(size(experimentCreationHelper.availableFilesByInstrument(bob, specie, instrument)), 2);
        assertEquals(size(experimentCreationHelper.availableFilesByInstrument(bob, anotherSpecie(specie), instrument)), 1);
        assertEquals(size(experimentCreationHelper.availableFilesByInstrument(bob, unspecifiedSpecie(), instrument)), 2);
    }

    private void creteFileWithInstrumentModel(long bob, long lab, long model, long specie) {
        final long instrument = createInstrumentBySpecifiedInstrumentModel(bob, lab, model);
        createFileWithInstrument(bob, instrument, specie);
    }

    private long anotherSpecie(long id) {
        long result = anySpecies();
        while (result == id) {
            result = anySpecies();
        }
        return result;
    }

    @Test(dependsOnMethods = "testFilesAreAvailableOnTypeRestriction")
    public void testFilesAreAvailableOnInstrumentRestriction() {
        final long bob = uc.createLab3AndBob();
        final long model = anyInstrumentModel();
        final long instrument = createInstrumentBySpecifiedInstrumentModel(bob, uc.getLab3(), model);
        uc.saveFile(bob, instrument);
        assertAvailableFilesByInstrumentModel(bob, unspecifiedSpecie(), model, 1);
    }

    @Test(dependsOnMethods = "testFilesAreAvailableOnTypeRestriction")
    public void testOnlyOwnersFilesAreAvailable() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long model = anyInstrumentModel();
        final long instrument = createInstrumentBySpecifiedInstrumentModel(bob, uc.getLab3(), model);
        uc.saveFile(bob, instrument);
        assertAvailableFilesByInstrumentModel(joe, 0, model, 1);
    }

    @Test(dependsOnMethods = "testFilesAreAvailableOnInstrumentRestriction")
    public void testFilesRestrictedByInstrument() {
        final long bob = uc.createLab3AndBob();
        final long model = anyInstrumentModel();
        final long instrument1 = createInstrumentBySpecifiedInstrumentModel(bob, uc.getLab3(), model);
        final long instrument2 = createInstrumentBySpecifiedInstrumentModel(bob, uc.getLab3(), model);
        uc.saveFile(bob, instrument1);

        assertEquals(size(experimentCreationHelper.availableFilesByInstrument(bob, 0, instrument2)), 0);
    }

    @Test
    public void testFileCouldBeAttachedToSeveralExperiments() {
        final long bob = uc.createLab3AndBob();
        final long file = uc.saveFile(bob);
        final long project = uc.createProject(bob, uc.getLab3());
       /* final long experiment1 = projectManagement.newExperimentWithoutFiles(bob, project, uc.getLab3(), experimentInfo(), false, restriction(bob));
        final long experiment2 = projectManagement.newExperimentWithoutFiles(bob, project, uc.getLab3(), experimentInfo(), false, restriction(bob));
        projectManagement.updateFiles(bob, experiment1, NO_FACTORS, noFactoredFile(file));
        projectManagement.updateFiles(bob, experiment2, NO_FACTORS, noFactoredFile(file));*/
        createExperiment(bob, project, uc.getLab3(), noFactoredFile(file));
        createExperiment(bob, project, uc.getLab3(), noFactoredFile(file));
    }

    @Test
    public void testExperimentDataOnDashboard() {
        final long bob = uc.createLab3AndBob();
        final long projectId = uc.createProject(bob, uc.getLab3());
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);
        final long file3 = uc.saveFile(bob);
        final long experimentId = createInstrumentAndExperiment(bob, uc.getLab3(), projectId, NO_FACTORS, noFactoredFiles(of(file1, file2, file3)));
        final Iterable<ExperimentLine> experiments
                = dashboardReader.readExperiments(bob, Filter.ALL);

        assertEquals(Iterables.size(experiments), 1);
        final ExperimentLine experiment = experiments.iterator().next();
        assertEquals(experiment.id, experimentId);
        assertEquals(experiment.files, 3);
        assertNotNull(experiment.modified);
    }

    @Test
    public void testExperimentDataDetails() {
        final long bob = uc.createLab3AndBob();
        final String projectName = generateString();
        final String experimentName = generateString();
        final String experimentDescription = generateString();
        instrument(bob, uc.getLab3(), anyInstrumentModel());
        long experimentType = anyExperimentType();
        final long projectId = studyManagement.createProject(bob, new ProjectInfo(projectName, generateString(), generateString(), uc.getLab3()));
        long specie = unspecified();
        final ImmutableList<com.infoclinika.mssharing.model.write.FileItem> files = anyFile(bob);

        final ExperimentInfo.Builder builder = new ExperimentInfo.Builder().name(experimentName).description(experimentDescription)
                .experimentType(experimentType).specie(specie)
                .project(projectId).lab(uc.getLab3()).billLab(uc.getLab3()).experimentLabels(new ExperimentLabelsInfo())
                .is2dLc(false).restriction(restriction(bob, files.get(0).id)).factors(NO_FACTORS)
                .files(files).bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES).sampleTypesCount(1);

        final long experimentId = studyManagement.createExperiment(bob, builder.build());

        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experimentId);
        assertNotNull(experimentItem.files);
        assertNotNull(experimentItem.factors);
        assertEquals(extractSampleNameToSample(experimentItem).size(), 1);
        assertNotNull(experimentItem.factorValues);
        assertEquals(experimentItem.ownerEmail, Data.BOBS_EMAIL);
        assertEquals(experimentItem.project, projectId);
        assertEquals(experimentItem.description, experimentDescription);
        assertEquals(experimentItem.name, experimentName);
        assertEquals(experimentItem.experimentType, experimentType);
        assertEquals(experimentItem.specie, specie);
    }

    @Test(enabled = false) //todo [pavel.kaplin]
    public void testUserCanEditExperiment() {
        final long bob = uc.createLab3AndBob();
        final long experimentId = experiment(bob, uc.getLab3());
        final String newName = generateString();

//        projectManagement.updateExperimentWithoutFiles(bob, experimentId, new StudyManagement.ExperimentInfo(newName, generateString(), anyWorkflowType(), anySpecie()));

        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experimentId);
        assertEquals(experimentItem.name, newName);
    }


    //attaching files to experiment
    @Test
    public void testCreatingExperimentWithFactoredFile() {
        long bob = uc.createLab3AndBob();
        final long file = uc.saveFile(bob);
        final long experiment = experimentInNewProject(bob, uc.getLab3());

        final String factorName = generateString();
        final String factorValue = "2";
        final ExperimentSampleItem sampleItem = sampleWithFactors(file, of(factorValue));
        addFilesToExperiment(bob, experiment,
                of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
                of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0, preparedSample(file, ImmutableSet.of(sampleItem)))), of(factorValue));

        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);

        assertEquals(experimentItem.factors.size(), 1);
        assertEquals(experimentItem.files.size(), 2);

        assertEquals(experimentItem.factors.iterator().next().name, factorName);
        assertEquals(experimentItem.files.iterator().next().id, file);
        assertEquals(experimentItem.factorValues[0][0], factorValue);
    }

    @Test(dependsOnMethods = "testCreatingExperimentWithFactoredFile")
    public void testRemovingExperiment() {
        long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);

        final int experimentsLengthWithNewExperiment = dashboardReader.readExperiments(bob, Filter.MY).size();
        assertEquals(experimentsLengthWithNewExperiment, 1);
        studyManagement.moveExperimentToTrash(bob, experiment);
        final int experimentsLengthWithoutNewExperiment = dashboardReader.readExperiments(bob, Filter.MY).size();
        assertEquals(experimentsLengthWithoutNewExperiment, 0);
    }

    @Test(dependsOnMethods = "testCreatingExperimentWithFactoredFile")
    public void testRemovingExperimentWithFactoredFiles() {
        long kate = uc.createKateAndLab2();
        uc.addKateToLab3();
        final long file = uc.saveFile(kate);
        final long experiment = experimentInNewProject(kate, uc.getLab3());

        final String factorName = generateString();
        final String factorValue = "2";
        final ExperimentSampleItem sampleItem = sampleWithFactors(file, of(factorValue));
        addFilesToExperiment(kate, experiment,
                of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
                of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0, preparedSample(file, ImmutableSet.of(sampleItem)))), of(factorValue));

        final int experimentsLengthWithNewExperiment = dashboardReader.readExperiments(kate, Filter.MY).size();
        assertEquals(experimentsLengthWithNewExperiment, 1);

        studyManagement.moveExperimentToTrash(kate, experiment);

        final int experimentsLengthWithoutNewExperiment = dashboardReader.readExperiments(kate, Filter.MY).size();
        assertEquals(experimentsLengthWithoutNewExperiment, 0);
    }


    @Test(expectedExceptions = AccessDenied.class, dependsOnMethods = "testCreatingExperimentWithFactoredFile")
    public void testRemovingPublicExperiment() {
        long john = uc.createJohnWithoutLab();
        long bob = uc.createLab3AndBob();
        final long project = createPublicProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);

        final int experimentsLengthWithNewExperiment = dashboardReader.readExperiments(bob, Filter.ALL).size();
        assertEquals(experimentsLengthWithNewExperiment, 1);
        studyManagement.moveExperimentToTrash(john, experiment);
    }

    @Test(expectedExceptions = AccessDenied.class, dependsOnMethods = "testRemovingExperiment")
    public void testRemovingUsedFile() {
        long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long file = uc.saveFile(bob, instrumentFromExperimentFile(bob, experiment));
        final String factorName = generateString();
        final String factorValue = "2";
        final ExperimentSampleItem sampleItem = sampleWithFactors(file, of(factorValue));
        addFilesToExperiment(bob, experiment,
                of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
                of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0, preparedSample(file, ImmutableSet.of(sampleItem)))), of(factorValue));
        final int experimentsLengthWithNewExperiment = dashboardReader.readExperiments(bob, Filter.MY).size();
        assertEquals(experimentsLengthWithNewExperiment, 1);
        instrumentManagement.moveFileToTrash(bob, file);
    }

    @Test(dependsOnMethods = "testRemovingExperiment")
    public void testRemovingUnusedFile() {
        long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long file = uc.saveFile(bob, instrumentFromExperimentFile(bob, experiment));
        final String factorName = generateString();
        final String factorValue = "2";
        final ExperimentSampleItem sampleItem = sampleWithFactors(file, of(factorValue));
        addFilesToExperiment(bob, experiment,
                of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
                of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0, preparedSample(file, ImmutableSet.of(sampleItem)))), of(factorValue));
        final int experimentsLengthWithNewExperiment = dashboardReader.readExperiments(bob, Filter.MY).size();
        assertEquals(experimentsLengthWithNewExperiment, 1);
        studyManagement.moveExperimentToTrash(bob, experiment);
        final int experimentsLengthWithoutExperiment = dashboardReader.readExperiments(bob, Filter.MY).size();
        assertEquals(experimentsLengthWithoutExperiment, 0);
        assertEquals(fileReader.readFiles(bob, Filter.ALL).size(), 2);
        instrumentManagement.moveFileToTrash(bob, file);
        assertEquals(fileReader.readFiles(bob, Filter.ALL).size(), 1);
    }

    @Test
    public void testRemoveSeveralExperimentsWithSameFiles() {
        long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long experiment2 = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long file = uc.saveFile(bob, instrumentFromExperimentFile(bob, experiment));

        final String factorName = generateString();
        final String factorValue = "2";
        final ExperimentSampleItem sampleItem = sampleWithFactors(file, of(factorValue));
        addFilesToExperiment(bob, experiment,
                of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
                of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0, preparedSample(file, ImmutableSet.of(sampleItem)))), of(factorValue));

        addFilesToExperiment(bob, experiment2,
                of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
                of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0, preparedSample(file, ImmutableSet.of(sampleItem)))), of(factorValue));

        final int experiments = dashboardReader.readExperiments(bob, Filter.MY).size();
        assertEquals(experiments, 2);
        studyManagement.moveExperimentToTrash(bob, experiment);
        studyManagement.moveExperimentToTrash(bob, experiment2);

        final int noExperiments = dashboardReader.readExperiments(bob, Filter.MY).size();
        assertEquals(noExperiments, 0);
        assertEquals(fileReader.readFiles(bob, Filter.ALL).size(), 3);
        instrumentManagement.moveFileToTrash(bob, file);
        assertEquals(fileReader.readFiles(bob, Filter.ALL).size(), 2);
    }

    @Test(expectedExceptions = AccessDenied.class, dependsOnMethods = "testRemovingExperiment")
    public void testRemovingPublicFile() {
        long bob = uc.createLab3AndBob();
        final long project = createPublicProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long file = uc.saveFile(bob, instrumentFromExperimentFile(bob, experiment));
        final String factorName = generateString();
        final String factorValue = "2";
        final ExperimentSampleItem sampleItem = sampleWithFactors(file, of(factorValue));
        addFilesToExperiment(bob, experiment,
                of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
                of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0, preparedSample(file, ImmutableSet.of(sampleItem)))),
                of(factorValue));

        assertEquals(fileReader.readFiles(bob, Filter.ALL).size(), 2);
        instrumentManagement.moveFileToTrash(bob, file);
    }


    @Test
    @Deprecated
    public void testCreatingExperimentWithAnnotatedFile() {
        long bob = uc.createLab3AndBob();
        final long file = uc.saveFile(bob);
        final long experiment = experimentInNewProject(bob, uc.getLab3());

        final ExperimentSampleItem sample = sampleWithNoFactors(file);
        addFilesToExperiment(bob, experiment,
                Collections.<ExperimentManagementTemplate.MetaFactorTemplate>emptyList(),
                of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0, preparedSample(file, ImmutableSet.of(sample)))), Collections.<String>emptyList());

        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);

        final FluentIterable<DetailsReaderTemplate.FileItemTemplate> annotatedFiles = from(experimentItem.files).filter(new Predicate<DetailsReaderTemplate.FileItemTemplate>() {
            @Override
            public boolean apply(DetailsReaderTemplate.FileItemTemplate input) {
                return !input.annotations.isEmpty();
            }
        });
        assertEquals(annotatedFiles.size(), 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAnnotationsMissed() {
        long bob = uc.createLab3AndBob();
        final long file = uc.saveFile(bob);
        final long project = uc.createProject(bob, uc.getLab3());
        final ExperimentSampleItem sample = sampleWithNoFactors(file);
        final ExperimentInfo.Builder builder = new ExperimentInfo.Builder().name("").description("").experimentType(anyExperimentType()).specie(anySpecies())
                .project(project).lab(uc.getLab3()).billLab(uc.getLab3()).is2dLc(true).restriction(restriction(bob)).factors(NO_FACTORS)
                .files(of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0, preparedSample(file, ImmutableSet.of(sample)))))
                .bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES);

        studyManagement.createExperiment(bob, builder.build());

    }

    @Test
    public void testFactoredValuesSetProperly() {
        long bob = uc.createLab3AndBob();
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);
        final long experiment = experimentInNewProject(bob, uc.getLab3());
        final ExperimentSampleItem sample1 = sampleWithFactors(file1, of("1.23", "a bcd", "-1"));
        final ExperimentSampleItem sample2 = sampleWithFactors(file2, of("23", "4", "7"));
        addFilesToExperiment(bob, experiment,
                of(new ExperimentManagementTemplate.MetaFactorTemplate("f1", "g", true, experiment),
                        new ExperimentManagementTemplate.MetaFactorTemplate("_f2", "", false, experiment),
                        new ExperimentManagementTemplate.MetaFactorTemplate("f3", "", true, experiment)),
                of(new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, ImmutableSet.of(sample1))),
                        new com.infoclinika.mssharing.model.write.FileItem(file2, false, 0, preparedSample(file2, ImmutableSet.of(sample2)))),
                of("33", "4", "8")
        );

        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);

        assertEquals(experimentItem.factors.size(), 3);
        assertEquals(experimentItem.files.size(), 3);
        final ImmutableMap<String, ExperimentSampleItem> sampleNameToSampleMap = extractSampleNameToSample(experimentItem);
        assertEquals(sampleNameToSampleMap.get(composeSampleUniqueKey(sample1)).factorValues.size(), 3);
        assertEquals(sampleNameToSampleMap.get(composeSampleUniqueKey(sample1)).factorValues.get(0), "1.23");
        assertEquals(sampleNameToSampleMap.get(composeSampleUniqueKey(sample1)).factorValues.get(1), "a bcd");
        assertEquals(sampleNameToSampleMap.get(composeSampleUniqueKey(sample1)).factorValues.get(2), "-1");
        assertTrue(Arrays.deepEquals(experimentItem.factorValues, new String[][]{
                {"1.23", "a bcd", "-1"},
                {"23", "4", "7"},
                {"33", "4", "8"}
        }));

        for (DetailsReaderTemplate.FileItemTemplate file : experimentItem.files) {
            assertTrue(file.annotations.isEmpty());
            assertTrue(file.labels.isEmpty());
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreatingExperimentWithoutSpecifyingAllFactorValues() {
        long bob = uc.createLab3AndBob();
        final long file = uc.saveFile(bob);
        final long experiment = experimentInNewProject(bob, uc.getLab3());
        final ExperimentSampleItem sample = sampleWithFactors(file, of("v1"));
        addFilesToExperiment(bob, experiment,
                of(new ExperimentManagementTemplate.MetaFactorTemplate("f1", "", false, experiment), new ExperimentManagementTemplate.MetaFactorTemplate(generateString(), "", true, experiment)),
                of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0, preparedSample(file, ImmutableSet.of(sample)))), of(""));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUserCantCreateExperimentAlreadyExistedName() {
        final long bob = uc.createLab3AndBob();
        final long project = studyManagement.createProject(bob, new ProjectInfo("Title", "area", "", uc.getLab3()));
        final long file = uc.saveFile(bob);
        final long experimentType = anyExperimentType();
        final long specie = unspecified();

        final ExperimentInfo.Builder builder = new ExperimentInfo.Builder().name("Duplicated title").description("area").experimentType(experimentType).specie(specie)
                .project(project).lab(uc.getLab3()).billLab(uc.getLab3()).is2dLc(false).restriction(restriction(bob)).experimentLabels(new ExperimentLabelsInfo())
                .factors(NO_FACTORS).files(noFactoredFile(file)).bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES);
        studyManagement.createExperiment(bob, builder.build());

        final ExperimentInfo.Builder duplicatedBuilder = new ExperimentInfo.Builder().name("Duplicated title").description("another area").experimentType(experimentType).specie(specie)
                .project(project).lab(uc.getLab3()).billLab(uc.getLab3()).is2dLc(false)
                .restriction(restriction(bob)).factors(NO_FACTORS).files(noFactoredFile(file)).bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES);

        studyManagement.createExperiment(bob, duplicatedBuilder.build());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUserCantUpdateExperimentWithAlreadyExistedName() {
        final long bob = uc.createLab3AndBob();
        final long project = studyManagement.createProject(bob, new ProjectInfo("Title", "area", "", uc.getLab3()));
        final long file = uc.saveFile(bob);
        final long experimentType = anyExperimentType();
        final long specie = unspecified();

        final ExperimentInfo.Builder builder = new ExperimentInfo.Builder().name("Duplicated title").description("area").experimentType(experimentType).specie(specie);
        builder.project(project).lab(uc.getLab3()).billLab(uc.getLab3()).is2dLc(false).restriction(restriction(bob)).experimentLabels(new ExperimentLabelsInfo())
                .factors(NO_FACTORS).files(noFactoredFile(file)).bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES);
        studyManagement.createExperiment(bob, builder.build());

        builder.name("Title").description("another area");
        final long experiment = studyManagement.createExperiment(bob, builder.build());

        builder.name("Duplicated title").description("another area");
        studyManagement.updateExperiment(bob, experiment, builder.build());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreatingExperimentWithFactorMissing() {
        long bob = uc.createLab3AndBob();
        final long file = uc.saveFile(bob);
        final long experiment = experimentInNewProject(bob, uc.getLab3());
        final ExperimentSampleItem sample = sampleWithNoFactors(file);
        addFilesToExperiment(bob, experiment,
                of(new ExperimentManagementTemplate.MetaFactorTemplate("width", "kg", true, experiment)),
                of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0, preparedSample(file, ImmutableSet.of(sample)))), ImmutableList.<String>of());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreatingExperimentWithMoreFactors() {
        long bob = uc.createLab3AndBob();
        final long file = uc.saveFile(bob);
        final long experiment = experimentInNewProject(bob, uc.getLab3());
        final ExperimentSampleItem sample = sampleWithFactors(file, of("2"));
        addFilesToExperiment(bob, experiment,
                ImmutableList.<ExperimentManagementTemplate.MetaFactorTemplate>of(),
                of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0, preparedSample(file, ImmutableSet.of(sample)))), ImmutableList.<String>of());
    }

    @Test
    public void testAddingFilesToExperiment() {
        final long bob = uc.createLab3AndBob();
        final long experiment = experiment(bob, uc.getLab3());
        final FileItem file0 = (FileItem)detailsReader.readExperiment(bob, experiment).files.iterator().next();
        final long instrument = instrumentFromExperimentFile(bob, experiment);
        final long file1 = uc.saveFile(bob, instrument);
        final long file2 = uc.saveFile(bob, instrument);
        final ExperimentSampleItem sample1 = sampleWithFactors(file1, of("1"));
        final ExperimentSampleItem sample2 = sampleWithFactors(file2, of("2"));
        addFilesToExperiment(bob, experiment, of(factor(experiment)), of(
                new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, ImmutableSet.of(sample1))),
                new com.infoclinika.mssharing.model.write.FileItem(file2, false, 0, preparedSample(file2, ImmutableSet.of(sample2)))), of("3"));

        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);

        assertTrue(Arrays.deepEquals(experimentItem.factorValues, new String[][]{{"3"}, {"1"}, {"2"}}));
        final ImmutableMap<String, ExperimentSampleItem> sampleNameToSampleMap = extractSampleNameToSample(experimentItem);
        assertEquals(sampleNameToSampleMap.get(composeSampleUniqueKey(sample1)).name, sample1.name);
        assertEquals(sampleNameToSampleMap.get(composeSampleUniqueKey(sample2)).name, sample2.name);

        final ExperimentSampleItem sample0 = file0.preparedSample.samples.iterator().next();
        assertEquals(sampleNameToSampleMap.get(composeSampleUniqueKey(sample0)).factorValues.size(), 1);
        assertEquals(sampleNameToSampleMap.get(composeSampleUniqueKey(sample0)).factorValues.get(0), "3");
        assertEquals(sampleNameToSampleMap.get(composeSampleUniqueKey(sample1)).factorValues.get(0), "1");
        assertEquals(sampleNameToSampleMap.get(composeSampleUniqueKey(sample2)).factorValues.get(0), "2");
    }

    @Test
    public void testRemoveFileFromExperiment() {
        final long bob = uc.createLab3AndBob();
        final long project = studyManagement.createProject(bob, new ProjectInfo(PROJECT_TITLE, "DNA", "Some proj", uc.getLab3()));
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long file1 = uc.saveFile(bob, instrumentFromExperimentFile(bob, experiment));
        final long file2 = uc.saveFile(bob, instrumentFromExperimentFile(bob, experiment));
        final ExperimentSampleItem sample1 = sampleWithFactors(file1, of("1"));
        final ExperimentSampleItem sample2 = sampleWithFactors(file2, of("2"));
        addFilesToExperiment(bob, experiment, of(factor(experiment)), of(
                new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, ImmutableSet.of(sample1))),
                        new com.infoclinika.mssharing.model.write.FileItem(file2, false, 0, preparedSample(file2, ImmutableSet.of(sample2)))), of("3"));

        final ExperimentInfo.Builder builder = new ExperimentInfo.Builder().name("").description("").experimentType(anyExperimentType()).specie(anySpecies())
                .project(project).billLab(uc.createLab3()).is2dLc(false).restriction(restriction(bob)).factors(of(factor(experiment)))
                .files(of(new com.infoclinika.mssharing.model.write.FileItem(file2, false, 0, preparedSample(file2, ImmutableSet.of(sample2)))))
                .lockMasses(NO_LOCK_MASSES).experimentLabels(new ExperimentLabelsInfo()).sampleTypesCount(1);

        studyManagement.updateExperiment(bob, experiment, builder.build());
    }

    @Test
    public void testRemovingFactorFromExperiment() {
        final long bob = uc.createLab3AndBob();
        final long experiment = experiment(bob, uc.getLab3());
        final long instrument = instrumentFromExperimentFile(bob, experiment);
        final long file1 = uc.saveFile(bob, instrument);
        final long file2 = uc.saveFile(bob, instrument);
        final long file3 = uc.saveFile(bob, instrument);
        final long file4 = uc.saveFile(bob, instrument);
        final ExperimentSampleItem sample1 = sampleWithFactors(file1, of("1", "42"));
        final ExperimentSampleItem sample2 = sampleWithFactors(file2, of("2", "98"));
        final ExperimentSampleItem sample3 = sampleWithFactors(file3, of("42"));
        final ExperimentSampleItem sample4 = sampleWithFactors(file4, of("98"));
        addFilesToExperiment(bob, experiment, of(factor(experiment), factor(experiment)), of(
                new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, ImmutableSet.of(sample1))),
                new com.infoclinika.mssharing.model.write.FileItem(file2, false, 0, preparedSample(file2, ImmutableSet.of(sample2)))), of("3", "50"));
        addFilesToExperiment(bob, experiment, of(factor(experiment)), of(
                new com.infoclinika.mssharing.model.write.FileItem(file3, false, 0, preparedSample(file3, ImmutableSet.of(sample3))),
                new com.infoclinika.mssharing.model.write.FileItem(file4, false, 0, preparedSample(file4, ImmutableSet.of(sample4)))), of("50"));
        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);
        assertTrue(Arrays.deepEquals(experimentItem.factorValues, new String[][]{{"50"}, {"50"}, {"50"}, {"42"}, {"98"}}));


    }

    @Test
    public void testAddingFactorToExperiment() {
        final long bob = uc.createLab3AndBob();
        final long experiment = experiment(bob, uc.getLab3());
        final long instrument = instrumentFromExperimentFile(bob, experiment);
        final long file1 = uc.saveFile(bob, instrument);
        final long file2 = uc.saveFile(bob, instrument);
        final long file3 = uc.saveFile(bob, instrument);
        final long file4 = uc.saveFile(bob, instrument);
        final ExperimentSampleItem sample1 = sampleWithFactors(file1, of("42"));
        final ExperimentSampleItem sample2 = sampleWithFactors(file2, of("98"));
        final ExperimentSampleItem sample3 = sampleWithFactors(file3, of("1", "42"));
        final ExperimentSampleItem sample4 = sampleWithFactors(file4, of("2", "98"));
        addFilesToExperiment(bob, experiment, of(factor(experiment)), of(
                new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, ImmutableSet.of(sample1))),
                new com.infoclinika.mssharing.model.write.FileItem(file2, false, 0, preparedSample(file2, ImmutableSet.of(sample2)))), of("50"));
        addFilesToExperiment(bob, experiment, of(factor(experiment), factor(experiment)), of(
                new com.infoclinika.mssharing.model.write.FileItem(file3, false, 0, preparedSample(file3, ImmutableSet.of(sample3))),
                new com.infoclinika.mssharing.model.write.FileItem(file4, false, 0, preparedSample(file4, ImmutableSet.of(sample4)))), of("3", "50"));
        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);
        assertTrue(Arrays.deepEquals(experimentItem.factorValues, new String[][]{{"3", "50"},
                {"3", "50"}, {"3", "50"},
                {"1", "42"}, {"2", "98"}
        }));
        assertEquals(experimentItem.files.size(), 5);
        final ImmutableMap<String, ExperimentSampleItem> sampleNameToSampleMap = extractSampleNameToSample(experimentItem);
        assertEquals(sampleNameToSampleMap.size(), 5);
        assertEquals(sampleNameToSampleMap.get(composeSampleUniqueKey(sample3)).factorValues.get(0), "1");
        assertEquals(sampleNameToSampleMap.get(composeSampleUniqueKey(sample3)).factorValues.get(1), "42");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAllFactorValuesShouldBeNotEmpty() {
        final long bob = uc.createLab3AndBob();
        final long experiment = experiment(bob, uc.getLab3());
        final long file1 = uc.saveFile(bob, instrumentFromExperimentFile(bob, experiment));
        final ExperimentSampleItem sample1 = sampleWithFactors(file1, of(""));
        addFilesToExperiment(bob, experiment, of(factor(experiment)), of(
                new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, ImmutableSet.of(sample1)))), of(""));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAllFactorValuesShouldBeSpecified() {
        final long bob = uc.createLab3AndBob();
        final long experiment = experiment(bob, uc.getLab3());
        final long instrument = instrumentFromExperimentFile(bob, experiment);
        final long file1 = uc.saveFile(bob, instrument);
        final ExperimentSampleItem sample1 = sampleWithFactors(file1, of("a", ""));
        addFilesToExperiment(bob, experiment, of(factor(experiment), factor(experiment)), of(
                new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, ImmutableSet.of(sample1)))), of("b", ""));
    }

    @Test(expectedExceptions = InvalidFactorException.class)
    public void testShouldNotAllowToCreateFactorWithoutName() {
        final long bob = uc.createLab3AndBob();
        final long experiment = experiment(bob, uc.getLab3());
        final long instrument = instrumentFromExperimentFile(bob, experiment);
        final long file1 = uc.saveFile(bob, instrument);
        final ExperimentSampleItem sample1 = sampleWithFactors(file1, of("q"));
        addFilesToExperiment(bob, experiment, of(new ExperimentManagementTemplate.MetaFactorTemplate("", "", false, experiment)), of(
                new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, ImmutableSet.of(sample1)))), of("a"));
    }

    @Test
    public void testRemovingUserFromLaboratoryWithExperiments() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();//labHead
        final long publicProject = createPublicProject(bob, uc.getLab3());
        createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), publicProject);
        createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), publicProject);

        assertEquals(dashboardReader.readUsersByLab(poll, uc.getLab3()).size(), 2);
        final int pollProjectsLength = dashboardReader.readProjects(poll, Filter.MY).size();
        final int pollExperimentsLength = dashboardReader.readExperiments(poll, Filter.MY).size();
        assertEquals(dashboardReader.readProjects(bob, Filter.MY).size(), 1);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 2);
        labHeadManagement.removeUserFromLab(poll, uc.getLab3(), bob);
        assertEquals(dashboardReader.readProjects(bob, Filter.MY).size(), 0);
        assertEquals(dashboardReader.readProjects(poll, Filter.MY).size(), pollProjectsLength + 1);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 0);
        assertEquals(dashboardReader.readExperiments(poll, Filter.MY).size(), pollExperimentsLength + 2);
    }

    @Test
    public void testRemovingUserFromLaboratoryWithFiles() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();//labHead
        final long publicProject = createPublicProject(bob, uc.getLab3());
        final long experiment1 = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), publicProject);


        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);
        final ExperimentSampleItem sample1 = sampleWithFactors(file1, of("1", "42"));
        final ExperimentSampleItem sample2 = sampleWithFactors(file1, of("2", "98"));
        addFilesToExperiment(bob, experiment1, of(factor(experiment1), factor(experiment1)), of(
                new com.infoclinika.mssharing.model.write.FileItem(file1, false, 0, preparedSample(file1, ImmutableSet.of(sample1))),
                        new com.infoclinika.mssharing.model.write.FileItem(file2, false, 0, preparedSample(file2, ImmutableSet.of(sample2)))), of("3", "50"));


        assertEquals(dashboardReader.readUsersByLab(poll, uc.getLab3()).size(), 2);
        final int pollProjectsLength = dashboardReader.readProjects(poll, Filter.MY).size();
        final int pollExperimentsLength = dashboardReader.readExperiments(poll, Filter.MY).size();
        final int pollFilesLength = fileReader.readFiles(poll, Filter.MY).size();
        assertEquals(dashboardReader.readProjects(bob, Filter.MY).size(), 1);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 1);
        assertEquals(fileReader.readFiles(bob, Filter.MY).size(), 3);
        labHeadManagement.removeUserFromLab(poll, uc.getLab3(), bob);
        assertEquals(dashboardReader.readProjects(bob, Filter.MY).size(), 0);
        assertEquals(dashboardReader.readProjects(poll, Filter.MY).size(), pollProjectsLength + 1);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 0);
        assertEquals(dashboardReader.readExperiments(poll, Filter.MY).size(), pollExperimentsLength + 1);
        assertEquals(fileReader.readFiles(bob, Filter.MY).size(), 0);
        assertEquals(fileReader.readFiles(poll, Filter.MY).size(), pollFilesLength);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDontAllowToCreateExperimentWithEmptyName() {
        final long bob = uc.createLab3AndBob();
        final long project = studyManagement.createProject(bob, new ProjectInfo("Title", "area", "", uc.getLab3()));
        final ExperimentInfo.Builder builder = new ExperimentInfo.Builder().name("Duplicated title").description("area").experimentType(anyExperimentType()).specie(anySpecies())
                .project(project).lab(uc.getLab3()).billLab(uc.getLab3()).is2dLc(false).restriction(restriction(bob)).factors(NO_FACTORS)
                .files(noFactoredFile(uc.saveFile(bob))).bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES);


        studyManagement.createExperiment(bob, builder.build());
    }

    private ExperimentManagementTemplate.MetaFactorTemplate factor(long experimentId) {
        return new ExperimentManagementTemplate.MetaFactorTemplate(generateString(), "", false, experimentId);
    }

    public long experiment(long user, long lab) {
        final long project = studyManagement.createProject(user, new ProjectInfo(PROJECT_TITLE, "DNA", "Some proj", lab));
        return createInstrumentAndExperimentWithOneFile(user, lab, project);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNotAllowCreateExperimentWithoutFiles() {
        final long bob = uc.createLab3AndBob();
        final long project = studyManagement.createProject(bob, new ProjectInfo("Title", "area", "", uc.getLab3()));

        final ExperimentInfo.Builder builder = new ExperimentInfo.Builder().name("Duplicated title").description("area").experimentType(anyExperimentType()).specie(anySpecies())
                .project(project).lab(uc.getLab3()).billLab(uc.getLab3()).is2dLc(false).restriction(restriction(bob)).factors(NO_FACTORS)
                .files(Collections.<com.infoclinika.mssharing.model.write.FileItem>emptyList()).bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES);

        studyManagement.createExperiment(bob, builder.build());
    }

    @Test
    public void testInstrumentsDisplayedInWizard() {
        final long bob = uc.createLab3AndBob();
        final long model = anyInstrumentModel();
        createInstrumentByModel(bob, uc.getLab3(), model);
        final long instrument = createInstrumentByModel(bob, uc.getLab3(), model);
        uc.saveFile(bob, instrument);
        assertTrue(experimentCreationHelper.availableInstrumentsByModel(bob, model).size() == 2);
    }

    @Test(expectedExceptions = ObjectNotFoundException.class)
    public void testCantReadRemovedExperimentDetails() {
        final long bob = uc.createLab3AndBob();
        final long experiment = createExperiment(bob, createPrivateProject(bob, uc.getLab3()), uc.getLab3());
        studyManagement.moveExperimentToTrash(bob, experiment);
        detailsReader.readExperiment(bob, experiment);
    }

    @Test
    public void testDownloadTokenAvailableForPublicExperiment() {
        long bob = uc.createLab3AndBob();
        final long project = createPublicProject(bob, uc.getLab3());
        long experimentWithOneFile = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        String downloadLink = get(dashboardReader.readExperiments(bob, Filter.ALL), 0).downloadLink;
        String token = downloadLink.substring(downloadLink.lastIndexOf("/") + 1);
        assertTrue(experimentDownloadHelper.isDownloadTokenAvailable(token));
        assertEquals(experimentDownloadHelper.getExperimentByDownloadToken(token).experiment, experimentWithOneFile);
    }

    @Test
    public void testDownloadTokenDeletedIfChangeSharing() {
        long bob = uc.createLab3AndBob();
        final long project = createPublicProject(bob, uc.getLab3());
        createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        uc.shareProjectToKateInGroup(bob, project);
        assertNull(get(dashboardReader.readExperiments(bob, Filter.ALL), 0).downloadLink);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCantCreateExperimentWithInvalidFilesAndSpeciesCombination() {
        final long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long anySpecies = anySpecies();
        final long anotherSpecies = anotherSpecie(anySpecies);
        final long file = createFile(bob, anySpecies, anyInstrumentModel());

        createExperiment(bob, project, instrumentModel(bob, file), file, anotherSpecies);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCantCreateExperimentWithInvalidFilesAndInstrumentModelCombination() {
        final long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long anySpecies = anySpecies();
        final long vendor = thermoVendor();
        final long model = anyInstrumentModelByVendor(vendor);
        final long anotherModel = anotherInstrumentModel(vendor, model);
        final long file = createFile(bob, anySpecies, model);

        createExperiment(bob, project, anotherModel, file, unspecified());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCantUpdateExperimentWithInvalidFilesAndSpeciesCombination() {
        final long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long anySpecies = anySpecies();
        final long file = createFile(bob, anySpecies, anyInstrumentModel());

        final long experiment = createExperiment(bob, project, instrumentModel(bob, file), file, anySpecies);

        final long anotherSpecies = anotherSpecie(anySpecies);
        final ExperimentItem details = detailsReader.readExperiment(bob, experiment);

        final ExperimentInfo.Builder builder = new ExperimentInfo.Builder().name(generateString()).description(generateString()).experimentType(anyExperimentType()).specie(anotherSpecies)
                .billLab(uc.createLab3()).project(details.project).lab(details.lab).billLab(uc.createLab3()).is2dLc(details.is2dLc)
                .restriction(restrictionForExperiment(details)).factors(NO_FACTORS)
                .files(noFactoredFile(file)).lockMasses(NO_LOCK_MASSES).experimentLabels(new ExperimentLabelsInfo());

        studyManagement.updateExperiment(bob, experiment, builder.build());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCantUpdateExperimentWithInvalidFilesAndInstrumentModelCombination() {
        final long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long anySpecies = anySpecies();
        final long vendor = thermoVendor();
        final long model = anyInstrumentModelByVendor(vendor);
        final long file = createFile(bob, anySpecies, model);

        final long experiment = createExperiment(bob, project, model, file, unspecified());
        final long anotherModel = anotherInstrumentModel(vendor, model);
        final ExperimentItem details = detailsReader.readExperiment(bob, experiment);
        final ExperimentInfo.Builder builder = new ExperimentInfo.Builder().name(generateString()).description(generateString()).experimentType(anyExperimentType()).specie(details.specie)
                .project(details.project).billLab(uc.createLab3()).is2dLc(details.is2dLc)
                .restriction(restrictionFromModelAndInstrument(anotherModel, details.instrument.get())).experimentLabels(new ExperimentLabelsInfo())
                .factors(NO_FACTORS).files(noFactoredFile(file)).lockMasses(NO_LOCK_MASSES);

        studyManagement.updateExperiment(bob, experiment, builder.build());
    }

    @Test
    public void testCanArchiveExperiment() throws ExecutionException, InterruptedException {
        setBilling(true);
        final long bob = uc.createLab3AndBob();
        billingManagement.makeLabAccountEnterprise(uc.createPaul(), uc.getLab3());
        final long experiment = createExperiment(bob, uc.createProject(bob));
        fileOperationsManager.markExperimentFilesToArchive(bob, experiment);
        fileOperationsManager.archiveMarkedFiles();
        final Set<FileLine> files = fileReader.readFilesByExperiment(bob, experiment);
        assertTrue(all(files, new Predicate<FileLine>() {
            @Override
            public boolean apply(FileLine input) {
                return input.storageStatus.equals(DashboardReader.StorageStatus.ARCHIVED);
            }
        }));
    }

    @Test
    public void testCanUnarchiveExperiment() throws ExecutionException, InterruptedException {
        setBilling(true);
        final long bob = uc.createLab3AndBob();
        billingManagement.makeLabAccountEnterprise(uc.createPaul(), uc.getLab3());
        final long experiment = createExperiment(bob, uc.createProject(bob));
        fileOperationsManager.markExperimentFilesToArchive(bob, experiment);
        fileOperationsManager.archiveMarkedFiles();
        fileOperationsManager.markExperimentFilesToUnarchive(bob, experiment);
        fileOperationsManager.unarchiveMarkedFiles();
        final Set<FileLine> files = fileReader.readFilesByExperiment(bob, experiment);
        Assert.assertTrue(all(files, new Predicate<FileLine>() {
            @Override
            public boolean apply(FileLine input) {
                final FileItem fileDetails = detailsReader.readFile(bob, input.id);
                return fileDetails.storageStatus.equals(DashboardReader.StorageStatus.UNARCHIVED);
            }
        }));
    }

    @Test
    public void testCreateExperimentAttachments() {
        final long bob = uc.createLab3AndBob();
        final long experiment = createExperiment(bob, uc.createProject(bob));
        final long pdfAttachment = attachmentForExperiment(bob, experiment);
        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);
        Assert.assertTrue(any(experimentItem.attachments, isGivenAttachment(pdfAttachment)));
    }

    @Test
    public void testRemoveExperimentAttachments() {
        final long bob = uc.createLab3AndBob();
        final long experiment = createExperiment(bob, uc.createProject(bob));
        attachmentForExperiment(bob, experiment);
        attachmentManagement.updateExperimentAttachments(bob, experiment, ImmutableSet.<Long>of());
        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);
        Assert.assertTrue(experimentItem.attachments.size() == 0);
    }

    @Test
    public void testUpdateExperimentAttachments() {
        final long bob = uc.createLab3AndBob();
        final long experiment = createExperiment(bob, uc.createProject(bob));
        attachmentForExperiment(bob, experiment);
        final long otherAttachment = attachmentManagement.newAttachment(bob, "otherAttachment.img", 1024 * 1024);
        attachmentManagement.updateExperimentAttachments(bob, experiment, ImmutableSet.of(otherAttachment));
        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);
        Assert.assertTrue(any(experimentItem.attachments, isGivenAttachment(otherAttachment)));
    }

    @Test
    public void testReadExperimentAttachments() {
        final long bob = uc.createLab3AndBob();
        final long experiment = createExperiment(bob, uc.createProject(bob));
        final long attachmentId = attachmentForExperiment(bob, experiment);
        assertThat(attachmentsReader.readAttachment(bob, attachmentId).id, is(attachmentId));
        assertThat(attachmentsReader.readAttachments(EXPERIMENT, bob, experiment).size(), is(1));
    }

    @Test
    public void createExperimentWithLabelsWithRightExperimentType() {
        final long bob = uc.createLab3AndBob();
        final long experimentLabelType = getExperimentLabelType();
        final List<ExperimentLabelReader.ExperimentLabelItem> allLabels = experimentLabelReader.readLabels(experimentLabelType);
        final List<Long> lightLabels;
        final List<Long> mediumLabels;
        if (allLabels.size() > 4) {
            lightLabels = transform(allLabels.subList(0, 2), EXTRACT_ID);
            mediumLabels = transform(allLabels.subList(2, allLabels.size()), EXTRACT_ID);
        } else {
            lightLabels = transform(allLabels, EXTRACT_ID);
            mediumLabels = newArrayList();
        }
        final ArrayList<Long> heavyLabels = newArrayList();
        final long experiment = createExperimentWithLabels(bob, uc.createProject(bob), experimentTypeLabeled(), new ExperimentLabelsInfo(lightLabels, mediumLabels, heavyLabels));
        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);
        assertTrue(experimentItem.labels.heavyLabels.containsAll(heavyLabels));
        assertTrue(experimentItem.labels.mediumLabels.containsAll(mediumLabels));
        assertTrue(experimentItem.labels.lightLabels.containsAll(lightLabels));

    }

    @Test
    public void createExperimentWithLightAndMediumLabelsAndSamples() {
        final long bob = uc.createLab3AndBob();
        final long experimentLabelType = getExperimentLabelType();
        final List<ExperimentLabelReader.ExperimentLabelItem> allLabels = experimentLabelReader.readLabels(experimentLabelType);
        final List<Long> lightLabels;
        final List<Long> mediumLabels;
        if (allLabels.size() > 4) {
            lightLabels = transform(allLabels.subList(0, 2), EXTRACT_ID);
            mediumLabels = transform(allLabels.subList(2, allLabels.size()), EXTRACT_ID);
        } else {
            lightLabels = transform(allLabels, EXTRACT_ID);
            mediumLabels = newArrayList();
        }
        final ArrayList<Long> heavyLabels = newArrayList();
        final long experiment = createExperimentWithLabels(bob, uc.createProject(bob), experimentTypeLabeled(), new ExperimentLabelsInfo(lightLabels, mediumLabels, heavyLabels));
        final ExperimentItem experimentItem = detailsReader.readExperiment(bob, experiment);
        assertTrue(experimentItem.labels.heavyLabels.containsAll(heavyLabels));
        assertTrue(experimentItem.labels.mediumLabels.containsAll(mediumLabels));
        assertTrue(experimentItem.labels.lightLabels.containsAll(lightLabels));

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void createExperimentWithLabelsWithExperimentTypeWhichDoesntSupportLabels() {
        final long bob = uc.createLab3AndBob();
        final long experimentLabelType = getExperimentLabelType();
        final List<ExperimentLabelReader.ExperimentLabelItem> allLabels = experimentLabelReader.readLabels(experimentLabelType);
        final List<Long> lightLabels;
        final List<Long> mediumLabels;
        if (allLabels.size() > 4) {
            lightLabels = transform(allLabels.subList(0, 2), EXTRACT_ID);
            mediumLabels = transform(allLabels.subList(2, allLabels.size()), EXTRACT_ID);
        } else {
            lightLabels = transform(allLabels, EXTRACT_ID);
            mediumLabels = newArrayList();
        }
        final long experiment = createExperimentWithLabels(bob, uc.createProject(bob), experimentTypeNotLabeled(), new ExperimentLabelsInfo(lightLabels, mediumLabels, newArrayList()));
    }


    private static final Function<ExperimentLabelReader.ExperimentLabelItem, Long> EXTRACT_ID = new Function<ExperimentLabelReader.ExperimentLabelItem, Long>() {
        @Override
        public Long apply(ExperimentLabelReader.ExperimentLabelItem experimentLabelItem) {
            return experimentLabelItem.id;
        }
    };

    private Predicate<DetailsReader.AttachmentItem> isGivenAttachment(final long otherAttachment) {
        return new Predicate<DetailsReader.AttachmentItem>() {
            @Override
            public boolean apply(DetailsReader.AttachmentItem input) {
                return input.id == otherAttachment;
            }
        };
    }


    private long createFile(long bob, long species, long model) {
        return createFile(bob, species, model, generateString());
    }

    private long createFile(long bob, long species, long model, String name) {
        final long file = instrumentManagement.createFile(bob, createInstrumentByModel(bob, uc.getLab3(), model), new FileMetaDataInfo(name, 0, "", null, species, false));
        uc.updateFileContent(bob, file);
        return file;
    }

    private long createExperiment(long bob, long project, long instrumentModel, long file, long species) {
        final ExperimentInfo.Builder builder = experimentInfo(species).project(project).lab(uc.getLab3())
                .billLab(uc.getLab3()).is2dLc(false).restriction(restrictionFromModel(instrumentModel))
                .factors(NO_FACTORS).files(noFactoredFile(file)).bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES).experimentLabels(new ExperimentLabelsInfo())
                .sampleTypesCount(1);

        return studyManagement.createExperiment(bob, builder.build());
    }


}
