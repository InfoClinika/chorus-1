package com.infoclinika.mssharing.web.controller.v2;

import com.infoclinika.mssharing.platform.web.security.RichUser;
import com.infoclinika.mssharing.web.controller.v2.service.UploadFileService;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;

@RestController
@RequestMapping("/v2/experiment/{experimentId}/processed-files")
public class ProcessedFilesController {
    private static final Logger LOGGER = Logger.getLogger(ProcessedFilesController.class);


    @Inject
    @Named(value = "uploadFileService")
    private UploadFileService uploadFileService;


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleAccessDeniedExeption(Exception ex, WebRequest request){
        return new ResponseEntity<>(ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED);

    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Object> handleMultipartError(Exception ex, MultipartException e){
        LOGGER.trace(e.getLocalizedMessage());
        return new ResponseEntity<>(e.getLocalizedMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(value ="", method = RequestMethod.POST, consumes = {"multipart/mixed", "multipart/form-data"})
    public ResponseEntity<?> uploadFile(Principal principal, @PathVariable("experimentId") long experimentId, @RequestParam("process-file") MultipartFile[] multipartFile) throws IOException {
        LOGGER.info("uploadFile start");
        LOGGER.info(multipartFile.length);
        for(MultipartFile multipartFile1: multipartFile){
            LOGGER.info(multipartFile1.getOriginalFilename() + " " + multipartFile1 + " " + multipartFile1.getContentType());
        }
        if(multipartFile.length == 0){
            return new ResponseEntity("Please select the file to upload S3", HttpStatus.OK);
        }
        return uploadFileService.uploadFileToStorage(RichUser.get(principal).getId(), experimentId, multipartFile);
    }
}
