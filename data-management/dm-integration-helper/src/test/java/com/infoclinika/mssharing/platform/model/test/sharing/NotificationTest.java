/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.model.test.sharing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.model.NotifierTemplate;
import com.infoclinika.mssharing.platform.model.write.ProjectManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Stanislav Kurilin
 */
public class NotificationTest extends AbstractSharingTest {
    @Test
    public void testUserReceivesEmailOnProjectSharingDirectly() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final String projName = generateString();
        final long proj = projectManagement.createProject(bob, new ProjectManagementTemplate.ProjectInfoTemplate(uc.getLab3(), projName, generateString(), generateString()));
        final NotifierTemplate notifier = notificator();

        sharingManagement.updateSharingPolicy(bob, proj, ImmutableMap.of(poll, SharingManagementTemplate.Access.WRITE), emptySharing, true);

        verify(notifier).projectShared(eq(bob), eq(poll), eq(proj));
    }

    @Test
    public void testUserReceivesEmailOnProjectSharingThrowGroup() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final String projName = generateString();
        final long proj = projectManagement.createProject(bob, new ProjectManagementTemplate.ProjectInfoTemplate(uc.getLab3(), projName, generateString(), generateString()));
        final NotifierTemplate notifier = notificator();
        final long group = sharingManagement.createGroup(bob, generateString(), ImmutableSet.of(poll));

        sharingManagement.updateSharingPolicy(bob, proj, emptySharing, ImmutableMap.of(group, SharingManagementTemplate.Access.WRITE), true);

        verify(notifier).projectShared(eq(bob), eq(poll), eq(proj));
    }

    @Test
    public void testUserReceivesEmailOnProjectSharingThrowGroupEditing() {
        final long bob = uc.createLab3AndBob();
        final long paul = uc.createPaul();
        final String projName = generateString();
        final long proj = projectManagement.createProject(bob, new ProjectManagementTemplate.ProjectInfoTemplate(uc.getLab3(), projName, generateString(), generateString()));
        final NotifierTemplate notifier = notificator();
        final long group = sharingManagement.createGroup(bob, generateString(), ImmutableSet.of(paul));
        sharingManagement.updateSharingPolicy(bob, proj, emptySharing, ImmutableMap.of(group, SharingManagementTemplate.Access.WRITE), true);

        sharingManagement.setCollaborators(bob, group, ImmutableSet.of(paul), true);

        verify(notifier).projectShared(eq(bob), eq(paul), eq(proj));
    }

    @Test
    public void testUserDontReceiveEmailOnProjectSharingThroughGroupEditingIfItWasMarked() {
        final long bob = uc.createLab3AndBob();
        final long paul = uc.createPaul();
        final String projName = generateString();
        final long proj = projectManagement.createProject(bob, new ProjectManagementTemplate.ProjectInfoTemplate(uc.getLab3(), projName, generateString(), generateString()));
        final NotifierTemplate notifier = notificator();
        final long group = sharingManagement.createGroup(bob, generateString(), ImmutableSet.of(paul));
        sharingManagement.updateSharingPolicy(bob, proj, emptySharing, ImmutableMap.of(group, SharingManagementTemplate.Access.WRITE), false);

        sharingManagement.setCollaborators(bob, group, ImmutableSet.of(paul), false);

        verify(notifier, never()).projectShared(eq(bob), eq(paul), eq(proj));
    }

    @Test
    public void testUserDontReceivesEmailOnProjectSharingIfItsWasNotSpecified() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long proj = uc.createProject(bob, uc.getLab3());
        final NotifierTemplate notifier = notificator();

        sharingManagement.updateSharingPolicy(bob, proj, ImmutableMap.of(poll, SharingManagementTemplate.Access.WRITE), emptySharing, false);

        verify(notifier, never()).projectShared(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void testOldUsersDontReceivesEmailOnProjectSharing() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long kate = uc.createKateAndLab2();
        final long proj = uc.createProject(bob, uc.getLab3());

        sharingManagement.updateSharingPolicy(bob, proj, ImmutableMap.of(poll, SharingManagementTemplate.Access.WRITE), emptySharing, false);
        sharingManagement.updateSharingPolicy(bob, proj, ImmutableMap.of(kate, SharingManagementTemplate.Access.WRITE), emptySharing, true);

        verify(notificator(), never()).projectShared(eq(bob), eq(poll), eq(proj));
    }

    @Test
    public void testUserGetsNotificationHeLoosesAccessToProjectBecauseItBecomePrivate() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob, uc.getLab3());
        long poll = uc.createPaul();
        uc.sharingWithCollaborator(bob, project, poll);

        sharingManagement.makeProjectPrivate(bob, project);
        verify(notificator()).removingFromProject(eq(poll), eq(project));
    }

    @Test
    public void testUserGetsNotificationHeLoosesAccessToProjectBecauseOfStopSharingWithHim() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob, uc.getLab3());
        long poll = uc.createPaul();
        uc.sharingWithCollaborator(bob, project, poll);

        uc.shareProjectToKateInGroup(bob, project);
        verify(notificator()).removingFromProject(eq(poll), eq(project));
    }

    @Test
    public void testUserGetsNotificationWhenHeLoosesAccessToProjectBecauseOfStopSharingWithHimThrowGroupEditing() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob, uc.getLab3());
        final long paul = uc.createPaul();
        final long kate = uc.createKateAndLab2();
        final long group = sharingManagement.createGroup(bob, generateString(), ImmutableSet.of(paul));
        sharingManagement.updateSharingPolicy(bob, project, emptySharing, ImmutableMap.of(group, SharingManagementTemplate.Access.WRITE), false);

        sharingManagement.setCollaborators(bob, group, ImmutableSet.of(kate), true);

        verify(notificator()).removingFromProject(eq(paul), eq(project));
    }

    @Test
    public void testUserDontGetNotificationWhenProjectSharedWithHimBecomePublic() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob, uc.getLab3());
        final long poll = uc.createPaul();

        sharingManagement.updateSharingPolicy(bob, project, ImmutableMap.of(poll, SharingManagementTemplate.Access.WRITE), emptySharing, false);

        sharingManagement.makeProjectPublic(bob, project);

        verify(notificator(), never()).removingFromProject(anyLong(), anyLong());
    }

    @Test
    public void testUserGetsNotificationWhenHisExperimentMoved() {
        final long bob = uc.createLab3AndBob();
        final long project = uc.createProject(bob, uc.getLab3());
        long poll = uc.createPaul();
        uc.sharingWithCollaborator(bob, project, poll);
        createInstrumentAndExperimentWithOneFile(poll, uc.getLab3(), project);

        sharingManagement.makeProjectPrivate(bob, project);
        verify(notificator()).removingFromProjectWithCreatingNew(eq(poll), eq(project), anyLong());
    }

    @Test
    public void testNotificationIsSentToNewOperator() {
        final long bob = uc.createLab3AndBob();
        final long paul = uc.createJoe();
        final Long instrument = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();

        instrumentManagement.setInstrumentOperators(bob, instrument, ImmutableList.of(paul));
        verify(notificator(), atMost(1)).userWasAddedToOperators(eq(bob), eq(paul), eq(instrument));
    }

    @Test
    public void testNotificationWhenExperimentsReadyForDownload() {

    }

    //TODO: [stanislav.kurilin] user should receive only one notification per change.
    /*consider next case:
    * Bob creates project P1
    * Bob creates project P2
    * Bob creates group G
    * Bob shares P1 with G
    * Bob shares P2 with G
    * Bob adds Poll to G
    * Poll should receive only one message !
    * */
}
