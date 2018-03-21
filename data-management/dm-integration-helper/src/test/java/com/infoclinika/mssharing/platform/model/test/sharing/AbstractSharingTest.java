/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.model.test.sharing;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.FileReaderTemplate.FileLineTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.test.helper.AbstractTest;
import com.infoclinika.mssharing.platform.model.write.ProjectManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;

import java.util.Map;

import static com.google.common.collect.Iterables.size;
import static java.util.Collections.emptyMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Stanislav Kurilin
 */
abstract class AbstractSharingTest extends AbstractTest {

    public ImmutableSet<Long> empty = ImmutableSet.of();
    public Map<Long, SharingManagementTemplate.Access> emptySharing = emptyMap();

    public void checkHasAccessToSharedFile(long user, long lab, final long file) {
        if (!Iterables.any(fileReader.readFiles(user, Filter.SHARED_WITH_ME), new Predicate<FileLineTemplate>() {
            @Override
            public boolean apply(FileLineTemplate input) {
                return input.id == file;
            }
        })) throw new AccessDenied("asserting");
        checkHasAccessToFile(user, lab, file);
        reuseFile(user, lab, file);
    }

    public long projectByUser(long user, long lab) {
        return projectManagement.createProject(user, new ProjectManagementTemplate.ProjectInfoTemplate(lab, generateString(), "DNA", "Some proj"));
    }

    public long createKateInLab2() {
        uc.createLab2();
        return uc.createKateAndLab2();
    }

    public void assertNumberOfAvailableInstrumentTypes(long user, long number) {
        final Iterable<DictionaryItem> models = experimentCreationHelper.availableInstrumentModels(user, null);
        assertEquals(size(models), number);
        for (DictionaryItem model : models) {
            assertTrue(size(experimentCreationHelper.availableInstrumentsByModel(user, model.id)) > 0);
        }
    }

    public boolean isGroupMember(long groupOwner, long group, final long testUser) {
        final DetailsReaderTemplate.GroupItemTemplate groupItem = detailsReader.readGroup(groupOwner, group);
        return Iterables.any(groupItem.members, new Predicate<DetailsReaderTemplate.MemberItemTemplate>() {
            @Override
            public boolean apply(DetailsReaderTemplate.MemberItemTemplate input) {
                return Long.valueOf(testUser).equals(input.id);
            }
        });
    }

    public long createKateInLab2and3() {
        uc.createLab2();
        uc.createLab3();
        final long kate = createKateInLab2();
        uc.addKateToLab3();
        return kate;
    }
}
