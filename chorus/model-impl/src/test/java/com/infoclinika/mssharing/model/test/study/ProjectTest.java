/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.test.study;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.model.read.DetailsReader.ExperimentShortInfoDetailed;
import com.infoclinika.mssharing.model.read.ExperimentLine;
import com.infoclinika.mssharing.model.read.ProjectLine;
import com.infoclinika.mssharing.model.read.RequestsReader;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.read.dto.details.ProjectItem;
import com.infoclinika.mssharing.model.write.ProjectInfo;
import com.infoclinika.mssharing.model.write.StudyManagement;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.ObjectNotFoundException;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;
import com.infoclinika.mssharing.platform.model.common.items.NamedItem;
import com.infoclinika.mssharing.platform.model.helper.ProjectCreationHelperTemplate;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.read.ProjectReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.*;
import static com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate.AttachmentType.PROJECT;
import static com.infoclinika.mssharing.platform.model.read.Filter.ALL;
import static com.infoclinika.mssharing.platform.model.read.Filter.MY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

/**
 * @author Stanislav Kurilin
 */
public class ProjectTest extends AbstractStudyTest {

    @Inject
    private AttachmentsReaderTemplate<AttachmentsReaderTemplate.AttachmentItem> attachmentsReader;

    @Inject
    private ProjectCreationHelperTemplate projectCreationHelperTemplate;

    @Inject
    @Named("projectReaderImpl")
    private ProjectReaderTemplate<ProjectLine> defaultProjectReader;

    @Inject
    @Named("projectDashboardRecordsReaderImpl")
    private ProjectReaderTemplate<ProjectLine> projectDashboardRecordsReader;

    private static final Logger LOGGER = Logger.getLogger(ProjectTest.class);


    @Test(enabled = false)
    public void testReadLargeAmountOfProjects() {
        final long bob = uc.createLab3AndBob();
        final Long lab = uc.getLab3();
        final int amount = 1000;
        createProjects(bob, lab, amount);

        LOGGER.info("Start read projects (default)");
        final long begin = System.currentTimeMillis();
        final ImmutableSet<ProjectLine> projectLines = ImmutableSet.copyOf(defaultProjectReader.readProjects(bob, ALL));
        final long defaultReadMills = System.currentTimeMillis() - begin;
        LOGGER.info("End read projects (default) im mills: " + defaultReadMills);
        Assert.assertEquals(projectLines.size(), amount);

        LOGGER.info("Start read projects (records)");
        final long beginRecords = System.currentTimeMillis();
        final ImmutableSet<ProjectLine> recordProjectLines = ImmutableSet.copyOf(projectDashboardRecordsReader.readProjects(bob, ALL));
        final long recordsReadMills = System.currentTimeMillis() - beginRecords;
        LOGGER.info("End read projects (records) in mills: " + recordsReadMills);
        Assert.assertEquals(recordProjectLines.size(), amount);

        LOGGER.info("Difference (recordsReadMills - defaultReadMills): " + (recordsReadMills - defaultReadMills));

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
        final ImmutableSet<ProjectLine> projectLines = ImmutableSet.copyOf(defaultProjectReader.readProjects(bob, ALL, new PagedItemInfo(100, 0, "laboratory", true, "")));
        final long defaultReadMills = System.currentTimeMillis() - begin;
        LOGGER.info("End read projects (default) im mills: " + defaultReadMills);
        Assert.assertEquals(projectLines.size(), 100);

        LOGGER.info("Start read projects (records)");
        final long beginRecords = System.currentTimeMillis();
        final ImmutableSet<ProjectLine> recordProjectLines = ImmutableSet.copyOf(projectDashboardRecordsReader.readProjects(bob, ALL, new PagedItemInfo(100, 0, "laboratory", true, "")));
        final long recordsReadMills = System.currentTimeMillis() - beginRecords;
        LOGGER.info("End read projects (records) in mills: " + recordsReadMills);
        Assert.assertEquals(recordProjectLines.size(), 100);

        LOGGER.info("Difference (recordsReadMills - defaultReadMills): " + (recordsReadMills - defaultReadMills));

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
        final Iterable<ProjectLine> projects = dashboardReader.readProjects(bob, ALL);

        assertEquals(Iterables.size(projects), 1);

        final ProjectLine project = projects.iterator().next();
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
        assertThat(namedItems.size(), is(1));
        assertThat(namedItems.iterator().next().id, is(project));
    }

