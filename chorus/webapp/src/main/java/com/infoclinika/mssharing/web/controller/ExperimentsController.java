package com.infoclinika.mssharing.web.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams;
import com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams.AdvancedFilterPredicateItem;
import com.infoclinika.mssharing.model.helper.ExperimentCreationHelper;
import com.infoclinika.mssharing.model.helper.ExperimentCreationHelper.ExperimentLabelItem;
import com.infoclinika.mssharing.model.helper.ExperimentCreationHelper.ExperimentLabelTypeItem;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.read.AdministrationToolsReader;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.ExperimentLine;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.write.ExperimentInfo;
import com.infoclinika.mssharing.model.write.FileOperationsManager;
import com.infoclinika.mssharing.model.write.StudyManagement;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.common.items.NamedItem;
import com.infoclinika.mssharing.platform.model.helper.ExperimentCreationHelperTemplate.ExperimentTypeItem;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ExperimentShortInfo;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.Restriction;
import com.infoclinika.mssharing.web.controller.request.ExperimentDetails;
import com.infoclinika.mssharing.web.controller.request.SameSpeciesCheckRequest;
import com.infoclinika.mssharing.web.controller.response.DetailsResponse;
import com.infoclinika.mssharing.web.controller.response.ExperimentIdResponse;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams.AdvancedFilterPredicateItem.AdvancedFilterOperator.*;
import static com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ShortExperimentFileItem;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;
import static com.infoclinika.mssharing.web.transform.ExperimentTransformer.TO_EXPERIMENT_DETAILS;
import static com.infoclinika.mssharing.web.transform.ExperimentTransformer.TO_EXPERIMENT_INFO;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

/**
 * @author Pavel Kaplin
 */
@Controller
@RequestMapping("/experiments")
public class ExperimentsController extends PagedItemsController {

    public static final String DATE_FILTER_PROPERTY_NAME = "uploadDate";
    public static final String CONTAINS_FILTER_PROPERTY_NAME = "name";
    private Logger LOG = Logger.getLogger(ExperimentsController.class);

    @Inject
    private DashboardReader dashboardReader;
    @Inject
    private DetailsReader detailsReader;
    @Inject
    private ExperimentCreationHelper experimentCreationHelper;
    @Inject
    private StudyManagement studyManagement;
    @Inject
    private AdministrationToolsReader administrationToolsReader;
    @Inject
    private FileOperationsManager fileOperationsManager;
    @Inject
    private RuleValidator validator;

