package com.infoclinika.mssharing.integration.test.test.instrument;

import com.infoclinika.mssharing.integration.test.data.EmailFolder;
import com.infoclinika.mssharing.integration.test.data.UserData;
import com.infoclinika.mssharing.integration.test.data.experiment.LockMz;
import com.infoclinika.mssharing.integration.test.data.instrument.InstrumentData;
import com.infoclinika.mssharing.integration.test.helper.BaseTest;
import com.infoclinika.mssharing.integration.test.preconditions.CleanEmailBoxBefore;
import com.infoclinika.mssharing.integration.test.preconditions.LoginRequired;
import com.infoclinika.mssharing.integration.test.stepdefinitions.*;
import com.infoclinika.mssharing.integration.test.stepdefinitions.instrument.InstrumentDetailsSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.instrument.InstrumentGeneralSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.instrument.InstrumentListSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.instrument.InstrumentOperatorsSteps;
import com.infoclinika.mssharing.integration.test.utils.EmailService;
import org.testng.annotations.Test;

import java.util.List;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;
import static org.testng.Assert.*;

/**
 * @author Sergii Moroz
 */
public class Instrument extends BaseTest {

    @Test(dataProvider = "createInstrumentStayingOnAllProjectsPage", dataProviderClass = InstrumentDataProvider.class,
            groups = {"staging"})
    @LoginRequired
    public void createInstrumentStayingOnAllProjectsPage(InstrumentData instrumentData) {
        dashboardPageSteps.createInstrument(instrumentData);
        assertTrue(dashboardPageSteps.getInstrumentListSteps().isInstrumentDisplayed(instrumentData.getName()),
                "Instrument isn't appear after creating");
        //clearing data
        dashboardPageSteps.getInstrumentListSteps()
                .deleteInstrument(instrumentData.getName());
    }

    @Test(dataProvider = "createInstrumentStayingOnInstrumentsPage", dataProviderClass = InstrumentDataProvider.class,
            groups = {"staging"})
    @LoginRequired
    public void createInstrumentStayingOnInstrumentsPage(InstrumentData instrumentData) {
        dashboardPageSteps.getSidebarMenuSteps()
                .selectInstrumentsItem();
        dashboardPageSteps.createInstrument(instrumentData);
        assertTrue(dashboardPageSteps.getInstrumentListSteps().isInstrumentDisplayed(instrumentData.getName()),
                "Instrument isn't appear after creating");
        //clearing data
        dashboardPageSteps.getInstrumentListSteps()
                .deleteInstrument(instrumentData.getName());
    }

