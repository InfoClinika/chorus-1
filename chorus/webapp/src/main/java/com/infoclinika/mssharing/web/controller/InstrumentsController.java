package com.infoclinika.mssharing.web.controller;


import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.read.*;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.model.write.LabManagement;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate;
import com.infoclinika.mssharing.web.controller.request.CreateDefaultInstrumentRequest;
import com.infoclinika.mssharing.web.controller.request.CreateInstrumentRequest;
import com.infoclinika.mssharing.web.controller.response.DetailsResponse;
import com.infoclinika.mssharing.web.controller.response.SuccessErrorResponse;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Pavel Kaplin
 */
@Controller
@RequestMapping("/instruments")
public class InstrumentsController extends PagedItemsController {

    @Inject
    private DashboardReader dashboardReader;

    @Inject
    private InstrumentCreationHelperTemplate helper;

    @Inject
    private InstrumentManagement management;

    @Inject
    private DetailsReader detailsReader;

    @Inject
    private ExtendedInfoReader infoReader;

    @Inject
    private LabManagement labManagement;
    @Inject
    private InstrumentReader instrumentReader;

    public InstrumentsController() {
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<InstrumentLine> getInstruments(Principal principal) {
        return newArrayList(dashboardReader.readInstruments(getUserId(principal)));
    }

    @RequestMapping(value = "/bylab/{labId}", method = RequestMethod.GET)
    @ResponseBody
    public List<InstrumentLine> getInstrumentsByLab(@PathVariable("labId") long labId, Principal principal) {
        return newArrayList(dashboardReader.readInstrumentsByLab(getUserId(principal), labId));
    }

    @RequestMapping(value = "/paged/bylab/{labId}", method = RequestMethod.GET)
    @ResponseBody
    public PagedItem<InstrumentLine> getInstrumentsByLab(@PathVariable("labId") long labId,
                                                         Principal principal,
                                                         @RequestParam int page, @RequestParam int items,
                                                         @RequestParam String sortingField, @RequestParam boolean asc,
                                                         @RequestParam(required = false) @Nullable String filterQuery) {
        return dashboardReader.readInstrumentsByLab(getUserId(principal), labId, createPagedInfo(page, items, sortingField, asc, filterQuery));
    }

    @RequestMapping(value = "/paged", method = RequestMethod.GET)
    @ResponseBody
    public PagedItem<InstrumentLine> getInstruments(Principal principal,
                                                    @RequestParam int page, @RequestParam int items,
                                                    @RequestParam String sortingField, @RequestParam boolean asc,
                                                    @RequestParam(required = false) @Nullable String filterQuery) {
        return dashboardReader.readInstruments(getUserId(principal), createPagedInfo(page, items, sortingField, asc, filterQuery));
    }

    @RequestMapping("/operated")
    @ResponseBody
    public List<InstrumentItem> getOperatedInstruments(Principal principal) {
        return newArrayList(instrumentReader.readInstrumentItemsWhereUserIsOperator(getUserId(principal)));
    }

    @RequestMapping("/studyTypes")
    @ResponseBody
    public ImmutableSortedSet<DictionaryItem> getStudyTypes() {
        //noinspection unchecked
        return helper.studyTypes();
    }

    @RequestMapping("/vendorsByStudyType")
    @ResponseBody
    public ImmutableSortedSet<DictionaryItem> getVendors(@RequestParam long techType) {
        //noinspection unchecked
        return helper.vendors(techType);
    }

    @RequestMapping("/vendors")
    @ResponseBody
    public ImmutableSortedSet<DictionaryItem> getVendors() {
        //noinspection unchecked
        return helper.vendors();
    }

    @RequestMapping("/operators")
    @ResponseBody
    public ImmutableSortedSet<InstrumentCreationHelperTemplate.PotentialOperator> getOperators(@RequestParam long lab) {
        //noinspection unchecked
        return helper.availableOperators(lab);
    }

    @RequestMapping("/models")
    @ResponseBody
    public ImmutableSortedSet<DictionaryItem> getModels(@RequestParam long vendor) {
        //noinspection unchecked
        return helper.models(vendor);
    }

    @RequestMapping(value = "/createDefaultInstrument", method = RequestMethod.POST)
    @ResponseBody
    public ValueResponse createDefaultInstrument(@RequestBody CreateDefaultInstrumentRequest request, Principal principal) {

        final long userId = getUserId(principal);
        final Optional<InstrumentLine> defaultInstrumentOpt = instrumentReader.readDefaultInstrument(userId, request.labId, request.modelId);

        if (defaultInstrumentOpt.isPresent()) {
            return new ValueResponse<>(defaultInstrumentOpt.get().id);
        }

        return new ValueResponse<>(management.createDefaultInstrument(userId, request.labId, request.modelId));
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public SuccessErrorResponse save(@RequestBody CreateInstrumentRequest instrument, Principal principal) {
        long userId = getUserId(principal);

        final boolean labHead = labManagement.isLabHead(userId, instrument.lab);

        if (labHead) {

            try {
                long instrumentId = management.createInstrument(userId, instrument.lab, instrument.model, instrument.details);
                for (long operator : instrument.operators) {
                    management.addOperatorDirectly(userId, instrumentId, operator);
                }
            } catch (AccessDenied e) {
                return new SuccessErrorResponse(e.getMessage(), null);
            }
            return new SuccessErrorResponse(null, "Instrument saved");

        } else {

            try {

                management.newInstrumentRequest(
                        userId,
                        instrument.lab,
                        instrument.model,
                        instrument.details,
                        instrument.operators
                );

            } catch (AccessDenied e) {
                return new SuccessErrorResponse(e.getMessage(), null);
            }
            return new SuccessErrorResponse(null, "Instrument creation request saved");
        }


    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public SuccessErrorResponse update(@RequestBody EditInstrumentRequest instrument, Principal principal) {
        long userId = getUserId(principal);
        try {
            management.editInstrument(userId, instrument.id, instrument.details);
            management.setInstrumentOperators(userId, instrument.id, instrument.operators);
        } catch (Exception e) {
            return new SuccessErrorResponse(e.getMessage(), null);
        }
        return new SuccessErrorResponse(null, "Instrument updated");
    }

    @RequestMapping("/{id}")
    @ResponseBody
    public DetailsResponse details(@PathVariable long id, Principal principal) {
        final com.infoclinika.mssharing.model.read.dto.details.InstrumentItem instrument = detailsReader.readInstrument(getUserId(principal), id);
        return DetailsResponse.ok(instrument);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removeInstrument(@RequestParam long instrument, Principal principal) {
        management.deleteInstrument(getUserId(principal), instrument);
    }

    /**
     * Check - method is not used by web client
     */
    @Deprecated
    @RequestMapping(value = "/{id}/model", method = RequestMethod.GET)
    @ResponseBody
    public DictionaryItem getModel(@PathVariable long id, Principal principal) {
        return infoReader.instrumentModel(getUserId(principal), id);
    }
}
