package com.infoclinika.mssharing.web.controller;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.read.TrashReader;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.model.write.LabHeadManagement;
import com.infoclinika.mssharing.model.write.StudyManagement;
import com.infoclinika.mssharing.web.controller.request.ReadNotRestorableItemsRequest;
import com.infoclinika.mssharing.web.controller.response.ReadNotRestorableItemsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Collection;
import java.util.List;

import static com.infoclinika.mssharing.model.read.TrashReader.TrashLine;
import static com.infoclinika.mssharing.model.read.TrashReader.TrashLineShort;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Elena Kurilina
 */
@Controller
@RequestMapping("/trash")
public class TrashController extends PagedItemsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrashController.class);

    @Inject
    private TrashReader trashReader;
    @Inject
    private InstrumentManagement instrumentManagement;
    @Inject
    private StudyManagement studyManagement;
    @Inject
    private LabHeadManagement labHeadManagement;

    @ResponseBody
    @RequestMapping(value = "/list")
    public Collection<TrashLine> list(Principal principal) {
        final long actor = getUserId(principal);

        return trashReader.readByOwnerOrLabHead(actor);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/restoreFile")
    public void restoreFile(Principal principal, @RequestParam List<Long> itemIds) {
        final long actor = getUserId(principal);
        for (Long itemId : itemIds) {
            instrumentManagement.restoreFile(actor, itemId);
        }
        LOGGER.debug("File was restored {}", actor);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/restoreExperiment")
    public void restoreExperiment(Principal principal, @RequestParam List<Long> itemIds) {
        final long actor = getUserId(principal);
        for (Long itemId : itemIds) {
            studyManagement.restoreExperiment(actor, itemId);
        }
        LOGGER.debug("Experiment was restored {}", actor);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/restoreProject")
    public void restoreProject(Principal principal, @RequestParam List<Long> itemIds) {
        final long actor = getUserId(principal);
        for (Long itemId : itemIds) {
            studyManagement.restoreProject(actor, itemId);
        }
        LOGGER.debug("Project was restored {}", actor);
    }

    @ResponseBody
    @RequestMapping(value = "/readNotRestorableItems")
    public ReadNotRestorableItemsResponse readNotRestorableItems(
        Principal principal,
        ReadNotRestorableItemsRequest request
    ) {
        final long actor = getUserId(principal);
        ImmutableSet<TrashLineShort> projects = trashReader.readNotRestorableProjects(actor, request.getProjectIds());
        ImmutableSet<TrashLineShort> experiments = trashReader.readNotRestorableExperiments(
            actor,
            request.getExperimentIds()
        );
        ImmutableSet<TrashLineShort> files = trashReader.readNotRestorableFiles(actor, request.getFileIds());

        return new ReadNotRestorableItemsResponse(projects, experiments, files);
    }
}
