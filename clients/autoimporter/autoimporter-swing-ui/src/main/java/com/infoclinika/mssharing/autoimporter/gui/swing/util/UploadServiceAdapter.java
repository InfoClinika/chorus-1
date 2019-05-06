package com.infoclinika.mssharing.autoimporter.gui.swing.util;

import com.google.common.collect.Lists;
import com.infoclinika.mssharing.autoimporter.model.bean.ContextInfo;
import com.infoclinika.mssharing.autoimporter.model.bean.DuplicateItem;
import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.model.bean.WaitItem;
import com.infoclinika.mssharing.autoimporter.service.api.internal.UploadService;
import com.infoclinika.mssharing.dto.NotSupportVendor;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * author Ruslan Duboveckij
 */
@Service
public class UploadServiceAdapter {
    private List<InstrumentDTO> instruments;

    @Inject
    @Qualifier("uploadServiceProxy")
    private UploadService uploadService;

    public boolean startWatch(ContextInfo info) {
        try {

            uploadService.startWatch(info.getFolder());
            return true;

        } catch (Exception ex) {
            FormUtils.printError(ex);
        }

        return false;
    }

    public boolean stopWatch(String folder) {

        try {

            uploadService.stopWatch(folder);
            return true;

        } catch (Exception ex) {
            FormUtils.printError(ex);
        }

        return false;
    }

    public void addContext(ContextInfo info, Component frame) {
        try {
            uploadService.addContext(
                info.getName(),
                info.getFolder(),
                info.getLabels(),
                info.getInstrument().getId(),
                info.getSpecie().getId(),
                info.getCompleteAction(),
                info.getFolderToMoveFiles()
            );
        } catch (NotSupportVendor ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }
    }

    public boolean removeContext(String folder) {

        try {

            uploadService.removeContext(folder);
            return true;

        } catch (Exception ex) {
            FormUtils.printError(ex);
        }

        return false;
    }

    public List<ContextInfo> getContexts() {
        try {
            return uploadService.getContexts();
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }
        return Lists.newArrayList();
    }

    public String getUserName() {
        try {
            return uploadService.getUserName();
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }
        return "";
    }

    public void clearConfig() {
        try {
            uploadService.clearConfig();
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }
    }

    public List<DictionaryDTO> getTechnologyTypes() {
        try {
            return uploadService.getTechnologyTypes();
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }
        return Lists.newArrayList();
    }

    public List<DictionaryDTO> getVendors() {
        try {
            return uploadService.getVendors();
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }
        return Lists.newArrayList();
    }

    public List<DictionaryDTO> getLabs() {
        try {
            return uploadService.getLabs();
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }
        return Lists.newArrayList();
    }

    public List<DictionaryDTO> getInstrumentModels(long technologyType, long vendor) {
        try {
            return uploadService.getInstrumentModels(technologyType, vendor);
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }
        return Lists.newArrayList();
    }

    private List<InstrumentDTO> getInstruments() {
        if (instruments != null) {
            return instruments;
        }

        instruments = new ArrayList<>();
        try {
            instruments.addAll(uploadService.getInstruments());
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }

        return instruments;
    }

    public List<InstrumentDTO> getInstruments(long lab, long technologyType, long vendor) {
        final List<InstrumentDTO> instruments = new ArrayList<>();
        final List<InstrumentDTO> allInstruments = getInstruments();

        for (InstrumentDTO instrument : allInstruments) {
            if (instrument.getVendor().studyTypeItem.getId() == technologyType
                && instrument.getVendor().id == vendor
                && instrument.getLab() == lab) {
                instruments.add(instrument);
            }
        }

        return instruments;
    }

    public InstrumentDTO getInstrument(long instrument) {
        try {
            return uploadService.getInstrument(instrument);
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }
        return null;
    }

    public InstrumentDTO createDefaultInstrument(long lab, long instrumentModel) {
        try {
            return uploadService.createDefaultInstrument(lab, instrumentModel);
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }
        return null;
    }

    public DictionaryDTO getDefaultSpecie() {

        try {
            return uploadService.getDefaultSpecie();
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }

        return null;

    }

    public void authorization(String email, String password) {
        uploadService.authorization(email, password);
    }

    public void authorization(String token) {
        uploadService.authorization(token);
    }

    public int getFolderListLength(String folder) {
        try {
            return uploadService.getFolderListLength(folder);
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }
        return 0;
    }

    public List<WaitItem> getWaitItem(String folder) {
        try {
            return uploadService.getWaitItem(folder);
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }
        return Lists.newArrayList();
    }

    public List<UploadItem> getUploadItem(String folder) {
        try {
            return uploadService.getUploadItem(folder);
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }
        return Lists.newArrayList();
    }

    public List<DuplicateItem> getDuplicateItems(String folder) {
        try {
            return uploadService.getDuplicateItems(folder);
        } catch (Exception ex) {
            FormUtils.printError(ex);
        }
        return Lists.newArrayList();
    }

    public boolean isThermoFileCheckingAvailable() {

        try {
            return uploadService.isThermoFileCheckingAvailable();
        } catch (Exception e) {
            FormUtils.printError(e);
        }

        return false;

    }

    public boolean isThermoFileCheckingEnabled() {
        return uploadService.isThermoFileCheckerEnabled();
    }
}
