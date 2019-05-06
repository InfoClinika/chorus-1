/*
package com.infoclinika.mssharing.autoimporter.service;

import com.google.common.collect.Lists;
import com.infoclinika.mssharing.autoimporter.service.api.FileFilterService;
import WebService;
import com.infoclinika.mssharing.autoimporter.service.util.AbstractTestSuite;
import com.infoclinika.mssharing.autoimporter.service.util.FileCreator;
import com.infoclinika.mssharing.autoimporter.service.util.MonitorFactory;
import com.infoclinika.mssharing.dto.response.FileDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

*/
/**
 * author Ruslan Duboveckij
 *//*

public class FileFilterServiceTest extends AbstractTestSuite {
    @Inject
    MonitorFactory monitorFactory;
    @Inject
    WebService webService;

    @Test
    public void testThermoFilter() throws IOException {
        FileFilterService filter = getFileFilterService(instrumentThermo0);
        File raw = FileCreator.createFile("test1.file.raw");
        File noRaw = FileCreator.createFile("test1.file.d");
        Assert.assertTrue("Theram filter is ignored .raw", filter.accept(raw));
        Assert.assertFalse("Theram filter isn't ingored bad extension", filter.accept(noRaw));
        Assert.assertFalse("Theram filter isn't ingored folder", filter.accept(rawFolder));
    }

    private FileFilterService getFileFilterService(InstrumentDTO instrumentDTO) {
        return createContext(instrumentDTO).getFileFilter();
    }

    @Test
    public void testWatersFilter() throws IOException {
        FileFilterService filter = getFileFilterService(instrumentWaters);
        File rawFile = FileCreator.createFile("test1.file.raw");
        File noRawFolder = FileCreator.createFolder("test2.folder.d");
        Assert.assertTrue("Waters filter is ignored .raw", filter.accept(rawFolder));
        Assert.assertFalse("Waters filter isn't ingored bad extension", filter.accept(noRawFolder));
        Assert.assertFalse("Waters filter isn't ingored file", filter.accept(rawFile));
    }

    @Test
    public void testAgilentFilter() throws IOException {
        FileFilterService filter = getFileFilterService(instrumentAgilent);
        File dFile = FileCreator.createFile("test1.file.d");
        File dFolder = FileCreator.createFolder("test2.folder.d");
        Assert.assertTrue("Agilent filter is ignored .d", filter.accept(dFolder));
        Assert.assertFalse("Agilent filter isn't ingored bad extension", filter.accept(rawFolder));
        Assert.assertFalse("Agilent filter isn't ingored file", filter.accept(dFile));
    }

    @Test
    public void testBrukerFilter()  throws IOException {
        FileFilterService filter = getFileFilterService(instrumentBruker);

        File folderRegions = FileCreator.createFolder("folderRegions1.d");
        File fileBadExt = FileCreator.createFile("folderRegions1.mir");
        File fileDat = FileCreator.createFile("folderRegions1.dat");
        File fileMis = FileCreator.createFile("folderRegions1.mis");

        Assert.assertTrue("Bruker filter is ignored.d", filter.accept(folderRegions));
        Assert.assertTrue("Bruker filter are ignored additionals", filter.accept(fileDat) &&
                filter.accept(fileMis));

        Assert.assertFalse("Bruker filter isn't ingored bad extension", filter.accept(fileBadExt));
    }

    @Test
    public void testAbSciex() throws IOException {
        FileFilterService filter = getFileFilterService(instrumentAbSCIEX);
        File wiffFile = FileCreator.createFile("test1.file.wiff");
        File wiffScanFile = FileCreator.createFile("test1.file.wiff.scan");
        File wiffMtdFile = FileCreator.createFile("test1.file.wiff.mtd");
        File wiffErrorFile = FileCreator.createFile("test1.file.wiff.exception");
        File noWiffFolder = FileCreator.createFolder("test2.folder.d");
        Assert.assertTrue("Ab Sciex filter is ignored .wiff", filter.accept(wiffFile));
        Assert.assertTrue("Ab Sciex filter are ignored additionals .wiff", filter.accept(wiffScanFile) &&
                filter.accept(wiffMtdFile));
        Assert.assertFalse("Agilent and bruker filter isn't ingored bad additional", filter.accept(wiffErrorFile));
        Assert.assertFalse("Agilent and bruker filter isn't ingored bad extension", filter.accept(noWiffFolder));
        Assert.assertFalse("Agilent and bruker filter isn't ingored file", filter.accept(noWiffFolder));
    }

    @Test
    public void testZipFile() throws IOException {
        final String fileNameUploaded = "test2.file.zip";
        FileDTO fileZip = new FileDTO(0, fileNameUploaded,
                (long) 0, (long) 0, "", "", "", false, null, false, (long) 0, null, null, false);
        Mockito.when(webService.getInstrumentFiles(instrumentAbSCIEX)).thenReturn(Lists.newArrayList(fileZip));

        FileFilterService filter = getFileFilterService(instrumentAbSCIEX);
        final boolean acceptName1 = filter.isAcceptName("test1.file.zip");
        final boolean acceptName2 = filter.isAcceptName("test2.file.zip");

        Mockito.when(webService.getInstrumentFiles(instrumentAbSCIEX)).thenReturn(instrumentFiles);

        Assert.assertTrue("Zip filter is ignored .zip", acceptName1);
        Assert.assertFalse("Zip filter isn't ignored .zip", acceptName2);
    }
}
*/
