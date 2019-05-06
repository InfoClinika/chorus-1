package com.infoclinika.mssharing.model.test.study;

import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.read.Filter;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Elena Kurilina
 */
public class FileUndoDeleteTest extends AbstractStudyTest {

    @Test
    public void testNotFindMovedToTrashFile() {
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);
        instrumentManagement.moveFileToTrash(bob, file);
        assertEquals(fileReader.readFiles(bob, Filter.MY).size(), 0);
    }

    @Test
    public void testFindMovedToTrashFileInTrash() {
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);
        instrumentManagement.moveFileToTrash(bob, file);
        assertEquals(trashReader.readByOwner(bob).size(), 1);
    }

    @Test
    public void testNotFindRestoredFileInTrash() {
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);
        long deleted = instrumentManagement.moveFileToTrash(bob, file);
        instrumentManagement.restoreFile(bob, deleted);
        assertEquals(trashReader.readByOwner(bob).size(), 0);
    }

    @Test(dependsOnMethods = "testNotFindMovedToTrashFile")
    public void testFindRestoredFile() {
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);
        long deleted = instrumentManagement.moveFileToTrash(bob, file);
        instrumentManagement.restoreFile(bob, deleted);
        assertEquals(fileReader.readFiles(bob, Filter.MY).size(), 1);
    }

    @Test(dependsOnMethods = "testFindRestoredFile")
    public void testRestoredFileHaveSameName() {
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);
        final String beforeNAme = fileReader.readFiles(bob, Filter.MY).iterator().next().name;
        long deleted = instrumentManagement.moveFileToTrash(bob, file);
        instrumentManagement.restoreFile(bob, deleted);
        final String afterNAme = fileReader.readFiles(bob, Filter.MY).iterator().next().name;
        assertEquals(afterNAme, beforeNAme);
    }

    @Test(dependsOnMethods = "testNotFindMovedToTrashFile")
    public void testCanCreateExperimentWithRestoredFile() {
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);
        long deleted = instrumentManagement.moveFileToTrash(bob, file);
        long restored = instrumentManagement.restoreFile(bob, deleted);
        long project = createPrivateProject(bob, uc.getLab3());
        createExperiment(bob, project, restored, uc.getLab3());
        assertEquals(dashboardReader.readExperiments(bob, Filter.MY).size(), 1);

    }

   @Test(dependsOnMethods = "testNotFindMovedToTrashFile",
       enabled = false,
       description = "test was disabled for open-chorus"
   )
    public void testNotFindRemovedFile() {
        long bob = uc.createLab3AndBob();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get(), 10737418);
        long deleted = instrumentManagement.moveFileToTrash(bob, file);
        instrumentManagement.deleteFile(deleted);
        assertEquals(fileReader.readFiles(bob, Filter.MY).size(), 0);

    }

    @Test(dependsOnMethods = "testNotFindMovedToTrashFile",
        enabled = false,
        description = "test was disabled for open-chorus"
    )
    public void testNotFindRemovedFileByRemovingInstrument() {
        long bob = uc.createLab3AndBob();
        long instr = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        long file = uc.saveFileWithSize(bob, instr, 10737418);
        long deleted = instrumentManagement.moveFileToTrash(bob, file);
        instrumentManagement.deleteInstrument(bob, instr);
        assertEquals(fileReader.readFiles(bob, Filter.MY).size(), 0);

    }

    @Test(expectedExceptions = AccessDenied.class,
        dependsOnMethods = {"testFindMovedToTrashFileInTrash", "testNotFindMovedToTrashFile"})
    public void testNotAbleToRestoreFileWithDuplicateName() {
        long bob = uc.createLab3AndBob();
        long instr = uc.createInstrumentAndApproveIfNeeded(bob, uc.getLab3()).get();
        final String fileName = "file.raw";
        long file = uc.saveFileWithName(bob, instr, fileName);
        long deleted = instrumentManagement.moveFileToTrash(bob, file);
        uc.saveFileWithName(bob, instr, fileName);
        instrumentManagement.restoreFile(bob, deleted);
    }

    @Test(dependsOnMethods = {"testFindMovedToTrashFileInTrash", "testNotFindMovedToTrashFile"})
    public void testNotFindMovedToTrashFileAfterRemovingUserFromLabMembership() {
        long paul = uc.createPaul();
        long bob = uc.createLab3AndBob();
        long lab = uc.getLab3();
        long file = uc.saveFileWithSize(bob, uc.createInstrumentAndApproveIfNeeded(bob, lab).get(), 10737418);
        instrumentManagement.moveFileToTrash(bob, file);
        assertEquals(trashReader.readByOwner(bob).size(), 1);

        labHeadManagement.removeUserFromLab(paul, lab, bob);
        assertEquals(trashReader.readByOwner(bob).size(), 0);
    }
}