    @Test
    public void testCreateProjectForUserWithoutLab() {
        final long john = uc.createJohnWithoutLab();
        uc.createProject(john);
        final Iterable<ProjectLine> projects = dashboardReader.readProjects(john, ALL);

        assertEquals(Iterables.size(projects), 1);
    }


    @Test
    public void testSeveralProjectsWithSameNameAreRead() {
        final long bob = uc.createLab3AndBob();
        final long projectId = studyManagement.createProject(bob, new ProjectInfo("Duplicated title", "area", "", uc.getLab3()));
        final long paul = uc.createPaul();
        uc.sharing(bob, projectId, ImmutableSet.of(paul), Collections.<Long>emptySet());
        //create with the same name
        studyManagement.createProject(paul, new ProjectInfo("Duplicated title", "area", "", uc.getLab3()));
        assertEquals(dashboardReader.readProjects(paul, ALL).size(), 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUserCantCreateProjectWithAlreadyExistedName() {
        final long bob = uc.createLab3AndBob();
        studyManagement.createProject(bob, new ProjectInfo("Duplicated title", "area", "", uc.getLab3()));
        //create with the same name
        studyManagement.createProject(bob, new ProjectInfo("Duplicated title", "area", "", uc.getLab3()));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUserCantUpdateExperimentWithAlreadyExistedName() {
        final long bob = uc.createLab3AndBob();
        studyManagement.createProject(bob, new ProjectInfo("Duplicated title", "area", "", uc.getLab3()));
        final long project = studyManagement.createProject(bob, new ProjectInfo("Title", "area", "", uc.getLab3()));
        //update with the same name
        studyManagement.updateProject(bob, project, new ProjectInfo("Duplicated title", "Bio", "", uc.getLab3()));
    }


    //Update project
    @Test
    public void testUpdatingProject() {
        final long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        studyManagement.updateProject(bob, project, new ProjectInfo("New Name", "Bio", "", uc.getLab3()));
        final ProjectLine updatedProject = dashboardReader.readProjects(bob, MY).iterator().next();

        assertEquals(updatedProject.name, "New Name");
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testOnlyOwnerCanUpdateProject() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        studyManagement.updateProject(joe, project, new ProjectInfo("New Name", "Bio", "", uc.getLab3()));
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void userDoesNotHaveWriteAccessToOtherUsersProject() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        Set<ProjectLine> projectLines = dashboardReader.readProjectsAllowedForWriting(joe);

        assertEquals(projectLines.size(), 0);

        createExperiment(joe, project);
    }

    @Test
    public void userHasWriteAccessToHisOwnProject() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long joesProject = createPrivateProject(joe, uc.getLab3());
        long bobsProject = createPrivateProject(bob, uc.getLab3());
        Set<ProjectLine> projectLines = dashboardReader.readProjectsAllowedForWriting(joe);

        assertEquals(projectLines.size(), 1);

        createExperiment(joe, joesProject);
        Set<ExperimentLine> experimentLines = dashboardReader.readExperiments(joe, MY);
        assertEquals(experimentLines.size(), 1);
    }

    @Test
    public void userHasWriteAccessToSharedProject() {
        final long bob = uc.createLab3AndBob();

        final long joe = uc.createJoe();
        final long joesProject = createPrivateProject(joe, uc.getLab3());
        long bobsProject = createPrivateProject(bob, uc.getLab3());
        Set<ProjectLine> projectLinesBeforeSharing = dashboardReader.readProjectsAllowedForWriting(joe);
        assertEquals(projectLinesBeforeSharing.size(), 1);

        uc.shareProjectThrowGroup(bob, joe, bobsProject);
        Set<ProjectLine> projectLinesAfterSharing = dashboardReader.readProjectsAllowedForWriting(joe);
        assertEquals(projectLinesAfterSharing.size(), 2);

        createExperiment(joe, bobsProject);
        Set<ExperimentLine> experimentLines = dashboardReader.readExperiments(joe, MY);
        assertEquals(experimentLines.size(), 1);
    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testCannotCreateExperimentInReadOnlyProject() {
        final long bob = uc.createLab3AndBob();

        final long joe = uc.createJoe();
        long bobsProject = createPrivateProject(bob, uc.getLab3());
        uc.shareProjectThrowGroup(bob, joe, bobsProject, SharingManagementTemplate.Access.READ);
        Set<ProjectLine> projectLinesBeforeSharing = dashboardReader.readProjectsAllowedForWriting(joe);
        assertEquals(projectLinesBeforeSharing.size(), 0);

        createExperiment(joe, bobsProject);
    }

    @Test
    public void testLAbHeadCanUpdateProject() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long project = createPrivateProject(bob, uc.getLab3());
        studyManagement.updateProject(poll, project, new ProjectInfo("New Name", "Bio", "", uc.getLab3()));
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
        assertEquals(dashboardReader.readProjects(bob, MY).size(), 1);
        studyManagement.moveProjectToTrash(bob, privateProject);
        assertEquals(dashboardReader.readProjects(bob, MY).size(), 0);

    }

    @Test(expectedExceptions = AccessDenied.class)
    public void testRemovingPublicProject() {
        final long bob = uc.createLab3AndBob();
        final long publicProject = createPublicProject(bob, uc.getLab3());
        assertEquals(dashboardReader.readProjects(bob, MY).size(), 1);
        studyManagement.moveProjectToTrash(bob, publicProject);
    }

    @Test
    public void testRemovingUserFromLaboratoryWithProjects() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();//labHead
        final long publicProject = createPublicProject(bob, uc.getLab3());

        assertEquals(dashboardReader.readUsersByLab(poll, uc.getLab3()).size(), 2);
        final int pollProjectsLength = dashboardReader.readProjects(poll, MY).size();
        assertEquals(dashboardReader.readProjects(bob, MY).size(), 1);
        labHeadManagement.removeUserFromLab(poll, uc.getLab3(), bob);
        assertEquals(dashboardReader.readProjects(bob, MY).size(), 0);
        assertEquals(dashboardReader.readProjects(poll, MY).size(), pollProjectsLength + 1);
    }

    @Test(expectedExceptions = ObjectNotFoundException.class)
    public void testCantReadRemovedProjectDetails() {
        final long bob = uc.createLab3AndBob();
        final long project = createPrivateProject(bob, uc.getLab3());
        studyManagement.moveProjectToTrash(bob, project);
        detailsReader.readProject(bob, project);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCantCreateProjectWithEmptyName() {
        final long bob = uc.createLab3AndBob();
        studyManagement.createProject(bob, new ProjectInfo("", "area", "", uc.getLab3()));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCantCreateProjectWithEmptyAreaOfResearch() {
        final long bob = uc.createLab3AndBob();
        studyManagement.createProject(bob, new ProjectInfo("empty area project", "", "", uc.getLab3()));
    }

    @Test
    public void testCopyProject() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long originId = createPrivateProject(bob, uc.getLab3());
        final long copyId = studyManagement.copyProject(bob, new StudyManagement.CopyProjectInfo(originId, joe, bob, uc.getLab3(), false));
        ProjectItem origin = detailsReader.readProject(bob, originId);
        ProjectItem copy = detailsReader.readProject(joe, copyId);
        assertNotEquals(copy.ownerEmail, origin.ownerEmail);
        assertNotEquals(copy.projectId, origin.projectId);
        assertEquals(dashboardReader.readExperimentsByProject(joe, copyId).size(), dashboardReader.readExperimentsByProject(bob, originId).size());
        assertEquals(fileReader.readFiles(joe, ALL), fileReader.readFiles(bob, ALL));

    }

    @Test
    public void testCopyProjectAttachments() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long originId = createPrivateProject(bob, uc.getLab3());
        attachmentForProject(bob, originId);
        final long copyId = studyManagement.copyProject(bob, new StudyManagement.CopyProjectInfo(originId, joe, bob, uc.getLab3(), false));
        ProjectItem origin = detailsReader.readProject(bob, originId);
        final ProjectItem copy = detailsReader.readProject(joe, copyId);
        assertThat(copy.attachments.size(), is(1));
        Assert.assertTrue(allAttachmentsSame(origin.attachments, copy.attachments), "Copied attachments doesn't match");
    }

    @Test
    public void testCopyProjectExperimentAttachments() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long originId = createPrivateProject(bob, uc.getLab3());
        final long originExperiment = createExperiment(bob, originId);
        final long attachmentForExperiment = attachmentForExperiment(bob, originExperiment);
        final long copyId = studyManagement.copyProject(bob, new StudyManagement.CopyProjectInfo(originId, joe, bob, uc.getLab3(), false));

        final ExperimentShortInfoDetailed originExperimentInfo = (ExperimentShortInfoDetailed) detailsReader.readExperimentShortInfo(bob, originExperiment);
        final ExperimentLine experimentLine = dashboardReader.readExperiments(joe, MY).iterator().next();
        final ExperimentItem origin = detailsReader.readExperiment(bob, originExperiment);

        final ExperimentItem copied = detailsReader.readExperiment(joe, experimentLine.id);
        final ExperimentShortInfoDetailed copiedExperimentInfo = (ExperimentShortInfoDetailed) detailsReader.readExperimentShortInfo(joe, experimentLine.id);

        assertThat(copied.attachments.size(), is(1));

        assertEquals(copiedExperimentInfo.category, originExperimentInfo.category);
        Assert.assertTrue(allAttachmentsSame(origin.attachments, copied.attachments), "Copied attachments doesn't match for copied project experiments");
    }

