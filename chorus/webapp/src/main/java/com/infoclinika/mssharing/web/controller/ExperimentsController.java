package com.infoclinika.mssharing.web.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams;
import com.infoclinika.mssharing.model.helper.ExperimentCreationHelper;
import com.infoclinika.mssharing.model.helper.ExperimentCreationHelper.ExperimentLabelItem;
import com.infoclinika.mssharing.model.helper.ExperimentCreationHelper.ExperimentLabelTypeItem;
import com.infoclinika.mssharing.model.read.AdministrationToolsReader;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.ExperimentLine;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.write.ExperimentInfo;
import com.infoclinika.mssharing.model.write.FileOperationsManager;
import com.infoclinika.mssharing.model.write.StudyManagement;
import com.infoclinika.mssharing.model.write.ngs.NgsExperimentImporter;
import com.infoclinika.mssharing.model.write.ngs.dto.NgsExperimentImportRequest;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.*;
import com.infoclinika.mssharing.platform.model.helper.ExperimentCreationHelperTemplate.ExperimentTypeItem;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ExperimentShortInfo;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.Restriction;
import com.infoclinika.mssharing.web.controller.request.ExperimentDetails;
import com.infoclinika.mssharing.web.controller.request.PageRequest;
import com.infoclinika.mssharing.web.controller.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.IOException;
import java.security.Principal;
import java.util.*;


import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.model.read.DashboardReader.ExperimentLevelItem;
import static com.infoclinika.mssharing.model.read.DashboardReader.StorageStatus;
import static com.infoclinika.mssharing.model.read.DashboardReader.StorageStatus.*;
import static com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.ShortExperimentFileItem;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;
import static com.infoclinika.mssharing.web.transform.ExperimentTransformer.TO_EXPERIMENT_DETAILS;
import static com.infoclinika.mssharing.web.transform.ExperimentTransformer.TO_EXPERIMENT_INFO;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Pavel Kaplin
 */
