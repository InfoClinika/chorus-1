/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.model.test.study;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.InvalidFactorException;
import com.infoclinika.mssharing.platform.model.ObjectNotFoundException;
import com.infoclinika.mssharing.platform.model.common.items.NamedItem;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.testing.helper.Data;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.FileItemTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.Restriction;
import com.infoclinika.mssharing.platform.model.write.FileManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.ProjectManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.ProjectManagementTemplate.ProjectInfoTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Iterables.*;
import static com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate.AttachmentType.EXPERIMENT;
import static com.infoclinika.mssharing.platform.model.testing.helper.Data.EMPTY_ANNOTATIONS;
import static com.infoclinika.mssharing.platform.model.testing.helper.Data.PROJECT_TITLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Stanislav Kurilin
 */
@SuppressWarnings("unused")
public class ExperimentTest extends AbstractStudyTest {
    @Inject
    private AttachmentsReaderTemplate attachmentsReader;
    //experiment creation

    @Inject
    private ExperimentDownloadHelperTemplate experimentDownloadHelper;

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
        assertEquals(ownedExperiments.size(), 1);
        assertEquals(ownedExperiments.iterator().next().id, experiment);
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
        //noinspection unchecked
        final ExperimentInfoTemplateBuilder builder = experimentInfo()
            .project(project)
            .lab(uc.getLab3())
            .is2Dlc(false)
            .restriction(restriction(bob))
            .factors(NO_FACTORS)
            .files(noFactoredFile(file1));

        final long experiment = experimentManagement.createExperiment(bob, builder.build());

        final long file2 = uc.saveFile(bob);
        final String factorName = generateString();
        final String factorValue = "2";
        addFilesToExperiment(bob, experiment,
            of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
            of(new FileItemTemplate(file2, of(factorValue), EMPTY_ANNOTATIONS, false)), of("3")
        );

        final long project2 = createPublicProject(john);
        createExperiment(john, project2);
        final SortedSet<? extends ExperimentReaderTemplate.ExperimentLineTemplate> experiments =
            experimentReader.readExperiments(john, Filter.MY);
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
        assertEquals(
            size(experimentCreationHelper.availableFilesByInstrument(bob, anotherSpecie(specie), instrument)), 1);
        assertEquals(
            size(experimentCreationHelper.availableFilesByInstrument(bob, unspecifiedSpecie(), instrument)), 2);
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
    public void testOnlyOwnersLabFilesAreAvailable() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long kate = uc.createKateAndLab2();
        final long model = anyInstrumentModel();
        final long instrument = createInstrumentBySpecifiedInstrumentModel(bob, uc.getLab3(), model);
        final long anotherInstrument = createInstrumentBySpecifiedInstrumentModel(kate, uc.getLab2(), model);
        uc.saveFile(bob, instrument);
        uc.saveFile(kate, anotherInstrument);
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
        final long experimentId = createInstrumentAndExperiment(
            bob,
            uc.getLab3(),
            projectId,
            NO_FACTORS,
            noFactoredFiles(of(file1, file2, file3))
        );
        final SortedSet<? extends ExperimentReaderTemplate.ExperimentLineTemplate> experiments
            = experimentReader.readExperiments(bob, Filter.ALL);

        assertEquals(Iterables.size(experiments), 1);
        final ExperimentReaderTemplate.ExperimentLineTemplate experiment = experiments.iterator().next();
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
        final long projectId = projectManagement.createProject(
            bob,
            new ProjectInfoTemplate(uc.getLab3(), projectName, generateString(), generateString())
        );
        long specie = unspecified();
        final ImmutableList<FileItemTemplate> files = anyFile(bob);

        //noinspection unchecked
        final ExperimentInfoTemplateBuilder builder = new ExperimentInfoTemplateBuilder()
            .name(experimentName)
            .description(experimentDescription)
            .experimentType(experimentType)
            .species(specie)
            .project(projectId)
            .lab(uc.getLab3())
            .is2Dlc(false).restriction(restriction(bob, files.get(0).id)).factors(NO_FACTORS)
            .files(files);

        final long experimentId = experimentManagement.createExperiment(bob, builder.build());

        final DetailsReaderTemplate.ExperimentItemTemplate experimentItem =
            detailsReader.readExperiment(bob, experimentId);
        assertNotNull(experimentItem.files);
        assertNotNull(experimentItem.factors);
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