    private boolean allAttachmentsSame(ImmutableList<DetailsReaderTemplate.AttachmentItem> attachmentsOrigin, final ImmutableList<DetailsReaderTemplate.AttachmentItem> attachmentsCopy) {
        return all(attachmentsOrigin, new Predicate<DetailsReaderTemplate.AttachmentItem>() {
            @Override
            public boolean apply(final DetailsReaderTemplate.AttachmentItem origin) {
                return any(attachmentsCopy, new Predicate<DetailsReaderTemplate.AttachmentItem>() {
                    @Override
                    public boolean apply(DetailsReaderTemplate.AttachmentItem copy) {
                        return origin.name.equals(copy.name);
                    }
                });
            }
        });
    }

    @Test
    public void testCopySharedProject() {
        final long bob = uc.createLab3AndBob();
        final long originId = createPublicProject(bob, uc.getLab3());
        final long kate = uc.createKateAndLab2();
        uc.shareProjectThrowGroup(bob, kate, originId);

        final long bobExperiment = createExperiment(bob, originId);
        final long kateExperiment = createExperiment(kate, originId);

        final long copyId = studyManagement.copyProject(bob, new StudyManagement.CopyProjectInfo(originId, kate, bob, uc.getLab3(), false));
        final ProjectItem origin = detailsReader.readProject(bob, originId);
        ProjectItem copy = detailsReader.readProject(kate, copyId);

        SortedSet<FileItem> originFiles = dashboardReader.readFileItemsByExperiment(bob, bobExperiment);
        ImmutableSortedSet<ExperimentLine> experimentLines = ImmutableSortedSet.copyOf(dashboardReader.readExperimentsByProject(kate, copy.projectId));
        SortedSet<FileItem> copiedFiles = dashboardReader.readFileItemsByExperiment(kate, experimentLines.first().id);


        ExperimentLine originExperiment = Iterables.find(dashboardReader.readExperimentsByProject(bob, originId), new Predicate<ExperimentLine>() {
            @Override
            public boolean apply(@Nullable ExperimentLine experimentLine) {
                return experimentLine.id == bobExperiment;
            }
        });

        ImmutableSortedSet<ExperimentLine> kateExperiments = ImmutableSortedSet.copyOf(dashboardReader.readExperimentsByProject(kate, copyId));


        assertNotEquals(copy.ownerEmail, origin.ownerEmail);
        assertNotEquals(copy.projectId, origin.projectId);

        assertEquals(kateExperiments.size(), 1); // expecting 1 since only owner's experiments will be copied
        assertEquals(originExperiment.name, kateExperiments.first().name);

        assertEquals(originFiles.size(), copiedFiles.size());


        for (final FileItem originFile : originFiles) {
            FileItem found = Iterables.find(copiedFiles, new Predicate<FileItem>() {
                @Override
                public boolean apply(FileItem fileItem) {
                    boolean nameContains = fileItem.name.contains(originFile.name);
                    boolean labelsEquals = fileItem.labels.equals(originFile.labels);
                    return nameContains && labelsEquals;
                }
            });
            boolean originFileCopied = found != null;
            assertTrue("File not copied: " + originFile.name, originFileCopied);
        }

    }