    @RequestMapping(value = "/details/{id}", method = RequestMethod.GET)
    @ResponseBody
    public DetailsResponse<ExperimentDetails> getDetails(@PathVariable final Long id, Principal principal) {
        ExperimentItem experiment = detailsReader.readExperiment(getUserId(principal), id);
        final ExperimentDetails experimentDetails = TO_EXPERIMENT_DETAILS.apply(experiment);
        return DetailsResponse.ok(experimentDetails);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ExperimentIdResponse save(@RequestBody ExperimentDetails experiment, Principal principal) {
        LOG.info("Saving an experiment: " + experiment);
        long userId = getUserId(principal);
        long experimentId;
        // decide which lab is going to pay
        if (experiment.lab != null) {
            experiment.billLab = experiment.lab;
        }

        final ExperimentInfo experimentInfo = TO_EXPERIMENT_INFO.apply(experiment);

        if (experiment.id == null) {
            experimentId = studyManagement.createExperiment(userId, experimentInfo);
        } else {
            experimentId = experiment.id;
            studyManagement.updateExperiment(userId, experimentId, experimentInfo);
        }
        return new ExperimentIdResponse(experimentId);
    }

    @RequestMapping("/new/restriction")
    @ResponseBody
    public Restriction getRestrictionForInstrument(@RequestParam Long instrumentId) {
        return experimentCreationHelper.getRestrictionForInstrument(instrumentId);
    }

    @RequestMapping("/new/instrumentModels")
    @ResponseBody
    public List<DictionaryItem> getInstrumentModels(Principal principal,
                                                    @RequestParam(required = false) Long lab,
                                                    @RequestParam(required = false) Long technologyType,
                                                    @RequestParam(required = false) Long vendor,
                                                    @RequestParam(required = false) Long instrumentType) {
        if (instrumentType == null) {
            return experimentCreationHelper.availableInstrumentModels(getUserId(principal), lab, technologyType, vendor);
        } else {
            return experimentCreationHelper.availableInstrumentModels(getUserId(principal), lab, technologyType, vendor, instrumentType);
        }
    }

    @RequestMapping("/new/instruments")
    @ResponseBody
    public List<InstrumentItem> getInstruments(@RequestParam long instrumentModel, Principal principal) {
        return experimentCreationHelper.availableInstrumentsByModel(getUserId(principal), instrumentModel);
    }

    @RequestMapping("/new/instrumentTypes")
    @ResponseBody
    public List<DictionaryItem> getInstruments(Principal principal,
                                               @RequestParam(required = false) Long lab,
                                               @RequestParam(required = false) Long technologyType,
                                               @RequestParam(required = false) Long vendor) {
        return experimentCreationHelper.availableInstrumentTypes(getUserId(principal), lab, technologyType, vendor);
    }

    @RequestMapping(value = "/new/files", method = RequestMethod.GET)
    @ResponseBody
    public List<FileItem> getFiles(@RequestParam long specie,
                                   @RequestParam(required = false) Long instrument,
                                   @RequestParam(required = false) Long model,
                                   @RequestParam(required = false) Long lab,
                                   Principal principal) {
        if (instrument != null) {
            return experimentCreationHelper.availableFilesByInstrument(getUserId(principal), specie, instrument);
        }
        return experimentCreationHelper.availableFilesByInstrumentModel(getUserId(principal), specie, model, lab);
    }

    @RequestMapping(value = "/new/files/exist", method = RequestMethod.GET)
    @ResponseBody
    public ValueResponse<Boolean> checkIfFilesExist(@RequestParam(required = false) long species,
                                                    @RequestParam(required = false) Long instrument,
                                                    @RequestParam(required = false) Long model,
                                                    @RequestParam(required = false) Long lab,
                                                    Principal principal) {

        if (instrument != null) {
            return new ValueResponse<>(experimentCreationHelper.hasFilesByInstrument(getUserId(principal), species, instrument));
        } else {
            return new ValueResponse<>(experimentCreationHelper.hasFilesByModel(getUserId(principal), species, model, lab));
        }
    }

    @RequestMapping(value = "/paged/new/files", method = RequestMethod.GET)
    @ResponseBody
    public PagedItem<FileItem> getPagedFiles(@RequestParam long specie,
                                             @RequestParam(required = false) Long instrument,
                                             @RequestParam(required = false) Long model, @RequestParam(required = false) Long lab,
                                             @RequestParam(required = false) String fromDateFilterQuery, @RequestParam(required = false) String toDateFilterQuery,
                                             @RequestParam int page, @RequestParam int items, @RequestParam String sortingField,
                                             @RequestParam boolean asc, @RequestParam(required = false) String filterQuery,
                                             Principal principal) {

        final List<AdvancedFilterPredicateItem> predicates = new ArrayList<>();

        if (fromDateFilterQuery != null && !fromDateFilterQuery.isEmpty()) {
            predicates.add(new AdvancedFilterPredicateItem(DATE_FILTER_PROPERTY_NAME, fromDateFilterQuery,
                    IS_ON_AND_AFTER));
        }

        if (toDateFilterQuery != null && !toDateFilterQuery.isEmpty()) {
            predicates.add(new AdvancedFilterPredicateItem(DATE_FILTER_PROPERTY_NAME, toDateFilterQuery,
                    IS_ON_OR_BEFORE));
        }

        if (filterQuery != null && !filterQuery.isEmpty()) {
            predicates.add(new AdvancedFilterPredicateItem(CONTAINS_FILTER_PROPERTY_NAME, filterQuery,
                    CONTAINS));
        }

        AdvancedFilterQueryParams advancedFilter = null;
        if (!predicates.isEmpty()) {
            advancedFilter = new AdvancedFilterQueryParams(true, predicates);
        }
        final PagedItemInfo pagedInfo = createPagedInfo(page, items, sortingField, asc, filterQuery, advancedFilter);

        if (instrument != null) {
            return experimentCreationHelper.availableFilesByInstrument(getUserId(principal), specie, instrument, pagedInfo);
        }

        return experimentCreationHelper.availableFilesByInstrumentModel(getUserId(principal), specie, model, lab, pagedInfo);
    }

    @RequestMapping(value = "/details/{experiment}/files", method = RequestMethod.GET)
    @ResponseBody
    public SortedSet<FileItem> getExperimentFiles(@PathVariable long experiment, Principal principal) {
        return dashboardReader.readFileItemsByExperiment(getUserId(principal), experiment);
    }

    @RequestMapping("/new/experimentTypes")
    @ResponseBody
    public ImmutableSortedSet<ExperimentTypeItem> getExperimentTypes() {
        return experimentCreationHelper.experimentTypes();
    }

    @RequestMapping("/new/species")
    @ResponseBody
    public ImmutableSet<DictionaryItem> species() {
        return experimentCreationHelper.species();
    }

    @RequestMapping("/new/labels")
    @ResponseBody
    public ImmutableSet<ExperimentLabelItem> labels() {
        return experimentCreationHelper.experimentLabels();
    }

    @RequestMapping("/new/labelTypes")
    @ResponseBody
    public ImmutableSet<ExperimentLabelTypeItem> labelTypes() {
        return experimentCreationHelper.experimentLabelTypes();
    }

    @RequestMapping(value = "/new/species/{id}", method = RequestMethod.GET)
    @ResponseBody
    public DictionaryItem getSpecie(@PathVariable long id) {
        return experimentCreationHelper.specie(id);
    }

    @RequestMapping(value = "/{filter}", method = RequestMethod.GET)
    @ResponseBody
    public Iterable<ExperimentLine> getExperiments(
            @PathVariable("filter") final Filter filter, Principal principal) {
        return newArrayList(dashboardReader.readExperiments(getUserId(principal), filter));
    }

    @RequestMapping(value = "/usedInOtherExperiments/files/{id}")
    @ResponseBody
    public List<ShortExperimentFileItem> getFilesUsedInOtherExperiments(@PathVariable long id, Principal principal) {
        return detailsReader.readFilesInOtherExperiments(getUserId(principal), id);
    }

    @RequestMapping(value = "/haveSameSpecies/files", method = RequestMethod.POST)
    @ResponseBody
    public ValueResponse<Boolean> doFilesHaveSameSpecie(@RequestBody SameSpeciesCheckRequest request) {
        return new ValueResponse<>(validator.canSaveExperimentWithSpecies(request.specieId, request.fileIds));
    }

    @RequestMapping(value = "/by-project/{id}", method = RequestMethod.POST)
    @ResponseBody
    public PagedItem<ExperimentLine> getPagedExperimentsByProject(@PathVariable("id") final long id,
                                                                  @RequestBody PagedExperimentRequest request,
                                                                  Principal principal) {
        return dashboardReader.readPagedExperimentsByProject(getUserId(principal), id, createPagedInfo(request.page, request.items, request.sortingField, request.asc, request.filterQuery, request.advancedFilter));
    }

    @RequestMapping(value = "/paged/{filter}", method = RequestMethod.POST)
    @ResponseBody
    public PagedItem<ExperimentLine> getPagedExperiments(@PathVariable Filter filter,
                                                         @RequestBody PagedExperimentRequest request,
                                                         Principal principal) {
        return dashboardReader.readExperiments(getUserId(principal), filter, createPagedInfo(request.page, request.items, request.sortingField, request.asc, request.filterQuery, request.advancedFilter));
    }

    @RequestMapping(value = "/paged", method = RequestMethod.POST)
    @ResponseBody
    public PagedItem<ExperimentLine> getPagedExperiments(@RequestBody PagedExperimentRequest request,
                                                         Principal principal) {
        final long actor = getUserId(principal);
        final PagedItemInfo pagedInfo = createPagedInfo(request.page, request.items, request.sortingField, request.asc, request.filterQuery, request.advancedFilter);
        return dashboardReader.readExperimentsByLab(actor, request.labId, pagedInfo);
    }

    //TODO: Remove "filter" path variable
    @RequestMapping(value = "/{filter}/shortDetails", method = RequestMethod.GET)
    @ResponseBody
    public Set<NamedItem> getExperimentShortItems(@PathVariable final Filter filter, Principal principal) {
        return experimentCreationHelper.ownedExperiments(getUserId(principal));
    }

    @RequestMapping(method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removeExperiment(@RequestParam long experiment, @RequestParam boolean removePermanently, Principal principal) {
        if (removePermanently) {
            studyManagement.deleteExperiment(getUserId(principal), experiment);
        } else {
            studyManagement.moveExperimentToTrash(getUserId(principal), experiment);
        }
    }


    @RequestMapping(value = "/details/{experimentId}/levels", method = RequestMethod.GET)
    @ResponseBody
    public Set<DashboardReader.ExperimentLevelItem> getExperimentLevels(@PathVariable final long experimentId, Principal principal) {
        return dashboardReader.readExperimentLevels(getUserId(principal), experimentId);
    }

    @RequestMapping(value = "/details/{id}/short", method = RequestMethod.GET)
    @ResponseBody
    public ExperimentShortInfo getShortInfo(@PathVariable final Long id, Principal principal) {
        return detailsReader.readExperimentShortInfo(getUserId(principal), id);
    }

    @RequestMapping(value = "/precache/{id}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void preCacheViewers(@PathVariable("id") long id, Principal principal) {
        LOG.debug("Pre-cache viewers call arrived to the controller. Experiment ID = " + id);
        studyManagement.runPreCacheViewers(getUserId(principal), id);
    }

    @RequestMapping(value = "/moveToStorage", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void moveToStorage(@RequestParam Long id, @RequestParam Long actor) {
        fileOperationsManager.makeExperimentFilesAvailableForDownload(actor, id);
    }

    @RequestMapping(value = "/archive/{id}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void archive(@PathVariable long id, Principal principal) {
        fileOperationsManager.markExperimentFilesToArchive(getUserId(principal), id);
    }

    @RequestMapping(value = "/unarchive/{id}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void unarchive(@PathVariable long id, Principal principal) {
        fileOperationsManager.markExperimentFilesToUnarchive(getUserId(principal), id);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PagedExperimentRequest {
        public Long labId;
        public int page;
        public int items;
        public String sortingField;
        public boolean asc;
        public String filterQuery;// nullable
        public AdvancedFilterQueryParams advancedFilter;// nullable
    }
}
