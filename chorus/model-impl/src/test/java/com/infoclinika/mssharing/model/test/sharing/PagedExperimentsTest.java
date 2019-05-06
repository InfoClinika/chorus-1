package com.infoclinika.mssharing.model.test.sharing;


import com.infoclinika.mssharing.platform.model.read.Filter;
import org.testng.annotations.Test;

import static com.infoclinika.mssharing.platform.model.read.Filter.ALL;
import static com.infoclinika.mssharing.platform.model.read.Filter.MY;
import static org.testng.Assert.assertSame;

public class PagedExperimentsTest extends AbstractPagedItemTest {


    @Test
    public void testReadAllAvailableExperimentsInSameLab() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createData(kate, bob, poll);

        assertSame(getPagedItemSize(poll, ALL), DEFAULT_PAGE_SIZE);
        assertSame(getPagedItemSize(kate, ALL), DEFAULT_PAGE_SIZE);
        assertSame(getPagedItemSize(bob, ALL), DEFAULT_PAGE_SIZE);
    }

    @Test
    public void testReadAllAvailableExperiments() {
        final long kate = createKateInLab2();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createData(kate, bob, poll);

        assertSame(getPagedItemSize(poll, ALL), DEFAULT_PAGE_SIZE);
        assertSame(getPagedItemSize(kate, ALL), 20);
        assertSame(getPagedItemSize(bob, ALL), DEFAULT_PAGE_SIZE);
    }

    private int getPagedItemSize(long poll, Filter filter) {
        return dashboardReader.readExperiments(poll, filter, getPagedItemRequest()).items.size();
    }

    @Test
    public void testReadMyExperiments() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createData(kate, bob, poll);

        assertSame(getPagedItemSize(poll, MY), DEFAULT_PAGE_SIZE);
        assertSame(getPagedItemSize(kate, MY), 0);
        assertSame(getPagedItemSize(bob, MY), 0);
    }

    @Test
    public void testReadPublicExperiments() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createData(kate, bob, poll);

        assertSame(dashboardReader.readExperiments(poll, Filter.PUBLIC, getPagedItemRequest("name")).items.size(), 10);
        assertSame(dashboardReader.readExperiments(kate, Filter.PUBLIC, getPagedItemRequest("name")).items.size(), 10);
        assertSame(dashboardReader.readExperiments(bob, Filter.PUBLIC, getPagedItemRequest("name")).items.size(), 10);
    }

    @Test
    public void testReadSharedExperimentsInSameLab() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createData(kate, bob, poll);

        assertSame(
            dashboardReader.readExperiments(poll, Filter.SHARED_WITH_ME, getPagedItemRequest()).items.size(),
            25
        );
        assertSame(dashboardReader.readExperiments(
            kate,
            Filter.SHARED_WITH_ME,
            getPagedItemRequest("name")
        ).items.size(), 25);
        assertSame(
            dashboardReader.readExperiments(poll, Filter.SHARED_WITH_ME, getPagedItemRequest()).items.size(),
            25
        );
    }

    @Test
    public void testReadSharedExperiments() {
        final long kate = createKateInLab2();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createData(kate, bob, poll);

        assertSame(
            dashboardReader.readExperiments(poll, Filter.SHARED_WITH_ME, getPagedItemRequest()).items.size(),
            DEFAULT_PAGE_SIZE
        );
        assertSame(dashboardReader.readExperiments(
            kate,
            Filter.SHARED_WITH_ME,
            getPagedItemRequest("name")
        ).items.size(), 10);
        assertSame(
            dashboardReader.readExperiments(poll, Filter.SHARED_WITH_ME, getPagedItemRequest()).items.size(),
            DEFAULT_PAGE_SIZE
        );
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