    @Test
    public void testCopiedProjectExperimentFilesNamedCorrectly() {
        final long bob = uc.createLab3AndBob();
        final long originId = createPublicProject(bob, uc.getLab3());
        final long kate = uc.createKateAndLab2();
        uc.shareProjectThrowGroup(bob, kate, originId);

        final long bobExperiment = createExperiment(bob, originId);
        final long kateExperiment = createExperiment(kate, originId);

        final long copyId = studyManagement.copyProject(bob, new StudyManagement.CopyProjectInfo(originId, kate, bob, uc.getLab3(), false));
        ProjectItem copy = detailsReader.readProject(kate, copyId);

        SortedSet<FileItem> originFiles = dashboardReader.readFileItemsByExperiment(bob, bobExperiment);
        ImmutableSortedSet<ExperimentLine> experimentLines = ImmutableSortedSet.copyOf(dashboardReader.readExperimentsByProject(kate, copy.projectId));
        SortedSet<FileItem> copiedFiles = dashboardReader.readFileItemsByExperiment(kate, experimentLines.first().id);

        final FileItem copiedFile = copiedFiles.first();
        final String originFileName = originFiles.first().name;
        final String fileNameWithoutExtension = originFileName.lastIndexOf(".") != -1 ?
                originFileName.substring(0, originFileName.lastIndexOf(".")) : originFileName;

        Assert.assertTrue(copiedFile.name.matches("Copy_of_" + fileNameWithoutExtension + "_(.*?)"),
                "Error. Copied file name " + copiedFile.name + " is incorrect for file " + originFileName);
    }

