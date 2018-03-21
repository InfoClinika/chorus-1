/*
package com.infoclinika.mssharing.upload.common.web.test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.dto.request.ConfirmMultipartUploadDTO;
import com.infoclinika.mssharing.dto.request.UploadFilesDTORequest;
import com.infoclinika.mssharing.dto.request.UserNamePassDTO;
import com.infoclinika.mssharing.dto.response.*;
import com.infoclinika.mssharing.upload.common.web.test.util.FileCreator;
import com.infoclinika.mssharing.upload.common.web.test.util.SpringConfig;
import com.infoclinika.mssharing.upload.common.web.api.WebService;
import org.apache.cxf.common.util.StringUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfig.class)
public class WebServiceTest {

    static File testTempFile;
    @Inject
    WebService webService;

    @BeforeClass
    public static void setUpInit() throws IOException {
        testTempFile = FileCreator.createTempFile();
    }

    @AfterClass
    public static void setDownInit() throws IOException {
        FileCreator.deleteTempDir();
    }

    @Before
    public void setUp() throws IOException {
        // authenticate created session
        AuthenticateDTO authenticate =
                webService.authenticate(new UserNamePassDTO("pavel.kaplin@gmail.com", "pwd"));
        Assert.assertTrue("Error - Server didn't respond", authenticate != null);
        Assert.assertFalse("Error - Bad credentials", StringUtils.isEmpty(authenticate.getRestToken()));
    }

    @Test
    public void testFindInstrumentsAndSpecies() {
        // chose instrument and specie
        final List<InstrumentDTO> instruments = webService.getInstruments();
        Assert.assertFalse("Error - Not work get all instruments",
                instruments.isEmpty());
        Assert.assertFalse("Error - Not work get all species",
                webService.getSpecies().isEmpty());

        Assert.assertTrue("Error - Not work get files for this instruments",
                FluentIterable.from(instruments).anyMatch(new Predicate<InstrumentDTO>() {
                    @Override
                    public boolean apply(@Nullable InstrumentDTO input) {
                        return !webService.getInstrumentFiles(input).isEmpty();
                    }
                }));
    }

    @Test
    @DependsOn("testFindInstrumentsAndSpecies")
    public void testPostStartUploadRequest() throws IOException {
        InstrumentDTO instrument1 = webService.getInstruments().get(1);
        UploadFilesDTORequest.UploadFile uploadFile = new UploadFilesDTORequest.UploadFile(
                testTempFile.getName(), "", testTempFile.length(), 1, false
        );
        UploadFilesDTORequest request = new UploadFilesDTORequest(instrument1.getId(), newArrayList(uploadFile));
        UploadFilesDTOResponse response = webService.postStartUploadRequestBeforeFinish(request);
        Assert.assertTrue("Error - Return other instrument file", response.getInstrument() == instrument1.getId());
        Assert.assertFalse("Error - File not write to request upload", response.getFiles().isEmpty());
    }

    @Test
    public void testFindUnfinishedUploads() throws IOException {
        testPostStartUploadRequest();
        List<FileDTO> files = webService.getUnfinishedUploads();
        Assert.assertFalse("Error - Not have unfinished files", files.isEmpty());
        List<String> fileNameUnfinished = ImmutableList.copyOf(
                Collections2.transform(files, new Function<FileDTO, String>() {
                    @Override
                    public String apply(FileDTO file) {
                        return file.getName();
                    }
                }));
        String name = testTempFile.getName();
            Assert.assertTrue("Error - File " + name + " not found",
                    fileNameUnfinished.contains(name));
    }

    @Test
    @DependsOn("testFindUnfinishedUploads")
    public void testDeleteUnfinishedUploads() throws IOException {
        List<FileDTO> files = webService.getUnfinishedUploads();
        for (FileDTO file : files) {
            DeleteUploadDTO result = webService.deleteUpload(file.getId());
            Assert.assertTrue("Error - File " + file.getName() + " not deleted", result.isDeleted());
        }
    }

    @Test
    @DependsOn("testFindUnfinishedUploads")
    public void postCompleteUploadRequest() throws IOException {
        List<FileDTO> files = webService.getUnfinishedUploads();
        for (FileDTO file : files) {
            CompleteUploadDTO result = webService.postCompleteUploadRequest(
                    new ConfirmMultipartUploadDTO(file.getId(), String.valueOf(file.getContentId())));
            Assert.assertTrue("Error - File " + file.getName() + " not confirmed", result.isConfirmed());
        }
    }
}*/
