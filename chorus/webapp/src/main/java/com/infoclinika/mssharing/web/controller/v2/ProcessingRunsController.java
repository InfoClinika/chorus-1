package com.infoclinika.mssharing.web.controller.v2;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import com.infoclinika.mssharing.web.controller.v2.dto.ProcessingRunsDTO;
import com.infoclinika.mssharing.web.controller.v2.service.UploadFileService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.inject.Inject;
import javax.inject.Named;
import java.security.Principal;
import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/v2/experiment/{experimentId}/processing-runs")
public class ProcessingRunsController {


    @Inject
    private UploadFileService uploadFileService;



    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleAccessDeniedExeption(Exception ex, WebRequest request){
        return new ResponseEntity<>(ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }


    @RequestMapping(name = "", method = RequestMethod.POST)
    public ResponseEntity<?> create(Principal principal, @PathVariable("experimentId") long experimentId, @RequestBody ProcessingRunsDTO processingRunsDTO){
        if(processingRunsDTO.getName() != null && !processingRunsDTO.getName().isEmpty()) {
            return uploadFileService.createProcessingRun(processingRunsDTO, RichUser.get(principal).getId(), experimentId);
        }
        return new ResponseEntity("Processing Run name can't be empty !", HttpStatus.BAD_REQUEST);
    }
}
