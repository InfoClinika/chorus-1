package com.infoclinika.mssharing.web.controller;


import com.infoclinika.mssharing.model.write.UserManagement;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.RequestsReader;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.model.write.LabManagement;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import com.infoclinika.mssharing.web.ResourceDeniedException;
import com.infoclinika.mssharing.web.controller.request.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;

import static com.infoclinika.mssharing.model.read.DetailsReader.InstrumentCreationItem;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

@Controller
@RequestMapping("/requests/details/")
public class RequestDetailsController extends ErrorHandler {

    @Inject
    DetailsReader detailsReader;

    @Inject
    InstrumentManagement instrumentManagement;

    @Inject
    LabManagement labManagement;

    @Inject
    RequestsReader requestsReader;


    @RequestMapping(value = "/lab/{id}", method = RequestMethod.GET)
    @ResponseBody
    public DetailsReaderTemplate.LabItemTemplate getLabDetails(@PathVariable long id, Principal principal) {
        try {
            return detailsReader.readLabRequestDetails(getUserId(principal), id);
        } catch (AccessDenied e) {
            throw new ResourceDeniedException(e.getMessage());
        }
    }

    @RequestMapping(value = "/instrument/{instrumentId}/{requesterId}", method = RequestMethod.GET)
    @ResponseBody
    public RequestsReader.InstrumentRequestDetails getInstrumentDetails(@PathVariable long instrumentId, @PathVariable long requesterId, Principal principal) {
        try {
            return requestsReader.myInstrumentInboxDetails(getUserId(principal), instrumentId, requesterId);
        } catch (AccessDenied e) {
            throw new ResourceDeniedException(e.getMessage());
        }
    }


    @RequestMapping(value = "/lab/update", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    private void updateLaboratory(@RequestBody LaboratoryOperationRequest laboratoryOperationRequest, Principal principal) throws Exception {
        UserManagement.PersonInfo personInfo = new UserManagement.PersonInfo(laboratoryOperationRequest.getHeadFirstName(),
                laboratoryOperationRequest.getHeadLastName(), laboratoryOperationRequest.getHeadEmail());
        LabManagementTemplate.LabInfoTemplate labInfo = new LabManagementTemplate.LabInfoTemplate(laboratoryOperationRequest.getInstitutionUrl(),
                personInfo, laboratoryOperationRequest.getName());
        labManagement.editLabRequestInfo(getUserId(principal), laboratoryOperationRequest.getId(), labInfo);
    }

    @RequestMapping(value = "/instrument-creation/update", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void updateInstrument(@RequestBody UpdateInstrumentRequest request, Principal principal){

        final long userId = getUserId(principal);
        instrumentManagement.updateNewInstrumentRequest(
                userId,
                request.id,
                request.model,
                request.details,
                request.operators
        );

    }

    @RequestMapping(value = "instrument-creation/{id}", method = RequestMethod.GET)
    @ResponseBody
    public InstrumentCreationItem instrumentCreationRequestDetails(@PathVariable Long id,
                                                                   Principal principal) {
        final long actor = getUserId(principal);
        return detailsReader.readInstrumentCreation(actor, id);
    }

    @RequestMapping(value = "/instrument-creation/approve/{id}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void approveInstrumentCreation(@PathVariable long id, Principal principal){
        final long userId = getUserId(principal);
        instrumentManagement.approveInstrumentCreation(userId, id);
    }

    @RequestMapping(value = "/instrument-creation/refuse", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void refuseInstrumentCreation(@RequestBody RefuseInstrumentCreationRequest request,
                                         Principal principal){
        final long userId = getUserId(principal);
        instrumentManagement.refuseInstrumentCreation(userId, request.getRequestId(), request.getComment());
    }

    @RequestMapping(value = "/instrument/refuse", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void refuseInstrument(@RequestBody RefuseInstrumentRequest request, Principal principal) throws Exception {
        instrumentManagement.refuseAccessToInstrument(getUserId(principal), request.getInstrumentId(), request.getRequesterId(), request.getComment());
    }

    @RequestMapping(value = "/instrument/approve/{id}/{requesterId}")
    @ResponseStatus(HttpStatus.OK)
    public void approve(@PathVariable long id, @PathVariable long requesterId, Principal principal) throws Exception {
        instrumentManagement.approveAccessToInstrument(getUserId(principal), id, requesterId);
    }

    @RequestMapping(value = "/lab/refuse", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void refuseLabCreation(@RequestBody RefuseLabOperationRequest request, Principal principal) throws Exception {
        labManagement.rejectLabCreation(getUserId(principal), request.getRequestId(), request.getComment());
    }

    @RequestMapping(value = "/lab/approve/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void approveLabCreation(@PathVariable long id, Principal principal) throws Exception {
        try {
            labManagement.confirmLabCreation(getUserId(principal), id);
        } catch (AccessDenied e) {
            throw new ResourceDeniedException(e.getMessage());
        }
    }


}
