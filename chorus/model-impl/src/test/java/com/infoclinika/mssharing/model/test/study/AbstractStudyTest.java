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
package com.infoclinika.mssharing.model.test.study;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.helper.ExperimentLabelsInfo;
import com.infoclinika.mssharing.model.write.AnalysisBounds;
import com.infoclinika.mssharing.model.write.ExperimentInfo;
import com.infoclinika.mssharing.model.write.FileMetaDataInfo;
import com.infoclinika.mssharing.model.write.ProjectInfo;
import com.infoclinika.mssharing.platform.fileserver.StoredObject;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Iterables.size;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

/**
 * @author Stanislav Kurilin
 */
abstract class AbstractStudyTest extends AbstractTest {

    protected long experimentInNewProject(long user, long lab) {
        final long project1 = uc.createProject(user, lab);
        final long file = uc.saveFile(user);
        final ExperimentInfo.Builder builder = new ExperimentInfo.Builder().name(generateString())
            .description("")
            .experimentType(anyExperimentType())
            .specie(unspecified())
            .experimentLabels(new ExperimentLabelsInfo())
            .project(project1)
            .lab(lab)
            .billLab(lab)
            .is2dLc(false)
            .restriction(restriction(user))
            .factors(NO_FACTORS)
            .files(noFactoredFile(file))
            .bounds(new AnalysisBounds())
            .lockMasses(NO_LOCK_MASSES)
            .sampleTypesCount(1);
        return studyManagement.createExperiment(user, builder.build());
    }

    protected long instrument(long bob, long lab, long model) {
        return createInstrumentAndApproveIfNeeded(bob, lab, model, instrumentDetails()).get();
    }

    protected void assertAvailableFilesByInstrumentModel(long user, long specie, long model, long size) {
        assertEquals(size(experimentCreationHelper.availableFilesByInstrumentModel(user, specie, model, null)), size);
    }

    protected long createInstrumentBySpecifiedInstrumentModel(long bob, long lab, long model) {
        return instrument(bob, lab, model);
    }

    protected static final List<ExperimentManagementTemplate.MetaFactorTemplate> NO_FACTORS = Collections.emptyList();

    protected void assertProjectsReadable(long bob, Filter filter, int number) {
        assertEquals(Iterables.size(dashboardReader.readProjects(bob, filter)), number);
    }

    public long createPrivateProject(long user, Long lab) {
        final long project = studyManagement.createProject(user, new ProjectInfo("private project", "area", "", lab));
        sharingManagement.makeProjectPrivate(user, project);
        return project;
    }

    public long createPublicProject(long user, Long lab) {
        final long project = studyManagement.createProject(user, new ProjectInfo("public project", "area", "", lab));
        sharingManagement.makeProjectPublic(user, project);
        return project;
    }

    protected long createProjectWithName(long user, Long lab, String name) {
        return studyManagement.createProject(user, new ProjectInfo(name, "area", "", lab));
    }

    protected void createFileWithInstrument(long bob, long instrument, long specie) {
        long file = instrumentManagement.createFile(bob,
            instrument,
            new FileMetaDataInfo(UUID.randomUUID().toString(), 0, "", null, specie, false)
        );
        instrumentManagement.setContent(bob, file, mock(StoredObject.class));
    }

    protected String getRequestedExperimentLink(long experiment) {
        return "http://host.com/download/bulk?experiment=" + experiment;
    }

    protected long attachmentForProject(long actor, long project) {
        final long attachment = attachmentManagement.newAttachment(actor, "someAttachment.pdf", 1024 * 1024);
        attachmentManagement.updateProjectAttachments(actor, project, ImmutableSet.of(attachment));
        //noinspection unchecked
        storageService().put(storedObjectPaths.projectAttachmentPath(actor, attachment), mock(StoredObject.class));
        return attachment;
    }

    protected long attachmentForExperiment(long bob, long experiment) {
        final long pdfAttachment = attachmentManagement.newAttachment(bob, "pdfAttachment.pdf", 1024 * 1024);
        attachmentManagement.updateExperimentAttachments(bob, experiment, ImmutableSet.of(pdfAttachment));
        //noinspection unchecked
        storageService().put(storedObjectPaths.experimentAttachmentPath(bob, pdfAttachment), mock(StoredObject.class));
        return pdfAttachment;
    }
}