    @Test(dataProvider = "editInstrument", dataProviderClass = InstrumentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void editInstrument(InstrumentData instrumentData, InstrumentData newInstrumentData) {
        InstrumentListSteps instrumentListSteps = dashboardPageSteps.createInstrument(instrumentData).getInstrumentListSteps();
        InstrumentDetailsSteps instrumentDetails = instrumentListSteps.openInstrumentDetails(instrumentData.getName());
        instrumentDetails.changeName(newInstrumentData.getName())
                .changeSerial(newInstrumentData.getSerialNumber())
                .expandOptionalFields()
                .changeHlpc(newInstrumentData.getOptionalFields().gethLPC())
                .changePeripherals(newInstrumentData.getOptionalFields().getPeripherals())
                .pressSaveAndWait();
        assertTrue(dashboardPageSteps.getInstrumentListSteps().isInstrumentDisplayed(newInstrumentData.getName()),
                "Instrument name isn't changed");
        assertTrue(!dashboardPageSteps.getInstrumentListSteps().isInstrumentDisplayed(instrumentData.getName()),
                "Instrument with old name still displayed");
        instrumentDetails = instrumentListSteps.openInstrumentDetails(newInstrumentData.getName());
        assertEquals(instrumentDetails.getName(), newInstrumentData.getName());
        assertEquals(instrumentDetails.getSerial(), newInstrumentData.getSerialNumber());
        assertEquals(instrumentDetails.getHplc(), newInstrumentData.getOptionalFields().gethLPC());
        assertEquals(instrumentDetails.getOperatorAccess(), "Operator");
        assertEquals(instrumentDetails.getPeripherals(), newInstrumentData.getOptionalFields().getPeripherals());
        //clearing data
        instrumentDetails.pressCancel()
                .deleteInstrument(newInstrumentData.getName());
    }

    @Test(dataProvider = "removeInstrument", dataProviderClass = InstrumentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void removeInstrument(InstrumentData instrumentData) {
        dashboardPageSteps
                .createInstrument(instrumentData).getInstrumentListSteps()
                .deleteInstrument(instrumentData.getName());
        assertFalse(dashboardPageSteps.getInstrumentListSteps().isInstrumentDisplayed(instrumentData.getName()),
                "Instrument is still displayed after selecting 'Remove' option");
    }

    @Test(dataProvider = "instrumentFormLimitationsDuringCreation", dataProviderClass = InstrumentDataProvider.class,
            groups = {"staging"})
    @LoginRequired
    public void instrumentFormLimitationsDuringCreation(InstrumentData instrumentData) {
        InstrumentGeneralSteps instrumentGeneralSteps = dashboardPageSteps
                .getTopPanelSteps()
                .openCreationMenu()
                .clickCreateInstrument()
                .fillFormRequiredFields(instrumentData);
        assertTrue(instrumentGeneralSteps.isCreationEnabled(),
                "'Create' button is disabled in Instrument Wizard, all required fields are filled in, though");
        instrumentGeneralSteps.clearNameField();
        assertFalse(instrumentGeneralSteps.isCreationEnabled(),
                "'Create' button is enabled, required field 'Name' is not filled in, though");
        instrumentGeneralSteps.specifyName(instrumentData.getName())
                .clearSerialNumberField();
        assertFalse(instrumentGeneralSteps.isCreationEnabled(),
                "'Create' button is enabled, required field 'Serial Number' is not filled in, though");
        instrumentGeneralSteps.pressCancel();
        //The steps below are started from creating new instrument, because after selecting Vendor, Model or Laboratory,
        // these fields cannot be revert to default (blank) values
        dashboardPageSteps.getTopPanelSteps().openCreationMenu()
                .clickCreateInstrument()
                .specifyName(randomizeName(instrumentData.getName()))
                .specifySerialNumber(randomizeName(instrumentData.getSerialNumber()))
                .selectLab(instrumentData.getLaboratory());
        assertFalse(instrumentGeneralSteps.isCreationEnabled(),
                "'Create' button is enabled, required field 'Vendor' is not specified, though");
        instrumentGeneralSteps.pressCancel();
        dashboardPageSteps.getTopPanelSteps().openCreationMenu()
                .clickCreateInstrument()
                .specifyName(randomizeName(instrumentData.getName()))
                .specifySerialNumber(randomizeName(instrumentData.getSerialNumber()))
                .selectLab(instrumentData.getLaboratory())
                .selectVendor(instrumentData.getVendor());
        assertFalse(instrumentGeneralSteps.isCreationEnabled(),
                "'Create' button is enabled, required field 'Model' is not specified, though");
        instrumentGeneralSteps.pressCancel();
        dashboardPageSteps.getTopPanelSteps().openCreationMenu()
                .clickCreateInstrument()
                .specifyName(randomizeName(instrumentData.getName()))
                .specifySerialNumber(randomizeName(instrumentData.getSerialNumber()))
                .selectVendor(instrumentData.getVendor())
                .selectModel(instrumentData.getModel());
        assertFalse(instrumentGeneralSteps.isCreationEnabled(),
                "'Create' button is enabled, required field 'Laboratory' is not specified, though");
        instrumentGeneralSteps.pressCancel();
    }

    @Test(dataProvider = "instrumentFormLimitationsDuringEditing", dataProviderClass = InstrumentDataProvider.class,
            groups = {"staging"})
    @LoginRequired
    public void instrumentFormLimitationsDuringEditing(InstrumentData instrumentData) {
        dashboardPageSteps.createInstrument(instrumentData);
        InstrumentDetailsSteps instrumentDetails = dashboardPageSteps.getInstrumentListSteps().openInstrumentDetails(instrumentData.getName());
        instrumentDetails.clearName();
        assertFalse(instrumentDetails.isCreationEnabled(), "Instrument can be saved without name");
        instrumentDetails.changeName(instrumentData.getName())
                .clearSerial();
        assertFalse(instrumentDetails.isCreationEnabled(), "Instrument can be saved without serial number");
        instrumentDetails.changeSerial(instrumentData.getSerialNumber())
                .pressSaveAndWait();
        assertTrue(dashboardPageSteps.getInstrumentListSteps().isInstrumentDisplayed(instrumentData.getName()),
                "Instrument isn't appear after creating");
        //clearing data
        dashboardPageSteps.getInstrumentListSteps()
                .deleteInstrument(instrumentData.getName());
    }

    @Test(dataProvider = "shouldNotAllowToCreateInstrumentsWithIdenticalSerialNumbers",
            dataProviderClass = InstrumentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void shouldNotAllowToCreateInstrumentsWithIdenticalSerialNumbers(InstrumentData instrumentData1, InstrumentData instrumentData2) {
        dashboardPageSteps.createInstrument(instrumentData1);
        InstrumentGeneralSteps instrumentGeneral = dashboardPageSteps.getTopPanelSteps()
                .openCreationMenu()
                .clickCreateInstrument()
                .fillFormAllFields(instrumentData2)
                .pressCreateForIncorrectForm();
        assertEquals(instrumentGeneral.getIncorrectSerialError(),
                "Incorrect value - instrument with this serial number already exists");
        //clearing data
        instrumentGeneral.pressCancel()
                .getInstrumentListSteps()
                .deleteInstrument(instrumentData1.getName());
    }


    @Test(dataProvider = "shouldNotAllowToCreateInstrumentsWithIdenticalNames",
            dataProviderClass = InstrumentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void shouldNotAllowToCreateInstrumentsWithIdenticalNames(InstrumentData instrumentData1, InstrumentData instrumentData2) {
        dashboardPageSteps.createInstrument(instrumentData1);
        InstrumentGeneralSteps instrumentGeneral = dashboardPageSteps.getTopPanelSteps()
                .openCreationMenu()
                .clickCreateInstrument()
                .fillFormAllFields(instrumentData2);
        assertEquals(instrumentGeneral.getIncorrectNameError(), "In selected lab instrument with this name already exists");
        //clearing data
        instrumentGeneral.pressCancel()
                .getInstrumentListSteps()
                .deleteInstrument(instrumentData1.getName());
    }

    //in order to cover issue #240
    @Test(dataProvider = "editInstrumentNameOnly", dataProviderClass = InstrumentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void editInstrumentNameOnly(InstrumentData instrumentData, String changedInstrumentName) {
        InstrumentListSteps instrumentListSteps = dashboardPageSteps.createInstrument(instrumentData)
                .getSidebarMenuSteps()
                .selectInstrumentsItem()
                .openInstrumentDetails(instrumentData.getName())
                .changeName(changedInstrumentName)
                .pressSaveAndWait();
        assertTrue(instrumentListSteps.isInstrumentDisplayed(changedInstrumentName),
                "Instrument doesn't appear after creating");
        //clearing data
        instrumentListSteps.deleteInstrument(changedInstrumentName);
    }

    @Test(dataProvider = "shareInstrument", dataProviderClass = InstrumentDataProvider.class, groups = {"staging"})
    @CleanEmailBoxBefore(folderName = {EmailFolder.INSTRUMENT_AVAILABLE})
    @LoginRequired
    public void shareInstrument(InstrumentData instrumentData, UserData userData, UserData labHeadData) {
        //Create instrument and share it to another user
        InstrumentOperatorsSteps instrumentOperatorsSteps = dashboardPageSteps.getTopPanelSteps().openCreationMenu()
                .clickCreateInstrument()
                .fillFormAllFields(instrumentData)
                .selectOperatorsTab()
                .specifyOperatorToInvite(userData.getFullName());
        assertTrue(instrumentOperatorsSteps.isOperatorAdded(userData.getFullName()),
                "Operator is not present in list after adding");
        InstrumentListSteps instrumentListSteps = instrumentOperatorsSteps.pressCreateButton();
        assertTrue(instrumentListSteps.isInstrumentDisplayed(instrumentData.getName()),
                "Created shared instrument is not present in list");
        //Relogin as user who received operator rights and verify, that instrument appears
        dashboardPageSteps
                .getHeader()
                .logout()
                .pressSignInButton()
                .login(userData)
                .getSidebarMenuSteps()
                .selectInstrumentsItem();
        assertTrue(instrumentListSteps.isInstrumentDisplayed(instrumentData.getName()),
                "Shared instrument is not appeared for user who received operator rights");
        //Verify, that email about new instrument availability received
        assertTrue(EmailService.connectAndGetAllEmailsFromFolder(EmailFolder.INSTRUMENT_AVAILABLE).size() == 1,
                "Email about new instrument availability is not received by user");
        //Relogin as instrument owner and remove it to clean application from test data
        dashboardPageSteps
                .getHeader().logout()
                .pressSignInButton()
                .login(labHeadData)
                .getSidebarMenuSteps().selectInstrumentsItem()
                .deleteInstrument(instrumentData.getName());
    }

    @Test(dataProvider = "requestInstrument", dataProviderClass = InstrumentDataProvider.class, groups = {"staging"})
    @CleanEmailBoxBefore(folderName = {EmailFolder.INSTRUMENT_APPROVED})
    @LoginRequired
    public void requestInstrument(InstrumentData instrumentData, UserData geneSimmonsAtGmail, UserData pavelKaplinAtGmail) {
        //Create instrument, relogin as another user and make request for this instrument
        dashboardPageSteps
                .createInstrument(instrumentData)
                .getHeader().logout()
                .pressSignInButton()
                .login(geneSimmonsAtGmail)
                .getSidebarMenuSteps().selectInstrumentsItem()
                .requestAccessToInstrument(instrumentData);
        OutboxListSteps outboxListSteps = dashboardPageSteps.getSidebarMenuSteps().selectOutbox();
        assertTrue(outboxListSteps.isItemPresentInOutbox("Operators of " + instrumentData.getName(), instrumentData.getName()),
                "Instrument request does not appear in the Outbox list");
        //Relogin as instrument owner and approve the request
        InboxListSteps inboxListSteps = dashboardPageSteps
                .getHeader().logout()
                .pressSignInButton()
                .login(pavelKaplinAtGmail)
                .getSidebarMenuSteps().selectInbox();
        assertTrue(inboxListSteps.isItemPresentInInboxList(geneSimmonsAtGmail.getFullName(), instrumentData.getName()),
                "Instrument request does not appear in the Inbox list of Operator");
        inboxListSteps.pressApproveButtonForItem(geneSimmonsAtGmail.getFullName(), instrumentData.getName());
        //Verify that email with approval received
        assertTrue(EmailService.connectAndGetAllEmailsFromFolder(EmailFolder.INSTRUMENT_APPROVED).size() == 1,
                "Email with approving request for access to the instrument is not received by user");
        //Relogin as user who requested for operator rights and verify, that instrument appears
        InstrumentListSteps instrumentListSteps = dashboardPageSteps
                .getHeader().logout()
                .pressSignInButton()
                .login(geneSimmonsAtGmail)
                .getSidebarMenuSteps().selectInstrumentsItem();
        assertTrue(instrumentListSteps.isInstrumentDisplayed(instrumentData.getName()),
                "Instrument does not appear for user after request approving");
        //Relogin as instrument owner and remove it to clean application from test data
        dashboardPageSteps
                .getHeader().logout()
                .pressSignInButton()
                .login(pavelKaplinAtGmail)
                .getSidebarMenuSteps()
                .selectInstrumentsItem()
                .deleteInstrument(instrumentData.getName());
    }

    @Test(dataProvider = "createInstrumentWithLockMasses", dataProviderClass = InstrumentDataProvider.class, groups = {"staging"})
    @LoginRequired
    public void createInstrumentWithLockMasses(InstrumentData instrumentData) {
        dashboardPageSteps.createInstrument(instrumentData);
        assertTrue(dashboardPageSteps.getInstrumentListSteps().isInstrumentDisplayed(instrumentData.getName()),
                "Instrument isn't appear after creating");
        InstrumentDetailsSteps instrumentDetailsSteps = dashboardPageSteps.getSidebarMenuSteps()
                .selectInstrumentsItem()
                .openInstrumentDetails(instrumentData.getName())
                .expandOptionalFields();
        List<LockMz> actualLockMzValues = instrumentDetailsSteps.readAllLockMasses();
        List<LockMz> expectedLockMzValues = instrumentData.getOptionalFields().getLockMasses();
        assertTrue(expectedLockMzValues.containsAll(actualLockMzValues),
                "Actual Lock Mz values does not equal to the expected.\nExpected: " + expectedLockMzValues
                        + "\nActual: " + actualLockMzValues);
        //clearing data
        instrumentDetailsSteps.pressCancel()
                .deleteInstrument(instrumentData.getName());
    }


}
