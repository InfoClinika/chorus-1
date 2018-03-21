/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.test.sharing;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams;
import com.infoclinika.mssharing.model.helper.Data;
import com.infoclinika.mssharing.model.helper.ExperimentLabelsInfo;
import com.infoclinika.mssharing.model.read.*;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.read.dto.details.ProjectItem;
import com.infoclinika.mssharing.model.write.AnalysisBounds;
import com.infoclinika.mssharing.model.write.ExperimentInfo;
import com.infoclinika.mssharing.model.write.FileItem;
import com.infoclinika.mssharing.model.write.FileMetaDataInfo;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.common.items.LabItem;
import com.infoclinika.mssharing.platform.model.read.AccessLevel;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.of;
import static com.infoclinika.mssharing.model.helper.Data.NO_FACTORS;
import static com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.GroupItemTemplate;
import static com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.SharedProjectItemTemplate;
import static com.infoclinika.mssharing.platform.model.read.Filter.MY;
import static com.infoclinika.mssharing.platform.model.read.Filter.SHARED_WITH_ME;
import static com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate.Access.READ;
import static org.testng.Assert.*;

/**
 * @author Stanislav Kurilin
 */
//TODO: [stanislav.kurilin] extract test groups to make test more consistence in single class
public class SharingTest extends AbstractSharingTest {
    //Users can share their projects with other users or with theirs group

    //sharing projects
    @Test
    public void testSharedProjectsDisplayedInGroupDetails() {
        final long bob = uc.createLab3AndBob();
        final long paul = uc.createPaul();
        final long group = sharingManagement.createGroup(bob, "Bobs Group", ImmutableSet.of(paul));
        final long project = uc.createProject(bob, uc.getLab3());
        sharingManagement.updateSharingPolicy(bob, project, emptySharing, ImmutableMap.of(group, SharingManagementTemplate.Access.WRITE), false);
        final GroupItemTemplate groupItem = detailsReader.readGroup(bob, group);
        assertTrue(Iterables.any(groupItem.sharedProjects, new Predicate<SharedProjectItemTemplate>() {
            @Override
            public boolean apply(SharedProjectItemTemplate input) {
                return input.id == project;
            }
        }));
    }

    @Test
    public void testSharedGroupDisplayedInProjectDetails() {
        final long bob = uc.createLab3AndBob();
        final long paul = uc.createPaul();
        final String groupName = generateString();
        final long group = sharingManagement.createGroup(bob, groupName, ImmutableSet.of(paul));
        final long project = uc.createProject(bob, uc.getLab3());
        sharingManagement.updateSharingPolicy(bob, project, emptySharing, ImmutableMap.of(group, SharingManagementTemplate.Access.WRITE), false);
        final ProjectItem projectItem = detailsReader.readProject(bob, project);
        assertTrue(Iterables.any(projectItem.sharedGroups, new Predicate<DetailsReader.SharedGroup>() {
            @Override
            public boolean apply(DetailsReader.SharedGroup input) {
                return input.name.equals(groupName);
            }
        }));
    }

    //Test sharing rules
    @Test
    public void createInSharedProject() {
        long bob = uc.createLab3AndBob();
        long kate = createKateInLab2();
        final long project = projectByUser(bob, uc.getLab3());
        uc.sharingWithCollaborator(bob, project, kate);
        createInstrumentAndExperimentWithOneFile(kate, uc.getLab2(), project);
    }

    @Test
    public void createInSharedThrowGroupProject() {
        long bob = uc.createLab3AndBob();
        long kate = createKateInLab2();
        final long project = projectByUser(bob, uc.getLab3());

        final long group = sharingManagement.createGroup(bob, "gg lab", ImmutableSet.of(kate));

        sharingManagement.updateSharingPolicy(bob, project, emptySharing, ImmutableMap.of(group, SharingManagementTemplate.Access.WRITE), false);

        createInstrumentAndExperimentWithOneFile(kate, uc.getLab2(), project);
    }

