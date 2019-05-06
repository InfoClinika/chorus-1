package com.infoclinika.mssharing.clients.common.web.impl;

import com.google.gson.Gson;
import com.infoclinika.mssharing.clients.common.web.api.WebService;
import com.infoclinika.mssharing.clients.common.web.api.exception.AuthenticateException;
import com.infoclinika.mssharing.clients.common.web.api.exception.RestServiceException;
import com.infoclinika.mssharing.clients.common.web.model.WebExceptionResponse;
import com.infoclinika.mssharing.dto.request.ConfirmMultipartUploadDTO;
import com.infoclinika.mssharing.dto.request.DesktopUploadDoneDTO;
import com.infoclinika.mssharing.dto.request.UploadFilesDTORequest;
import com.infoclinika.mssharing.dto.request.UserNamePassDTO;
import com.infoclinika.mssharing.dto.response.*;
import com.infoclinika.mssharing.propertiesprovider.DesktopClientsPropertiesProvider;
import com.infoclinika.mssharing.web.rest.*;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class WebServiceImpl implements WebService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServiceImpl.class);
    private static final String AUTHENTICATED_SUCCESSFULLY = "Authenticated successfully";
    private static final String GETTING_INSTRUMENTS = "Getting instruments";
    private static final String SERVER_IS_NOT_RESPONDING = "Server is not responding";
    private static final int TIMEOUT = 600000;

    @Inject
    private DesktopClientsPropertiesProvider desktopClientsPropertiesProvider;

    public WebServiceImpl() {
    }

    @Context
    @Resource
    private UploaderRestService uploaderRestService;

    private AuthenticateDTO authentication;
    private final Gson gson = new Gson();

    @PostConstruct
    private void postConstruct() {
        LOGGER.info("# Using next api URL: {}", desktopClientsPropertiesProvider.getUploaderApiUrl());

        final Client webClient = WebClient.client(uploaderRestService);

        final ClientConfiguration config = WebClient.getConfig(webClient);
        final HTTPConduit conduit = config.getHttpConduit();

        final HTTPClientPolicy clientPolicy = new HTTPClientPolicy();

        clientPolicy.setAllowChunking(false);
        clientPolicy.setReceiveTimeout(TIMEOUT);
        clientPolicy.setConnectionTimeout(TIMEOUT);

        conduit.setClient(clientPolicy);
    }

    @Override
    public AuthenticateDTO authenticate(UserNamePassDTO credentials) {
        LOGGER.info("Authenticating for {}", credentials.getUsername());

        try {
            authentication = uploaderRestService.authenticate(credentials);
        } catch (RuntimeException ex) {
            LOGGER.error("Error. Failed to authenticate user : {}", ex);
            throw getException(ex);
        }

        LOGGER.info(AUTHENTICATED_SUCCESSFULLY);

        return authentication;

    }

    @Override
    public AuthenticateDTO authenticate(String token) {
        LOGGER.info("Authenticating using token.");

        try {
            authentication = uploaderRestService.authenticate(token);
        } catch (RuntimeException ex) {
            LOGGER.error("Error. Failed to authenticate user : {}", ex);
            throw getException(ex);
        }

        LOGGER.info(AUTHENTICATED_SUCCESSFULLY);

        return authentication;
    }

    @Override
    public boolean isArchivingRequired(InstrumentDTO instrument) {
        final VendorDTO vendor = instrument.getVendor();

        return vendor.folderArchiveUploadSupport || vendor.multipleFiles;
    }

    @Override
    public List<DictionaryDTO> getTechnologyTypes() {
        LOGGER.info("Getting Technology Types");

        try {
            return new ArrayList<>(uploaderRestService.getTechnologyTypes(getToken()));
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public List<DictionaryDTO> getVendors() {
        LOGGER.info("Getting Vendors");

        try {
            return new ArrayList<>(uploaderRestService.getVendors(getToken()));
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public List<DictionaryDTO> getVendorsByTechnologyType(long technologyTypeId) {
        LOGGER.info("Getting Vendors By technologyType={}", technologyTypeId);
        try {
            return new ArrayList<>(uploaderRestService.getVendorsByTechnologyType(getToken(), technologyTypeId));
        } catch (Exception ex) {
            LOGGER.error("Failed to get vendors by technology type={}", technologyTypeId, ex);
            throw getException(ex);
        }
    }

    @Override
    public List<DictionaryDTO> getLabs() {
        LOGGER.info("Getting Vendors");

        try {
            return new ArrayList<>(uploaderRestService.getLabs(getToken()));
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public List<DictionaryDTO> getInstrumentModels(long technologyType, long vendor) {
        LOGGER.info("Getting Instrument Models");

        try {
            return new ArrayList<>(uploaderRestService.getInstrumentModels(getToken(), technologyType, vendor));
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public List<InstrumentDTO> getInstruments() {
        LOGGER.info(GETTING_INSTRUMENTS);
        try {
            return uploaderRestService.getInstruments(getToken());
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public List<InstrumentDTO> getInstruments(long instrumentModel) {
        LOGGER.info(GETTING_INSTRUMENTS);
        try {
            return uploaderRestService.getInstruments(getToken(), instrumentModel);
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public InstrumentDTO getInstrument(long instrument) {
        LOGGER.info("Getting instrument");
        try {
            return uploaderRestService.getInstrument(instrument, getToken());
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public InstrumentDTO createDefaultInstrument(long lab, long instrumentModel) {
        LOGGER.info("Creating default instrument for lab: {} and instrument model: {}", lab, instrumentModel);
        try {
            return uploaderRestService.createDefaultInstrument(lab, instrumentModel, getToken());
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public FilesReadyToUploadResponse isReadyToUpload(FilesReadyToUploadRequest request) {
        LOGGER.info("Checking if files has been already uploaded: {}", Arrays.toString(request.fileDescriptions));
        try {
            return uploaderRestService.isReadyToUpload(request, getToken());
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public ComposeFilesResponse composeFiles(ComposeFilesRequest request) {
        LOGGER.info("Composing files");
        try {
            return uploaderRestService.composeFiles(request, getToken());
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    private String getToken() {
        if (authentication == null) {
            throw new AuthenticateException();
        }

        return authentication.getRestToken();
    }

    @Override
    public List<FileDTO> getInstrumentFiles(InstrumentDTO instrument) {
        LOGGER.info("Getting instrument's files. Instrument: {}", instrument.getName());
        try {
            final Set<FileDTO> instrumentFiles = uploaderRestService.getInstrumentFiles(instrument.getId(), getToken());
            return newArrayList(instrumentFiles);
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public List<DictionaryDTO> getSpecies() {
        LOGGER.info("Getting species");
        try {
            Set<DictionaryDTO> species = uploaderRestService.getSpecies(getToken());
            return newArrayList(species);
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public DictionaryDTO getDefaultSpecie() {
        LOGGER.info("Getting default specie");
        try {
            return uploaderRestService.getDefaultSpecie(getToken());
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public DeleteUploadDTO deleteUpload(long fileId) {
        LOGGER.info("Deleting upload. File id: {}", fileId);
        try {

            return uploaderRestService.deleteUpload(fileId, getToken());

        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public List<FileDTO> getUnfinishedUploads() {
        LOGGER.info("Getting unfinished uploads");
        try {

            final List<FileDTO> unfinishedUploads = uploaderRestService.getUnfinishedUploads(getToken());
            return newArrayList(unfinishedUploads);

        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public SimpleUploadFilesDTOResponse postStartUploadRequest(UploadFilesDTORequest request) {
        LOGGER.info("Posting start upload request. {}", request);
        try {

            return uploaderRestService.simpleUploadRequest(request, getToken());

        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public SSEUploadFilesDTOResponse postStartSSEUploadRequest(UploadFilesDTORequest request) {
        LOGGER.info("Posting start sse upload request. {}", request);
        try {

            return uploaderRestService.sseUploadRequest(request, getToken());

        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public UploadFilesDTOResponse postStartUploadRequestBeforeFinish(UploadFilesDTORequest request) {
        LOGGER.info("Posting upload request before finish. {}", request);
        try {

            return uploaderRestService.uploadRequest(request, getToken());

        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public CompleteUploadDTO postCompleteUploadRequest(ConfirmMultipartUploadDTO request) {
        LOGGER.info("Posting complete upload request");
        try {
            return uploaderRestService.completeUpload(request, getToken());
        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public void finalizeUpload(String id, DesktopUploadDoneDTO dto) {
        LOGGER.info("Posting all files complete upload request... id = {}", id);
        try {
            uploaderRestService.finalizeUpload(id, dto, getToken());
        } catch (Exception ex) {
            throw getException(ex);
        }
    }

    @Override
    public UploadConfigDTO getUploadConfig() {
        return uploaderRestService.getUploadConfig(getToken());
    }

    private RestServiceException getException(Exception ex) {

        if (ex instanceof WebApplicationException) {
            try {

                final WebApplicationException wae = (WebApplicationException) ex;
                final InputStream entityStream = (InputStream) wae.getResponse().getEntity();
                final byte[] entityBytes = new byte[entityStream.available()];
                final int read = entityStream.read(entityBytes);
                if (read != entityBytes.length) {
                    throw new RuntimeException();
                }
                final String entityAsString = new String(entityBytes, Charset.defaultCharset());
                final WebExceptionResponse valueResponse = gson.fromJson(entityAsString, WebExceptionResponse.class);
                return new RestServiceException(valueResponse.getMessage(), valueResponse.getType());

            } catch (Exception e) {
                return new RestServiceException(SERVER_IS_NOT_RESPONDING, RestExceptionType.SERVER_IS_NOT_RESPONDING);
            }
        } else {
            return new RestServiceException(SERVER_IS_NOT_RESPONDING, RestExceptionType.SERVER_IS_NOT_RESPONDING);
        }
    }
}
