package com.infoclinika.mssharing.model.test;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.internal.helper.AccountRemovalHelper;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.model.read.dto.details.ProjectItem;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Date;
import java.util.Set;

/**
 * @author timofei.kasianov 7/30/18
 */
public class DeletedUserTest extends AbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeletedUserTest.class);

    @Inject
    private AccountRemovalHelper accountRemovalHelper;

    @Test
    public void testDataOfDeletedUserIsAvailable() {

        final long paulId = uc.createPaul();
        final long labId = uc.createLab3();
        final long johnId = uc.createJohnWithoutLab();

        // add John to lab3
        final long requestId = userManagement.applyForLabMembership(johnId, labId);
        final LabReaderTemplate.LabLineTemplate labDetails = dashboardReader.readLab(labId);
        userManagement.approveLabMembershipRequest(labDetails.labHead, requestId);

        // John creates a project and shares it with Paul
        final long projectId = uc.createProject(johnId);
        uc.shareProjectThrowGroup(johnId, paulId, projectId, SharingManagementTemplate.Access.WRITE);

        // John creates an experiment
        final long instrumentId = createInstrumentAndApproveIfNeeded(johnId, labId);
        final String fileName = generateString();
        final long fileId = createFile(johnId, instrumentId, fileName);
        final long experimentId = createExperiment(johnId, projectId, fileId, labId);

        // get John's personal data before deletion
        final UserManagementTemplate.PersonInfo johnDetailsBeforeDeletion = userReader.readPersonInfo(johnId);

        // delete John (mark as deleted)
        userManagement.markUserAsDeleted(johnId);

        final UserManagementTemplate.PersonInfo johnDetailsAfterDeletion = userReader.readPersonInfo(johnId);

        // personal data has been erased
        Assert.assertNotEquals(johnDetailsAfterDeletion.email, johnDetailsBeforeDeletion.email);
        Assert.assertNotEquals(johnDetailsAfterDeletion.firstName, johnDetailsBeforeDeletion.firstName);
        Assert.assertNotEquals(johnDetailsAfterDeletion.lastName, johnDetailsBeforeDeletion.lastName);

        // John's data is still available to Paul
        final ProjectItem projectDetails = detailsReader.readProject(paulId, projectId);
        final ExperimentItem experimentDetails = detailsReader.readExperiment(paulId, experimentId);
        final FileItem fileDetails = detailsReader.readFile(paulId, fileId);
        Assert.assertNotNull(projectDetails);
        Assert.assertNotNull(experimentDetails);
        Assert.assertNotNull(fileDetails);

        // John is no longer visible within lab3
        final ImmutableSet<DashboardReader.UserLine> labMembers =
            dashboardReader.readUsersByLab(labDetails.labHead, labDetails.id);
        Assert.assertTrue(labMembers.size() == 1);
        Assert.assertTrue(labMembers.iterator().next().id == paulId);

        // delete Paul
        userManagement.markUserAsDeleted(paulId);

        // update lab to set new lab head
        final LabReaderTemplate.LabLineTemplate labLineTemplate = dashboardReader.readLab(labId);
        labManagement.editLab(admin(), labId, new LabManagementTemplate.LabInfoTemplate(
            labLineTemplate.institutionUrl,
            new UserManagementTemplate.PersonInfo(
                "new-lab-head-firstName",
                "new-lab-head-lastName",
                "new-lab-head-email"
            ),
            labLineTemplate.name
        ));

        final LabReaderTemplate.LabLineTemplate labDetailsWithUpdatedLabHead = dashboardReader.readLab(labId);
        final long kateId = uc.createKateAndLab2();
        final long kateLabRequestId = userManagement.applyForLabMembership(kateId, labId);
        userManagement.approveLabMembershipRequest(labDetailsWithUpdatedLabHead.labHead, kateLabRequestId);

        // lab files are available
        final Set<FileLine> labFiles = dashboardReader.readFilesByLab(kateId, labId);
        final long joeProjectId = uc.createProject(kateId);
        final long joeExperimentId = createExperiment(kateId, joeProjectId, labFiles.iterator().next().id, labId);
        final ExperimentItem joeExperimentDetails = detailsReader.readExperiment(kateId, joeExperimentId);
        Assert.assertNotNull(joeExperimentDetails);

    }

    @Test
    public void testDeletedUserGetsAccessDenied() {

        final long paulId = uc.createPaul();
        final long labId = uc.createLab3();
        final long projectId = uc.createProject(paulId);
        final long instrumentId = createInstrumentAndApproveIfNeeded(paulId, labId);
        final long fileId = createFile(paulId, instrumentId, generateString());
        final long experimentId = createExperiment(paulId, projectId);

        userManagement.markUserAsDeleted(paulId);

        // unable to create a project
        try {
            uc.createProject(paulId);
            Assert.fail();
        } catch (AccessDenied e) {
            LOGGER.error("Error while creating project", e);
        }

        // unable to create an instrument
        try {
            createInstrumentAndApproveIfNeeded(paulId, labId);
            Assert.fail();
        } catch (AccessDenied e) {
            LOGGER.error("Error while creating instrument", e);
        }

        // unable to create a file
        try {
            createFile(paulId, instrumentId, generateString());
            Assert.fail();
        } catch (AccessDenied e) {
            LOGGER.error("Error while creating file", e);
        }

        // unable to create an experiment
        try {
            createExperiment(paulId, projectId);
            Assert.fail();
        } catch (AccessDenied e) {
            LOGGER.error("Error while creating experiment", e);
        }

    }

    @Test
    public void testAccountsAreDeletedAsRequested() {
        final long labId = uc.createLab3();
        final long kateId = uc.createKateAndLab2();
        uc.addKateToLab3();

        final LabReaderTemplate.LabLineTemplate labDetails = dashboardReader.readLab(labId);
        // lab users with Kate
        final ImmutableSet<DashboardReader.UserLine> labUsers =
            dashboardReader.readUsersByLab(labDetails.labHead, labId);
        // request account removal for Kate
        userManagement.setAccountRemovalRequestDate(kateId, new Date());
        // trigger account removal
        accountRemovalHelper.removeUsersWithRemovalRequestOlderThan(-1L);
        // lab users without Kate
        final ImmutableSet<DashboardReader.UserLine> labUsersAfterKateDeletion =
            dashboardReader.readUsersByLab(labDetails.labHead, labId);

        Assert.assertNotEquals(labUsers.size(), labUsersAfterKateDeletion.size());
    }
}
