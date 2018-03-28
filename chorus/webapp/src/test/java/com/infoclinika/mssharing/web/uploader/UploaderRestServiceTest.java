package com.infoclinika.mssharing.web.uploader;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.dto.request.UploadFilesDTORequest;
import com.infoclinika.mssharing.dto.response.UploadFilesDTOResponse;
import com.infoclinika.mssharing.model.UploadLimitException;
import com.infoclinika.mssharing.model.helper.LockMzItem;
import com.infoclinika.mssharing.model.helper.RestHelper;
import com.infoclinika.mssharing.model.write.FileMetaDataInfo;
import com.infoclinika.mssharing.model.write.InstrumentDetails;
import com.infoclinika.mssharing.model.write.UserManagement;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.web.helper.AbstractDataBasedTest;
import org.springframework.beans.factory.annotation.Value;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;
import java.util.*;
import static org.testng.Assert.*;

import static com.google.common.collect.Sets.newHashSet;


public class UploaderRestServiceTest extends AbstractDataBasedTest {

    private static final Logger LOGGER = Logger.getLogger(UploaderRestServiceTest.class);


    @Value("${database.data.admin.email}")
    private String adminEmail;
    @Value("${database.data.admin.password}")
    private String adminPassword;

    private RestHelper.Token restToken;
    private UploadFilesDTORequest.UploadFile uploadFile;

    @BeforeClass
    public void setUp(){

        uploadFile = new UploadFilesDTORequest.UploadFile("The Witcher", "test labels", 1999999999, 1l, false);
        userManagement.createPerson(new UserManagementTemplate.PersonInfo("Grisha", "Wong", "example@email.com"),"password", new HashSet<>(), "");
        RestHelper.UserDetails userDetailsWithoutLab = restHelper.getUserDetailsByEmail("example@email.com");
        restToken  = restHelper.generateToken(userDetailsWithoutLab);

    }


    @Test(expectedExceptions = AccessDenied.class)
    public void checkUserDoesNotHaveAccessInLabUploadRequest(){
        uploaderRestService.uploadRequest(new UploadFilesDTORequest(), restToken.token);
    }


    @Test(expectedExceptions = AccessDenied.class)
    public void checkUserDoesNotHaveAccessInLabSSEUploadRequest(){
        uploaderRestService.sseUploadRequest(new UploadFilesDTORequest(), restToken.token);
    }

    @Test(expectedExceptions = UploadLimitException.class)
    public void userCantUploadMoreToUploadRequest(){
        ArrayList<UploadFilesDTORequest.UploadFile> files = new ArrayList<>();
        files.add(uploadFile);

        final UserManagement.PersonInfo mishaHead = new UserManagement.PersonInfo("Misha", "Wong", "wong@email.com");
        long admin = createAdmin();

        long lab = createLab(admin, "head@email.com", "/", "TEST Lab", mishaHead);
        long user = createUserAndVerifyEmail(mishaHead, lab);

        long instrument = createInstrumentAndApprove(user,lab, "F1", "121", "", "", anyInstrumentModel()).get();
        attachFilesToInstrument(user, instrument);

        RestHelper.UserDetails userDetailsWithLab = restHelper.getUserDetailsByEmail("wong@email.com");
        RestHelper.Token  restTokenUserInLab = restHelper.generateToken(userDetailsWithLab);

        UploadFilesDTORequest uploadFilesDTORequest = new UploadFilesDTORequest(instrument, files);
        uploaderRestService.uploadRequest(uploadFilesDTORequest, restTokenUserInLab.token);
    }


