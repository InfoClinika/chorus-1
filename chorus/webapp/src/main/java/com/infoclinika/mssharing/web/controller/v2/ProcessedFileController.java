package com.infoclinika.mssharing.web.controller.v2;

import com.infoclinika.mssharing.model.internal.helper.ProcessedFileService;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import com.infoclinika.mssharing.web.controller.ErrorHandler;
import com.infoclinika.mssharing.web.controller.response.SuccessErrorResponse;
import com.infoclinika.mssharing.web.controller.v2.dto.ProcessedFileDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

/**
 * @author slava
 *     on 5/31/17.
 */
@RestController
@RequestMapping(value = "/experiment/{experimentId}/processed-files")
@Api(tags = {"processed files"})
public class ProcessedFileController extends ErrorHandler {

    @Inject
    private ProcessedFileService processedFileService;

    @ApiOperation(value = "Creates a new processed file with provided filename and return an ID of the file",
        response = Long.class, httpMethod = "POST", tags = {"processed files"})
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Long create(@PathVariable final long experimentId, @RequestBody ProcessedFileDto processingFileDto) {
        final long userId = RichUser.getCurrentUserId();

        return processedFileService.create(userId, experimentId, processingFileDto.getFilename());
    }

    @ApiOperation(value = "Generates and returns temporary download link for the file specified by {fileId}",
        response = String.class, httpMethod = "GET", tags = {"processed files"})
    @RequestMapping(value = "/{fileId}", method = RequestMethod.GET)
    public String get(@PathVariable final long experimentId, @PathVariable final long fileId) {
        final long userId = RichUser.getCurrentUserId();

        return processedFileService.get(userId, experimentId, fileId);
    }

    @ApiOperation(value = "Specifies that the content of the file with ID {fileId} has been successfully uploaded",
        response = Void.class, httpMethod = "PATCH", tags = {"processed files"})
    @RequestMapping(value = "/{fileId}", method = RequestMethod.PATCH)
    @ResponseBody
    public SuccessErrorResponse done(@PathVariable final long experimentId, @PathVariable final long fileId) {
        final long userId = RichUser.getCurrentUserId();
        try {
            processedFileService.uploadDone(userId, experimentId, fileId);
            return SuccessErrorResponse.success();
        } catch (Exception ex) {
            return SuccessErrorResponse.error(ex.getMessage());
        }
    }
}
