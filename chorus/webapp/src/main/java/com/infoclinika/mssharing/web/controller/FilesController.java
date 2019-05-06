package com.infoclinika.mssharing.web.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import com.infoclinika.mssharing.dto.FileDescription;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.read.AdministrationToolsReader;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.model.write.*;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.common.items.VendorItem;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.web.ResourceDeniedException;
import com.infoclinika.mssharing.web.controller.request.BulkFileLabelUpdateRequest;
import com.infoclinika.mssharing.web.controller.request.BulkFileUpdateSpeciesRequest;
import com.infoclinika.mssharing.web.controller.request.FileOperationRequest;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import com.infoclinika.mssharing.web.rest.FilesReadyToUploadRequest;
import com.infoclinika.mssharing.web.rest.FilesReadyToUploadResponse;
import com.infoclinika.mssharing.web.uploader.FileUploadHelper;
import com.infoclinika.util.FilenameUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getCurrentUserId;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Oleksii Tymchenko
 */
@Controller
@RequestMapping("/files")
@Api(tags = {"files"})
public class FilesController extends PagedItemsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilesController.class);
    private static final String USER = "User ";
    private static final String READING_FILES_BY_INSTRUMENT_WITH_ID = "Reading files by instrument with ID: ";

    @Inject
    private DashboardReader dashboardReader;

    @Inject
    private DetailsReader detailsReader;

    @Inject
    private InstrumentManagement instrumentManagement;

    @Inject
    private StudyManagement studyManagement;

    @Inject
    private AdministrationToolsReader administrationToolsReader;

    @Inject
    private FileOperationsManager fileOperationsManager;

    public FilesController() {
    }

    @ApiOperation(value = "Get lab files by {labId} ", tags = {"files"})
    @RequestMapping(value = "/bylab/{labId}", method = RequestMethod.GET)
    @ResponseBody
    public Set<FileLine> myLabFiles(@PathVariable("labId") long labId, Principal principal) {

        final long userId = getUserId(principal);
        //this is basically the same as just my files for now
        return dashboardReader.readFilesByLab(userId, labId);
    }

    @RequestMapping(value = "paged/bylab/{labId}", method = RequestMethod.POST)
    @ResponseBody
    public PagedItem<FileLine> myLabFiles(
        @PathVariable("labId") long labId,
        @RequestBody PagedFileRequest request,
        Principal principal
    ) {

        final long userId = getUserId(principal);

        return dashboardReader.readFilesByLab(userId, labId, createPagedInfo(request.page, request.items,
            request.sortingField, request.asc, request.filterQuery, request.advancedFilter
        ));
    }

    @RequestMapping(value = "/{filter}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Set<FileLine> getFiles(@PathVariable("filter") Filter filter, Principal principal) {
        final long userId = getUserId(principal);
        return dashboardReader.readFiles(userId, filter);
    }

    @RequestMapping(value = "/paged/{filter}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public PagedItem<FileLine> getFiles(
        @PathVariable("filter") Filter filter,
        @RequestBody PagedFileRequest request,
        Principal principal
    ) {
        final long userId = getUserId(principal);
        return dashboardReader.readFiles(userId, filter, createPagedInfo(request.page, request.items,
            request.sortingField, request.asc, request.filterQuery, request.advancedFilter
        ));
    }

    @RequestMapping(value = "/paged/by-study/{studyType}/{filter}", method = RequestMethod.POST, produces =
        "application/json")
    @ResponseBody
    public PagedItem<FileLine> getFiles(
        @PathVariable("filter") Filter filter,
        @PathVariable("studyType") String studyType,
        @RequestBody PagedFileRequest rq
    ) {
        return dashboardReader.readFiles(getCurrentUserId(), filter, createPagedInfoByStudyType(rq.page, rq.items,
            rq.sortingField, rq.asc, studyType, rq.filterQuery, rq.advancedFilter
        ));
    }

    @ApiOperation(value = "Get experiment files by {experiment} id", tags = {"files"})
    @RequestMapping(value = "/by-experiment/{experiment}", method = RequestMethod.GET)
    @ResponseBody
    public Set<FileLine> getExperimentFiles(Principal principal, @PathVariable long experiment) {
        try {
            return dashboardReader.readFilesByExperiment(getUserId(principal), experiment);
        } catch (AccessDenied e) {
            throw new ResourceDeniedException(e.getMessage());
        }
    }

    @RequestMapping(value = "/paged/by-experiment/{experiment}", method = RequestMethod.POST)
    @ResponseBody
    public PagedItem<FileLine> getExperimentFiles(
        @PathVariable long experiment,
        @RequestBody PagedFileRequest request, Principal principal
    ) {
        try {
            return dashboardReader.readFilesByExperiment(getUserId(principal), experiment,
                createPagedInfo(request.page, request.items, request.sortingField, request.asc, request.filterQuery,
                    request.advancedFilter
                )
            );
        } catch (AccessDenied e) {
            throw new ResourceDeniedException(e.getMessage());
        }
    }

    @ApiOperation(value = "Get files by instrument {id}", tags = {"files"})
    @RequestMapping(value = "/my/instrument/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Set<FileLine> filesByInstrument(@PathVariable("id") final Long instrumentId, Principal principal) {

        LOGGER.debug("Reading files by instrument with ID: {}", instrumentId);
        final long userId = getUserId(principal);
        return dashboardReader.readFilesByInstrument(userId, instrumentId);
    }

    @RequestMapping(value = "paged/my/instrument/{id}", method = RequestMethod.POST)
    @ResponseBody
    public PagedItem<FileLine> filesByInstrument(
        @PathVariable("id") final Long instrumentId,
        @RequestBody PagedFileRequest request,
        Principal principal
    ) {

        LOGGER.debug("Reading files by instrument with ID: {}", instrumentId);
        final long userId = getUserId(principal);
        return dashboardReader.readFilesByInstrument(userId, instrumentId, createPagedInfo(request.page,
            request.items, request.sortingField, request.asc, request.filterQuery, request.advancedFilter
        ));
    }

    @RequestMapping(value = "/isReadyToUpload", method = RequestMethod.POST)
    @ResponseBody
    public ValueResponse<FilesReadyToUploadResponse> isReadyToUpload(
        @RequestBody FilesReadyToUploadRequest request,
        Principal principal
    ) {
        final long instrumentId = request.instrumentId;
        LOGGER.debug("Checking if files are ready to upload. Instrument: {}", instrumentId);

        final InstrumentItem instrument = dashboardReader.readInstrument(instrumentId);
        final VendorItem vendor = instrument.vendor;

        final FileDescription[] fileDescriptions = FileUploadHelper.filesReadyToUpload(
            getUserId(principal),
            instrumentId,
            vendor,
            request.fileDescriptions,
            instrumentManagement,
            dashboardReader
        );

        final FilesReadyToUploadResponse response = new FilesReadyToUploadResponse();
        response.fileDescriptions = fileDescriptions;

        return new ValueResponse<>(response);
    }

    @RequestMapping(value = "/details/{id}", method = RequestMethod.GET)
    @ResponseBody
    public FileItem fileDetails(@PathVariable final Long id, Principal principal) {
        try {
            return detailsReader.readFile(getUserId(principal), id);
        } catch (AccessDenied e) {
            throw new ResourceDeniedException(e.getMessage());
        }
    }

    @RequestMapping(value = "/detailsWithConditions/{experimentId}/{id}", method = RequestMethod.GET)
    @ResponseBody
    public FileItem fileDetailsWithConditions(
        @PathVariable final Long id,
        @PathVariable final Long experimentId,
        Principal principal
    ) {
        try {
            return detailsReader.readFileDetailsWithConditions(getUserId(principal), id, experimentId);
        } catch (AccessDenied e) {
            throw new ResourceDeniedException(e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void updateFile(
        @RequestBody FileOperationRequest fileOperationRequest,
        Principal principal
    ) {
        instrumentManagement.setLabels(getUserId(principal), fileOperationRequest.getFileId(),
            fileOperationRequest.getLabels()
        );
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removeFiles(@RequestParam List<Long> files, Principal principal) {
        instrumentManagement.moveFilesToTrash(getUserId(principal), files);
    }

    @RequestMapping(value = "/delete-permanently", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removePermanently(@RequestParam Set<Long> files, Principal principal) {
        instrumentManagement.removeFilesPermanently(getUserId(principal), files);
    }

    @RequestMapping(value = "/bulk/labels", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void bulkUpdateLabels(@RequestBody BulkFileLabelUpdateRequest request, Principal principal) {
        final Set<Long> fileIds = new HashSet<Long>(request.getFileIds());
        instrumentManagement.bulkSetLabels(getUserId(principal), fileIds, request.getNewValue(),
            request.isAppendLabels()
        );
    }

    @RequestMapping(value = "/bulk/species", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void bulkUpdateSpecies(@RequestBody BulkFileUpdateSpeciesRequest request, Principal principal) {
        final Set<Long> fileIds = new HashSet<Long>(request.getFileIds());
        instrumentManagement.bulkSetSpecies(getUserId(principal), fileIds, request.getNewValue());
    }

    @RequestMapping(value = "/charts/url", method = RequestMethod.GET)
    @ResponseBody
    public Object getChartUrlForFiles(
        @RequestParam(value = "fileIds") String[] rawIds,
        Principal principal
    ) {
        throw new RuntimeException();
    }

    @ApiOperation(
        value = "Archive file/files to Archive storage. Available only for Enterprise accounts",
        tags = {"files"})
    @RequestMapping(value = "/archive", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void archiveFiles(@RequestBody FilesRequest request, Principal principal) {
        LOGGER.info("User {} requested archive files: {}", getUserId(principal), request);
        fileOperationsManager.markFilesToArchive(getUserId(principal), copyOf(request.files));
    }

    @ApiOperation(
        value = "Unarchive file/files to Archive storage. Available only for Enterprise accounts",
        tags = {"files"})
    @RequestMapping(value = "/un-archive", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void unarchiveFiles(@RequestBody FilesRequest request, Principal principal) {
        LOGGER.info("User {} requested Un-archive files: {}", getUserId(principal), request);
        fileOperationsManager.markFilesToUnarchive(getUserId(principal), copyOf(request.files));
    }

    @RequestMapping(value = "/compoundsDownload/{fileId}", method = RequestMethod.GET)
    public void compoundsDownload(@PathVariable long fileId, Principal principal, HttpServletResponse response) {
        LOGGER.info("User {} requested compounds for a file: {}", getUserId(principal), fileId);

        final long actor = getUserId(principal);
        final StringBuffer compoundsBuffer = dashboardReader.readFileCompoundsAsCSV(actor, Sets.newHashSet(fileId));

        returnCompounds(actor, fileId, response, compoundsBuffer);
    }

    @RequestMapping(value = "/compoundsDownload/raw/{fileId}", method = RequestMethod.GET)
    public void compoundsDownloadForRawFile(
        @PathVariable long fileId,
        Principal principal,
        HttpServletResponse response
    ) {
        LOGGER.info("User {}  requested compounds for a raw file: {}", getUserId(principal), fileId);

        final long actor = getUserId(principal);
        final StringBuffer compoundsBuffer = dashboardReader.readRawFileCompoundsAsCSV(actor, Sets.newHashSet(fileId));

        returnCompounds(actor, fileId, response, compoundsBuffer);
    }

    private void returnCompounds(long actor, long fileId, HttpServletResponse response, StringBuffer compoundsBuffer) {
        try {
            final FileItem file = detailsReader.readFile(actor, fileId);
            final String validFileName = getFileName(file);
            ServletOutputStream out = response.getOutputStream();

            response.setContentType("text/csv");
            response.setHeader(
                "Content-Disposition",
                "attachment;filename=" + fileId + "-" + validFileName + "-compounds.csv"
            );

            InputStream in = new ByteArrayInputStream(compoundsBuffer.toString().getBytes("UTF-8"));
            IOUtils.copy(in, out);

            in.close();
            out.flush();
            out.close();
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException("Can't download compounds for files", e);
        }
    }

    private String getFileName(FileItem fileItem) {
        final String nameWithoutForbiddenCharacters = FilenameUtil.replaceForbiddenPathCharacters(fileItem.name);
        return FilenameUtil.replaceWhiteSpacesWithUnderscores(nameWithoutForbiddenCharacters);
    }

    private static class SetQcStatusRequest {
        public Boolean qcStatus;
    }

    private static class RemoveTranslationDataRequest {
        public List<Long> files;
        public Long lab;

        @Override
        public String toString() {
            return "RemoveTranslationDataRequest{" +
                "files=" + files +
                ", lab=" + lab +
                '}';
        }
    }

    private static class FilesRequest {
        public List<Long> files;
        public long lab;
        public boolean metadataOnly;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("files", files)
                .add("lab", lab)
                .add("metadataOnly", metadataOnly)
                .toString();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PagedFileRequest {
        public int page;
        public int items;
        public String sortingField;
        public boolean asc;
        public String filterQuery;// nullable
        public PaginationItems.AdvancedFilterQueryParams advancedFilter;// nullable
    }
}
