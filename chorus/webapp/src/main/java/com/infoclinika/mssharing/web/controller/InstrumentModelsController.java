package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.InstrumentModelReader;
import com.infoclinika.mssharing.model.read.InstrumentModelReader.InstrumentModelLine;
import com.infoclinika.mssharing.model.write.InstrumentModelManagement;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.read.InstrumentModelReaderTemplate.InstrumentModelLineTemplate;
import com.infoclinika.mssharing.platform.model.write.InstrumentModelManagementTemplate.InstrumentModelDetails;
import com.infoclinika.mssharing.web.controller.request.CreateInstrumentModelRequest;
import com.infoclinika.mssharing.web.controller.request.PageRequest;
import com.infoclinika.mssharing.web.controller.request.UpdateInstrumentModelRequest;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author timofei.kasianov 12/7/16
 */
@RequestMapping("/instrument-models")
@Controller
public class InstrumentModelsController extends PagedItemsController {

    @Inject
    private InstrumentModelManagement instrumentModelManagement;
    @Inject
    private InstrumentModelReader instrumentModelReader;
    @Inject
    private DashboardReader dashboardReader;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ValueResponse getById(@RequestParam long id, Principal principal) {
        final long userId = getUserId(principal);
        final InstrumentModelLineTemplate instrumentModel = dashboardReader.readById(userId, id);
        return new ValueResponse<>(instrumentModel);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ValueResponse save(@RequestBody CreateInstrumentModelRequest request, Principal principal) {

        final long userId = getUserId(principal);
        final InstrumentModelDetails details = new InstrumentModelDetails(
                request.name,
                request.technologyType,
                request.vendor,
                request.instrumentType,
                request.extensions.stream().collect(Collectors.toSet())
        );
        final long id = instrumentModelManagement.create(userId, details);

        return new ValueResponse<>(id);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public ValueResponse update(@RequestBody UpdateInstrumentModelRequest request, Principal principal) {

        final long userId = getUserId(principal);
        final InstrumentModelDetails details = new InstrumentModelDetails(
                request.name,
                request.technologyType,
                request.vendor,
                request.instrumentType,
                request.extensions.stream().collect(Collectors.toSet())
        );
        instrumentModelManagement.update(userId, request.id, details);

        return new ValueResponse<>(request.id);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void delete(@RequestParam long id, Principal principal) {
        final long userId = getUserId(principal);
        instrumentModelManagement.delete(userId, id);
    }

    @RequestMapping(value = "/paged", method = RequestMethod.GET)
    @ResponseBody
    public ValueResponse listPaged(PageRequest pageRequest,
                                   Principal principal) {
        final long userId = getUserId(principal);
        final PagedItemInfo pagedInfo = createPagedInfo(pageRequest);
        final PagedItem<InstrumentModelLine> instrumentModels = instrumentModelReader.read(userId, pagedInfo);
        return new ValueResponse<>(instrumentModels);
    }

    @RequestMapping("/instrumentTypesByTechnologyTypeAndVendor")
    @ResponseBody
    public ValueResponse getInstrumentTypesByTechnologyTypeAndVendor(@RequestParam Long technologyType,
                                                                     @RequestParam Long vendor,
                                                                     Principal principal) {
        if (technologyType == null || vendor == null) {
            return new ValueResponse<>(new ArrayList<>());
        }

        final long userId = getUserId(principal);
        final Set<InstrumentModelLineTemplate> instrumentModels = dashboardReader.readByStudyTypeAndVendor(userId, technologyType, vendor);
        final List<DictionaryItem> uniqueInstrumentTypes = instrumentModels
                .stream()
                .map(im -> im.instrumentType)
                .collect(Collectors.toSet())
                .stream().collect(Collectors.toList());

        return new ValueResponse<>(uniqueInstrumentTypes);
    }

    @RequestMapping("/vendorExtensionsByTechnologyTypeAndVendor")
    @ResponseBody
    public ValueResponse getVendorExtensionsByTechnologyTypeAndVendor(@RequestParam long technologyType,
                                                                      @RequestParam long vendor,
                                                                      Principal principal) {
        final long userId = getUserId(principal);
        final Set<InstrumentModelLineTemplate> instrumentModels = dashboardReader.readByStudyTypeAndVendor(userId, technologyType, vendor);
        final List<String> uniqueExtensions = instrumentModels
                .stream()
                .map(im -> im.extensions)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet())
                .stream().collect(Collectors.toList());

        return new ValueResponse<>(uniqueExtensions);
    }

    @RequestMapping("/getByTechnologyTypeAndVendor")
    @ResponseBody
    public ValueResponse getByTechnologyTypeAndVendor(@RequestParam long technologyType,
                                                      @RequestParam long vendor,
                                                      Principal principal) {
        final long userId = getUserId(principal);
        final Set<InstrumentModelLineTemplate> instrumentModels = dashboardReader.readByStudyTypeAndVendor(userId, technologyType, vendor);
        return new ValueResponse<>(instrumentModels);
    }

    @RequestMapping("/getByLabTechnologyTypeAndVendor")
    @ResponseBody
    public ValueResponse getByLabTechnologyTypeAndVendor(@RequestParam long lab,
                                                         @RequestParam long technologyType,
                                                         @RequestParam long vendor,
                                                         Principal principal) {
        final long userId = getUserId(principal);
        final Set<InstrumentModelLineTemplate> instrumentModels = dashboardReader.readByStudyTypeAndVendor(userId, technologyType, vendor);
        return new ValueResponse<>(instrumentModels);
    }

    @RequestMapping("/isNameUnique")
    @ResponseBody
    public ValueResponse validateModelNameUniqueness(@RequestParam(required = false) Long modelId,
                                                     @RequestParam String name,
                                                     @RequestParam(required = false) Long vendor,
                                                     Principal principal) {
        final long userId = getUserId(principal);
        final boolean unique = instrumentModelReader.isNameUnique(userId, name, vendor, modelId);
        return new ValueResponse<>(unique);
    }

    @RequestMapping("/byVendor")
    @ResponseBody
    public ValueResponse getModels(@RequestParam long vendor, Principal principal) {
        final long userId = getUserId(principal);
        final Set<InstrumentModelLineTemplate> instrumentModels = dashboardReader.readByVendor(userId, vendor);
        final List<DictionaryItem> dictionaryItems = instrumentModels
                .stream()
                .map(im -> new DictionaryItem(im.id, im.name))
                .collect(Collectors.toList());
        return new ValueResponse<>(dictionaryItems);
    }
}