@Controller
@RequestMapping("/experiments")
public class ExperimentsController extends PagedItemsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentsController.class);
    private static final EnumSet<StorageStatus> STORAGE_STATUSES = EnumSet.of(
        ARCHIVED,
        ARCHIVING_IN_PROCESS,
        UN_ARCHIVING_IN_PROCESS,
        UN_ARCHIVING_FOR_DOWNLOAD_IN_PROCESS
    );

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
    private NgsExperimentImporter ngsExperimentImporter;

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
        LOGGER.info("Saving an experiment: {}", experiment);
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
    public List<DictionaryItem> getInstrumentModels(
        Principal principal,
        @RequestParam(required = false) Long lab,
        @RequestParam(required = false) Long technologyType,
        @RequestParam(required = false) Long vendor,
        @RequestParam(required = false) Long instrumentType
    ) {
        if (instrumentType == null) {
            return experimentCreationHelper.availableInstrumentModels(
                getUserId(principal),
                lab,
                technologyType,
                vendor
            );
        } else {
            return experimentCreationHelper.availableInstrumentModels(
                getUserId(principal),
                lab,
                technologyType,
                vendor,
                instrumentType
            );
        }
    }

    @RequestMapping("/new/instruments")
    @ResponseBody
    public List<InstrumentItem> getInstruments(@RequestParam long instrumentModel, Principal principal) {
        return experimentCreationHelper.availableInstrumentsByModel(getUserId(principal), instrumentModel);
    }

    @RequestMapping("/new/instrumentTypes")
    @ResponseBody
    public List<DictionaryItem> getInstruments(
        Principal principal,
        @RequestParam(required = false) Long lab,
        @RequestParam(required = false) Long technologyType,
        @RequestParam(required = false) Long vendor
    ) {
        return experimentCreationHelper.availableInstrumentTypes(getUserId(principal), lab, technologyType, vendor);
    }

    @RequestMapping(value = "/new/files/exist", method = RequestMethod.GET)
    @ResponseBody
    public ValueResponse<Boolean> checkIfFilesExist(
        @RequestParam(required = false) Long species,
        @RequestParam(required = false) Long instrument,
        @RequestParam(required = false) Long model,
        @RequestParam(required = false) Long lab,
        Principal principal
    ) {
        return new ValueResponse<>(experimentCreationHelper.hasFilesByParams(
            getUserId(principal),
            species,
            instrument,
            model,
            lab
        ));
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
    public Set<ExperimentLabelItem> labels() {
        return experimentCreationHelper.experimentLabels();
    }

    @RequestMapping("/new/labelTypes")
    @ResponseBody
    public Set<ExperimentLabelTypeItem> labelTypes() {
        return experimentCreationHelper.experimentLabelTypes();
    }

    @RequestMapping(value = "/new/species/{id}", method = RequestMethod.GET)
    @ResponseBody
    public DictionaryItem getSpecie(@PathVariable long id) {
        return experimentCreationHelper.specie(id);
    }

    @RequestMapping(value = "/new/species/default", method = RequestMethod.GET)
    @ResponseBody
    public DictionaryItem getDefaultSpecie() {
        return experimentCreationHelper.defaultSpecie();
    }

    @RequestMapping(value = "/{filter}", method = RequestMethod.GET)
    @ResponseBody
    public Iterable<ExperimentLine> getExperiments(
        @PathVariable("filter") final Filter filter, Principal principal
    ) {
        return newArrayList(dashboardReader.readExperiments(getUserId(principal), filter));
    }

    @RequestMapping(value = "/usedInOtherExperiments/files/{id}")
    @ResponseBody
    public List<ShortExperimentFileItem> getFilesUsedInOtherExperiments(@PathVariable long id, Principal principal) {
        return detailsReader.readFilesInOtherExperiments(getUserId(principal), id);
    }

    @RequestMapping(value = "/by-project/{id}", method = RequestMethod.POST)
    @ResponseBody
    public PagedItem<ExperimentLine> getPagedExperimentsByProject(
        @PathVariable("id") final long id,
        @RequestBody PagedExperimentRequest request,
        Principal principal
    ) {
        return dashboardReader.readPagedExperimentsByProject(
            getUserId(principal),
            id,
            createPagedInfo(
                request.page,
                request.items,
                request.sortingField,
                request.asc,
                request.filterQuery,
                request.advancedFilter
            )
        );
    }

    @RequestMapping(value = "/paged/{filter}", method = RequestMethod.POST)
    @ResponseBody
    public PagedItem<ExperimentLine> getPagedExperiments(
        @PathVariable Filter filter,
        @RequestBody PagedExperimentRequest request,
        Principal principal
    ) {
        return dashboardReader.readExperiments(
            getUserId(principal),
            filter,
            createPagedInfo(
                request.page,
                request.items,
                request.sortingField,
                request.asc,
                request.filterQuery,
                request.advancedFilter
            )
        );
    }

    @RequestMapping(value = "/paged", method = RequestMethod.POST)
    @ResponseBody
    public PagedItem<ExperimentLine> getPagedExperiments(
        @RequestBody PagedExperimentRequest request,
        Principal principal
    ) {
        final long actor = getUserId(principal);
        final PagedItemInfo pagedInfo = createPagedInfo(
            request.page,
            request.items,
            request.sortingField,
            request.asc,
            request.filterQuery,
            request.advancedFilter
        );
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
    public void removeExperiment(
        @RequestParam long experiment,
        @RequestParam boolean removePermanently,
        Principal principal
    ) {
        if (removePermanently) {
            studyManagement.deleteExperiment(getUserId(principal), experiment);
        } else {
            studyManagement.moveExperimentToTrash(getUserId(principal), experiment);
        }
    }


    @RequestMapping(value = "/details/{experimentId}/levels", method = RequestMethod.GET)
    @ResponseBody
    public Set<ExperimentLevelItem> getExperimentLevels(
        @PathVariable final long experimentId,
        Principal principal
    ) {
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
        LOGGER.debug("Pre-cache viewers call arrived to the controller. Experiment ID = {}", id);
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

    @RequestMapping(value = "/markAsFailed/{id}/{failed}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void markAsFailed(@PathVariable long id, @PathVariable boolean failed, Principal principal) {
        LOGGER.info("Changed status of experiment: " + id + " on failed: " + failed);

        dashboardReader.setExperimentFailed(getUserId(principal), id, failed);
    }

    @RequestMapping(value = "/importNGSExperiment", method = RequestMethod.POST)
    @ResponseBody
    public SuccessErrorResponse importNGSExperiment(ImportNgsExperimentRequest request, Principal principal)
        throws IOException {

        final long actor = getUserId(principal);
        final byte[] fileAsBytes = request.getFile().getBytes();
        final NgsExperimentImportRequest importRequest = new NgsExperimentImportRequest(
            request.getName(),
            request.getProject(),
            request.getLab(),
            request.getInstrument(),
            fileAsBytes
        );

        try {
            final long experimentId = ngsExperimentImporter.makeImport(actor, importRequest);

            return new SuccessErrorResponse(
                null,
                "NGS Experiment has been successfully imported. Experiment ID: " + experimentId
            );
        } catch (Exception ex) {
            return new SuccessErrorResponse(ex.getMessage(), null);
        }
    }

    @RequestMapping(value = "/ngs/getLibraryPrepTypes", method = GET)
    @ResponseBody
    public ValueResponse<List<DictItem<String, String>>> getLibraryPrepTypes() {
        final List<DictItem<String, String>> ngsLibraryPrepTypes = experimentCreationHelper.getNgsLibraryPrepTypes();

        return new ValueResponse<>(ngsLibraryPrepTypes);
    }

    @RequestMapping(value = "/ngs/getExperimentPrepMethodsByType", method = GET)
    @ResponseBody
    public ValueResponse<List<DictItem<Integer, String>>> getExperimentPrepMethodsByType(
        @RequestParam("typeId") int typeId
    ) {
        final List<DictItem<Integer, String>> ngsExperimentPrepMethods =
            experimentCreationHelper.getNgsExperimentPrepMethodsByExperimentType(typeId);

        return new ValueResponse<>(ngsExperimentPrepMethods);
    }

    @RequestMapping(value = "/ngs/getNtExtractionMethods", method = GET)
    @ResponseBody
    public ValueResponse<List<DictItem<Long, String>>> getNtExtractionMethods() {
        final List<DictItem<Long, String>> ntExtractionMethods = experimentCreationHelper.getNtExtractionMethods();

        return new ValueResponse<>(ntExtractionMethods);
    }

    @RequestMapping(value = "/ngs/getNgsExperimentTypes", method = GET)
    @ResponseBody
    public ValueResponse<List<DictItem<Integer, String>>> getNgsExperimentTypes() {
        final List<DictItem<Integer, String>> ngsExperimentTypes = experimentCreationHelper.getNgsExperimentTypes();

        return new ValueResponse<>(ngsExperimentTypes);
    }

    private static class RetranslateExperimentsRequest {
        public List<Long> experiments;
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

    public static class ImportNgsExperimentRequest {

        private MultipartFile file;
        private String name;
        private long project;
        private long lab;
        private long instrument;

        public MultipartFile getFile() {
            return file;
        }

        public void setFile(MultipartFile file) {
            this.file = file;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getProject() {
            return project;
        }

        public void setProject(long project) {
            this.project = project;
        }

        public long getLab() {
            return lab;
        }

        public void setLab(long lab) {
            this.lab = lab;
        }

        public long getInstrument() {
            return instrument;
        }

        public void setInstrument(long instrument) {
            this.instrument = instrument;
        }
    }
}