    @Test
    public void successfulUploadFileResponse(){
        ArrayList<UploadFilesDTORequest.UploadFile> files = new ArrayList<>();
        files.add(uploadFile);
        final UserManagement.PersonInfo bobHead = new UserManagement.PersonInfo("Bob", "Borg", "borg@email.com");

        long admin = createAdmin();
        long lab = createLab(admin, "bob-head@email.com", "/", "Chorus Lab", bobHead);
        long user = createUserAndVerifyEmail(bobHead, lab);

        long instrument = createInstrumentAndApprove(user,lab, "Good instrument", "121000", "", "", anyInstrumentModel()).get();
        attachFileToInstrument(user, instrument);

        RestHelper.UserDetails bobInLab = restHelper.getUserDetailsByEmail("borg@email.com");
        RestHelper.Token restTokenBob = restHelper.generateToken(bobInLab);

        UploadFilesDTORequest uploadFilesDTORequest = new UploadFilesDTORequest(instrument, files);
        UploadFilesDTOResponse response = uploaderRestService.uploadRequest(uploadFilesDTORequest, restTokenBob.token);

        assertEquals(response.getFiles().size(), 1);
        assertEquals(response.getInstrument(), instrument);
    }

    @Test
    public void checkValidUserRestToken(){

        final UserManagement.PersonInfo bobMarleyHead = new UserManagement.PersonInfo("Bob", "Marley", "borg@email.com");
        long admin = createAdmin();

        long lab = createLab(admin, "bob-head@email.com", "/", "Marley Lab", bobMarleyHead);
        createUserAndVerifyEmail(bobMarleyHead, lab);

        RestHelper.UserDetails bobInLab = restHelper.getUserDetailsByEmail("borg@email.com");
        RestHelper.Token restToken = restHelper.generateToken(bobInLab);
        RestHelper.Token restToken1 = restHelper.generateToken(bobInLab);

        assertNotEquals(restToken.token, restToken1.token);
    }

    @Test
    public void checkGeneratedRestToken(){

        final UserManagement.PersonInfo bobMarleyHead = new UserManagement.PersonInfo("Bob", "Marley", "borg@email.com");

        long admin = createAdmin();
        long lab = createLab(admin, "bob-head@email.com", "/", "New Lab", bobMarleyHead);
        createUserAndVerifyEmail(bobMarleyHead, lab);

        RestHelper.UserDetails bobInLab = restHelper.getUserDetailsByEmail("borg@email.com");
        RestHelper.Token restToken = restHelper.generateToken(bobInLab);

        final RestHelper.UserDetails userDetails = restHelper.checkToken(restToken.token);

        assertEquals(userDetails.id, bobInLab.id);
        assertNotNull(userDetails);

    }

    private long createAdmin() {
        return initiator.admin("Dima", "Dima", adminEmail, adminPassword);
    }


    private long createLab(long admin, String labHeadMail, String institutionUrl, String labName, UserManagement.PersonInfo personInfoLab) {
        long lab = labManagement.requestLabCreation(new LabManagementTemplate.LabInfoTemplate(institutionUrl, personInfoLab, labName), labHeadMail);
        labManagement.confirmLabCreation(admin, lab);
        return lab;
    }


    private long createUserAndVerifyEmail(UserManagement.PersonInfo head, long lab) {
        final long userId = userManagement.createPersonAndApproveMembership(head, "pwd", lab, null);
        userManagement.verifyEmail(userId);
        return userId;
    }

    private Optional<Long> createInstrumentAndApprove(long user, long lab, String instrumentName, String serialNumber, String hplc, String peripherals, long model) {

        final boolean labHead = labManagement.isLabHead(user, lab);
        final InstrumentDetails details = new InstrumentDetails(instrumentName, serialNumber, hplc, peripherals, Collections.<LockMzItem>emptyList());

        if (labHead) {
            return Optional.of(instrumentManagement.createInstrument(user, lab, model, details));
        } else {
            final Optional<Long> instrumentRequest = instrumentManagement.newInstrumentRequest(user, lab, model, details, new ArrayList<Long>());
            final LabReaderTemplate.LabLineTemplate labLine = dashboardReader.readLab(lab);
            return Optional.of(instrumentManagement.approveInstrumentCreation(labLine.labHead, instrumentRequest.get()));
        }

    }

    private void attachFilesToInstrument(long user, long instrument){

        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide", 1263981338,"Test", null, anySpecie(), false));
    }

    private void attachFileToInstrument(long user, long instrument){

        instrumentManagement.createFile(user, instrument, new FileMetaDataInfo("Chorus guide!!!", 100000,"Test1", null, anySpecie(), false));
    }

    private long anySpecie() {
        return experimentCreationHelper.species().iterator().next().id;
    }

}
