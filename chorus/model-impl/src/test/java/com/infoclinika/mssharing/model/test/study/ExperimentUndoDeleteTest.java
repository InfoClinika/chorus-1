package com.infoclinika.mssharing.model.test.study;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.model.helper.ExperimentSampleItem;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.write.FileItem;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import org.testng.annotations.Test;

import java.util.Set;

import static com.google.common.collect.ImmutableList.of;
import static org.testng.Assert.assertEquals;

/**
 * @author Elena Kurilina
 */
public class ExperimentUndoDeleteTest extends AbstractStudyTest {

    @Test
    public void testNotFindExperimentMovedToTrash() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        long exp = createExperiment(bob, project);
        studyManagement.moveExperimentToTrash(bob, exp);
        assertEquals(dashboardReader.readExperimentsByProject(bob, project).size(), 0);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 0);
    }


    @Test
    public void testFindExperimentInTrash() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        long exp = createExperiment(bob, project);
        studyManagement.moveExperimentToTrash(bob, exp);
        assertEquals(trashReader.readByOwner(bob).size(), 1);
        assertEquals(trashReader.readByOwner(bob).iterator().next().type, "experiment");
    }

    @Test
    public void testFindExperimentWithNoLabInTrash() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, null);
        long exp = createExperiment(bob, project, null);
        studyManagement.moveExperimentToTrash(bob, exp);
        assertEquals(trashReader.readByOwner(bob).size(), 1);
        assertEquals(trashReader.readByOwner(bob).iterator().next().type, "experiment");
    }

    @Test
    public void testNotFindExperimentInTrash() {
        long bob = uc.createLab3AndBob();
        long project = createPrivateProject(bob, uc.getLab3());
        long exp = createExperiment(bob, project);
        long deleted = studyManagement.moveExperimentToTrash(bob, exp);
        studyManagement.restoreExperiment(bob, deleted);
        assertEquals(trashReader.readByOwner(bob).size(), 0);
    }

    @Test
    public void testFindRestoredExperiment() { //TODO: check
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);
        long project = createPrivateProject(bob, uc.getLab3());
        long exp = createExperiment(bob, project);
        long deleted = studyManagement.moveExperimentToTrash(bob, exp);
        studyManagement.restoreExperiment(bob, deleted);
        assertEquals(dashboardReader.readExperimentsByProject(bob, project).size(), 1);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 1);
    }

    @Test
    public void testRestoreExperimentWithDeletedFiles() { //TODO: check
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);
        long project = createPrivateProject(bob, uc.getLab3());
        long exp = createExperiment(bob, project, file, uc.getLab3());
        long deleted = studyManagement.moveExperimentToTrash(bob, exp);
        long fileD = instrumentManagement.moveFileToTrash(bob, file);
        studyManagement.restoreExperiment(bob, deleted);
        assertEquals(dashboardReader.readExperimentsByProject(bob, project).size(), 1);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 1);
        assertEquals(fileReader.readFiles(bob, Filter.MY).size(), 1);
    }

    @Test(dependsOnMethods = "testFindRestoredExperiment")
    public void testRestoredExperimentHaveSameName() {
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);
        long project = createPrivateProject(bob, uc.getLab3());
        long exp = createExperiment(bob, project);
        final String beforeName = dashboardReader.readExperiments(bob, Filter.MY).iterator().next().name;
        final String afterName = dashboardReader.readExperiments(bob, Filter.MY).iterator().next().name;
        long deleted = studyManagement.moveExperimentToTrash(bob, exp);
        studyManagement.restoreExperiment(bob, deleted);
        assertEquals(afterName, beforeName);
    }

    @Test(dependsOnMethods = "testFindRestoredExperiment")
    public void testFindFileByRestoredExperiment() {
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);
        long project = createPrivateProject(bob, uc.getLab3());
        long exp = createExperiment(bob, project);
        long deleted = studyManagement.moveExperimentToTrash(bob, exp);
        studyManagement.restoreExperiment(bob, deleted);
        Set<FileLine> fileItems = fileReader.readFiles(bob, Filter.MY);
        assertEquals(fileItems.size(), 2);
    }

    @Test(dependsOnMethods = "testFindRestoredExperiment")
    public void testTotalFilesAfterRestoreExperiment() {
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);
        long project = createPrivateProject(bob, uc.getLab3());
        long exp = createExperiment(bob, project);
        Set<FileLine> before = fileReader.readFiles(bob, Filter.MY);
        long deleted = studyManagement.moveExperimentToTrash(bob, exp);
        studyManagement.restoreExperiment(bob, deleted);
        Set<FileLine> after = fileReader.readFiles(bob, Filter.MY);
        assertEquals(after.size(), before.size());
    }

    @Test
    public void testNotFindRemovedExperiment() {
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);
        long project = createPrivateProject(bob, uc.getLab3());
        long exp = createExperiment(bob, project);
        long deleted = studyManagement.moveExperimentToTrash(bob, exp);
        studyManagement.removeExperiment(deleted);
        assertEquals(dashboardReader.readExperimentsByProject(bob, project).size(), 0);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 0);
    }

    @Test
    public void testNotFindRemovedExperimentByRemovingInstrument() {
        long bob = uc.createLab3AndBob();
        long instr = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        long file = uc.saveFileWithSize(bob, instr, 10737418);
        long project = createPrivateProject(bob, uc.createLab3());
        long exp = createExperiment(bob, project, file, uc.createLab3());
        studyManagement.moveExperimentToTrash(bob, exp);
        instrumentManagement.moveFileToTrash(bob, file);
        instrumentManagement.deleteInstrument(bob, instr);
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 0);

    }

    @Test(enabled = false /*todo: investigate behavior, fails periodically*/, expectedExceptions = AccessDenied.class/*, dependsOnMethods = {"testNotFindExperimentMovedToTrash", "testFindExperimentInTrash"}*/)
    public void testNotAbleToRestoreExperimentHavingFilesWithDuplicateNames() {
        long bob = uc.createLab3AndBob();
        long instr = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3(), anyThermoInstrumentModel()).get();
        final String fileName = "file.raw";
        long file = uc.saveFileWithName(bob, instr, fileName);
        final long experiment = experimentInNewProject(bob, uc.getLab3());
        final String factorName = generateString();
        final String factorValue = "2";
        final ExperimentSampleItem sample = sampleWithFactors(file, of(factorValue));
        addFilesToExperiment(bob, experiment,
                of(new ExperimentManagementTemplate.MetaFactorTemplate(factorName, "kg", true, experiment)),
                of(new FileItem(file, false, 0, preparedSample(file, ImmutableSet.of(sample)))), of(factorValue));
        long deleted = studyManagement.moveExperimentToTrash(bob, experiment);
        instrumentManagement.moveFileToTrash(bob, file);
        uc.saveFileWithName(bob, instr, fileName);

        studyManagement.restoreExperiment(bob, deleted);
    }

    @Test(dependsOnMethods = {"testNotFindExperimentMovedToTrash", "testFindExperimentInTrash"})
    public void testNotFindMovedToTrashExperimentAfterRemovingUserFromLabMembership() {
        long paul = uc.createPaul();
        long bob = uc.createLab3AndBob();
        long lab = uc.getLab3();
        long experiment = experimentInNewProject(bob, lab);
        studyManagement.moveExperimentToTrash(bob, experiment);
        assertEquals(trashReader.readByOwner(bob).size(), 1);

        labHeadManagement.removeUserFromLab(paul, lab, bob);
        assertEquals(trashReader.readByOwner(bob).size(), 0);
    }
}
