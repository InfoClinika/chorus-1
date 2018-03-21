package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.write.AdministrationToolsManagement;
import com.infoclinika.mssharing.model.write.ArchiveSynchronizationManagement;
import com.infoclinika.mssharing.model.write.FileOperationsManager;
import com.infoclinika.mssharing.model.write.StudyManagement;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import com.infoclinika.mssharing.web.controller.request.AdminBroadcastNotificationRequest;
import com.infoclinika.mssharing.web.demo.RunDemoDataCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.inject.Inject;
import java.security.Principal;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;


/**
 * @author Herman Zamula
 */
@Controller
@RequestMapping("/admin/tools")
public class AdminToolsController extends ErrorHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AdminToolsController.class);

    @Inject
    private AdministrationToolsManagement administrationToolsManagement;

    @Inject
    private ArchiveSynchronizationManagement synchronizationManagement;

    @Inject
    private FileOperationsManager fileOperationsManager;
    @Inject
    private BillingService billingService;
    @Inject
    private RunDemoDataCreator runDemoDataCreator;

    @RequestMapping(value = "/notification", method = RequestMethod.POST)
    public void broadcastNotification(@RequestBody AdminBroadcastNotificationRequest request, Principal principal) {
        administrationToolsManagement.broadcastNotification(getUserId(principal), request.title, request.body);
    }

    @RequestMapping(value = "/synchronize-s3-state-with-db", method = RequestMethod.GET)
    @ResponseBody
    public String synchronizeS3StateWithDB() {
        synchronizationManagement.synchronizeS3StateWithDB();

        return synchronizationManagement.checkSynchronizationState().toString();
    }

    @RequestMapping(value = "/synchronize-s3-state-with-db-cancel", method = RequestMethod.GET)
    @ResponseBody
    public String cancelSynchronization() {
        synchronizationManagement.cancelSynchronization();

        return synchronizationManagement.checkSynchronizationState().toString();
    }

    @RequestMapping(value = "/synchronize-s3-state-with-db-check", method = RequestMethod.GET)
    @ResponseBody
    public String checkSynchronizationState() {
        return synchronizationManagement.checkSynchronizationState().toString();
    }

    @RequestMapping(value = "/check-is-file-size-consistent", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void checkIsFilesSizeConsistent(Principal principal) {
        final long actor = getUserId(principal);
        fileOperationsManager.checkIsFilesConsistent(actor);
    }

    @RequestMapping(value = "/run-billing-migration", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void runBillingMigration(Principal principal) {
        final long userId = getUserId(principal);
        billingService.runMigration(userId);
    }

    @RequestMapping(value = "/unarchive-inconsistent-files", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void unarchiveInconsistentFiles(Principal principal) {
        final long userId = getUserId(principal);
        administrationToolsManagement.unarchiveInconsistentFiles(userId);
    }

    @RequestMapping(value = "/generate-demo-cdf-databases", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void generateDemoCdfDatabases(Principal principal) {
        final long userId = getUserId(principal);
        runDemoDataCreator.createCdfDatabases(userId);
    }

}
