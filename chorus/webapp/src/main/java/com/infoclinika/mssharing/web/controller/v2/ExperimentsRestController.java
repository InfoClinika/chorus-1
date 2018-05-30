package com.infoclinika.mssharing.web.controller.v2;



import com.infoclinika.mssharing.platform.web.security.RichUser;
import com.infoclinika.mssharing.web.controller.v2.dto.ExperimentInfoDTO;
import com.infoclinika.mssharing.web.controller.v2.service.ExperimentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import javax.inject.Inject;
import java.security.Principal;


@RestController
@RequestMapping(value = "/v2/experiments")
public class ExperimentsRestController {


    @Inject
    private ExperimentService experimentService;



    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleAccessDeniedExeption(Exception ex, WebRequest request){
        return new ResponseEntity<>(ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED);

    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> anyUnknownException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @RequestMapping(value = "/{experimentId}", method = RequestMethod.GET)
    public ResponseEntity<ExperimentInfoDTO> getExperimentInfoCertainFields(@PathVariable long experimentId, Principal principal) {
        return experimentService.returnExperimentInfo(RichUser.get(principal).getId(), experimentId);
    }
}
