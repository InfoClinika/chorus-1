/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.model.test.sharing;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.ObjectNotFoundException;
import com.infoclinika.mssharing.platform.model.helper.SharingProjectHelperTemplate;
import com.infoclinika.mssharing.platform.model.read.GroupsReaderTemplate.GroupLine;
import org.testng.annotations.Test;

import static com.google.common.base.Preconditions.checkState;
import static org.testng.Assert.*;

/**
 * @author Stanislav Kurilin
 */
public class GroupCreationTest extends AbstractSharingTest {


    //Testing with who user can share his project
    @Test
    public void testAvailableUsers() {
        uc.createLab3AndBob();
        uc.createPaul();
        assertTrue(sharingProjectHelper.getAvailableUsers().size() > 2);
    }

    @Test
    public void testEmptyGroupsOnProjectCreation() {
        final long bob = uc.createLab3AndBob();
        assertEquals(sharingProjectHelper.getAvailableGroups(bob).size(), 1, "All Users group is present");
    }

    @Test
    public void testEmptyGroupsInDashboard() {
        final long bob = uc.createLab3AndBob();

        assertEquals(groupsReader.readGroups(bob, true).size(), 1, "All Users group is present");
    }

    @Test
    public void testReadGroups() {
        final long bobsId = uc.createLab3AndBob();
        final long paul = uc.createPaul();
        final long projectId = uc.createProject(bobsId, uc.getLab3());
        createInstrumentAndExperimentWithOneFile(bobsId, uc.getLab3(), projectId);

        uc.shareProjectThrowGroup(bobsId, paul, projectId);

        final ImmutableSet<GroupLine> groups = groupsReader.readGroups(bobsId, true);
        assertEquals(FluentIterable.from(groups).size(), 2, "All users group is present");

        // we expect to find "My favorite" group last in this sorted set, cause "All" group should come first
        final GroupLine group = Iterables.getLast(groups);
        assertEquals(group.numberOfProjects, 1);
    }

    //Creating group for sharing
    @Test(dependsOnMethods = "testEmptyGroupsInDashboard")
    public void testOwnGroupsAreAvailable() {
        final long bob = uc.createLab3AndBob();
        sharingManagement.createGroup(bob, "My favorite", paulAsMember());

        final Iterable<SharingProjectHelperTemplate.GroupDetails> availableGroups = sharingProjectHelper.getAvailableGroups(bob);
        assertEquals(Iterables.size(availableGroups), 2);
    }

    @Test(dependsOnMethods = "testEmptyGroupsInDashboard", expectedExceptions = IllegalArgumentException.class)
    public void testShouldNotBeAbleToCreateEmptyGroup() {
        final long bob = uc.createLab3AndBob();
        sharingManagement.createGroup(bob, "My favorite", empty);
    }

    private ImmutableSet<Long> paulAsMember() {
        final long paul = uc.createPaul();
        return ImmutableSet.of(paul);
    }

    @Test(dependsOnMethods = "testOwnGroupsAreAvailable")
    public void testGroupsContent() {
        final long bob = uc.createLab3AndBob();
        final long group = sharingManagement.createGroup(bob, "My favorite", paulAsMember());

        // we expect to find "My favorite" group last in this sorted set, cause "All" group should come first
        final SharingProjectHelperTemplate.GroupDetails next = sharingProjectHelper.getAvailableGroups(bob).last();
        assertEquals(next.id, group);
        assertEquals(next.name, "My favorite");
        assertEquals(next.numberOfMembers, 1);
    }

    @Test(dependsOnMethods = "testOwnGroupsAreAvailable")
    public void testUserCanRenameGroup() {
        final long bob = uc.createLab3AndBob();
        final String firstName = generateString();
        final String secondName = generateString();
        checkState(!firstName.equals(secondName));
        final long group = sharingManagement.createGroup(bob, firstName, paulAsMember());
        sharingManagement.renameGroup(bob, group, secondName);
        assertEquals(detailsReader.readGroup(bob, group).name, secondName);
    }