    @Test
    public void testCopyProjectNameSetCorrectly() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long originId = createPrivateProject(bob, uc.getLab3());
        final long copyId = studyManagement.copyProject(bob, new StudyManagement.CopyProjectInfo(originId, joe, bob, uc.getLab3(), false));
        ProjectItem origin = detailsReader.readProject(bob, originId);
        ProjectItem copy = detailsReader.readProject(joe, copyId);
        assertNotEquals(copy.ownerEmail, origin.ownerEmail);
        assertNotEquals(copy.projectId, origin.projectId);
        assertEquals(copy.name, origin.name);
    }

    @Test
    public void testCopyProjectRequest() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long origin = createPrivateProject(bob, uc.getLab3());
        final long copyRequest = studyManagement.newProjectCopyRequest(bob, joe, origin);
        assertTrue(from(requestsReader.myCopyProjectInbox(joe)).anyMatch(new Predicate<RequestsReader.ProjectCopyRequest>() {
            @Override
            public boolean apply(RequestsReader.ProjectCopyRequest input) {
                return input.id == copyRequest;
            }
        }));
    }

    // Test is added to verify issue when only one message about project copying is displayed in the Inbox, even if
    // several projects have been copied (https://bitbucket.org/Infoclinika/chorus-dev/issues/1335)
    @Test
    public void testCopySeveralProjects() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project1 = studyManagement.createProject(bob, new ProjectInfo("private project1", "area", "", uc.getLab3()));
        final long project2 = studyManagement.createProject(bob, new ProjectInfo("private project2", "area", "", uc.getLab3()));
        studyManagement.newProjectCopyRequest(bob, joe, project1);
        studyManagement.newProjectCopyRequest(bob, joe, project2);
        assertEquals(requestsReader.myCopyProjectInbox(joe).size(), 2);
    }

    @Test
    public void testApproveCopyProjectRequest() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();
        final long origin = createPrivateProject(bob, uc.getLab3());
        studyManagement.newProjectCopyRequest(bob, kate, origin);
        studyManagement.approveCopyProjectRequest(kate, origin, uc.kateLab());
        assertThat(dashboardReader.readProjects(kate, MY).size(), is(1));
        assertThat(requestsReader.myProjectSharingInbox(kate).size(), is(0));
    }

    @Test
    public void testRejectCopyProjectRequest() {
        final long bob = uc.createLab3AndBob();
        final long kate = uc.createKateAndLab2();
        final long origin = createPrivateProject(bob, uc.getLab3());
        studyManagement.newProjectCopyRequest(bob, kate, origin);
        studyManagement.refuseCopyProjectRequest(kate, origin);
        assertThat(dashboardReader.readProjects(kate, MY).size(), is(0));
        assertThat(requestsReader.myProjectSharingInbox(kate).size(), is(0));
    }

    @Test
    public void testProjectSharingRequestPlacedInRequests() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final String requestedExperimentLink = "http://host.com/download/bulk?experiment=" + experiment;

        final long requestId = studyManagement.newProjectSharingRequest(joe, experiment, requestedExperimentLink);

        final ImmutableSortedSet<RequestsReader.ProjectSharingInfo> sharingRequests = requestsReader.myProjectSharingInbox(bob);
        assertEquals(sharingRequests.size(), 1);
        final RequestsReader.ProjectSharingInfo request = sharingRequests.first();
        assertEquals(request.projectSharingRequest, requestId);
        assertEquals(request.project, project);
        assertEquals(request.requester, joe);
        assertNotNull(request.sent);
    }

    @Test
    public void testProjectOwnerNotifiedAfterProjectSharingRequest() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);
        final String requestedExperimentLink = "http://host.com/download/bulk?experiment=" + experiment;

        studyManagement.newProjectSharingRequest(joe, experiment, requestedExperimentLink);
        final RequestsReader.ProjectSharingInfo request = requestsReader.myProjectSharingInbox(bob).first();

        verify(notificator()).sendProjectSharingRequestNotification(eq(bob), eq(joe), eq(request.project), eq(experiment));
    }

    @Test(dependsOnMethods = "testProjectSharingRequestPlacedInRequests")
    public void testRequesterReceivesEmailOnProjectSharingApproval() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);

        studyManagement.newProjectSharingRequest(joe, experiment, getRequestedExperimentLink(experiment));
        final RequestsReader.ProjectSharingInfo request = requestsReader.myProjectSharingInbox(bob).first();
        studyManagement.approveSharingProject(bob, project, joe);

        verify(notificator()).projectSharingApproved(eq(joe), eq(request.projectName), sameListAs(request.experimentLinks));
    }

    @Test(dependsOnMethods = "testProjectSharingRequestPlacedInRequests")
    public void testRequesterReceivesEmailOnProjectSharingRefusal() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);

        studyManagement.newProjectSharingRequest(joe, experiment, getRequestedExperimentLink(experiment));
        final RequestsReader.ProjectSharingInfo request = requestsReader.myProjectSharingInbox(bob).first();
        final String refuseComment = "sad but true";
        studyManagement.refuseSharingProject(bob, project, joe, refuseComment);

        verify(notificator()).projectSharingRejected(eq(joe), eq(request.projectName), eq(refuseComment));
    }

    @Test(dependsOnMethods = "testProjectSharingRequestPlacedInRequests")
    public void testProjectSharedAfterProjectSharingApproval() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);

        final int joeProjects = dashboardReader.readProjects(joe, Filter.SHARED_WITH_ME).size();
        assertEquals(joeProjects, 0);

        studyManagement.newProjectSharingRequest(joe, experiment, getRequestedExperimentLink(experiment));
        studyManagement.approveSharingProject(bob, project, joe);

        final int sharedProjects = dashboardReader.readProjects(joe, Filter.SHARED_WITH_ME).size();
        assertEquals(sharedProjects, 1);
    }

    @Test(dependsOnMethods = "testProjectSharingRequestPlacedInRequests")
    public void testProjectStillNotSharedAfterProjectSharingRefusal() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = createPrivateProject(bob, uc.getLab3());
        final long experiment = createInstrumentAndExperimentWithOneFile(bob, uc.getLab3(), project);

        final int joeProjects = dashboardReader.readProjects(joe, Filter.SHARED_WITH_ME).size();
        assertEquals(joeProjects, 0);

        studyManagement.newProjectSharingRequest(joe, experiment, getRequestedExperimentLink(experiment));
        studyManagement.refuseSharingProject(bob, project, joe, "sad but true");

        final int sharedProjects = dashboardReader.readProjects(joe, Filter.SHARED_WITH_ME).size();
        assertEquals(sharedProjects, 0);
    }

    @Test
    public void testProjectWithNoLabCanBeReadAndModifiedOnlyByOwner() {
        final long lab3LabHead = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long projectWithNoLab = studyManagement.createProject(joe, new ProjectInfo("Project with no lab", "area", "", null));

        try {
            dashboardReader.readProject(lab3LabHead, projectWithNoLab);
            fail("Access denied was expected when not project owner attempts to read project with no lab even if it is Lab head of some lab");
        } catch (AccessDenied e) {
            //goes as planned
        }

        try {
            studyManagement.moveProjectToTrash(lab3LabHead, projectWithNoLab);
            fail("Access denied was expected when not project owner attempts to remove project with no lab even if it is Lab head of some lab");
        } catch (AccessDenied e) {
            //goes as planned
        }

        assertEquals(dashboardReader.readProjects(joe, MY).size(), 1);
        studyManagement.moveProjectToTrash(joe, projectWithNoLab);
        assertEquals(dashboardReader.readProjects(joe, MY).size(), 0);
    }

    @Test
    public void testCreatedProjectCanBeRead() {
        final String projectName = "Manhattan";
        final String area = "51";
        long bob = uc.createLab3AndBob();
        final long projectWithNoLab = studyManagement.createProject(bob, new ProjectInfo(projectName, area, "", null));

        ProjectLine projectLine = dashboardReader.readProject(bob, projectWithNoLab);

        assertEquals(dashboardReader.readProjects(bob, MY).size(), 1);
        assertEquals(projectLine.name, projectName);
        assertEquals(projectLine.areaOfResearch, area);

    }

    @Test
    public void testCreateProjectWithAttachments() {
        final long bob = uc.createLab3AndBob();
        final long project = studyManagement.createProject(bob, new ProjectInfo("Bob's Project", "area", "Some description", uc.getLab3()));
        final long attachment = attachmentForProject(bob, project);
        final ProjectItem projectItem = detailsReader.readProject(bob, project);
        assertThat(getOnlyElement(projectItem.attachments).id, is(attachment));
    }

    @Test
    public void testRemoveProjectAttachments() {
        final long bob = uc.createLab3AndBob();
        final long project = studyManagement.createProject(bob, new ProjectInfo("Bob's Project", "area", "Some description", uc.getLab3()));
        attachmentForProject(bob, project);
        attachmentManagement.updateProjectAttachments(bob, project, ImmutableSet.<Long>of());
        final ProjectItem projectItem = detailsReader.readProject(bob, project);
        assertTrue(projectItem.attachments.size() == 0);
    }

    @Test
    public void testUpdateProjectAttachments() {
        final long bob = uc.createLab3AndBob();
        final long project = studyManagement.createProject(bob, new ProjectInfo("Bob's Project", "area", "Some description", uc.getLab3()));
        attachmentForProject(bob, project);
        final long imageAttachment = attachmentManagement.newAttachment(bob, "someImageAttachment.png", 1024);
        attachmentManagement.updateProjectAttachments(bob, project, ImmutableSet.of(imageAttachment));
        final ProjectItem projectItem = detailsReader.readProject(bob, project);
        assertThat(getOnlyElement(projectItem.attachments).id, is((imageAttachment)));
    }

    @Test
    public void testReadProjectAttachments() {
        final long bob = uc.createLab3AndBob();
        final long project = studyManagement.createProject(bob, new ProjectInfo("Bob's Project", "area", "Some description", uc.getLab3()));
        final long attachmentId = attachmentForProject(bob, project);
        assertThat(attachmentsReader.readAttachment(bob, attachmentId).id, is(attachmentId));
        assertThat(attachmentsReader.readAttachments(PROJECT, bob, project).size(), is(1));
    }

    @Test
    public void testCanDeleteProjectPermanentlyWithActiveCopyRequest() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = uc.createProject(bob);
        studyManagement.newProjectCopyRequest(bob, joe, project);
        studyManagement.removeProject(bob, project);
        assertTrue(isEmpty(dashboardReader.readProjects(bob, ALL)));
    }

    @Test
    public void testCanMoveProjectToTrashWithActiveCopyRequest() {
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        final long project = uc.createProject(bob);
        studyManagement.newProjectCopyRequest(bob, joe, project);
        studyManagement.moveProjectToTrash(bob, project);
        assertTrue(isEmpty(dashboardReader.readProjects(bob, ALL)));
    }

}
