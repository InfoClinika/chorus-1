package com.infoclinika.mssharing.clients.common.web.api;

import com.infoclinika.mssharing.dto.request.ConfirmMultipartUploadDTO;
import com.infoclinika.mssharing.dto.request.DesktopUploadDoneDTO;
import com.infoclinika.mssharing.dto.request.UploadFilesDTORequest;
import com.infoclinika.mssharing.dto.request.UserNamePassDTO;
import com.infoclinika.mssharing.dto.response.*;
import com.infoclinika.mssharing.web.rest.ComposeFilesRequest;
import com.infoclinika.mssharing.web.rest.ComposeFilesResponse;
import com.infoclinika.mssharing.web.rest.FilesReadyToUploadRequest;
import com.infoclinika.mssharing.web.rest.FilesReadyToUploadResponse;

import java.util.List;

public interface WebService {

    AuthenticateDTO authenticate(UserNamePassDTO credentials);

    AuthenticateDTO authenticate(String token);

    boolean isArchivingRequired(InstrumentDTO instrument);

    List<DictionaryDTO> getTechnologyTypes();

    List<DictionaryDTO> getVendors();

    List<DictionaryDTO> getVendorsByTechnologyType(long technologyType);

    List<DictionaryDTO> getLabs();

    List<DictionaryDTO> getInstrumentModels(long technologyType, long vendor);

    List<InstrumentDTO> getInstruments();

    List<InstrumentDTO> getInstruments(long instrumentModel);

    InstrumentDTO getInstrument(long instrument);

    InstrumentDTO createDefaultInstrument(long lab, long instrumentModel);

    FilesReadyToUploadResponse isReadyToUpload(FilesReadyToUploadRequest request);

    ComposeFilesResponse composeFiles(ComposeFilesRequest request);

    List<FileDTO> getInstrumentFiles(InstrumentDTO instrument);

    List<DictionaryDTO> getSpecies();

    DictionaryDTO getDefaultSpecie();

    List<FileDTO> getUnfinishedUploads();

    DeleteUploadDTO deleteUpload(long fileId);

    SimpleUploadFilesDTOResponse postStartUploadRequest(UploadFilesDTORequest request);

    SSEUploadFilesDTOResponse postStartSSEUploadRequest(UploadFilesDTORequest request);

    UploadFilesDTOResponse postStartUploadRequestBeforeFinish(UploadFilesDTORequest request);

    CompleteUploadDTO postCompleteUploadRequest(ConfirmMultipartUploadDTO request);

    UploadConfigDTO getUploadConfig();

    void finalizeUpload(String id, DesktopUploadDoneDTO dto);
}
