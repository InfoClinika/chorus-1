package com.infoclinika.mssharing.upload.common.web.impl;

import com.google.gson.Gson;
import com.infoclinika.mssharing.dto.request.ConfirmMultipartUploadDTO;
import com.infoclinika.mssharing.dto.request.UploadFilesDTORequest;
import com.infoclinika.mssharing.dto.request.UserNamePassDTO;
import com.infoclinika.mssharing.dto.response.*;
import com.infoclinika.mssharing.upload.common.web.api.WebService;
import com.infoclinika.mssharing.upload.common.web.api.exception.AuthenticateException;
import com.infoclinika.mssharing.upload.common.web.api.exception.RestServiceException;
import com.infoclinika.mssharing.upload.common.web.model.WebExceptionResponse;
import com.infoclinika.mssharing.web.rest.*;
import com.infoclinika.mssharing.web.rest.FilesReadyToUploadRequest;
import com.infoclinika.mssharing.web.rest.FilesReadyToUploadResponse;
import com.infoclinika.mssharing.web.rest.RestExceptionType;
import com.infoclinika.mssharing.web.rest.UploaderRestService;
import org.apache.cxf.configuration.security.ProxyAuthorizationPolicy;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.transports.http.configuration.ProxyServerType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class WebServiceImpl implements WebService {
    private static final Logger LOGGER = Logger.getLogger(WebServiceImpl.class);
    private static final String AUTHENTICATED_SUCCESSFULLY = "Authenticated successfully";
    private static final String GETTING_INSTRUMENTS = "Getting instruments";
    private static final String SERVER_IS_NOT_RESPONDING = "Server is not responding";
    private static final int TIMEOUT = 600000;

    @Value("${client.proxy.enabled}")
    private boolean isProxyEnabled;

    @Value("${client.proxyHost}")
    private String proxyHost;

    @Value("${client.proxyPort}")
    private int proxyPort;

    @Value("${client.proxyUsername}")
    private String proxyUsername;

    @Value("${client.proxyPassword}")
    private String proxyPassword;

    public WebServiceImpl() {
    }

    @Context
    @Resource
    private UploaderRestService uploaderRestService;

    private AuthenticateDTO authentication;
    private final Gson gson = new Gson();

    @PostConstruct
    private void postConstruct() {
        final Client webClient = WebClient.client(uploaderRestService);

        final ClientConfiguration config = WebClient.getConfig(webClient);
        final HTTPConduit conduit = config.getHttpConduit();

        final HTTPClientPolicy clientPolicy = new HTTPClientPolicy();
        clientPolicy.setAllowChunking(false);
        clientPolicy.setReceiveTimeout(TIMEOUT);
        clientPolicy.setConnectionTimeout(TIMEOUT);

        if (isProxyEnabled) {

            LOGGER.info("# Using next proxy info to configure web service:");
            LOGGER.info("# Host: " + proxyHost);
            LOGGER.info("# Port: " + proxyPort);
            LOGGER.info("# Username: " + proxyUsername);
            LOGGER.info("# Password: " + proxyPassword);

            clientPolicy.setProxyServer(proxyHost);
            clientPolicy.setProxyServerPort(proxyPort);

            final ProxyAuthorizationPolicy proxyAuthorization = new ProxyAuthorizationPolicy();
            proxyAuthorization.setUserName(proxyUsername);
            proxyAuthorization.setPassword(proxyPassword);
            conduit.setProxyAuthorization(proxyAuthorization);
        }

        conduit.setClient(clientPolicy);
    }

    @Override
    public AuthenticateDTO authenticate(UserNamePassDTO credentials) {
        LOGGER.info("Authenticating for " + credentials.getUsername());

        try {
            authentication = uploaderRestService.authenticate(credentials);
        } catch (RuntimeException ex) {
            LOGGER.error("Error. Failed to authenticate user : " + ex.getMessage(), ex);
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
            LOGGER.error("Error. Failed to authenticate user : " + ex.getMessage(), ex);
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
        LOGGER.info("Creating default instrument for lab: " + lab + " and instrument model: " + instrumentModel);
        try {
            return uploaderRestService.createDefaultInstrument(lab, instrumentModel, getToken());
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
    public FilesReadyToUploadResponse isReadyToUpload(FilesReadyToUploadRequest request) {
        LOGGER.info("Checking if file is already uploaded instruments");
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
        LOGGER.info("Getting instrument's files. Instrument: " + instrument.getName());
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
        LOGGER.info("Deleting upload. File id: " + fileId);
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
        LOGGER.info("Posting start upload request. " + request);
        try {

            return uploaderRestService.simpleUploadRequest(request, getToken());

        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public SSEUploadFilesDTOResponse postStartSSEUploadRequest(UploadFilesDTORequest request) {
        LOGGER.info("Posting start sse upload request. " + request);
        try {

            return uploaderRestService.sseUploadRequest(request, getToken());

        } catch (Exception ex) {
            LOGGER.error("", ex);
            throw getException(ex);
        }
    }

    @Override
    public UploadFilesDTOResponse postStartUploadRequestBeforeFinish(UploadFilesDTORequest request) {
        LOGGER.info("Posting upload request before finish. " + request);
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
                final String entityAsString = new String(entityBytes);
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
