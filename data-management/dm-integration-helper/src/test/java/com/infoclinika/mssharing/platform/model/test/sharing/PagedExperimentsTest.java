package com.infoclinika.mssharing.platform.model.test.sharing;


import com.infoclinika.mssharing.platform.model.read.Filter;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.platform.model.read.Filter.*;
import static org.testng.Assert.assertSame;

public class PagedExperimentsTest extends AbstractPagedItemTest {


    @Test
    public void testReadAllAvailableExperiments() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createData(kate, bob, poll);

        //Max items in PagedItemInfo has been set to 25
        assertSame(getPagedItemSize(poll, ALL), 25);
        assertSame(getPagedItemSize(kate, ALL), 20);
        assertSame(getPagedItemSize(bob, ALL), 10);
    }

    private int getPagedItemSize(long poll, Filter filter) {
        return experimentReader.readExperiments(poll, filter, getPagedItemRequest()).items.size();
    }

    @Test
    public void testReadMyExperiments() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createData(kate, bob, poll);

        //Max items in PagedItemInfo has been set to 25
        assertSame(getPagedItemSize(poll, MY), 25);
        assertSame(getPagedItemSize(kate, MY), 0);
        assertSame(getPagedItemSize(bob, MY), 0);
    }

    @Test
    public void testReadPublicExperiments() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createData(kate, bob, poll);

        //Max items in PagedItemInfo has been set to 25
        assertSame(experimentReader.readExperiments(poll, Filter.PUBLIC, getPagedItemRequest("name")).items.size(), 10);
        assertSame(experimentReader.readExperiments(kate, Filter.PUBLIC, getPagedItemRequest("name")).items.size(), 10);
        assertSame(experimentReader.readExperiments(bob, Filter.PUBLIC, getPagedItemRequest("name")).items.size(), 10);
    }

    @Test
    public void testSharedExperiments() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createData(kate, bob, poll);

        //Max items in PagedItemInfo has been set to 25
        assertSame(experimentReader.readExperiments(poll, SHARED_WITH_ME, getPagedItemRequest()).items.size(), 0);
        assertSame(
            experimentReader.readExperiments(kate, SHARED_WITH_ME, getPagedItemRequest("name")).items.size(),
            10
        );
        assertSame(experimentReader.readExperiments(poll, SHARED_WITH_ME, getPagedItemRequest()).items.size(), 0);
    }

    private void createData(long kate, long bob, long poll) {
        final long project = uc.createProject(poll, uc.getLab3());
        final long sharedProject = uc.createProject(poll, uc.getLab3());
        final long publicProject = uc.createProject(poll, uc.getLab3());

        for (int i = 0; i < 10; i++) {
            createExperiment(poll, project, uc.getLab3());
        }
        for (int i = 0; i < 10; i++) {
            createExperiment(poll, sharedProject, uc.getLab3());
        }
        for (int i = 0; i < 10; i++) {
            createExperiment(poll, publicProject, uc.getLab3());
        }
        uc.sharingWithCollaborator(poll, sharedProject, kate);
        sharingManagement.makeProjectPublic(poll, publicProject);
    }

}
