package com.infoclinika.mssharing.model.test.study;

import com.infoclinika.mssharing.model.write.StudyManagement;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.read.Filter;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Elena Kurilina
 */
public class ProjectUndoDeleteTest extends AbstractStudyTest {

    @Test
    public void testNotFindProjectMovedToTrash() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        studyManagement.moveProjectToTrash(bob, project);
        assertEquals(dashboardReader.readProjects(bob, Filter.MY).size(), 0);
    }

    @Test
    public void testFindProjectInTrash() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        studyManagement.moveProjectToTrash(bob, project);
        assertEquals(trashReader.readByOwner(bob).size(), 1);
    }


    @Test
    public void testNotFindRestoredProjectInTrash() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        long deleted = studyManagement.moveProjectToTrash(bob, project);
        studyManagement.restoreProject(bob, deleted);
        assertEquals(trashReader.readByOwner(bob).size(), 0);
    }

    @Test
    public void testNotFindProjectWithExperimentMovedToTrash() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        createExperiment(bob, project);
        studyManagement.moveProjectToTrash(bob, project);
        assertEquals(dashboardReader.readProjects(bob, Filter.MY).size(), 0);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 0);
    }

    @Test
    public void testNotFindProjectDeltedAfterEsxperiment() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        long exp = createExperiment(bob, project);
        studyManagement.moveExperimentToTrash(bob, exp);
        studyManagement.moveProjectToTrash(bob, project);
        assertEquals(dashboardReader.readProjects(bob, Filter.MY).size(), 0);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 0);
    }

    @Test
    public void testFindProjectWithExperimentInTrash() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        createExperiment(bob, project);
        studyManagement.moveProjectToTrash(bob, project);
        assertEquals(trashReader.readByOwner(bob).size(), 1);
        assertEquals(trashReader.readByOwner(bob).iterator().next().type, "project");
    }

    @Test(dependsOnMethods = "testNotFindProjectMovedToTrash")
    public void testNotFindRemovedProject() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        long deleted = studyManagement.moveProjectToTrash(bob, project);
        studyManagement.removeProject(deleted);
        assertEquals(dashboardReader.readProjects(bob, Filter.MY).size(), 0);
    }

    @Test(dependsOnMethods = "testNotFindProjectMovedToTrash")
    public void testNotFindRemovedCopiedProject() {
        long bob = uc.createLab3AndBob();
        long joe = uc.createJoe();
        long project = createPrivateProject(bob, uc.getLab3());
        long file = uc.saveFile(bob);
        createExperiment(bob, project, file, uc.getLab3());
        long copy = studyManagement.copyProject(
            bob,
            new StudyManagement.CopyProjectInfo(project, joe, bob, uc.getLab3(), false)
        );
        long deleted = studyManagement.moveProjectToTrash(joe, copy);
        studyManagement.removeProject(deleted);
        assertEquals(dashboardReader.readProjects(joe, Filter.MY).size(), 0);
    }

    @Test(dependsOnMethods = "testNotFindProjectMovedToTrash")
    public void testNotFindRemovedProjectWithExperiment() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        createExperiment(bob, project);
        long deleted = studyManagement.moveProjectToTrash(bob, project);
        studyManagement.removeProject(deleted);
        assertEquals(dashboardReader.readProjects(bob, Filter.MY).size(), 0);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 0);
    }

    @Test(dependsOnMethods = "testNotFindProjectMovedToTrash")
    public void testFindRestoredProject() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        long deleted = studyManagement.moveProjectToTrash(bob, project);
        studyManagement.restoreProject(bob, deleted);
        assertEquals(dashboardReader.readProjects(bob, Filter.MY).size(), 1);
    }

    @Test(dependsOnMethods = "testFindRestoredProject")
    public void testRestoredProjectHaveSameName() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        final String beforeName = dashboardReader.readProjects(bob, Filter.MY).iterator().next().name;
        long deleted = studyManagement.moveProjectToTrash(bob, project);
        studyManagement.restoreProject(bob, deleted);
        final String afterName = dashboardReader.readProjects(bob, Filter.MY).iterator().next().name;
        assertEquals(afterName, beforeName);
    }

    @Test(dependsOnMethods = "testNotFindProjectMovedToTrash")
    public void testCreateExperimentInRestoredProject() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        long deleted = studyManagement.moveProjectToTrash(bob, project);
        long restored = studyManagement.restoreProject(bob, deleted);
        createExperiment(bob, restored);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 1);
    }

    @Test(dependsOnMethods = "testNotFindProjectWithExperimentMovedToTrash")
    public void testFindRestoredProjectWithExperiment() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        createExperiment(bob, project);
        long deleted = studyManagement.moveProjectToTrash(bob, project);
        studyManagement.restoreProject(bob, deleted);
        assertEquals(dashboardReader.readProjects(bob, Filter.MY).size(), 1);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 1);
    }

    @Test(dependsOnMethods = "testNotFindProjectWithExperimentMovedToTrash")
    public void testFindRestoredProjectWithExperimentAndDeletedFiles() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        long file = uc.saveFile(bob);
        createExperiment(bob, project, file, uc.getLab3());
        long deleted = studyManagement.moveProjectToTrash(bob, project);
        long filedeleted = instrumentManagement.moveFileToTrash(bob, file);
        studyManagement.restoreProject(bob, deleted);
        assertEquals(dashboardReader.readProjects(bob, Filter.MY).size(), 1);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 1);
        assertEquals(fileReader.readFiles(bob, Filter.MY).size(), 1);
    }


    @Test(dependsOnMethods = "testNotFindProjectWithExperimentMovedToTrash")
    public void testFindDeletedItemsForLabHeadInTrash() {
        long bob = uc.createLab3AndBob();
        long joe = uc.createJoe();
        long paul = uc.createPaul();        // is labhead for lab3
        long project = createPrivateProject(bob, uc.getLab3());
        long project1 = createPrivateProject(joe, uc.getLab3());
        long file = uc.saveFile(bob);
        long file1 = uc.saveFile(joe);
        createExperiment(bob, project, file, uc.getLab3());
        createExperiment(joe, project1, file1, uc.getLab3());
        long deleted = studyManagement.moveProjectToTrash(bob, project);
        long deleted1 = studyManagement.moveProjectToTrash(joe, project1);
        long filedeleted = instrumentManagement.moveFileToTrash(bob, file);
        long filedeleted1 = instrumentManagement.moveFileToTrash(joe, file1);
        assertEquals(trashReader.readByOwnerOrLabHead(paul).size(), 4);

    }

    @Test(dependsOnMethods = "testNotFindProjectWithExperimentMovedToTrash")
    public void testCanLabHeadRestoreElements() {
        long bob = uc.createLab3AndBob();
        long joe = uc.createJoe();
        long paul = uc.createPaul();
        long project = createPrivateProject(bob, uc.getLab3());
        long project1 = createPrivateProject(joe, uc.getLab3());
        long file = uc.saveFile(bob);
        long file1 = uc.saveFile(joe);
        createExperiment(bob, project, file, uc.getLab3());
        createExperiment(joe, project1, file1, uc.getLab3());
        long deleted = studyManagement.moveProjectToTrash(bob, project);
        long deleted1 = studyManagement.moveProjectToTrash(joe, project1);
        long filedeleted = instrumentManagement.moveFileToTrash(bob, file);
        long filedeleted1 = instrumentManagement.moveFileToTrash(joe, file1);
        studyManagement.restoreProject(paul, deleted);
        studyManagement.restoreProject(paul, deleted1);
        assertEquals(dashboardReader.readProjects(bob, Filter.MY).size(), 1);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 1);
        assertEquals(fileReader.readFiles(bob, Filter.MY).size(), 2);

    }

    @Test(expectedExceptions = AccessDenied.class,
        dependsOnMethods = {"testNotFindProjectMovedToTrash", "testFindProjectInTrash"})
    public void testNotAbleToRestoreProjectWithDuplicateName() {
        long bob = uc.createLab3AndBob();
        final String projectName = "NewProject";
        long projectToDelete = createProjectWithName(bob, uc.getLab3(), projectName);
        long deleted = studyManagement.moveProjectToTrash(bob, projectToDelete);

        createProjectWithName(bob, uc.getLab3(), projectName);
        studyManagement.restoreProject(bob, deleted);
    }

    @Test(expectedExceptions = AccessDenied.class,
        dependsOnMethods = {"testNotFindProjectMovedToTrash", "testFindProjectInTrash"})
    public void testNotAbleToRestoreProjectHavingExperimentsWithDuplicateNames() {
        long bob = uc.createLab3AndBob();
        final String experimentName = "NewExperiment";
        long project = createProjectWithName(bob, uc.getLab3(), "project");
        long projectToDelete = createProjectWithName(bob, uc.getLab3(), "project to delete");
        createExperimentWithName(bob, projectToDelete, experimentName);
        long deleted = studyManagement.moveProjectToTrash(bob, projectToDelete);

        createExperimentWithName(bob, project, experimentName);
        studyManagement.restoreProject(bob, deleted);
    }

    @Test(dependsOnMethods = {"testNotFindProjectMovedToTrash", "testFindProjectInTrash"})
    public void testNotFindMovedToTrashProjectAfterRemovingUserFromLabMembership() {
        long paul = uc.createPaul();
        long bob = uc.createLab3AndBob();
        long lab = uc.getLab3();
        long project = createPrivateProject(bob, lab);
        studyManagement.moveProjectToTrash(bob, project);
        assertEquals(trashReader.readByOwner(bob).size(), 1);

        labHeadManagement.removeUserFromLab(paul, lab, bob);
        assertEquals(trashReader.readByOwner(bob).size(), 0);
    }
}