    @Test
    public void removingSharedGroup() {
        long bob = uc.createLab3AndBob();
        long kate = createKateInLab2();
        final long group = sharingManagement.createGroup(bob, "gg lab", ImmutableSet.of(kate));
        sharingManagement.removeGroup(bob, group);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void removingSharedGroupIfItUsed() {
        long bob = uc.createLab3AndBob();
        long kate = createKateInLab2();
        final long project = projectByUser(bob, uc.getLab3());
        final long group = sharingManagement.createGroup(bob, "gg lab", ImmutableSet.of(kate));
        sharingManagement.updateSharingPolicy(bob, project, emptySharing, ImmutableMap.of(group, SharingManagementTemplate.Access.WRITE), false);
        sharingManagement.removeGroup(bob, group);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void failedOnCreationInNonSharedProject() {
        long bob = uc.createLab3AndBob();
        long kate = createKateInLab2();
        final long project = projectByUser(bob, uc.getLab3());
        createInstrumentAndExperimentWithOneFile(kate, uc.getLab2(), project);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testStopSharing() {
        long bob = uc.createLab3AndBob();
        long kate = createKateInLab2();
        final long project = projectByUser(bob, uc.getLab3());
        uc.sharingWithCollaborator(bob, project, kate);
        uc.sharing(bob, project, ImmutableSet.of(uc.createJoe()), empty);
        createInstrumentAndExperimentWithOneFile(kate, uc.getLab2(), project);
    }

    @Test
    public void testEditingByCreator() {
        long bob = uc.createLab3AndBob();
        final long project = projectByUser(bob, uc.getLab3());
        createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testEditingBySharableDirectly() {
        long bob = uc.createLab3AndBob();
        long kate = createKateInLab2();
        final long project = projectByUser(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        uc.sharingWithCollaborator(bob, project, kate);
        addFilesToExperiment(kate, experiment);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testAttachingToWrongExperiment() {
        long bob = uc.createLab3AndBob();
        long kate = createKateInLab2();
        final long project = projectByUser(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        addFilesToExperiment(kate, experiment);
    }

    @Test
    public void testSharingFileInSeveralExperimentsByOwner() {
        long bob = uc.createLab3AndBob();
        final long project = projectByUser(bob, uc.getLab3());

        final long experimentA = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long experimentB = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);

        final long file = uc.saveFile(bob, instrumentFromExperimentFile(bob, experimentA));
        updateExperimentFiles(bob, experimentA, file);
        updateExperimentFiles(bob, experimentB, file);
    }

   /**
     * 1) Kate creates project P
     * 2) Kate use file A in project
     * 3) Kate shares P with Bob
     * 4) Bob reused file A in his own project
     */
    @Test
    public void testReusingSharedFiles() {
        long bob = uc.createLab3AndBob();
        long kate = createKateInLab2();
        final long project = projectByUser(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);

        final long file = uc.saveFile(bob);
        updateExperimentFiles(bob, experiment, file);
        uc.sharingWithCollaborator(bob, project, kate);

        checkHasAccessToSharedFile(kate, uc.getLab2(), file);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testAccessingNotSharedFiles() {
        long bob = uc.createLab3AndBob();
        long kate = createKateInLab2();
        final long project = projectByUser(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long file = uc.saveFile(bob);
        updateExperimentFiles(bob, experiment, file);

        checkHasAccessToSharedFile(kate, uc.getLab2(), file);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testReusingFileAfterStopSharing() {
        long bob = uc.createLab3AndBob();
        long kate = createKateInLab2();
        final long project = projectByUser(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long file = uc.saveFile(bob);
        updateExperimentFiles(bob, experiment, file);

        uc.sharingWithCollaborator(bob, project, kate);
        uc.sharing(bob, project, ImmutableSet.of(uc.createJoe()), empty);

        checkHasAccessToSharedFile(kate, uc.getLab2(), file);
    }

    @Test
    public void testFilesFromSharedProjectsAreAvailable() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long project = uc.createProject(poll, uc.getLab3());
        super.createInstrumentAndExperimentWithOneFile(poll, uc.getLab3(), project);
        // attachFileToExperiment(poll, experiment);
        assertNumberOfAvailableInstrumentTypes(bob, 1);

        uc.sharingWithCollaborator(poll, project, bob);

        assertNumberOfAvailableInstrumentTypes(bob, 1);
    }

    @Test
    public void testReusingFileAfterStopSharingAfterItWasUsed() {
        long bob = uc.createLab3AndBob();
        long kate = createKateInLab2();
        final long project = projectByUser(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long file = uc.saveFile(bob);
        updateExperimentFiles(bob, experiment, file);

        uc.sharingWithCollaborator(bob, project, kate);
        reuseFile(kate, uc.getLab2(), file);
        // uc.shareProjectToKateInGroup(bob, project);

        checkHasAccessToSharedFile(kate, uc.getLab2(), file);
    }

    /**
     * Test case:
     * 1) Kate shares project with Bob
     * 2) Kate stop sharing
     * 3) Kate use file A in project
     * Bob can not use file A
     */
    @Test(expectedExceptions = AccessDenied.class)
    public void testReusingFileThatWasNewerAccessed() {
        long bob = uc.createLab3AndBob();
        long kate = createKateInLab2();
        final long project = projectByUser(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final long file = uc.saveFile(bob);
        updateExperimentFiles(bob, experiment, file);

        uc.sharingWithCollaborator(bob, project, kate);
        uc.sharing(bob, project, ImmutableSet.of(uc.createJoe()), empty);

        checkHasAccessToSharedFile(kate, uc.getLab2(), file);
    }

    @Test
    public void testSharingSeveralProjectWithSamePerson() {
        long bob = uc.createLab3AndBob();
        long project1 = uc.createProject(bob, uc.getLab3());
        long poll = uc.createPaul();
        uc.sharingWithCollaborator(bob, project1, poll);
        long project2 = uc.createProject(bob, uc.getLab3());
        uc.sharingWithCollaborator(bob, project2, poll);
    }

    @Test
    public void testCreatingNewExperimentInCaseOfLoosingAccessToUsedProject() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob, uc.getLab3());
        long paul = uc.createPaul();
        uc.sharingWithCollaborator(bob, project, paul);
        createInstrumentAndExperimentWithOneFile(paul, uc.getLab3(), project);

        uc.shareProjectToKateInGroup(bob, project);

        expectProjectCopy(bob, paul);
    }

    @Test
    public void testCreatingNewExperimentInCaseOfLoosingWriteAccessToUsedProject() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob, uc.getLab3());
        long paul = uc.createPaul();
        uc.sharingWithCollaborator(bob, project, paul);
        createInstrumentAndExperimentWithOneFile(paul, uc.getLab3(), project);

        sharingManagement.updateSharingPolicy(bob, project, Collections.singletonMap(paul, READ), emptySharing, false);

        expectProjectCopy(bob, paul);
    }

    private void expectProjectCopy(long bob, long paul) {
        final ProjectLine bobProject = dashboardReader.readProjects(bob, Filter.MY).iterator().next();
        final ProjectLine pollProject = dashboardReader.readProjects(paul, Filter.MY).iterator().next();

        assertEquals(bobProject.areaOfResearch, pollProject.areaOfResearch);
        assertEquals(bobProject.name, pollProject.name);

        assertEquals(pollProject.creator, Data.PAUL_INFO.firstName + " " + Data.PAUL_INFO.lastName);

        assertNotNull(pollProject.modified);

    }

    @Test
    public void testExperimentDoesntLoseIdentityOnMovingBetweenProjects() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob, uc.getLab3());
        long poll = uc.createPaul();
        uc.sharingWithCollaborator(bob, project, poll);
        final long experiment = createInstrumentAndExperimentWithOneFile(poll, uc.getLab3(), project);

        final ExperimentLine pollExperimentLine = dashboardReader.readExperiments(poll, Filter.MY).iterator().next();
        assertEquals(pollExperimentLine.id, experiment);
    }

    @Test
    public void testDisplaySharedExperimentsForProjectCreator() {
        final long kate = createKateInLab2();
        final long bob = uc.createLab3AndBob();
        final long project = projectByUser(bob, uc.getLab3());
        uc.sharingWithCollaborator(bob, project, kate);
        final long experiment = createInstrumentAndExperimentWithOneFile(kate, uc.getLab2(), project);
        final ExperimentLine shared = dashboardReader.readExperiments(bob, SHARED_WITH_ME).iterator().next();
        assertEquals(shared.id, experiment);
        assertEquals(dashboardReader.readExperiments(bob, Filter.PUBLIC).size(), 0);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 0);
    }

    @Test
    public void testDisplaySharedExperimentsForExperimentCreator() {
        final long kate = createKateInLab2();
        final long bob = uc.createLab3AndBob();
        final long project = projectByUser(kate, uc.getLab3());
        uc.sharingWithCollaborator(kate, project, bob);
        final long experiment = createInstrumentAndExperimentWithOneFile(kate, uc.getLab2(), project);
        assertEquals(dashboardReader.readExperiments(kate, SHARED_WITH_ME).size(), 0);
        assertEquals(dashboardReader.readExperiments(kate, Filter.PUBLIC).size(), 0);
        assertEquals(dashboardReader.readExperiments(kate, Filter.MY).size(), 1);
    }

    @Test
    public void testDisplayMyExperiments() {
        final long kate = createKateInLab2();
        final long bob = uc.createLab3AndBob();
        final long project = projectByUser(bob, uc.getLab3());
        final long secondProject = projectByUser(kate, uc.getLab2());
        uc.sharingWithCollaborator(bob, project, kate);

        createInstrumentAndExperimentWithOneFile(kate, uc.getLab2(), project);
        createInstrumentAndExperimentWithOneFile(kate, uc.getLab2(), secondProject);
        final Set<ExperimentLine> experiments = dashboardReader.readExperiments(kate, Filter.MY);
        assertEquals(experiments.size(), 2);
        assertEquals(dashboardReader.readExperiments(kate, Filter.PUBLIC).size(), 0);
        assertEquals(dashboardReader.readExperiments(kate, SHARED_WITH_ME).size(), 0);
    }

    @Test
    public void testDisplayInstrumentsForCurrentLab() {
        final long kate = createKateInLab2and3();
        uc.createInstrumentAndApproveIfNeeded(kate, uc.getLab2());
        uc.createInstrumentAndApproveIfNeeded(kate, uc.getLab3());
        uc.createInstrumentAndApproveIfNeeded(kate, uc.getLab3());

        assertEquals(dashboardReader.readInstrumentsByLab(kate, uc.getLab2()).size(), 1);
        assertEquals(dashboardReader.readInstrumentsByLab(kate, uc.getLab3()).size(), 2);

    }

    @Test
    public void testDisplayFilesForCurrentLab() {
        final long kate = createKateInLab2and3();
        final long project = projectByUser(kate, uc.getLab2());
        final long experiment = createInstrumentAndExperimentWithOneFile(kate, uc.getLab2(), project);
        final long experiment2 = createInstrumentAndExperimentWithOneFile(kate, uc.getLab3(), project);
        addFilesToExperiment(kate, experiment);
        addFilesToExperiment(kate, experiment2);
        assertEquals(dashboardReader.readInstrumentsByLab(kate, uc.getLab2()).size(), 2);
        assertEquals(dashboardReader.readInstrumentsByLab(kate, uc.getLab3()).size(), 1);
    }

    @Test
    public void testReadLabItems() {
        final long kate = createKateInLab2and3();
        uc.createInstrumentAndApproveIfNeeded(kate, uc.getLab2());
        uc.createInstrumentAndApproveIfNeeded(kate, uc.getLab3());
        uc.createInstrumentAndApproveIfNeeded(kate, uc.getLab3());
        final SortedSet<LabItem> items = dashboardReader.readLabItems(kate);
        assertEquals(items.size(), 2);
        assertEquals(items.last().instruments.size(), 1);
        assertEquals(items.first().instruments.size(), 2);
    }

    @Test
    public void testCreateOrUpdateSharingProjectWithoutCollaborators() {
        final long bob = uc.createLab3AndBob();
        final long project = projectByUser(bob, uc.getLab3());
        sharingManagement.updateSharingPolicy(bob, project, emptySharing, emptySharing, false);
        assertEquals(dashboardReader.readProject(bob, project).accessLevel, AccessLevel.PRIVATE);
    }

    @Test
    public void testCreateExperimentForPublicProjectOnlyForOwner() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = uc.createProject(bob, uc.getLab3());
        long poll = uc.createPaul();
        sharingManagement.makeProjectPublic(bob, project);
        createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        try {
            createInstrumentAndExperimentWithOneFile(joe, uc.getLab3(), project);
            fail("Access denied for case \"Create Experiment in Public Project by not owner user\" was expected");
        } catch (AccessDenied e) {

        }
    }

    @Test
    public void testCreateExperimentForPublicProjectForLabHead() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = uc.createProject(bob, uc.getLab3());
        long labHead = uc.createPaul();
        sharingManagement.makeProjectPublic(bob, project);
        createInstrumentAndExperimentWithOneFile(labHead, uc.getLab3(), project);
    }

    @Test
    public void testCreateExperimentForPublicProjectByCreatorWithNoLab() {
        final long bob = uc.createLab3AndBob();
        final long projectWithNoLab = uc.createProject(bob);
        createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), projectWithNoLab);
    }

