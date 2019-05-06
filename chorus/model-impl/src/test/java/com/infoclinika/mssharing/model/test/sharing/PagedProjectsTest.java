package com.infoclinika.mssharing.model.test.sharing;

import com.infoclinika.mssharing.platform.model.read.Filter;
import org.testng.annotations.Test;

import static org.testng.Assert.assertSame;

public class PagedProjectsTest extends AbstractPagedItemTest {

    @Test
    public void testReadAllAvailableProjects() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createTestData(kate, poll);

        assertSame(dashboardReader.readProjects(poll, Filter.ALL, getPagedItemRequest()).items.size(), 12);
        assertSame(dashboardReader.readProjects(kate, Filter.ALL, getPagedItemRequest()).items.size(), 2);
        assertSame(dashboardReader.readProjects(bob, Filter.ALL, getPagedItemRequest()).items.size(), 1);
    }

    @Test
    public void testReadAllByLAbProjects() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createTestData(kate, poll);

        assertSame(dashboardReader.readProjectsByLab(poll, uc.getLab3(), getPagedItemRequest()).items.size(), 12);
    }

    @Test
    public void testReadPublicProjects() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createTestData(kate, poll);

        assertSame(dashboardReader.readProjects(poll, Filter.PUBLIC, getPagedItemRequest()).items.size(), 0);
        assertSame(dashboardReader.readProjects(kate, Filter.PUBLIC, getPagedItemRequest()).items.size(), 1);
        assertSame(dashboardReader.readProjects(bob, Filter.PUBLIC, getPagedItemRequest()).items.size(), 1);
    }

    @Test
    public void testReadMyProjects() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createTestData(kate, poll);

        //Public and shared projects for creator displayed in "My Projects" list
        assertSame(dashboardReader.readProjects(poll, Filter.MY, getPagedItemRequest()).items.size(), 12);
        assertSame(dashboardReader.readProjects(kate, Filter.MY, getPagedItemRequest()).items.size(), 0);
        assertSame(dashboardReader.readProjects(bob, Filter.MY, getPagedItemRequest()).items.size(), 0);
    }

    @Test
    public void testReadSharedWithMeProjects() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        createTestData(kate, poll);

        assertSame(dashboardReader.readProjects(poll, Filter.SHARED_WITH_ME, getPagedItemRequest()).items.size(), 0);
        assertSame(dashboardReader.readProjects(kate, Filter.SHARED_WITH_ME, getPagedItemRequest()).items.size(), 1);
        assertSame(dashboardReader.readProjects(bob, Filter.SHARED_WITH_ME, getPagedItemRequest()).items.size(), 0);
    }

    private void createTestData(long kate, long poll) {
        for (int i = 0; i < 10; i++) {
            uc.createProject(poll, uc.getLab3());
        }
        final long publicProject = uc.createProject(poll, uc.getLab3());
        sharingManagement.makeProjectPublic(poll, publicProject);
        final long sharedProject = uc.createProject(poll, uc.getLab3());
        uc.sharingWithCollaborator(poll, sharedProject, kate);
    }

}
