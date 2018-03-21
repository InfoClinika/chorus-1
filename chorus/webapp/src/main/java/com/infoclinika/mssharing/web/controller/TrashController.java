package com.infoclinika.mssharing.web.controller;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.read.TrashReader;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.model.write.LabHeadManagement;
import com.infoclinika.mssharing.model.write.StudyManagement;
import com.infoclinika.mssharing.web.controller.request.ReadNotRestorableItemsRequest;
import com.infoclinika.mssharing.web.controller.response.ReadNotRestorableItemsResponse;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Collection;
import java.util.List;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Elena Kurilina
 */
@Controller
@RequestMapping("/trash")
public class TrashController extends PagedItemsController {
    private static final Logger LOG = Logger.getLogger(TrashController.class);

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
    public Collection<TrashReader.TrashLine> list(Principal principal) {
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
        LOG.debug("File was restored " + actor);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/restoreExperiment")
    public void restoreExperiment(Principal principal, @RequestParam List<Long> itemIds) {
        final long actor = getUserId(principal);
        for (Long itemId : itemIds) {
            studyManagement.restoreExperiment(actor, itemId);
        }
        LOG.debug("Experiment was restored " + actor);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/restoreProject")
    public void restoreProject(Principal principal, @RequestParam List<Long> itemIds) {
        final long actor = getUserId(principal);
        for (Long itemId : itemIds) {
            studyManagement.restoreProject(actor, itemId);
        }
        LOG.debug("Project was restored " + actor);
    }

    @ResponseBody
    @RequestMapping(value = "/readNotRestorableItems")
    public ReadNotRestorableItemsResponse readNotRestorableItems(Principal principal, ReadNotRestorableItemsRequest request) {
        final long actor = getUserId(principal);
        ImmutableSet<TrashReader.TrashLineShort> projects = trashReader.readNotRestorableProjects(actor, request.getProjectIds());
        ImmutableSet<TrashReader.TrashLineShort> experiments = trashReader.readNotRestorableExperiments(actor, request.getExperimentIds());
        ImmutableSet<TrashReader.TrashLineShort> files = trashReader.readNotRestorableFiles(actor, request.getFileIds());
        return new ReadNotRestorableItemsResponse(projects, experiments, files);
    }

}