        final DetailsReaderTemplate.ExperimentItemTemplate experimentItem =
            detailsReader.readExperiment(bob, experimentId);
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
        addFilesToExperiment(bob, experiment,
            of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
            of(new FileItemTemplate(file, of(factorValue), EMPTY_ANNOTATIONS, false)), of(factorValue)
        );

        final DetailsReaderTemplate.ExperimentItemTemplate experimentItem =
            detailsReader.readExperiment(bob, experiment);

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

        final int experimentsLengthWithNewExperiment = experimentReader.readExperiments(bob, Filter.MY).size();
        assertEquals(experimentsLengthWithNewExperiment, 1);
        experimentManagement.deleteExperiment(bob, experiment);
        final int experimentsLengthWithoutNewExperiment = experimentReader.readExperiments(bob, Filter.MY).size();
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
        addFilesToExperiment(kate, experiment,
            of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
            of(new FileItemTemplate(file, of(factorValue), EMPTY_ANNOTATIONS, false)), of(factorValue)
        );

        final int experimentsLengthWithNewExperiment = experimentReader.readExperiments(kate, Filter.MY).size();
        assertEquals(experimentsLengthWithNewExperiment, 1);

        experimentManagement.deleteExperiment(kate, experiment);

        final int experimentsLengthWithoutNewExperiment = experimentReader.readExperiments(kate, Filter.MY).size();
        assertEquals(experimentsLengthWithoutNewExperiment, 0);
    }

    @Test(expectedExceptions = AccessDenied.class, dependsOnMethods = "testCreatingExperimentWithFactoredFile")
    public void testRemovingPublicExperiment() {
        long john = uc.createJohnWithoutLab();
        long bob = uc.createLab3AndBob();
        final long project = createPublicProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);

        final int experimentsLengthWithNewExperiment = experimentReader.readExperiments(bob, Filter.ALL).size();
        assertEquals(experimentsLengthWithNewExperiment, 1);
        experimentManagement.deleteExperiment(john, experiment);
    }

    @Test(expectedExceptions = AccessDenied.class, dependsOnMethods = "testRemovingExperiment")
    public void testRemovingUsedFile() {
        long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long file = uc.saveFile(bob, instrumentFromExperimentFile(bob, experiment));
        final String factorName = generateString();
        final String factorValue = "2";
        addFilesToExperiment(bob, experiment,
            of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
            of(new FileItemTemplate(file, of(factorValue), EMPTY_ANNOTATIONS, false)), of(factorValue)
        );
        final int experimentsLengthWithNewExperiment = experimentReader.readExperiments(bob, Filter.MY).size();
        assertEquals(experimentsLengthWithNewExperiment, 1);
        fileManagement.deleteFile(bob, file, true);
    }

    @Test(dependsOnMethods = "testRemovingExperiment")
    public void testRemovingUnusedFile() {
        long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long file = uc.saveFile(bob, instrumentFromExperimentFile(bob, experiment));
        final String factorName = generateString();
        final String factorValue = "2";
        addFilesToExperiment(bob, experiment,
            of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
            of(new FileItemTemplate(file, of(factorValue), EMPTY_ANNOTATIONS, false)), of(factorValue)
        );
        final int experimentsLengthWithNewExperiment = experimentReader.readExperiments(bob, Filter.MY).size();
        assertEquals(experimentsLengthWithNewExperiment, 1);
        experimentManagement.deleteExperiment(bob, experiment);
        final int experimentsLengthWithoutExperiment = experimentReader.readExperiments(bob, Filter.MY).size();
        assertEquals(experimentsLengthWithoutExperiment, 0);
        assertEquals(fileReader.readFiles(bob, Filter.ALL).size(), 2);
        fileManagement.deleteFile(bob, file, true);
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
        addFilesToExperiment(bob, experiment,
            of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
            of(new FileItemTemplate(file, of(factorValue), EMPTY_ANNOTATIONS, false)), of(factorValue)
        );

        addFilesToExperiment(bob, experiment2,
            of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
            of(new FileItemTemplate(file, of(factorValue), EMPTY_ANNOTATIONS, false)), of(factorValue)
        );

        final int experiments = experimentReader.readExperiments(bob, Filter.MY).size();
        assertEquals(experiments, 2);
        experimentManagement.deleteExperiment(bob, experiment);
        experimentManagement.deleteExperiment(bob, experiment2);

        final int noExperiments = experimentReader.readExperiments(bob, Filter.MY).size();
        assertEquals(noExperiments, 0);
        assertEquals(fileReader.readFiles(bob, Filter.ALL).size(), 3);
        fileManagement.deleteFile(bob, file, true);
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
        addFilesToExperiment(bob, experiment,
            of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
            of(new FileItemTemplate(file, of(factorValue), EMPTY_ANNOTATIONS, false)),
            of(factorValue)
        );

        assertEquals(fileReader.readFiles(bob, Filter.ALL).size(), 2);
        fileManagement.deleteFile(bob, file, true);
    }


    @Test
    public void testCreatingExperimentWithAnnotatedFile() {
        long bob = uc.createLab3AndBob();
        final long file = uc.saveFile(bob);
        final long experiment = experimentInNewProject(bob, uc.getLab3());

        final String fractionNumber = generateString();
        final String sampleId = generateString();
        final ImmutableList<ExperimentManagementTemplate.AnnotationTemplate> annotations = ImmutableList.of(
            new ExperimentManagementTemplate.AnnotationTemplate("fractionNumber", fractionNumber, "", false),
            new ExperimentManagementTemplate.AnnotationTemplate("sampleId", sampleId, "", false)
        );
        addFilesToExperiment(bob,
            experiment,
            Collections.emptyList(),
            of(new FileItemTemplate(file, Collections.emptyList(), annotations, false)),
            Collections.emptyList()
        );

        final DetailsReaderTemplate.ExperimentItemTemplate experimentItem =
            detailsReader.readExperiment(bob, experiment);
        final DetailsReaderTemplate.FileItemTemplate fileItemTemplate =
            detailsReader.readFile(bob, experimentItem.files.get(0).id);
        assertEquals(find(fileItemTemplate.annotations, annotationsByName("fractionNumber")).value, fractionNumber);
        assertEquals(find(fileItemTemplate.annotations, annotationsByName("sampleId")).value, sampleId);
    }

    protected Predicate<DetailsReaderTemplate.AnnotationItem> annotationsByName(final String name) {
        return input -> input.name.equals(name);
    }

    @Test
    public void testFactoredValuesSetProperly() {
        long bob = uc.createLab3AndBob();
        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);
        final long experiment = experimentInNewProject(bob, uc.getLab3());

        addFilesToExperiment(bob, experiment,
            of(
                new ExperimentManagementTemplate.MetaFactorTemplate("f1", "g", true, experiment),
                new ExperimentManagementTemplate.MetaFactorTemplate("_f2", "", false, experiment),
                new ExperimentManagementTemplate.MetaFactorTemplate("f3", "", true, experiment)
            ),
            of(
                new FileItemTemplate(file1, of("1.23", "a bcd", "-1"), EMPTY_ANNOTATIONS, false),
                new FileItemTemplate(file2, of("23", "4", "7"), EMPTY_ANNOTATIONS, false)
            ), of("33", "4", "8")
        );

        final DetailsReaderTemplate.ExperimentItemTemplate experimentItem =
            detailsReader.readExperiment(bob, experiment);

        assertEquals(experimentItem.factors.size(), 3);
        assertEquals(experimentItem.files.size(), 3);
        assertTrue(Arrays.deepEquals(experimentItem.factorValues, new String[][] {
            {"1.23", "a bcd", "-1"},
            {"23", "4", "7"},
            {"33", "4", "8"}
        }));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreatingExperimentWithoutSpecifyingAllFactorValues() {
        long bob = uc.createLab3AndBob();
        final long file = uc.saveFile(bob);
        final long experiment = experimentInNewProject(bob, uc.getLab3());

        addFilesToExperiment(bob, experiment,
            of(
                new ExperimentManagementTemplate.MetaFactorTemplate("f1", "", false, experiment),
                new ExperimentManagementTemplate.MetaFactorTemplate(generateString(), "", true, experiment)
            ),
            of(new FileItemTemplate(file, of("v1"), EMPTY_ANNOTATIONS, false)), of("")
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUserCantCreateExperimentAlreadyExistedName() {
        final long bob = uc.createLab3AndBob();
        final long project = projectManagement
            .createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "Title", "area", ""));
        final long file = uc.saveFile(bob);
        final long experimentType = anyExperimentType();
        final long specie = unspecified();

        //noinspection unchecked
        final ExperimentInfoTemplateBuilder builder = new ExperimentInfoTemplateBuilder()
            .name("Duplicated title")
            .description("area")
            .experimentType(experimentType)
            .species(specie)
            .project(project)
            .lab(uc.getLab3())
            .is2Dlc(false)
            .restriction(restriction(bob))
            .factors(NO_FACTORS).files(noFactoredFile(file));
        experimentManagement.createExperiment(bob, builder.build());

        //noinspection unchecked
        final ExperimentInfoTemplateBuilder duplicatedBuilder = new ExperimentInfoTemplateBuilder()
            .name("Duplicated title")
            .description("another area")
            .experimentType(experimentType)
            .species(specie)
            .project(project)
            .lab(uc.getLab3())
            .is2Dlc(false)
            .restriction(restriction(bob)).factors(NO_FACTORS).files(noFactoredFile(file));
        experimentManagement.createExperiment(bob, duplicatedBuilder.build());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUserCantUpdateExperimentWithAlreadyExistedName() {
        final long bob = uc.createLab3AndBob();
        final long project = projectManagement
            .createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "Title", "area", ""));
        final long file = uc.saveFile(bob);
        final long experimentType = anyExperimentType();
        final long specie = unspecified();

        final ExperimentInfoTemplateBuilder builder =
            new ExperimentInfoTemplateBuilder().name("Duplicated title").description("area")
                .experimentType(experimentType).species(specie);
        //noinspection unchecked
        builder.project(project).lab(uc.getLab3())
            .is2Dlc(false)
            .restriction(restriction(bob))
            .factors(NO_FACTORS)
            .files(noFactoredFile(file));

        experimentManagement.createExperiment(bob, builder.build());

        builder.name("Title").description("another area");
        final long experiment = experimentManagement.createExperiment(bob, builder.build());

        builder.name("Duplicated title").description("another area");
        experimentManagement.updateExperiment(bob, experiment, builder.build());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreatingExperimentWithFactorMissing() {
        long bob = uc.createLab3AndBob();
        final long file = uc.saveFile(bob);
        final long experiment = experimentInNewProject(bob, uc.getLab3());
        addFilesToExperiment(bob, experiment,
            of(new ExperimentManagementTemplate.MetaFactorTemplate("width", "kg", true, experiment)),
            of(new FileItemTemplate(file, ImmutableList.<String>of(), EMPTY_ANNOTATIONS, false)),
            ImmutableList.<String>of()
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreatingExperimentWithMoreFactors() {
        long bob = uc.createLab3AndBob();
        final long file = uc.saveFile(bob);
        final long experiment = experimentInNewProject(bob, uc.getLab3());
        addFilesToExperiment(bob, experiment,
            ImmutableList.<ExperimentManagementTemplate.MetaFactorTemplate>of(),
            of(new FileItemTemplate(file, of("2"), EMPTY_ANNOTATIONS, false)), ImmutableList.<String>of()
        );
    }

    @Test
    public void testAddingFilesToExperiment() {
        final long bob = uc.createLab3AndBob();
        final long experiment = experiment(bob, uc.getLab3());
        final long instrument = instrumentFromExperimentFile(bob, experiment);
        final long file1 = uc.saveFile(bob, instrument);
        final long file2 = uc.saveFile(bob, instrument);
        addFilesToExperiment(bob, experiment, of(factor(experiment)), of(
            new FileItemTemplate(file1, of("1"), EMPTY_ANNOTATIONS, false),
            new FileItemTemplate(file2, of("2"), EMPTY_ANNOTATIONS, false)
        ), of("3"));
        final DetailsReaderTemplate.ExperimentItemTemplate experimentItem =
            detailsReader.readExperiment(bob, experiment);
        assertTrue(Arrays.deepEquals(experimentItem.factorValues, new String[][] {{"1"}, {"2"}, {"3"}}));
    }

    @Test
    public void testRemoveFileFromExperiment() {
        final long bob = uc.createLab3AndBob();
        final long project = projectManagement.createProject(bob,
            new ProjectInfoTemplate(uc.getLab3(), PROJECT_TITLE, "DNA", "Some proj")
        );
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long file1 = uc.saveFile(bob, instrumentFromExperimentFile(bob, experiment));
        final long file2 = uc.saveFile(bob, instrumentFromExperimentFile(bob, experiment));
        addFilesToExperiment(bob, experiment, of(factor(experiment)), of(
            new FileItemTemplate(file1, of("1"), EMPTY_ANNOTATIONS, false),
            new FileItemTemplate(file2, of("2"), EMPTY_ANNOTATIONS, false)
        ), of("3"));

        //noinspection unchecked
        final ExperimentInfoTemplateBuilder builder =
            new ExperimentInfoTemplateBuilder<>().name("").description("").experimentType(anyExperimentType())
                .species(anySpecies())
                .project(project)
                .is2Dlc(false)
                .restriction(restriction(bob)).factors(of(factor(experiment)))
                .files(of(new FileItemTemplate(file2, of("2"), EMPTY_ANNOTATIONS, false)));

        experimentManagement.updateExperiment(bob, experiment, builder.build());
    }

    @Test
    public void testRemovingFactorFromExperiment() {
        final long bob = uc.createLab3AndBob();
        final long experiment = experiment(bob, uc.getLab3());
        final long instrument = instrumentFromExperimentFile(bob, experiment);
        final long file1 = uc.saveFile(bob, instrument);
        final long file2 = uc.saveFile(bob, instrument);
        addFilesToExperiment(bob, experiment, of(factor(experiment), factor(experiment)), of(
            new FileItemTemplate(file1, of("1", "42"), EMPTY_ANNOTATIONS, false),
            new FileItemTemplate(file2, of("2", "98"), EMPTY_ANNOTATIONS, false)
        ), of("3", "50"));
        addFilesToExperiment(bob, experiment, of(factor(experiment)), of(
            new FileItemTemplate(file1, of("42"), EMPTY_ANNOTATIONS, false),
            new FileItemTemplate(file2, of("98"), EMPTY_ANNOTATIONS, false)
        ), of("50"));
        final DetailsReaderTemplate.ExperimentItemTemplate experimentItem =
            detailsReader.readExperiment(bob, experiment);
        assertTrue(
            Arrays.deepEquals(experimentItem.factorValues, new String[][] {{"42"}, {"98"}, {"50"}, {"50"}, {"50"}})
        );
    }

    @Test
    public void testAddingFactorToExperiment() {
        final long bob = uc.createLab3AndBob();
        final long experiment = experiment(bob, uc.getLab3());
        final long instrument = instrumentFromExperimentFile(bob, experiment);
        final long file1 = uc.saveFile(bob, instrument);
        final long file2 = uc.saveFile(bob, instrument);
        addFilesToExperiment(bob, experiment, of(factor(experiment)), of(
            new FileItemTemplate(file1, of("42"), EMPTY_ANNOTATIONS, false),
            new FileItemTemplate(file2, of("98"), EMPTY_ANNOTATIONS, false)
        ), of("50"));
        addFilesToExperiment(bob, experiment, of(factor(experiment), factor(experiment)), of(
            new FileItemTemplate(file1, of("1", "42"), EMPTY_ANNOTATIONS, false),
            new FileItemTemplate(file2, of("2", "98"), EMPTY_ANNOTATIONS, false)
        ), of("3", "50"));
        final DetailsReaderTemplate.ExperimentItemTemplate experimentItem =
            detailsReader.readExperiment(bob, experiment);
        assertTrue(Arrays.deepEquals(experimentItem.factorValues, new String[][] {{"1", "42"},
            {"2", "98"}, {"3", "50"},
            {"3", "50"}, {"3", "50"}
        }));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAllFactorValuesShouldBeNotEmpty() {
        final long bob = uc.createLab3AndBob();
        final long experiment = experiment(bob, uc.getLab3());
        final long file1 = uc.saveFile(bob, instrumentFromExperimentFile(bob, experiment));
        addFilesToExperiment(bob, experiment, of(factor(experiment)), of(
            new FileItemTemplate(file1, of(""), EMPTY_ANNOTATIONS, false)), of(""));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAllFactorValuesShouldBeSpecified() {
        final long bob = uc.createLab3AndBob();
        final long experiment = experiment(bob, uc.getLab3());
        final long instrument = instrumentFromExperimentFile(bob, experiment);
        final long file1 = uc.saveFile(bob, instrument);
        addFilesToExperiment(bob, experiment, of(factor(experiment), factor(experiment)), of(
            new FileItemTemplate(file1, of("a", ""), EMPTY_ANNOTATIONS, false)), of("b", ""));
    }

    @Test(expectedExceptions = InvalidFactorException.class)
    public void testShouldNotAllowToCreateFactorWithoutName() {
        final long bob = uc.createLab3AndBob();
        final long experiment = experiment(bob, uc.getLab3());
        final long instrument = instrumentFromExperimentFile(bob, experiment);
        final long file1 = uc.saveFile(bob, instrument);
        addFilesToExperiment(
            bob, experiment, of(new ExperimentManagementTemplate.MetaFactorTemplate("", "", false, experiment)), of(
                new FileItemTemplate(file1, of("q"), EMPTY_ANNOTATIONS, false)), of("a"));
    }

    @Test
    public void testRemovingUserFromLaboratoryWithExperiments() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();//labHead
        final long publicProject = createPublicProject(bob, uc.getLab3());
        createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), publicProject);
        createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), publicProject);

        assertEquals(userReader.readUsersByLab(poll, uc.getLab3()).size(), 2);
        final int pollProjectsLength = projectReader.readProjects(poll, Filter.MY).size();
        final int pollExperimentsLength = experimentReader.readExperiments(poll, Filter.MY).size();
        assertEquals(projectReader.readProjects(bob, Filter.MY).size(), 1);
        assertEquals(experimentReader.readExperiments(bob, Filter.MY).size(), 2);
        labHeadManagement.removeUserFromLab(poll, uc.getLab3(), bob);
        assertEquals(projectReader.readProjects(bob, Filter.MY).size(), 0);
        assertEquals(projectReader.readProjects(poll, Filter.MY).size(), pollProjectsLength + 1);
        assertEquals(experimentReader.readExperiments(bob, Filter.MY).size(), 0);
        assertEquals(experimentReader.readExperiments(poll, Filter.MY).size(), pollExperimentsLength + 2);
    }

    @Test
    public void testRemovingUserFromLaboratoryWithFiles() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();//labHead
        final long publicProject = createPublicProject(bob, uc.getLab3());
        final long experiment1 = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), publicProject);


        final long file1 = uc.saveFile(bob);
        final long file2 = uc.saveFile(bob);
        addFilesToExperiment(bob, experiment1, of(factor(experiment1), factor(experiment1)), of(
            new FileItemTemplate(file1, of("1", "42"), EMPTY_ANNOTATIONS, false),
            new FileItemTemplate(file2, of("2", "98"), EMPTY_ANNOTATIONS, false)
        ), of("3", "50"));


        assertEquals(userReader.readUsersByLab(poll, uc.getLab3()).size(), 2);
        final int pollProjectsLength = projectReader.readProjects(poll, Filter.MY).size();
        final int pollExperimentsLength = experimentReader.readExperiments(poll, Filter.MY).size();
        final int pollFilesLength = fileReader.readFiles(poll, Filter.MY).size();
        assertEquals(projectReader.readProjects(bob, Filter.MY).size(), 1);
        assertEquals(experimentReader.readExperiments(bob, Filter.MY).size(), 1);
        assertEquals(fileReader.readFiles(bob, Filter.MY).size(), 3);
        labHeadManagement.removeUserFromLab(poll, uc.getLab3(), bob);
        assertEquals(projectReader.readProjects(bob, Filter.MY).size(), 0);
        assertEquals(projectReader.readProjects(poll, Filter.MY).size(), pollProjectsLength + 1);
        assertEquals(experimentReader.readExperiments(bob, Filter.MY).size(), 0);
        assertEquals(experimentReader.readExperiments(poll, Filter.MY).size(), pollExperimentsLength + 1);
        assertEquals(fileReader.readFiles(bob, Filter.MY).size(), 0);
        assertEquals(fileReader.readFiles(poll, Filter.MY).size(), pollFilesLength);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDontAllowToCreateExperimentWithEmptyName() {
        final long bob = uc.createLab3AndBob();
        final long project = projectManagement
            .createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "Title", "area", ""));
        //noinspection unchecked
        final ExperimentInfoTemplateBuilder builder = new ExperimentInfoTemplateBuilder<>().name("Duplicated title")
            .description("area").experimentType(anyExperimentType())
            .species(anySpecies())
            .project(project).lab(uc.getLab3())

            .is2Dlc(false).restriction(restriction(bob)).factors(NO_FACTORS)
            .files(noFactoredFile(uc.saveFile(bob)));

        experimentManagement.createExperiment(bob, builder.build());
    }

    private ExperimentManagementTemplate.MetaFactorTemplate factor(long experimentId) {
        return new ExperimentManagementTemplate.MetaFactorTemplate(generateString(), "", false, experimentId);
    }

    public long experiment(long user, long lab) {
        final long project = projectManagement.createProject(user,
            new ProjectInfoTemplate(lab, PROJECT_TITLE, "DNA", "Some proj")
        );
        return createInstrumentAndExperimentWithOneFile(user, lab, project);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNotAllowCreateExperimentWithoutFiles() {
        final long bob = uc.createLab3AndBob();
        final long project = projectManagement
            .createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "Title", "area", ""));
        //noinspection unchecked
        final ExperimentInfoTemplateBuilder builder = new ExperimentInfoTemplateBuilder<>()
            .name("Duplicated title")
            .description("area")
            .experimentType(anyExperimentType())
            .species(anySpecies())
            .project(project).lab(uc.getLab3())
            .is2Dlc(false)
            .restriction(restriction(bob))
            .factors(NO_FACTORS)
            .files(Collections.<FileItemTemplate>emptyList());
        experimentManagement.createExperiment(bob, builder.build());
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
        experimentManagement.deleteExperiment(bob, experiment);
        detailsReader.readExperiment(bob, experiment);
    }

    @Test
    public void testDownloadTokenAvailableForPublicExperiment() {
        long bob = uc.createLab3AndBob();
        final long project = createPublicProject(bob, uc.getLab3());
        long experimentWithOneFile = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        String downloadLink = get(experimentReader.readExperiments(bob, Filter.ALL), 0).downloadLink;
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
        assertNull(get(experimentReader.readExperiments(bob, Filter.ALL), 0).downloadLink);
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
        final long vendor = anyVendor();
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
        final DetailsReaderTemplate.ExperimentItemTemplate details = detailsReader.readExperiment(bob, experiment);


        //noinspection unchecked
        final ExperimentInfoTemplateBuilder builder = new ExperimentInfoTemplateBuilder<>()
            .name(generateString())
            .description(generateString())
            .experimentType(anyExperimentType())
            .species(anotherSpecies)
            .project(details.project)
            .files(noFactoredFile(file))
            .lab(details.lab)
            .restriction(new Restriction(details.instrumentModel, details.instrument)).factors(NO_FACTORS);

        experimentManagement.updateExperiment(bob, experiment, builder.build());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testCantUpdateExperimentWithInvalidFilesAndInstrumentModelCombination() {
        final long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long anySpecies = anySpecies();
        final long vendor = anyVendor();
        final long model = anyInstrumentModelByVendor(vendor);
        final long file = createFile(bob, anySpecies, model);

        final long experiment = createExperiment(bob, project, model, file, unspecified());
        final long anotherModel = anotherInstrumentModel(vendor, model);
        final DetailsReaderTemplate.ExperimentItemTemplate details = detailsReader.readExperiment(bob, experiment);
        //noinspection unchecked
        final ExperimentInfoTemplateBuilder builder = new ExperimentInfoTemplateBuilder()
            .name(generateString())
            .description(generateString())
            .experimentType(anyExperimentType())
            .species(details.specie)
            .files(noFactoredFile(file))
            .project(details.project)
            .restriction(new Restriction(anotherModel, details.instrument));

        experimentManagement.updateExperiment(bob, experiment, builder.build());
    }


    @Test
    public void testCreateExperimentAttachments() {
        final long bob = uc.createLab3AndBob();
        final long experiment = createExperiment(bob, uc.createProject(bob));
        final long pdfAttachment = attachmentForExperiment(bob, experiment);
        final DetailsReaderTemplate.ExperimentItemTemplate experimentItem =
            detailsReader.readExperiment(bob, experiment);
        Assert.assertTrue(any(experimentItem.attachments, isGivenAttachment(pdfAttachment)));
    }

    @Test
    public void testRemoveExperimentAttachments() {
        final long bob = uc.createLab3AndBob();
        final long experiment = createExperiment(bob, uc.createProject(bob));
        attachmentForExperiment(bob, experiment);
        attachmentManagement.updateExperimentAttachments(bob, experiment, ImmutableSet.<Long>of());
        final DetailsReaderTemplate.ExperimentItemTemplate experimentItem =
            detailsReader.readExperiment(bob, experiment);
        Assert.assertTrue(experimentItem.attachments.size() == 0);
    }

    @Test
    public void testUpdateExperimentAttachments() {
        final long bob = uc.createLab3AndBob();
        final long experiment = createExperiment(bob, uc.createProject(bob));
        attachmentForExperiment(bob, experiment);
        final long otherAttachment = attachmentManagement.newAttachment(bob, "otherAttachment.img", 1024 * 1024);
        attachmentManagement.updateExperimentAttachments(bob, experiment, ImmutableSet.of(otherAttachment));
        final DetailsReaderTemplate.ExperimentItemTemplate experimentItem =
            detailsReader.readExperiment(bob, experiment);
        Assert.assertTrue(any(experimentItem.attachments, isGivenAttachment(otherAttachment)));
    }

    @Test
    public void testReadExperimentAttachments() {
        final long bob = uc.createLab3AndBob();
        final long experiment = createExperiment(bob, uc.createProject(bob));
        final long attachmentId = attachmentForExperiment(bob, experiment);
        assertEquals(attachmentsReader.readAttachment(bob, attachmentId).id, attachmentId);
        assertEquals(attachmentsReader.readAttachments(EXPERIMENT, bob, experiment).size(), 1);
    }

    @Test
    public void testCreateExperimentWithoutLab() {

        final long paul = uc.createPaul();
        final long experimentWithNoLab = createExperiment(paul, uc.createProject(paul, null), null);
        final DetailsReaderTemplate.ExperimentItemTemplate experiment =
            detailsReader.readExperiment(paul, experimentWithNoLab);

        Assert.assertNull(experiment.lab, "Experiment laboratory id must be null");
        Assert.assertNull(experiment.labHead, "Experiment laboratory head name must be null");
        Assert.assertNull(experiment.labName, "Experiment laboratory name must be null");

    }

    @Test
    public void testEditExperimentWithoutLab() {

        final long paul = uc.createPaul();
        final long project = uc.createProject(paul, null);
        final long experimentWithNoLab = createExperiment(paul, project, null);

        final long file = uc.saveFile(paul);
        experimentManagement.updateExperiment(paul, experimentWithNoLab, experimentInfo()
            .files(noFactoredFile(file))
            .project(project)
            .restriction(restriction(paul, file))
            .lab(null)
            .build());

        final DetailsReaderTemplate.ExperimentItemTemplate afterUpdate =
            detailsReader.readExperiment(paul, experimentWithNoLab);

        Assert.assertNull(afterUpdate.lab, "Experiment laboratory id must be null");
        Assert.assertNull(afterUpdate.labHead, "Experiment laboratory head name must be null");
        Assert.assertNull(afterUpdate.labName, "Experiment laboratory name must be null");

    }

    private Predicate<DetailsReaderTemplate.AttachmentItem> isGivenAttachment(final long otherAttachment) {
        return input -> input.id == otherAttachment;
    }


    private long createFile(long bob, long species, long model) {
        return createFile(bob, species, model, generateString());
    }

    private long createFile(long bob, long species, long model, String name) {
        //noinspection unchecked
        final long file = fileManagement.createFile(bob, createInstrumentByModel(bob, uc.getLab3(), model),
            new FileManagementTemplate.FileMetaDataInfoTemplate(name, 0, "", null, species, false)
        );
        uc.updateFileContent(bob, file);
        return file;
    }

    private long createExperiment(long bob, long project, long instrumentModel, long file, long species) {
        //noinspection unchecked
        final ExperimentInfoTemplateBuilder builder = experimentInfo(species)
            .project(project)
            .lab(uc.getLab3())
            .is2Dlc(false)
            .restriction(new Restriction(instrumentModel, Optional.absent()))
            .factors(NO_FACTORS)
            .files(noFactoredFile(file));

        return experimentManagement.createExperiment(bob, builder.build());
    }


}
