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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.ObjectNotFoundException;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;
import com.infoclinika.mssharing.platform.model.common.items.NamedItem;
import com.infoclinika.mssharing.platform.model.helper.ProjectCreationHelperTemplate;
import com.infoclinika.mssharing.platform.model.read.*;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ProjectItemTemplate;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate.ExperimentLineTemplate;
import com.infoclinika.mssharing.platform.model.write.ProjectManagementTemplate.CopyProjectInfoTemplate;
import com.infoclinika.mssharing.platform.model.write.ProjectManagementTemplate.ProjectInfoTemplate;
import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

import static com.google.common.collect.Iterables.*;
import static com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate.AttachmentType.PROJECT;
import static com.infoclinika.mssharing.platform.model.read.Filter.ALL;
import static com.infoclinika.mssharing.platform.model.read.Filter.MY;
import static com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate.ProjectSharingInfo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Stanislav Kurilin
 */
public class ProjectTest extends AbstractStudyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectTest.class);
    @Inject
    private AttachmentsReaderTemplate<AttachmentsReaderTemplate.AttachmentItem> attachmentsReader;
    @Inject
    private ProjectCreationHelperTemplate projectCreationHelperTemplate;
    @Inject
    private ProjectReaderTemplate<ProjectReaderTemplate.ProjectLineTemplate> defaultProjectReader;
    @Inject
    private ProjectReaderTemplate<ProjectReaderTemplate.ProjectLineTemplate> projectDashboardRecordsReader;

    @Test(enabled = false)
    public void testReadLargeAmountOfProjects() {
        final long bob = uc.createLab3AndBob();
        final Long lab = uc.getLab3();
        final int amount = 1000;
        createProjects(bob, lab, amount);

        LOGGER.info("Start read projects (default)");
        final long begin = System.currentTimeMillis();
        final ImmutableSet<ProjectReaderTemplate.ProjectLineTemplate> projectLines =
            ImmutableSet.copyOf(defaultProjectReader.readProjects(bob, ALL));
        final long defaultReadMills = System.currentTimeMillis() - begin;
        LOGGER.info("End read projects (default) im mills: {}", defaultReadMills);
        Assert.assertEquals(projectLines.size(), amount);

        LOGGER.info("Start read projects (records)");
        final long beginRecords = System.currentTimeMillis();
        final ImmutableSet<ProjectReaderTemplate.ProjectLineTemplate> recordProjectLines =
            ImmutableSet.copyOf(projectDashboardRecordsReader.readProjects(bob, ALL));
        final long recordsReadMills = System.currentTimeMillis() - beginRecords;
        LOGGER.info("End read projects (records) in mills: {}", recordsReadMills);
        Assert.assertEquals(recordProjectLines.size(), amount);

        LOGGER.info("Difference (recordsReadMills - defaultReadMills): {}", (recordsReadMills - defaultReadMills));

        Assert.assertTrue(defaultReadMills < recordsReadMills);
    }

    @Test(enabled = false)
    public void testReadLargeAmountOfPagedProjects() {
        final long bob = uc.createLab3AndBob();
        final Long lab = uc.getLab3();
        final int amount = 1000;
        createProjects(bob, null, amount);

        LOGGER.info("Start read projects (default)");
        final long begin = System.currentTimeMillis();
        final ImmutableSet<ProjectReaderTemplate.ProjectLineTemplate> projectLines = ImmutableSet
            .copyOf(defaultProjectReader.readProjects(bob, ALL, new PagedItemInfo(100, 0, "laboratory", true, "")));
        final long defaultReadMills = System.currentTimeMillis() - begin;
        LOGGER.info("End read projects (default) im mills: {}", defaultReadMills);
        Assert.assertEquals(projectLines.size(), 100);

        LOGGER.info("Start read projects (records)");
        final long beginRecords = System.currentTimeMillis();
        final ImmutableSet<ProjectReaderTemplate.ProjectLineTemplate> recordProjectLines = ImmutableSet.copyOf(
            projectDashboardRecordsReader.readProjects(bob, ALL, new PagedItemInfo(100, 0, "laboratory", true, "")));
        final long recordsReadMills = System.currentTimeMillis() - beginRecords;
        LOGGER.info("End read projects (records) in mills: {}", recordsReadMills);
        Assert.assertEquals(recordProjectLines.size(), 100);

        LOGGER.info("Difference (recordsReadMills - defaultReadMills): {}", (recordsReadMills - defaultReadMills));

        Assert.assertTrue(defaultReadMills < recordsReadMills);
    }

    protected void createProjects(long bob, Long lab, int amount) {
        final long kate = uc.createKateAndLab2();
        for (int i = 0; i < amount; i++) {
            final long project = uc.createProject(bob, lab);
            uc.sharingWithCollaborator(bob, project, kate);
        }
    }

    //There is no projects
    //Project created
    @Test
    public void testProjectDataReadFine() {
        final long bob = uc.createLab3AndBob();
        final long projectId = uc.createProject(bob, uc.getLab3());
        createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), projectId);
        final SortedSet<? extends ProjectReaderTemplate.ProjectLineTemplate> projects =
            projectReader.readProjects(bob, ALL);

        assertEquals(Iterables.size(projects), 1);

        final ProjectReaderTemplate.ProjectLineTemplate project = projects.iterator().next();
        assertEquals(project.id, projectId);
        assertEquals(project.areaOfResearch, "DNA");
        assertEquals(project.creator, "Bob Marley");
        assertNotNull(project.modified);
    }

    @Test
    public void testReadShortProjectsItems() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob);
        final Set<NamedItem> namedItems = projectCreationHelperTemplate.ownedProjects(bob);
        assertEquals(namedItems.size(), 1);
        assertEquals(namedItems.iterator().next().id, project);
    }

    @Test
    public void testCreateProjectForUserWithoutLab() {
        final long john = uc.createJohnWithoutLab();
        uc.createProject(john);
        final SortedSet<? extends ProjectReaderTemplate.ProjectLineTemplate> projects =
            projectReader.readProjects(john, ALL);

        assertEquals(Iterables.size(projects), 1);
    }


    @Test
    public void testSeveralProjectsWithSameNameAreRead() {
        final long bob = uc.createLab3AndBob();
        final long projectId =
            projectManagement.createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "Duplicated title", "", "area"));
        final long paul = uc.createPaul();
        uc.sharing(bob, projectId, ImmutableSet.of(paul), Collections.<Long>emptySet());
        //create with the same name
        projectManagement.createProject(paul, new ProjectInfoTemplate(uc.getLab3(), "Duplicated title", "", "area"));
        assertEquals(projectReader.readProjects(paul, ALL).size(), 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUserCantCreateProjectWithAlreadyExistedName() {
        final long bob = uc.createLab3AndBob();
        projectManagement.createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "Duplicated title", "area", ""));
        //create with the same name
        projectManagement.createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "Duplicated title", "area", ""));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUserCantUpdateExperimentWithAlreadyExistedName() {
        final long bob = uc.createLab3AndBob();
        projectManagement.createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "Duplicated title", "area", ""));
        final long project =
            projectManagement.createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "Title", "area", ""));
        //update with the same name
        projectManagement
            .updateProject(bob, project, new ProjectInfoTemplate(uc.getLab3(), "Duplicated title", "Bio", ""));
    }


    //Update project
    @Test
    public void testUpdatingProject() {
        final long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        projectManagement.updateProject(bob, project, new ProjectInfoTemplate(uc.getLab3(), "New Name", "", "Bio"));
        final ProjectReaderTemplate.ProjectLineTemplate updatedProject =
            projectReader.readProjects(bob, MY).iterator().next();

        assertEquals(updatedProject.name, "New Name");
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testOnlyOwnerCanUpdateProject() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        projectManagement.updateProject(joe, project, new ProjectInfoTemplate(uc.getLab3(), "New Name", "Bio", ""));
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void userDoesNotHaveWriteAccessToOtherUsersProject() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        SortedSet<? extends ProjectReaderTemplate.ProjectLineTemplate> projectLines =
            projectReader.readProjectsAllowedForWriting(joe);

        assertEquals(projectLines.size(), 0);

        createExperiment(joe, project);
    }

    @Test
    public void userHasWriteAccessToHisOwnProject() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long joesProject = createPrivateProject(joe, uc.getLab3());
        long bobsProject = createPrivateProject(bob, uc.getLab3());
        SortedSet<? extends ProjectReaderTemplate.ProjectLineTemplate> projectLines =
            projectReader.readProjectsAllowedForWriting(joe);

        assertEquals(projectLines.size(), 1);

        createExperiment(joe, joesProject);
        SortedSet<? extends ExperimentLineTemplate> experimentLines = experimentReader.readExperiments(joe, MY);
        assertEquals(experimentLines.size(), 1);
    }

    @Test
    public void userHasWriteAccessToSharedProject() {
        final long bob = uc.createLab3AndBob();

        final long joe = uc.createJoe();
        final long joesProject = createPrivateProject(joe, uc.getLab3());
        long bobsProject = createPrivateProject(bob, uc.getLab3());
        SortedSet<? extends ProjectReaderTemplate.ProjectLineTemplate> projectLinesBeforeSharing =
            projectReader.readProjectsAllowedForWriting(joe);
        assertEquals(projectLinesBeforeSharing.size(), 1);

        uc.shareProjectThrowGroup(bob, joe, bobsProject);
        SortedSet<? extends ProjectReaderTemplate.ProjectLineTemplate> projectLinesAfterSharing =
            projectReader.readProjectsAllowedForWriting(joe);
        assertEquals(projectLinesAfterSharing.size(), 2);

        createExperiment(joe, bobsProject);
        SortedSet<? extends ExperimentLineTemplate> experimentLines = experimentReader.readExperiments(joe, MY);
        assertEquals(experimentLines.size(), 1);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testCannotCreateExperimentInReadOnlyProject() {
        final long bob = uc.createLab3AndBob();

        final long joe = uc.createJoe();
        long bobsProject = createPrivateProject(bob, uc.getLab3());
        uc.shareProjectThrowGroup(bob, joe, bobsProject, SharingManagementTemplate.Access.READ);
        SortedSet<? extends ProjectReaderTemplate.ProjectLineTemplate> projectLinesBeforeSharing =
            projectReader.readProjectsAllowedForWriting(joe);
        assertEquals(projectLinesBeforeSharing.size(), 0);

        createExperiment(joe, bobsProject);
    }

    @Test
    public void testLAbHeadCanUpdateProject() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long project = createPrivateProject(bob, uc.getLab3());
        projectManagement.updateProject(poll, project, new ProjectInfoTemplate(uc.getLab3(), "New Name", "", "Bio"));
    }

    @Test
    public void createInOwnProject() {
        long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob, uc.getLab3());
        createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
    }

    @Test
    public void testProjectDetailsAvailableAfterCreation() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob, uc.getLab3());
        detailsReader.readProject(bob, project);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testAccessToProjectDetails() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob, uc.getLab3());
        detailsReader.readProject(uc.createKateAndLab2(), project);
    }

    @Test
    public void testProjectInMyWhenPrivate() {
        final long bob = uc.createLab3AndBob();
        createPrivateProject(bob, uc.getLab3());

        assertProjectsReadable(bob, MY, 1);
        assertProjectsReadable(bob, Filter.PUBLIC, 0);
        assertProjectsReadable(bob, Filter.SHARED_WITH_ME, 0);
        assertProjectsReadable(bob, ALL, 1);
    }

    @Test
    public void testMyPublicPresentedOnlyInMy() {
        final long bob = uc.createLab3AndBob();
        createPublicProject(bob, uc.getLab3());

        assertProjectsReadable(bob, MY, 1);
        assertProjectsReadable(bob, Filter.PUBLIC, 0);
        assertProjectsReadable(bob, Filter.SHARED_WITH_ME, 0);
        assertProjectsReadable(bob, ALL, 1);
    }

    @Test
    public void testPublicAccessibleByOtherUsers() {
        final long bob = uc.createLab3AndBob();
        createPublicProject(bob, uc.getLab3());
        final long kate = uc.createKateAndLab2();

        assertProjectsReadable(kate, MY, 0);
        assertProjectsReadable(kate, Filter.PUBLIC, 1);
        assertProjectsReadable(kate, Filter.SHARED_WITH_ME, 0);
        assertProjectsReadable(kate, ALL, 1);
    }

    @Test
    public void testSharedAccessibleToCollaborators() {
        final long bob = uc.createLab3AndBob();
        final long project = createPublicProject(bob, uc.getLab3());
        final long kate = uc.createKateAndLab2();
        uc.shareProjectThrowGroup(bob, kate, project);

        assertProjectsReadable(kate, MY, 0);
        assertProjectsReadable(kate, Filter.PUBLIC, 0);
        assertProjectsReadable(kate, Filter.SHARED_WITH_ME, 1);
        assertProjectsReadable(kate, ALL, 1);
    }

    @Test
    public void testSharedAccessibleOnlyToCollaborators() {
        final long bob = uc.createLab3AndBob();
        final long project = createPublicProject(bob, uc.getLab3());
        final long kate = uc.createKateAndLab2();
        final long joe = uc.createJoe();
        uc.shareProjectThrowGroup(bob, kate, project);

        assertProjectsReadable(joe, MY, 0);
        assertProjectsReadable(joe, ALL, 0);
        assertProjectsReadable(joe, Filter.PUBLIC, 0);
        assertProjectsReadable(joe, Filter.SHARED_WITH_ME, 0);
    }

    @Test
    public void testRemovingPrivateProject() {
        final long bob = uc.createLab3AndBob();
        final long privateProject = createPrivateProject(bob, uc.getLab3());
        assertEquals(projectReader.readProjects(bob, MY).size(), 1);
        projectManagement.removeProject(bob, privateProject);
        assertEquals(projectReader.readProjects(bob, MY).size(), 0);

    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testRemovingPublicProject() {
        final long bob = uc.createLab3AndBob();
        final long publicProject = createPublicProject(bob, uc.getLab3());
        assertEquals(projectReader.readProjects(bob, MY).size(), 1);
        projectManagement.removeProject(bob, publicProject);
    }

    @Test
    public void testRemovingUserFromLaboratoryWithProjects() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();//labHead
        final long publicProject = createPublicProject(bob, uc.getLab3());

        assertEquals(userReader.readUsersByLab(poll, uc.getLab3()).size(), 2);
        final int pollProjectsLength = projectReader.readProjects(poll, MY).size();
        assertEquals(projectReader.readProjects(bob, MY).size(), 1);
        labHeadManagement.removeUserFromLab(poll, uc.getLab3(), bob);
        assertEquals(projectReader.readProjects(bob, MY).size(), 0);
        assertEquals(projectReader.readProjects(poll, MY).size(), pollProjectsLength + 1);
    }

    @Test(expectedExceptions = ObjectNotFoundException.class)
    public void testCantReadRemovedProjectDetails() {
        final long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        projectManagement.removeProject(bob, project);
        detailsReader.readProject(bob, project);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCantCreateProjectWithEmptyName() {
        final long bob = uc.createLab3AndBob();
        projectManagement.createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "", "area", ""));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCantCreateProjectWithEmptyAreaOfResearch() {
        final long bob = uc.createLab3AndBob();
        projectManagement.createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "empty area project", "", ""));
    }

    @Test
    public void testCopyProject() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long originId = createPrivateProject(bob, uc.getLab3());
        final long copyId = projectManagement.copyProject(bob, new CopyProjectInfoTemplate(originId, joe, bob, false));
        ProjectItemTemplate origin = detailsReader.readProject(bob, originId);
        ProjectItemTemplate copy = detailsReader.readProject(joe, copyId);
        assertNotEquals(copy.ownerEmail, origin.ownerEmail);
        assertNotEquals(copy.id, origin.id);
        assertEquals(
            experimentReader.readExperimentsByProject(joe, copyId).size(),
            experimentReader.readExperimentsByProject(bob, originId).size()
        );
        assertEquals(fileReader.readFiles(joe, ALL), fileReader.readFiles(bob, ALL));

    }

    @Test
    public void testCopyProjectAttachments() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long originId = createPrivateProject(bob, uc.getLab3());
        attachmentForProject(bob, originId);
        final long copyId = projectManagement.copyProject(bob, new CopyProjectInfoTemplate(originId, joe, bob, false));
        ProjectItemTemplate origin = detailsReader.readProject(bob, originId);
        final ProjectItemTemplate copy = detailsReader.readProject(joe, copyId);
        assertEquals(copy.attachments.size(), 1);
        Assert.assertTrue(allAttachmentsSame(origin.attachments, copy.attachments), "Copied attachments doesn't match");
    }

    @Test
    public void testCopyProjectExperimentAttachments() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long originId = createPrivateProject(bob, uc.getLab3());
        final long originExperiment = createExperiment(bob, originId);
        final long attachmentForExperiment = attachmentForExperiment(bob, originExperiment);
        final long copyId = projectManagement.copyProject(bob, new CopyProjectInfoTemplate(originId, joe, bob, false));

        final ExperimentLineTemplate experimentLine = experimentReader.readExperiments(joe, MY).iterator().next();
        final DetailsReaderTemplate.ExperimentItemTemplate origin = detailsReader.readExperiment(bob, originExperiment);
        final DetailsReaderTemplate.ExperimentItemTemplate copied =
            detailsReader.readExperiment(joe, experimentLine.id);

        assertEquals(copied.attachments.size(), 1);
        Assert.assertTrue(
            allAttachmentsSame(origin.attachments, copied.attachments),
            "Copied attachments doesn't match for copied project experiments"
        );
    }

    private boolean allAttachmentsSame(ImmutableList<DetailsReaderTemplate.AttachmentItem> attachmentsOrigin,
                                       final ImmutableList<DetailsReaderTemplate.AttachmentItem> attachmentsCopy) {
        return attachmentsOrigin.stream().allMatch(origin -> attachmentsCopy.stream()
            .anyMatch(copy -> Objects.requireNonNull(origin).name.equals(copy.name)));
    }

    @Test
    public void testCopySharedProject() {
        final long bob = uc.createLab3AndBob();
        final long originId = createPublicProject(bob, uc.getLab3());
        final long kate = uc.createKateAndLab2();
        uc.shareProjectThrowGroup(bob, kate, originId);

        final long bobExperiment = createExperiment(bob, originId);
        final long kateExperiment = createExperiment(kate, originId);

        final long copyId = projectManagement.copyProject(bob, new CopyProjectInfoTemplate(originId, kate, bob, false));
        final ProjectItemTemplate origin = detailsReader.readProject(bob, originId);
        ProjectItemTemplate copy = detailsReader.readProject(kate, copyId);

        assertNotEquals(copy.ownerEmail, origin.ownerEmail);
        assertNotEquals(copy.id, origin.id);

        ExperimentLineTemplate originExperiment = Iterables.find(
            experimentReader.readExperimentsByProject(bob, originId),
            (Predicate<ExperimentLineTemplate>) experimentLine -> experimentLine.id == bobExperiment
        );

        ImmutableSortedSet<ExperimentLineTemplate> kateExperiments =
            ImmutableSortedSet.copyOf(experimentReader.readExperimentsByProject(kate, copyId));

        assertEquals(kateExperiments.size(), 1); // expecting 1 since only owner's experiments will be copied
        assertEquals(originExperiment.name, kateExperiments.first().name);

        ImmutableSortedSet<ExperimentLineTemplate> experimentLines =
            ImmutableSortedSet.copyOf(experimentReader.readExperimentsByProject(kate, copy.id));
        SortedSet<FileItem> originFiles = fileReader.readFileItemsByExperiment(bob, bobExperiment);
        SortedSet<FileItem> copiedFiles = fileReader.readFileItemsByExperiment(kate, experimentLines.first().id);
        assertEquals(originFiles.size(), copiedFiles.size());


        for (final FileItem originFile : originFiles) {
            FileItem found = Iterables.find(copiedFiles, fileItem -> {
                boolean nameContains = fileItem.name.contains(originFile.name);
                boolean labelsEquals = fileItem.labels.equals(originFile.labels);
                return nameContains && labelsEquals;
            });
            boolean originFileCopied = found != null;
            assertTrue("File not copied: " + originFile.name, originFileCopied);
        }

    }

    @Test
    public void testCopyProjectNameSetCorrectly() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long originId = createPrivateProject(bob, uc.getLab3());
        final long copyId = projectManagement.copyProject(bob, new CopyProjectInfoTemplate(originId, joe, bob, false));
        ProjectItemTemplate origin = detailsReader.readProject(bob, originId);
        ProjectItemTemplate copy = detailsReader.readProject(joe, copyId);
        assertNotEquals(copy.ownerEmail, origin.ownerEmail);
        assertNotEquals(copy.id, origin.id);
        assertEquals(copy.name, origin.name);
    }

    @Test
    public void testProjectSharingRequestPlacedInRequests() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final String requestedExperimentLink = "http://host.com/download/bulk?experiment=" + experiment;

        final long requestId =
            projectSharingRequestManagement.newProjectSharingRequest(joe, experiment, requestedExperimentLink);

        final ImmutableSortedSet<RequestsReaderTemplate.ProjectSharingInfo> sharingRequests =
            requestsReader.myProjectSharingInbox(bob);
        assertEquals(sharingRequests.size(), 1);
        final ProjectSharingInfo request = sharingRequests.first();
        assertEquals(request.projectSharingRequest, requestId);
        assertEquals(request.project, project);
        assertEquals(request.requester, joe);
        assertNotNull(request.sent);
    }

    //This test is added to verify issue 'Experiment sharing request is appeared in Inbox folder of all users'
    // (https://bitbucket.org/Infoclinika/chorus-dev/issues/1396/)
    @Test
    public void testProjectSharingRequestPlacedInProjectsOwnerInboxOnly() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long paul = uc.createPaul();
        createPrivateProject(paul, uc.getLab3());
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final String requestedExperimentLink = "http://host.com/download/bulk?experiment=" + experiment;

        projectSharingRequestManagement.newProjectSharingRequest(joe, experiment, requestedExperimentLink);

        final ImmutableSortedSet<RequestsReaderTemplate.ProjectSharingInfo> sharingRequestsOwner =
            requestsReader.myProjectSharingInbox(bob);
        final ImmutableSortedSet<RequestsReaderTemplate.ProjectSharingInfo> sharingRequestsNotOwner =
            requestsReader.myProjectSharingInbox(paul);

        assertEquals(
            sharingRequestsOwner.size(),
            1,
            "Project sharing request does not appear in Project's owner Inbox"
        );
        assertEquals(
            sharingRequestsNotOwner.size(),
            0,
            "Project sharing request is unexpectedly appeared in user's Inbox. User is not an owner of this project."
        );
    }

    @Test
    public void testProjectOwnerNotifiedAfterProjectSharingRequest() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final String requestedExperimentLink = "http://host.com/download/bulk?experiment=" + experiment;

        projectSharingRequestManagement.newProjectSharingRequest(joe, experiment, requestedExperimentLink);
        final ProjectSharingInfo request = requestsReader.myProjectSharingInbox(bob).first();

        verify(notificator())
            .sendProjectSharingRequestNotification(eq(bob), eq(joe), eq(request.project), eq(experiment));
    }

    @Test(dependsOnMethods = "testProjectSharingRequestPlacedInRequests")
    public void testRequesterReceivesEmailOnProjectSharingApproval() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);

        projectSharingRequestManagement
            .newProjectSharingRequest(joe, experiment, getRequestedExperimentLink(experiment));
        final ProjectSharingInfo request = requestsReader.myProjectSharingInbox(bob).first();
        projectSharingRequestManagement.approveSharingProject(bob, project, joe);

        verify(notificator())
            .projectSharingApproved(eq(joe), eq(request.projectName), sameListAs(request.experimentLinks));
    }

    @Test(dependsOnMethods = "testProjectSharingRequestPlacedInRequests")
    public void testRequesterReceivesEmailOnProjectSharingRefusal() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);

        projectSharingRequestManagement
            .newProjectSharingRequest(joe, experiment, getRequestedExperimentLink(experiment));
        final ProjectSharingInfo request = requestsReader.myProjectSharingInbox(bob).first();
        final String refuseComment = "sad but true";
        projectSharingRequestManagement.refuseSharingProject(bob, project, joe, refuseComment);

        verify(notificator()).projectSharingRejected(eq(joe), eq(request.projectName), eq(refuseComment));
    }

    @Test(dependsOnMethods = "testProjectSharingRequestPlacedInRequests")
    public void testProjectSharedAfterProjectSharingApproval() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);

        final int joeProjects = projectReader.readProjects(joe, Filter.SHARED_WITH_ME).size();
        assertEquals(joeProjects, 0);

        projectSharingRequestManagement
            .newProjectSharingRequest(joe, experiment, getRequestedExperimentLink(experiment));
        projectSharingRequestManagement.approveSharingProject(bob, project, joe);

        final int sharedProjects = projectReader.readProjects(joe, Filter.SHARED_WITH_ME).size();
        assertEquals(sharedProjects, 1);
    }

    @Test(dependsOnMethods = "testProjectSharingRequestPlacedInRequests")
    public void testProjectStillNotSharedAfterProjectSharingRefusal() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);

        final int joeProjects = projectReader.readProjects(joe, Filter.SHARED_WITH_ME).size();
        assertEquals(joeProjects, 0);

        projectSharingRequestManagement
            .newProjectSharingRequest(joe, experiment, getRequestedExperimentLink(experiment));
        projectSharingRequestManagement.refuseSharingProject(bob, project, joe, "sad but true");

        final int sharedProjects = projectReader.readProjects(joe, Filter.SHARED_WITH_ME).size();
        assertEquals(sharedProjects, 0);
    }

    @Test
    public void testProjectWithNoLabCanBeReadAndModifiedOnlyByOwner() {
        final long lab3LabHead = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long projectWithNoLab =
            projectManagement.createProject(joe, new ProjectInfoTemplate(null, "Project with no lab", "", "area"));

        try {
            projectReader.readProject(lab3LabHead, projectWithNoLab);
            fail(
                "Access denied was expected when not project owner attempts to read project with no lab even if it is" +
                    " Lab head of some lab");
        } catch (AccessDenied e) {
            //goes as planned
        }

        try {
            projectManagement.removeProject(lab3LabHead, projectWithNoLab);
            fail(
                "Access denied was expected when not project owner attempts to remove project with no lab even if it " +
                    "is Lab head of some lab");
        } catch (AccessDenied e) {
            //goes as planned
        }

        assertEquals(projectReader.readProjects(joe, MY).size(), 1);
        projectManagement.removeProject(joe, projectWithNoLab);
        assertEquals(projectReader.readProjects(joe, MY).size(), 0);
    }

    @Test
    public void testCreatedProjectCanBeRead() {
        final String projectName = "Manhattan";
        final String area = "51";
        long bob = uc.createLab3AndBob();
        final long projectWithNoLab =
            projectManagement.createProject(bob, new ProjectInfoTemplate(null, projectName, "", area));

        ProjectReaderTemplate.ProjectLineTemplate projectLine = projectReader.readProject(bob, projectWithNoLab);

        assertEquals(projectReader.readProjects(bob, MY).size(), 1);
        assertEquals(projectLine.name, projectName);
        assertEquals(projectLine.areaOfResearch, area);

    }

    @Test
    public void testCreateProjectWithAttachments() {
        final long bob = uc.createLab3AndBob();
        final long project = projectManagement
            .createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "Bob's Project", "area", "Some description"));
        final long attachment = attachmentForProject(bob, project);
        final ProjectItemTemplate projectItem = detailsReader.readProject(bob, project);
        assertEquals(getOnlyElement(projectItem.attachments).id, attachment);
    }

    @Test
    public void testRemoveProjectAttachments() {
        final long bob = uc.createLab3AndBob();
        final long project = projectManagement
            .createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "Bob's Project", "area", "Some description"));
        attachmentForProject(bob, project);
        attachmentManagement.updateProjectAttachments(bob, project, ImmutableSet.<Long>of());
        final ProjectItemTemplate projectItem = detailsReader.readProject(bob, project);
        assertTrue(projectItem.attachments.size() == 0);
    }

    @Test
    public void testUpdateProjectAttachments() {
        final long bob = uc.createLab3AndBob();
        final long project = projectManagement
            .createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "Bob's Project", "area", "Some description"));
        attachmentForProject(bob, project);
        final long imageAttachment = attachmentManagement.newAttachment(bob, "someImageAttachment.png", 1024);
        attachmentManagement.updateProjectAttachments(bob, project, ImmutableSet.of(imageAttachment));
        final ProjectItemTemplate projectItem = detailsReader.readProject(bob, project);
        assertEquals(getOnlyElement(projectItem.attachments).id, imageAttachment);
    }

    @Test
    public void testReadProjectAttachments() {
        final long bob = uc.createLab3AndBob();
        final long project = projectManagement
            .createProject(bob, new ProjectInfoTemplate(uc.getLab3(), "Bob's Project", "area", "Some description"));
        final long attachmentId = attachmentForProject(bob, project);
        assertEquals(attachmentsReader.readAttachment(bob, attachmentId).id, attachmentId);
        assertEquals(attachmentsReader.readAttachments(PROJECT, bob, project).size(), 1);
    }


}
