package com.infoclinika.mssharing.web.controller;


import com.infoclinika.mssharing.model.AdMediaItemNotResolvableException;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.read.AdvertisementReader;
import com.infoclinika.mssharing.model.read.AdvertisementReader.AdvertisementItem;
import com.infoclinika.mssharing.model.write.AdvertisementManagement;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.web.controller.request.CompleteAdvertisementImageUploadRequest;
import com.infoclinika.mssharing.web.controller.request.StartAdvertisementImageUploadRequest;
import com.infoclinika.mssharing.web.controller.response.StartAttachmentUploadResponse;
import com.infoclinika.mssharing.web.controller.response.UploadFilePathResponse;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;
import static com.infoclinika.mssharing.web.downloader.AttachmentsDownloadHelper.encodeContentDisposition;

/**
 * @author andrii.loboda
 */
@RequestMapping("/poster")
@Controller
public class AdvertisementController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdvertisementController.class);
    private static final String USER_AGENT = "User-Agent";
    @Inject
    private AdvertisementReader advertisementReader;
    @Inject
    private AdvertisementManagement advertisementManagement;
    @Inject
    private StoredObjectPaths storedObjectPaths;
    @Inject
    private javax.inject.Provider<Date> current;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Set<AdvertisementReader.AdvertisementDetailsItem> getAdvertisementList(Principal principal) {
        return advertisementReader.readAll(getUserId(principal));
    }

    @RequestMapping(value = "/details/{id}", method = RequestMethod.GET)
    @ResponseBody
    public AdvertisementReader.AdvertisementDetailsItem getDetails(@PathVariable("id") long id, Principal principal) {
        return advertisementReader.readAdvertisement(getUserId(principal), id);
    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    @ResponseBody
    public StartAttachmentUploadResponse createAdvertisement(
        @RequestBody StartAdvertisementImageUploadRequest request,
        Principal principal
    ) {
        long attachmentId = advertisementManagement.createAdvertisement(
            getUserId(principal),
            new AdvertisementManagement.AdvertisementInfo(
                request.title,
                request.startDate,
                request.endDate,
                request.redirectLink,
                request.currentDate,
                request.filename,
                request.sizeInBytes,
                request.isEnabled
            )
        );
        return new StartAttachmentUploadResponse(request.filename, attachmentId);
    }

    @RequestMapping(value = "/updateImageUrl", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void updateAdvertisementImageInfo(
        @RequestBody final CompleteAdvertisementImageUploadRequest request,
        final Principal principal
    ) {
        LOGGER.debug("Attaching image file to the advertisement: {}", request);
        final long userId = getUserId(principal);
        advertisementManagement.specifyAdvertisementContent(userId, request.advertisementId, request.contentUrl);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public List<AdvertisementReader.AdvertisementAttachmentItem> readImageForAdvertisement(
        @PathVariable final Long id,
        Principal principal
    ) {
        return advertisementReader.readAttachment(getUserId(principal), id);
    }

    @RequestMapping(value = "/destination/{id}", method = RequestMethod.GET)
    @ResponseBody
    public UploadFilePathResponse composeAdvertisementAttachmentDestination(
        @PathVariable("id") long advertisementId,
        Principal principal
    ) {
        final NodePath nodePath = advertisementReader.readPathForImageUpload(getUserId(principal), advertisementId);
        return new UploadFilePathResponse(nodePath.getPath());
    }

    @RequestMapping(value = "/updateDetails/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public StartAttachmentUploadResponse updateAdvertisementDetails(
        @PathVariable("id") long advertisementId,
        @RequestBody StartAdvertisementImageUploadRequest adRequest,
        Principal principal
    ) {
        final AdvertisementManagement.AdvertisementInfo info =
            new AdvertisementManagement.AdvertisementInfo(adRequest.title,
                adRequest.startDate,
                adRequest.endDate,
                adRequest.redirectLink,
                adRequest.currentDate,
                adRequest.filename,
                adRequest.sizeInBytes,
                adRequest.isEnabled
            );
        advertisementManagement.updateAdvertisement(getUserId(principal), adRequest.id, info);
        return new StartAttachmentUploadResponse(adRequest.filename, adRequest.id);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteNews(@PathVariable long id, Principal principal) {
        advertisementManagement.deleteAdvertisement(getUserId(principal), id);
    }

    @RequestMapping(value = "/maxSizeInBytes", method = RequestMethod.GET)
    @ResponseBody
    public ValueResponse<Long> getMaxSize() {
        return new ValueResponse<>(advertisementManagement.getMaxAttachmentSize());
    }

    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
    @ResponseBody
    public void attachmentDownload(
        @PathVariable("id") final long attachmentId,
        HttpServletRequest request,
        HttpServletResponse response,
        Principal principal
    ) {
        try {
            final long userId = getUserId(principal);
            LOGGER.debug(
                "Got the download request for the project attachment with ID = {} from user with ID {}",
                attachmentId,
                userId
            );
            AdvertisementReader.AdvertisementImageToDownload advertisementImageToDownload =
                advertisementReader.readAdvertImageFile(userId, attachmentId);
            postAdvertImageToResponse(advertisementImageToDownload, request, response);
        } catch (IOException e) {
            LOGGER.error("Error writing file to output stream.", e);
            throw new RuntimeException("IOError writing file to output stream");
        }
    }

    @RequestMapping(value = "/postToDisplay", method = RequestMethod.GET)
    @ResponseBody
    public AdvertisementItem getAdvertToDisplay() {
        return advertisementReader.readAdvertisementToDisplay(current.get());
    }

    @RequestMapping(value = "/incrementsClickCount", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void incrementsClickCount(@RequestParam("id") final long ad) {
        LOGGER.debug("Incrementing clicks count for: {}", ad);
        advertisementManagement.incrementClickedCount(ad, current.get());
    }

    @RequestMapping(value = "/incrementsDisplayCount", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void incrementsDisplayCount(@RequestParam("id") final long ad) {
        advertisementManagement.incrementDisplayedCount(ad, current.get());
    }

    @ExceptionHandler(AdMediaItemNotResolvableException.class)
    @ResponseBody
    public void handleAdvertisementException(Exception ex, HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.sendError(404, ex.getLocalizedMessage());
    }

    private void postAdvertImageToResponse(
        AdvertisementReader.AdvertisementImageToDownload advertisementImageToDownload,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        final FileInputStream is = new FileInputStream(advertisementImageToDownload.file);

        response.setContentType(identifyContentType(advertisementImageToDownload.name));
        response.setHeader(
            "Content-Disposition",
            encodeContentDisposition(advertisementImageToDownload.name, request.getHeader(USER_AGENT))
        );
        IOUtils.copy(is, response.getOutputStream());

        //Set cookie to satisfy AJAX downloader at the client:
        //http://johnculviner.com/post/2012/03/22/Ajax-like-feature-rich-file-downloads-with-jQuery-File-Download.aspx
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");
        response.flushBuffer();
    }

    private String identifyContentType(String fileName) {
        String ext = FilenameUtils.getExtension(fileName).toLowerCase();
        String contentType = null;
        switch (ext) {
            case "jpeg":
            case "jpg":
                contentType = "image/jpeg";
                break;
            case "gif":
                contentType = "image/gif";
                break;
            case "png":
                contentType = "image/png";
                break;
            default:
                throw new IllegalStateException("There is no such content type for " + ext);
        }
        return contentType;
    }
}
