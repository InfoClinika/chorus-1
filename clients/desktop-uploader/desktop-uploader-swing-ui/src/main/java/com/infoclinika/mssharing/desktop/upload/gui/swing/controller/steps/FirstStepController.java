package com.infoclinika.mssharing.desktop.upload.gui.swing.controller.steps;

import com.infoclinika.mssharing.clients.common.dto.DictionaryWrapper;
import com.infoclinika.mssharing.clients.common.dto.InstrumentWrapper;
import com.infoclinika.mssharing.clients.common.web.api.WebService;
import com.infoclinika.mssharing.desktop.messages.MessagesSource;
import com.infoclinika.mssharing.desktop.upload.gui.swing.controller.MainController;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.WizardMainForm;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.steps.FirstStep;
import com.infoclinika.mssharing.desktop.upload.gui.swing.model.bean.DesktopUploaderSession;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.DisplayMessageHelper;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static com.infoclinika.mssharing.clients.common.Transformers.TO_DICTIONARY_WRAPPER;
import static com.infoclinika.mssharing.clients.common.Transformers.TO_INSTRUMENT_WRAPPER;
import static com.infoclinika.mssharing.desktop.messages.MessageKey.*;
import static com.infoclinika.mssharing.desktop.messages.MessagesSource.getMessage;

/**
 * @author timofey.kasyanov
 *     date:   30.01.14
 */
@Controller
public class FirstStepController extends AbstractStepController {
    private final DictionaryWrapper defaultTechnologyType = TO_DICTIONARY_WRAPPER.apply(
        new DictionaryDTO(-1, MessagesSource.getMessage(ONE_COMBO_SELECT_ONE))
    );
    private final DictionaryWrapper defaultVendor = TO_DICTIONARY_WRAPPER.apply(
        new DictionaryDTO(-1, MessagesSource.getMessage(ONE_COMBO_SELECT_ONE))
    );
    private final DictionaryWrapper defaultLab = TO_DICTIONARY_WRAPPER.apply(
        new DictionaryDTO(-1, MessagesSource.getMessage(ONE_COMBO_SELECT_ONE))
    );
    private final InstrumentWrapper defaultInstrument = TO_INSTRUMENT_WRAPPER.apply(
        new InstrumentDTO(
            -1,
            MessagesSource.getMessage(ONE_COMBO_SELECT_ONE),
            null,
            -1,
            "",
            -1
        )
    );

    private FirstStep firstStep;

    @Inject
    private WizardMainForm mainForm;

    @Inject
    private DesktopUploaderSession desktopUploaderSession;

    @Inject
    private WebService webService;

    @Inject
    private DisplayMessageHelper messageHelper;

    @Inject
    private MainController mainController;

    public void setFirstStep(FirstStep firstStep) {
        this.firstStep = firstStep;
    }

    @Override
    public void activate() {
        mainController.stepOneUpdateButtons();
    }

    public void initialize() {
        final List<DictionaryDTO> technologyTypes = webService.getTechnologyTypes();
        final List<InstrumentDTO> instruments = webService.getInstruments();
        final List<DictionaryDTO> species = webService.getSpecies();
        final DictionaryDTO defaultSpecie = webService.getDefaultSpecie();

        desktopUploaderSession.setTechnologyTypes(technologyTypes);
        desktopUploaderSession.setInstruments(instruments);
        desktopUploaderSession.setSpecies(species);
        desktopUploaderSession.setDefaultSpecie(defaultSpecie);

        desktopUploaderSession.getDesktopUploaderContext().setTechnologyType(defaultTechnologyType.getDictionary());
        desktopUploaderSession.getDesktopUploaderContext().setInstrument(defaultInstrument.getInstrument());
        desktopUploaderSession.getDesktopUploaderContext().setSpecie(defaultSpecie);

        firstStep.initialize();

        mainController.stepOneUpdateButtons();

        if (instruments.size() == 0) {
            messageHelper.showMainWindowMessage(
                getMessage(MODALS_NO_INSTRUMENTS_TEXT),
                getMessage(MODALS_WARNING_TITLE),
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    public List<DictionaryDTO> getTechnologyTypes() {
        return webService.getTechnologyTypes();
    }

    public List<DictionaryDTO> getVendorsByTechnologyType(long technologyTypeId) {
        return webService.getVendorsByTechnologyType(technologyTypeId);
    }

    public List<DictionaryDTO> getLabs() {
        return webService.getLabs();
    }

    public List<DictionaryDTO> getInstrumentModels(long technologyType, long vendor) {
        return webService.getInstrumentModels(technologyType, vendor);
    }

    public List<InstrumentDTO> getInstruments(long lab, long technologyType, long vendor) {
        final List<InstrumentDTO> instruments = new ArrayList<>();
        final List<InstrumentDTO> allInstruments = desktopUploaderSession.getInstruments();

        for (InstrumentDTO instrument : allInstruments) {
            if (instrument.getVendor().studyTypeItem.getId() == technologyType
                && instrument.getVendor().id == vendor
                && instrument.getLab() == lab) {
                instruments.add(instrument);
            }
        }

        return instruments;
    }

    public List<DictionaryDTO> getSpecies() {
        return desktopUploaderSession.getSpecies();
    }

    public DictionaryWrapper getDefaultTechnologyType() {
        return defaultTechnologyType;
    }

    public DictionaryWrapper getDefaultVendor() {
        return defaultVendor;
    }

    public DictionaryWrapper getDefaultLab() {
        return defaultLab;
    }

    public InstrumentWrapper getDefaultInstrument() {
        return defaultInstrument;
    }

    public DictionaryWrapper getDefaultSpecie() {
        return TO_DICTIONARY_WRAPPER.apply(desktopUploaderSession.getDefaultSpecie());
    }

    public void changeSelectedInstrument(InstrumentWrapper instrument) {
        final long id = instrument.getInstrument().getId();
        final InstrumentDTO instrumentDTO = id > 0 ? webService.getInstrument(id) : instrument.getInstrument();

        desktopUploaderSession.getDesktopUploaderContext().setInstrument(instrumentDTO);
        mainController.stepOneUpdateButtons();
        mainController.instrumentChanged();
    }

    public void changeSelectedSpecie(DictionaryWrapper specie) {
        desktopUploaderSession.getDesktopUploaderContext().setSpecie(specie.getDictionary());
        mainController.stepOneUpdateButtons();
    }

    public InstrumentWrapper createDefaultInstrument(long lab, long instrumentModel) {
        final InstrumentDTO defaultInstrument = webService.createDefaultInstrument(lab, instrumentModel);

        return new InstrumentWrapper(defaultInstrument);
    }
}
