package com.infoclinika.mssharing.platform.model.test.sharing;


import com.google.common.base.Optional;
import com.infoclinika.mssharing.platform.model.read.Filter;
import org.testng.annotations.Test;

import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class PagedFilesTest extends AbstractPagedItemTest {
    @Test
    public void testReadAllAvailableFiles() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long project = uc.createProject(poll, uc.getLab3());
        final long experiment = super.createInstrumentAndExperimentWithOneFile(poll, uc.getLab3(), project);
        final long anothherExperiment = createInstrumentAndExperimentWithOneFile(poll, uc.getLab3(), uc.createProject(poll, uc.getLab3()));

        final long file = uc.saveFile(poll);
        updateExperimentFiles(poll, experiment, file);
        updateExperimentFiles(poll, anothherExperiment, file);
        for (int i = 0; i < 2; i++) {
            uc.saveFile(kate);
        }
        uc.sharingWithCollaborator(poll, project, bob);

        assertSame(fileReader.readFiles(bob, Filter.ALL, getPagedItemRequest()).items.size(), 5);
        uc.sharingWithCollaborator(poll, project, kate);
        assertSame(fileReader.readFiles(kate, Filter.ALL, getPagedItemRequest()).items.size(), 5);
        //User is operator of instrument, where private file has been created by kate
        assertSame(fileReader.readFiles(poll, Filter.ALL, getPagedItemRequest()).items.size(), 5);
    }

    @Test
    public void testReadMyFiles() {
        final long kate = createKateInLab2and3();
        final long bob = uc.createLab3AndBob();
        final long joe = uc.createJoe();
        Optional<Long> instrument = uc.createInstrumentAndApproveIfNeeded(kate, uc.getLab3());
        instrumentManagement.addOperatorDirectly(kate, instrument.get(), bob);
        for (int i = 0; i < 10; i++) {
            uc.saveFile(kate, instrument.get());
        }
        assertTrue(fileReader.readFiles(kate, Filter.MY, getPagedItemRequest()).items.size() == 10);
        //Files from operated instrument
        assertTrue(fileReader.readFiles(bob, Filter.MY, getPagedItemRequest()).items.size() == 10);
        assertTrue(fileReader.readFiles(joe, Filter.MY, getPagedItemRequest()).items.size() == 10);
    }

    @Test
    public void testFilesFromSharedProjectsAreAvailable() {
        final long poll = uc.createPaul();
        final long project = uc.createProject(poll, uc.getLab3());
        final long experiment = super.createInstrumentAndExperimentWithOneFile(poll, uc.getLab3(), project);
        final long publicProject = uc.createProject(poll, uc.getLab3());
        final long anothherExperiment = createInstrumentAndExperimentWithOneFile(poll, uc.getLab3(), publicProject);
        sharingManagement.makeProjectPublic(poll, publicProject);

        final long file = uc.saveFile(poll);
        updateExperimentFiles(poll, experiment, file);
        updateExperimentFiles(poll, anothherExperiment, file);


        final long kate = uc.createKateAndLab2();
        uc.sharingWithCollaborator(poll, project, kate);
        assertSame(fileReader.readFiles(kate, Filter.SHARED_WITH_ME, getPagedItemRequest()).items.size(), 1);
        assertTrue(fileReader.readFiles(poll, Filter.MY, getPagedItemRequest()).items.size() == 3);
    }

    @Test
    public void testReadPublicFiles() {
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long publicProject = uc.createProject(poll, uc.getLab3());
        sharingManagement.makeProjectPublic(poll, publicProject);
        final long publicExperiment = super.createInstrumentAndExperimentWithOneFile(poll, uc.getLab3(), publicProject);
        final long privateExpeirment = createInstrumentAndExperimentWithOneFile(poll, uc.getLab3(), uc.createProject(poll, uc.getLab3()));
        updateExperimentFiles(poll, publicExperiment, uc.saveFile(poll, instrumentFromExperimentFile(bob, publicExperiment)));

        assertSame(fileReader.readFiles(bob, Filter.PUBLIC, getPagedItemRequest()).items.size(), 2);
        assertSame(fileReader.readFiles(poll, Filter.PUBLIC, getPagedItemRequest()).items.size(), 2);
    }

    @Test
    public void testReadFilesByLab() {
        uc.createKateAndLab2();
        uc.addKateToLab3();
        final long bob = uc.createLab3AndBob();
        final long poll = uc.createPaul();
        final long project = uc.createProject(poll, uc.getLab3());
        sharingManagement.makeProjectPublic(poll, project);
        final long publicExperiment = super.createInstrumentAndExperimentWithOneFile(poll, uc.getLab3(), project);
        createInstrumentAndExperimentWithOneFile(poll, uc.getLab3(), uc.createProject(poll, uc.getLab3()));


        final long instrument = instrumentFromExperimentFile(poll, publicExperiment);
        updateExperimentFiles(poll, publicExperiment, uc.saveFile(poll, instrument));
        updateExperimentFiles(poll, publicExperiment, uc.saveFile(poll, instrument));

        assertSame(fileReader.readFilesByLab(poll, uc.getLab2(), getPagedItemRequest()).items.size(), 0);
        assertSame(fileReader.readFilesByLab(poll, uc.getLab3(), getPagedItemRequest()).items.size(), 4);
        assertSame(fileReader.readFilesByLab(bob, uc.getLab3(), getPagedItemRequest()).items.size(), 4);

        assertSame(fileReader.readFilesByLab(poll, uc.getLab2()).size(), 0);
        assertSame(fileReader.readFilesByLab(poll, uc.getLab3()).size(), 4);
        assertSame(fileReader.readFilesByLab(bob, uc.getLab3()).size(), 4);
    }


    @Test
    public void testReadFilesByInstrument() {
        uc.createKateAndLab2();
        final long bob = uc.createLab3AndBob();
        uc.addKateToLab3();
        final long poll = uc.createPaul();
        final long project = uc.createProject(poll, uc.getLab3());
        sharingManagement.makeProjectPublic(poll, project);
        final long firstInstrument = createInstrumentAndApproveIfNeeded(poll, uc.getLab3());
        final long secondInstrument = createInstrumentAndApproveIfNeeded(poll, uc.getLab3());
        final long publicExperiment = createExperiment(poll, project, uc.saveFile(poll, firstInstrument), uc.getLab3());
        final long privateExperiment = createExperiment(poll, uc.createProject(poll, uc.getLab3()), uc.saveFile(poll, secondInstrument), uc.getLab3());

        updateExperimentFiles(poll, publicExperiment, uc.saveFile(poll, firstInstrument));
        updateExperimentFiles(poll, publicExperiment, uc.saveFile(poll, firstInstrument));

        assertSame(fileReader.readFilesByInstrument(poll, firstInstrument, getPagedItemRequest()).items.size(), 3);
        assertSame(fileReader.readFilesByInstrument(poll, secondInstrument, getPagedItemRequest()).items.size(), 1);
    }

    @Test
    public void testReadFilesByExperiment() {
        final long poll = uc.createPaul();
        final long project = uc.createProject(poll, uc.getLab3());
        final long experiment = super.createInstrumentAndExperimentWithOneFile(poll, uc.getLab3(), project);
        final long anothherExperiment = createInstrumentAndExperimentWithOneFile(poll, uc.getLab3(), uc.createProject(poll, uc.getLab3()));

        final long file = uc.saveFile(poll);
        updateExperimentFiles(poll, experiment, file);
        updateExperimentFiles(poll, anothherExperiment, file);

        assertSame(fileReader.readFilesByExperiment(poll, experiment, getPagedItemRequest()).items.size(), 2);
        assertTrue(fileReader.readFilesByExperiment(poll, anothherExperiment, getPagedItemRequest()).items.size() == 2);
    }
}