    @Test
    public void testReadSharedFilesInPublicExperiment() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long sharedProject = projectByUser(kate, uc.getLab3());
        final long publicProject = projectByUser(kate, uc.createLab3());
        sharingManagement.makeProjectPublic(kate, publicProject);

        uc.sharingWithCollaborator(kate, sharedProject, bob);
        createInstrumentAndExperimentWithOneFile(kate, uc.getLab3(), sharedProject);

        final Set<FileLine> fileLines = fileReader.readFiles(bob, MY);
        assertTrue(fileLines.size() == 1);
        final long id = Iterables.get(fileLines, 0).id;

        final ExperimentInfo.Builder builder = new ExperimentInfo.Builder().name("Duplicated title").description("area").experimentType(anyExperimentType()).specie(unspecified())
                .project(publicProject).lab(uc.getLab3()).billLab(uc.getLab3()).is2dLc(false).experimentLabels(new ExperimentLabelsInfo())
                .restriction(restriction(kate)).factors(NO_FACTORS)
                .files(of(new FileItem(id, false, 0, preparedSample(id))))
        .bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES).sampleTypesCount(1);

        studyManagement.createExperiment(kate, builder.build());
        assertTrue(fileReader.readFiles(bob, SHARED_WITH_ME).size() == 0);
        assertTrue(fileReader.readFiles(bob, Filter.PUBLIC).size() == 1);
    }

    @Test
    public void testDownloadJobAndGroupCreationAfterSharing() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long sharedProject = projectByUser(kate, uc.getLab3());

        final long fileId = instrumentManagement.createFile(kate, createInstrumentAndApproveIfNeeded(kate, uc.getLab2()), new FileMetaDataInfo("ddd", 222, "String", null, anySpecies(), false));
        uc.updateFileContent(kate, fileId);

        final ExperimentInfo.Builder builder = new ExperimentInfo.Builder().name("Duplicated title").description("area").experimentType(anyExperimentType()).specie(unspecified())
                .project(sharedProject).lab(uc.getLab3()).billLab(uc.getLab3()).is2dLc(false).restriction(restriction(kate))
                .factors(NO_FACTORS).files(of(new FileItem(fileId, false, 0, preparedSample(fileId))))
                .bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES).experimentLabels(new ExperimentLabelsInfo()).sampleTypesCount(1);

        long expId = studyManagement.createExperiment(kate, builder.build());
        fileMovingManager.moveToArchiveStorage(fileId);
        uc.sharingWithCollaborator(kate, sharedProject, bob);
        final DownloadFileReader.DownloadFileJob job = downloadFileReader.readJobByFile(fileId);
        assertEquals(job.fileMetaData.longValue(), fileId);
        assertEquals(downloadFileReader.readGroupByJob(job.id).get(0).experimentId.longValue(), expId);
    }

    @Test
    public void testNotCreatingDuplicateDownloadGroup() {
        setBilling(true);

        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        billingManagement.makeLabAccountEnterprise(kate, uc.getLab2());
        billingManagement.makeLabAccountEnterprise(uc.createPaul(), uc.getLab3());
        final long sharedProject = projectByUser(kate, uc.getLab3());

        final long fileId = instrumentManagement.createFile(kate, createInstrumentAndApproveIfNeeded(kate, uc.getLab2()), new FileMetaDataInfo("ddd", 222, "String", null, anySpecies(), false));
        uc.updateFileContent(kate, fileId);

        final ExperimentInfo.Builder builder = new ExperimentInfo.Builder().name("Duplicated title").description("area").experimentType(anyExperimentType()).specie(unspecified())
                .project(sharedProject).lab(uc.getLab3()).billLab(uc.getLab3()).is2dLc(false).restriction(restriction(kate))
                .factors(NO_FACTORS).files(of(new FileItem(fileId, false, 0, preparedSample(fileId))))
                .bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES).experimentLabels(new ExperimentLabelsInfo()).sampleTypesCount(1);
        long expId = studyManagement.createExperiment(kate, builder.build());

        uc.sharingWithCollaborator(kate, sharedProject, bob);

        archiveExperiment(kate, expId);

        fileMovingManager.requestExperimentFilesUnarchiving(expId, Lists.newArrayList(bob));
        final DownloadFileReader.DownloadFileJob job = downloadFileReader.readJobByFile(fileId);
        assertTrue(downloadFileReader.readGroupByJob(job.id).size() == 1);
    }

    @Test
    public void testUserCanCreateExperimentWithSharedFiles() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();

        final long bobsSharedProject = projectByUser(bob, uc.getLab3());
        createExperiment(bob, bobsSharedProject);
        sharingManagement.updateSharingPolicy(bob, bobsSharedProject, ImmutableMap.of(kate, READ), emptySharing, false);

        final long katesProject = projectByUser(kate, uc.getLab2());
        final Set<FileLine> sharedFiles = fileReader.readFiles(kate, SHARED_WITH_ME);
        final long experiment = createExperiment(kate, katesProject, sharedFiles.iterator().next().id, uc.getLab2());
        final ExperimentItem experimentItem = detailsReader.readExperiment(kate, experiment);

        assertEquals(sharedFiles.iterator().next().id, experimentItem.files.iterator().next().id);
    }

    @Test
    public void testLabHeadCanArchivePrivateExperimentsForItsLab() {
        uc.createLab3();
        final long head = uc.createPaul();
        billingManagement.makeLabAccountEnterprise(head, uc.getLab3());
        long joe = uc.createJoe();
        setBilling(true);
        createExperiment(joe, projectByUser(joe, uc.getLab3()));
        final ExperimentLine experimentLine = dashboardReader.readExperimentsByLab(head, uc.getLab3(), new PaginationItems.PagedItemInfo(100, 0, "name", false, null, Optional.<AdvancedFilterQueryParams>absent())).items.iterator().next();
        assertTrue(experimentLine.canArchive);
        archiveExperiment(head, experimentLine.id);
        assertTrue(from(dashboardReader.readExperimentsByLab(head, uc.getLab3(), new PaginationItems.PagedItemInfo(100, 0, "name", false, null, Optional.<AdvancedFilterQueryParams>absent())))
                .allMatch(new Predicate<ExperimentLine>() {
                    @Override
                    public boolean apply(ExperimentLine input) {
                        return input.canUnarchive;
                    }
                }));
    }

    @Test
    public void testLabHeadCanUnarchivePrivateExperimentsForItsLab() {
        setBilling(true);
        uc.createLab3();
        final long head = uc.createPaul();
        billingManagement.makeLabAccountEnterprise(head, uc.getLab3());
        long joe = uc.createJoe();
        final long experiment = createExperiment(joe, projectByUser(joe, uc.getLab3()));
        archiveExperiment(head, experiment);
        final ExperimentLine experimentLine = dashboardReader.readExperimentsByLab(head, uc.getLab3(), new PaginationItems.PagedItemInfo(100, 0, "name", false, null, Optional.<AdvancedFilterQueryParams>absent())).items.iterator().next();
        assertTrue(experimentLine.canUnarchive);
        fileOperationsManager.markExperimentFilesToUnarchive(head, experimentLine.id);
        assertTrue(from(dashboardReader.readExperimentsByLab(head, uc.getLab3(), new PaginationItems.PagedItemInfo(100, 0, "name", false, null, Optional.<AdvancedFilterQueryParams>absent())))
                .allMatch(new Predicate<ExperimentLine>() {
                    @Override
                    public boolean apply(ExperimentLine input) {
                        return input.canArchive;
                    }
                }), "All experiments must be available for archiving for lab head");
    }

    private void archiveExperiment(long head, long experiment) {
        fileOperationsManager.markExperimentFilesToArchive(head, experiment);
        fileOperationsManager.archiveMarkedFiles();
    }

    @Test
    public void testUserCanReadInstrumentThroughSharedFiles() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();

        final long bobsSharedProject = projectByUser(bob, uc.getLab3());
        createExperiment(bob, bobsSharedProject);
        sharingManagement.updateSharingPolicy(bob, bobsSharedProject, ImmutableMap.of(kate, READ), emptySharing, false);

        final Set<FileLine> sharedFiles = fileReader.readFiles(kate, SHARED_WITH_ME);
        detailsReader.readInstrument(kate, sharedFiles.iterator().next().instrumentId);
    }

}
