package com.infoclinika.mssharing.web.controller.v2;


import com.infoclinika.mssharing.platform.web.security.RichUser;
import com.infoclinika.mssharing.web.controller.v2.dto.ProcessingRunDetails;
import com.infoclinika.mssharing.web.controller.v2.dto.ProcessingRunsDTO;
import com.infoclinika.mssharing.web.controller.v2.service.ProcessingRunService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.inject.Inject;
import java.security.Principal;

@RestController
@RequestMapping("/v2/experiment/{experimentId}/processing-runs")
public class ProcessingRunsController {


    @Inject
    private ProcessingRunService processingRunService;



    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleAccessDeniedExeption(Exception ex, WebRequest request){
        return new ResponseEntity<>(ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }


    @RequestMapping(name = "", method = RequestMethod.POST)
    public ResponseEntity<?> create(Principal principal, @PathVariable("experimentId") long experimentId, @RequestBody ProcessingRunsDTO processingRunsDTO){
        if(processingRunsDTO.getName() != null && !processingRunsDTO.getName().isEmpty()) {
            return processingRunService.createProcessingRun(processingRunsDTO, RichUser.get(principal).getId(), experimentId);
        }
        return new ResponseEntity("Processing Run name can't be empty !", HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(name = "", method = RequestMethod.PATCH)
    public ResponseEntity<?> update(Principal principal, @PathVariable("experimentId") long experimentId, @RequestBody ProcessingRunsDTO processingRunsDTO){
        if(processingRunsDTO.getName() != null && !processingRunsDTO.getName().isEmpty()) {
            return processingRunService.updateProcessingRun(processingRunsDTO, experimentId, RichUser.get(principal).getId());
        }
        return new ResponseEntity("Processing Run name can't be empty !", HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseEntity<?> getAllProcessingRunsByExperiment(Principal principal, @PathVariable("experimentId") long experimentId){
        return processingRunService.getAllProcessingRuns(experimentId, RichUser.get(principal).getId());
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    public ResponseEntity<ProcessingRunDetails> getSpecificProcessingRunInformation(Principal principal, @RequestParam(value = "id")long processingRunId, @PathVariable("experimentId") long experimentId){
        return processingRunService.showProcessingRunDetails(processingRunId, RichUser.get(principal).getId(), experimentId);
    }

}
