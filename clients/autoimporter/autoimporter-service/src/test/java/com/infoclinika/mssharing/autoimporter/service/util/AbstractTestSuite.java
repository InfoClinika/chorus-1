/*
package com.infoclinika.mssharing.autoimporter.service.util;

import com.google.common.collect.Lists;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.FileDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import com.infoclinika.mssharing.dto.response.VendorDTO;
import com.infoclinika.mssharing.autoimporter.model.Context;
import com.infoclinika.mssharing.autoimporter.model.Session;
import com.infoclinika.mssharing.autoimporter.model.bean.ContextInfo;
import com.infoclinika.mssharing.autoimporter.service.api.UploadConfigurationService;
import WebService;
import com.infoclinika.mssharing.autoimporter.service.api.internal.UploadService;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

*/
/**
 * author Ruslan Duboveckij
 *//*

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfig.class)
public abstract class AbstractTestSuite {
    public static final VendorDTO vendorThermo = new VendorDTO(1, "Thermo");
    public static final VendorDTO vendorWaters = new VendorDTO(2, "Waters");
    public static final VendorDTO vendorAgilent = new VendorDTO(3, "Agilent");
    public static final VendorDTO vendorBruker = new VendorDTO(4, "Bruker");
    public static final VendorDTO vendorAbSciex = new VendorDTO(5, "AB SCIEX");
    public static final InstrumentDTO instrumentThermo0 = new InstrumentDTO(3, "Cell Machine", vendorThermo, 1,
    "1ABC123456", 3);
    public static final InstrumentDTO instrumentThermo1 = new InstrumentDTO(1, "Genome Machine", vendorThermo, 1,
    "ABC123456", 2);
    public static final InstrumentDTO instrumentWaters = new InstrumentDTO(3, "Test Waters", vendorWaters, 1,
    "1ABC123456", 2);
    public static final InstrumentDTO instrumentAgilent = new InstrumentDTO(3, "Test Agilent", vendorAgilent, 1,
    "1ABC123456", 2);
    public static final InstrumentDTO instrumentBruker = new InstrumentDTO(3, "Test Bruker", vendorBruker, 1,
    "1ABC123456", 2);
    public static final InstrumentDTO instrumentAbSCIEX = new InstrumentDTO(8, "Test Ab Sciex", vendorAbSciex, 1,
    "1ABC123456", 2);
    public static final DictionaryDTO specie0 = new DictionaryDTO(22, "Zea mays");
    public static final DictionaryDTO specie1 = new DictionaryDTO(99, "Zea mays 99");
    public static final List<FileDTO> unfinishedFiles = Lists.newArrayList();
    public static final List<FileDTO> instrumentFiles = Lists.newArrayList();
    public static List<File> twoRawFiles1;
    public static List<File> twoRawFiles2;
    public static File folderMany1;
    public static File folderMany2;
    protected File rawFolder;
    @Inject
    protected Session session;
    @Inject
    protected WebService webService;
    @Inject
    protected UploadService uploadService;
    @Inject
    protected ConfigBean configBean;

    @BeforeClass
    public static void setUpInit() throws IOException {
        // created file and folder with temp file
        setDownInit();
        folderMany1 = FileCreator.createFolder("UploadFolderTestMany1");
        folderMany2 = FileCreator.createFolder("UploadFolderTestMany2");

        twoRawFiles1 = Lists.newArrayList(FileCreator.createTempFile(folderMany1),
                FileCreator.createTempFile(folderMany1));
        twoRawFiles2 = Lists.newArrayList(FileCreator.createTempFile(folderMany2),
                FileCreator.createTempFile(folderMany2));
    }

    @AfterClass
    public static void setDownInit() throws IOException {
        FileCreator.deleteTempDir();
    }

    protected Context createContext(InstrumentDTO instrumentDTO) {
        final String randomFolderName = String.valueOf(System.nanoTime());
        session.addContext(new ContextInfo(0, "", randomFolderName, false, "", instrumentDTO, specie0, new Date(),
        UploadConfigurationService.CompleteAction.NOTHING, ""));
        return session.getContext(randomFolderName);
    }

    @Before
    public void setUp() throws IOException {
        Mockito.when(webService.getInstruments()).thenReturn(
                Lists.newArrayList(instrumentThermo0, instrumentThermo1, instrumentWaters,
                        instrumentAgilent, instrumentBruker, instrumentAbSCIEX));
        Mockito.when(webService.getSpecies()).thenReturn(Lists.newArrayList(specie0, specie1));
        Mockito.when(webService.getInstrumentFiles(instrumentThermo0)).thenReturn(instrumentFiles);
        Mockito.when(webService.getUnfinishedUploads()).thenReturn(unfinishedFiles);
        rawFolder = FileCreator.createFolder("test1.folder.raw");
    }

    @After
    public void setDown() throws IOException {
        final Collection<Context> contexts = session.getContexts().values();
        for (Context context : contexts) {
            if(context.getInfo().isStarted()){
                uploadService.stopWatch(context.getInfo().getFolder());
            }
        }
        session.getContexts().clear();
        FileUtils.deleteDirectory(new File(configBean.getZipFolderPath()));
    }
}
*/
