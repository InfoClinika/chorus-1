package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.internal.migration.MigrationHelper;
import com.infoclinika.mssharing.model.write.*;
import com.infoclinika.mssharing.retries.RetriesService;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import com.infoclinika.mssharing.web.controller.request.AdminBroadcastNotificationRequest;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.inject.Inject;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static com.infoclinika.mssharing.model.write.StorageUsageStatisticsService.StatisticsSearchDTO;
import static com.infoclinika.mssharing.model.write.StorageUsageStatisticsService.StorageUsageStatisticsDTO;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;


/**
 * @author Herman Zamula
 */
@Controller
@RequestMapping("/admin/tools")
public class AdminToolsController extends ErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminToolsController.class);
    private static final int MAX_ATTEMPTS = 10;
    private static final long INTERVAL = 1000L; //one second

    @Inject
    private AdministrationToolsManagement administrationToolsManagement;

    @Inject
    private ArchiveSynchronizationManagement synchronizationManagement;

    @Inject
    private FileOperationsManager fileOperationsManager;

    @Inject
    private BillingService billingService;

    @Inject
    private MigrationHelper migrationHelper;

    @Inject
    private StorageUsageStatisticsService storageUsageStatisticsService;

    private Map<String, SseEmitter> aggregateStatisticsResultsHandlers = new HashMap<>();

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

    @RequestMapping(value = "/nates-migration", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void performMigrationForNatesLab() {
        LOGGER.info("# Migration for Nate's labs");
        migrationHelper.removeData();
    }

    @RequestMapping(value = "/aggregateStatistics/subscribe/{aggregateStatId}", method = RequestMethod.GET)
    public SseEmitter registerAggregateStatResultsHandler(@PathVariable("aggregateStatId") String aggregateStatId) {
        LOGGER.info("Subscribing to aggregate statistics results ready event, id: {}", aggregateStatId);

        SseEmitter emitter = new SseEmitter(0L);
        aggregateStatisticsResultsHandlers.put(aggregateStatId, emitter);

        return emitter;
    }

    @RequestMapping(value = "/aggregateStatistics", method = RequestMethod.POST)
    @ResponseBody
    public ValueResponse<String> aggregateStatistics(@RequestBody AggregateStatisticsRequest request) {
        LOGGER.info(
            "Aggregating storage usage statistics per lab using next params: {}",
            request.toString()
        );

        final String aggregateStatisticsId = UUID.randomUUID().toString();
        final Consumer<List<StorageUsageStatisticsDTO>> aggregateStatHandler = createAggregateStatResultsHandler(
            aggregateStatisticsId
        );

        Executors.newSingleThreadExecutor().execute(() -> {
            //used deprecated methods to get identical dates on client and server sides
            final Date from = new Date(request.fromDate);
            final Date to = new Date(request.toDate);
            final List<StorageUsageStatisticsDTO> statisticsList =
                storageUsageStatisticsService.aggregateStatisticsAndSave(from, to);

            new RetriesService<Void>().doWithRetries(MAX_ATTEMPTS, INTERVAL, () -> {
                aggregateStatHandler.accept(statisticsList);

                return null;
            });
        });

        return new ValueResponse<>(aggregateStatisticsId);
    }

    @RequestMapping(value = "/getStatistics", method = RequestMethod.POST)
    @ResponseBody
    public List<StorageUsageStatisticsDTO> getStatistics(
        @RequestBody GetStatisticsRequest request
    ) {
        LOGGER.info(
            "Getting storage usage statistics per lab using next params: {}",
            request.toString()
        );

        return storageUsageStatisticsService.findByDeadline(new Date(request.deadline));
    }

    @RequestMapping(value = "/getStatisticsSearches", method = RequestMethod.POST)
    @ResponseBody
    public List<StatisticsSearchDTO> getStatisticsSearches() {
        LOGGER.info("Getting statistics searches");

        return storageUsageStatisticsService.findAllStatisticsSearches();
    }

    private Consumer<List<StorageUsageStatisticsDTO>> createAggregateStatResultsHandler(String aggregateStatisticsId) {
        return statisticsSummaryList -> {
            final SseEmitter emitter = aggregateStatisticsResultsHandlers.get(aggregateStatisticsId);

            if (emitter == null) {
                throw new RuntimeException("No emitter to push the data");
            }

            try {
                emitter.send(statisticsSummaryList);
                emitter.complete();
                aggregateStatisticsResultsHandlers.remove(aggregateStatisticsId);
            } catch (IOException e) {
                LOGGER.error("Error during sending dc export results.", e);
                emitter.completeWithError(e);
            }
        };
    }

    private static class AggregateStatisticsRequest {
        public String fromDate;
        public String toDate;

        @Override
        public String toString() {
            return "AggregateStatisticsRequest{" +
                "fromDate=" + fromDate +
                ", toDate=" + toDate +
                '}';
        }
    }

    private static class GetStatisticsRequest {
        public String deadline;

        @Override
        public String toString() {
            return "GetStatisticsRequest{" +
                "deadline=" + deadline +
                '}';
        }
    }
}
