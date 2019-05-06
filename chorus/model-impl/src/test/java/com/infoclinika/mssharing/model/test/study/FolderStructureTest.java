package com.infoclinika.mssharing.model.test.study;

import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.platform.model.read.Filter;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.testng.AssertJUnit.assertNotNull;

/**
 * @author Oleksii Tymchenko
 */
public class FolderStructureTest extends AbstractStudyTest {

    @Inject
    private DashboardReader dashboardReader;

    @Test
    public void testObtainingFolderStructure() {
        final long bob = uc.createLab3AndBob();
        final long model = anyInstrumentModel();
        final long instrument = createInstrumentBySpecifiedInstrumentModel(bob, uc.getLab3(), model);
        long specie = anySpecies();

        createFileWithInstrument(bob, instrument, specie);
        createFileWithInstrument(bob, instrument, unspecifiedSpecie());


        final DashboardReader.FullFolderStructure folderStructure = dashboardReader.readFolderStructure(bob);
        assertNotNull(folderStructure);
    }

    @Test
    public void testFilteredFolderStructure() {
        final long bob = uc.createLab3AndBob();
        final long model = anyInstrumentModel();
        final long instrument = createInstrumentBySpecifiedInstrumentModel(bob, uc.getLab3(), model);
        long specie = anySpecies();

        createFileWithInstrument(bob, instrument, specie);
        createFileWithInstrument(bob, instrument, unspecifiedSpecie());

        final DashboardReader.FolderStructure folderStructure = dashboardReader.readFolderStructure(bob, Filter.MY);
        assertNotNull(folderStructure);
    }

}
