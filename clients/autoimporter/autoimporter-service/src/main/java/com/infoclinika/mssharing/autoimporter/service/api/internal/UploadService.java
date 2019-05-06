package com.infoclinika.mssharing.autoimporter.service.api.internal;

import com.infoclinika.mssharing.autoimporter.model.bean.ContextInfo;
import com.infoclinika.mssharing.autoimporter.model.bean.DuplicateItem;
import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.model.bean.WaitItem;
import com.infoclinika.mssharing.autoimporter.service.api.UploadConfigurationService;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;

import java.util.List;

/**
 * author Ruslan Duboveckij
 */
public interface UploadService {
    void authorization(String email, String password);

    void authorization(String token);

    boolean readAuthorization();

    void clearConfig();

    String getUserName();

    List<DictionaryDTO> getTechnologyTypes();

    List<DictionaryDTO> getVendors();

    List<DictionaryDTO> getLabs();

    List<DictionaryDTO> getInstrumentModels(long technologyType, long vendor);

    List<InstrumentDTO> getInstruments(long instrumentModel);

    List<InstrumentDTO> getInstruments();

    InstrumentDTO getInstrument(long instrument);

    InstrumentDTO createDefaultInstrument(long lab, long instrumentModel);


    List<DictionaryDTO> getSpecies();

    DictionaryDTO getDefaultSpecie();

    void addContext(String name, String folder, String labels, long instrumentId, long specieId,
                    UploadConfigurationService.CompleteAction completeAction, String folderToMoveFiles);

    List<ContextInfo> getContexts();

    void removeContext(String folder);

    void startWatch(String folder);

    void stopWatch(String folder);

    int getFolderListLength(String folder);

    List<WaitItem> getWaitItem(String folder);

    List<UploadItem> getUploadItem(String folder);

    List<DuplicateItem> getDuplicateItems(String folder);

    boolean isThermoFileCheckingAvailable();

    boolean isThermoFileCheckerEnabled();

    void disableFileChecking();
}