    @Test(dependsOnMethods = "testUserCanRenameGroup", expectedExceptions = AccessDenied.class)
    public void testOnlyOwnerCanRenameGroup() {
        final long bob = uc.createLab3AndBob();
        final long paul = uc.createPaul();
        final long group = sharingManagement.createGroup(paul, generateString(), ImmutableSet.of(bob));
        sharingManagement.renameGroup(bob, group, generateString());
    }

    @Test(dependsOnMethods = "testGroupsContent")
    public void testOnlyOwnGroupsAreVisible() {
        final long bob = uc.createLab3AndBob();
        final long paul = uc.createPaul();

        sharingManagement.createGroup(paul, "Paul Group", ImmutableSet.of(bob));

        assertEquals(sharingProjectHelper.getAvailableGroups(bob).size(), 1, "All Users group is present");
    }

    @Test(dependsOnMethods = "testGroupsContent")
    public void testUserNotBecomeMemberAutomatically() {
        final long bob = uc.createLab3AndBob();
        final long paul = uc.createPaul();
        final long kate = uc.createKateAndLab2();

        final long group = sharingManagement.createGroup(bob, "Bobs Group", ImmutableSet.of(kate));


        assertFalse(isGroupMember(bob, group, paul));
    }

    @Test(dependsOnMethods = "testUserNotBecomeMemberAutomatically")
    public void testUserBecomeMemberIfWasSpecifiedOnGroupCreation() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();

        final long group = sharingManagement.createGroup(bob, "Bobs Group", ImmutableSet.of(poll));

        assertTrue(isGroupMember(bob, group, poll));
    }

    @Test(dependsOnMethods = "testUserNotBecomeMemberAutomatically")
    public void testUserBecomeMemberIfItWasAdded() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();

        final long group = sharingManagement.createGroup(bob, "Bobs Group", paulAsMember());
        sharingManagement.setCollaborators(bob, group, ImmutableSet.of(poll), false);

        assertTrue(isGroupMember(bob, group, poll));
    }

    @Test
    public void testUserCanParticipateInSeveralGroups() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        sharingManagement.createGroup(bob, generateString(), ImmutableSet.of(poll));
        sharingManagement.createGroup(bob, generateString(), ImmutableSet.of(poll));
    }

    @Test
    public void testCreateGroupForUserWhoHasNoLabs() {
        final long kate = uc.createKateAndLab2();
        final long group = sharingManagement.createGroup(kate, "My favorite", paulAsMember());

        // we expect to find "My favorite" group last in this sorted set, cause "All" group should come first
        final SharingProjectHelperTemplate.GroupDetails next = sharingProjectHelper.getAvailableGroups(kate).last();
        assertEquals(next.id, group);
        assertEquals(next.name, "My favorite");
        assertEquals(next.numberOfMembers, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateGroupWithAlreadyUsedName() {
        final long kate = uc.createKateAndLab2();
        final long group1 = sharingManagement.createGroup(kate, "My favorite", paulAsMember());
        final long joe = uc.createJoe();
        final long group2 = sharingManagement.createGroup(joe, "My favorite", paulAsMember());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRenameGroupWithAlreadyUsedName() {
        final long kate = uc.createKateAndLab2();
        final long group1 = sharingManagement.createGroup(kate, "Group 1", paulAsMember());
        final long joe = uc.createJoe();
        final long group2 = sharingManagement.createGroup(joe, "Group 2", paulAsMember());
        sharingManagement.renameGroup(joe, group2, "Group 1");
    }

    @Test
    public void testUpdateGroupWithoutNameChange() {
        final long kate = uc.createKateAndLab2();
        final long group1 = sharingManagement.createGroup(kate, "Group 1", paulAsMember());
        sharingManagement.renameGroup(kate, group1, "Group 1");
    }

    @Test(expectedExceptions = ObjectNotFoundException.class)
    public void testCantReadRemovedExperimentDetails() {
        final long bob = uc.createLab3AndBob();
        final long group1 = sharingManagement.createGroup(bob, "Group 1", paulAsMember());
        sharingManagement.removeGroup(bob, group1);
        detailsReader.readGroup(bob, group1);
    }

}
