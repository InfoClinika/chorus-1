package com.infoclinika.mssharing.desktop.upload.gui.swing.model.bean;

import com.infoclinika.mssharing.desktop.upload.service.impl.list.EditFileItemList;
import com.infoclinika.mssharing.desktop.upload.service.impl.list.UploadZipList;
import com.infoclinika.mssharing.desktop.upload.service.impl.list.ViewFileItemList;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author timofey.kasyanov
 *     date:   28.01.14
 */
@Component
public class DesktopUploaderContext {
    private DictionaryDTO technologyType;
    private InstrumentDTO instrument;
    private DictionaryDTO specie;

    @Inject
    private ViewFileItemList viewFileItemList;

    @Inject
    private EditFileItemList editFileItemsList;

    @Inject
    private UploadZipList uploadZipList;

    public DictionaryDTO getTechnologyType() {
        return technologyType;
    }

    public void setTechnologyType(DictionaryDTO technologyType) {
        this.technologyType = technologyType;
    }

    public InstrumentDTO getInstrument() {
        return instrument;
    }

    public void setInstrument(InstrumentDTO instrument) {
        this.instrument = instrument;
    }

    public DictionaryDTO getSpecie() {
        return specie;
    }

    public void setSpecie(DictionaryDTO specie) {
        this.specie = specie;
    }

    public ViewFileItemList getViewFileItemList() {
        return viewFileItemList;
    }

    public EditFileItemList getEditFileItemsList() {
        return editFileItemsList;
    }

    public UploadZipList getUploadZipList() {
        return uploadZipList;
    }

}
