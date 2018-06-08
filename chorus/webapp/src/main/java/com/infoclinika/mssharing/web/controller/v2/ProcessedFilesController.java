package com.infoclinika.mssharing.web.controller.v2;

import com.infoclinika.mssharing.platform.web.security.RichUser;
import com.infoclinika.mssharing.web.controller.v2.service.ProcessingFileService;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;


import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/v2/experiment/{experimentId}/processed-files")
public class ProcessedFilesController {
    private static final Logger LOGGER = Logger.getLogger(ProcessedFilesController.class);


    @Inject
    @Named(value = "processingFileService")
    private ProcessingFileService processingFileService;


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleAccessDeniedExeption(Exception ex, WebRequest request){
        return new ResponseEntity<>(ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED);

    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleMultipartError(Exception ex, IOException e){
        LOGGER.trace(e.getLocalizedMessage());
        return new ResponseEntity<>(e.getLocalizedMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);

    }
    @RequestMapping(value ="", method = RequestMethod.POST, consumes = {"multipart/mixed", "multipart/form-data"})
    public ResponseEntity<?> uploadFile(Principal principal, @PathVariable("experimentId") long experimentId, @RequestParam(value = "process-file", required = false) MultipartFile[] multipartFile){
        try{

            if(multipartFile.length == 0){
                return new ResponseEntity("Please select the file to upload S3", HttpStatus.BAD_REQUEST);
            }

            return processingFileService.uploadFileToStorage(RichUser.get(principal).getId(), experimentId, multipartFile);

        }catch (IOException e){
            LOGGER.trace(e.getLocalizedMessage());
        }

        return new ResponseEntity("Please select the file to upload S3", HttpStatus.INTERNAL_SERVER_ERROR);


    }
}
