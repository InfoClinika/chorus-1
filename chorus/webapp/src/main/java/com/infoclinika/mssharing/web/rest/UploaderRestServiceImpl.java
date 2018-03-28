package com.infoclinika.mssharing.web.rest;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.dto.ComposedFileDescription;
import com.infoclinika.mssharing.dto.FileDescription;
import com.infoclinika.mssharing.dto.request.ConfirmMultipartUploadDTO;
import com.infoclinika.mssharing.dto.request.UploadFilesDTORequest;
import com.infoclinika.mssharing.dto.request.UserNamePassDTO;
import com.infoclinika.mssharing.dto.response.*;
import com.infoclinika.mssharing.model.UploadLimitException;
import com.infoclinika.mssharing.model.UploadUnavailable;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.ExperimentCreationHelper;
import com.infoclinika.mssharing.model.helper.RestHelper;
import com.infoclinika.mssharing.model.helper.StoredObjectPaths;
import com.infoclinika.mssharing.model.internal.FileNameSpotter;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.InstrumentLine;
import com.infoclinika.mssharing.model.read.InstrumentReader;
import com.infoclinika.mssharing.model.write.ClientTokenService;
import com.infoclinika.mssharing.model.write.FileMetaDataInfo;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.model.write.billing.BillingManagement;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.common.items.NamedItem;
import com.infoclinika.mssharing.platform.model.common.items.VendorItem;
import com.infoclinika.mssharing.platform.model.helper.CorsRequestSignerTemplate;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate;
import com.infoclinika.mssharing.platform.model.read.InstrumentModelReaderTemplate;
import com.infoclinika.mssharing.web.rest.exception.BadCredentialsException;
import com.infoclinika.mssharing.web.uploader.FileUploadHelper;
import org.apache.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.dto.FunctionTransformerAbstract.fromListDto;
import static com.infoclinika.mssharing.dto.FunctionTransformerAbstract.toListDto;
import static com.infoclinika.mssharing.dto.FunctionTransformerAbstract.toSetDto;
import static com.infoclinika.mssharing.model.write.ClientTokenService.ClientToken;
import static com.infoclinika.mssharing.model.write.billing.BillingManagement.UploadLimitCheckResult;
import static com.infoclinika.mssharing.web.transform.DtoTransformer.*;

/**
 * @author timofey.kasyanov
 *         19.03.14.
 */
public class UploaderRestServiceImpl implements UploaderRestService {

    private static final String DOT = " .";
    private static final Logger LOG = Logger.getLogger(UploaderRestServiceImpl.class);

    @Inject
    private RestHelper restHelper;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private InstrumentManagement instrumentManagement;

    @Inject
    private DashboardReader dashboardReader;

    @Inject
    private InstrumentReader instrumentReader;

    @Inject
    private InstrumentCreationHelperTemplate helper;

    @Inject
    private ExperimentCreationHelper experimentCreationHelper;

    @Inject
    private StoredObjectPaths storedObjectPaths;

    @Inject
    private BillingManagement billingManagement;

    @Inject
    private ClientTokenService clientTokenService;
    @Inject
    private CorsRequestSignerTemplate requestSigner;


    @Override
    public AuthenticateDTO authenticate(UserNamePassDTO credentials) {

        LOG.debug("Authenticating desktop app user: " + credentials);

        final RestHelper.UserDetails userDetails =
                restHelper.getUserDetailsByEmail(credentials.getUsername());

        if (userDetails == null
                || !passwordEncoder.matches(credentials.getPassword(), userDetails.passwordHash)
                || !userDetails.emailVerified) {

            LOG.debug("Bad credentials: " + credentials);
            throw new BadCredentialsException();

        }

        return prolongOrCreateNewToken(userDetails);
    }

    @Override
    public AuthenticateDTO authenticate(String token) {
        LOG.debug("Authenticating desktop app token: " + token);

        final Long userId = clientTokenService.readUserByToken(new ClientToken(token));
        if (userId == null) {
            LOG.debug("Bad token: " + token);
            throw new BadCredentialsException();
        }

        final RestHelper.UserDetails userDetails = restHelper.getUserDetails(userId);
        if (!userDetails.emailVerified) {
            LOG.debug("Error. Email user with id: " + userDetails.id + ", and email: " + userDetails.email + " wasn't verified.");
            throw new BadCredentialsException();
        }

        return prolongOrCreateNewToken(userDetails);
    }

    @Override
    public Set<DictionaryDTO> getTechnologyTypes(String token) {
        LOG.debug("Getting Technology Types. " + token);

        getUserAndCheckToken(token);

        return toSetDto(helper.studyTypes(), TO_DICTIONARY);
    }

    @Override
    public Set<DictionaryDTO> getVendors(String token) {
        LOG.debug("Getting Vendors. " + token);

        getUserAndCheckToken(token);

        return toSetDto(helper.vendors(), TO_DICTIONARY);
    }

    @Override
    public Set<DictionaryDTO> getLabs(String token) {
        LOG.debug("Getting Laboratories. " + token);

        final RestHelper.UserDetails userDetails = getUserAndCheckToken(token);
        final ImmutableSortedSet<NamedItem> labs = experimentCreationHelper.availableLabs(userDetails.id);

        return toSetDto(labs, new Function<NamedItem, DictionaryDTO>() {
            @Nullable
            @Override
            public DictionaryDTO apply(@Nullable NamedItem input) {
                return new DictionaryDTO(input.id, input.name);
            }
        });
    }

    @Override
    public Set<DictionaryDTO> getInstrumentModels(String token, long technologyType, long vendor) {
        LOG.debug("Getting Instrument Models. " + token);

        final RestHelper.UserDetails userDetails = getUserAndCheckToken(token);
        final Set<InstrumentModelReaderTemplate.InstrumentModelLineTemplate> instrumentModels = dashboardReader.readByStudyTypeAndVendor(
                userDetails.id,
                technologyType,
                vendor
        );

        return toSetDto(instrumentModels, new Function<InstrumentModelReaderTemplate.InstrumentModelLineTemplate, DictionaryDTO>() {
            @Nullable
            @Override
            public DictionaryDTO apply(@Nullable InstrumentModelReaderTemplate.InstrumentModelLineTemplate input) {
                return new DictionaryDTO(input.id, input.name);
            }
        });
    }

    @Override
    public InstrumentDTO getInstrument(long instrument, String token) {
        LOG.debug("Returning instrument to a desktop app user. " + token);

        final InstrumentItem instrumentItem = dashboardReader.readInstrument(instrument);

        return TO_INSTRUMENT_DTO.apply(instrumentItem);
    }

    @Override
    public InstrumentDTO createDefaultInstrument(long lab, long instrumentModel, String token) {
        LOG.debug("Creating default instrument for lab: " + lab);

        final RestHelper.UserDetails userDetails = getUserAndCheckToken(token);

        final Optional<InstrumentLine> defaultInstrumentOpt = instrumentReader.readDefaultInstrument(userDetails.id, lab, instrumentModel);
        if (defaultInstrumentOpt.isPresent()) {
            return TO_INSTRUMENT_DTO.apply(dashboardReader.readInstrument(defaultInstrumentOpt.get().id));
        }

        final long defaultInstrument = instrumentManagement.createDefaultInstrument(userDetails.id, lab, instrumentModel);
        final InstrumentItem instrumentItem = dashboardReader.readInstrument(defaultInstrument);

        return TO_INSTRUMENT_DTO.apply(instrumentItem);
    }

    @Override
    public List<InstrumentDTO> getInstruments(String token, long instrumentModel) {
        LOG.debug("Returning instruments to a desktop app user. " + token);

        final RestHelper.UserDetails userDetails = getUserAndCheckToken(token);
        final List<InstrumentLine> instruments = instrumentReader.findByInstrumentModel(userDetails.id, instrumentModel);

        return toListDto(instruments, TO_SIMPLE_INSTRUMENT_DTO);
    }

    @Override
    public List<InstrumentDTO> getInstruments(String token) {
        LOG.debug("Returning instruments to a desktop app user. " + token);

        final RestHelper.UserDetails userDetails = getUserAndCheckToken(token);
        final Set<InstrumentItem> instrumentItems =
                dashboardReader.readInstrumentsWhereUserIsOperator(userDetails.id);

        return toListDto(instrumentItems, TO_INSTRUMENT_DTO);
    }

    @Override
    public Set<FileDTO> getInstrumentFiles(Long instrumentId, String token) {

        LOG.debug("Returning instrument files to a desktop app user. Instrument: " + instrumentId + DOT + token);

        final RestHelper.UserDetails userDetails = getUserAndCheckToken(token);
        final Set<FileLine> fileLines = dashboardReader.readFilesByInstrument(userDetails.id, instrumentId)
                .stream()
                .filter(fileLine -> !fileLine.toReplace)
                .collect(Collectors.toSet());
        return toSetDto(fileLines, TO_FILE_DTO);
    }

    @Override
    public FilesReadyToUploadResponse isReadyToUpload(FilesReadyToUploadRequest request, String token) {
        LOG.debug("Checking if file is already uploaded. Instrument: " + request.instrumentId + DOT + token);

        final long instrumentId = request.instrumentId;

        final InstrumentItem instrument = dashboardReader.readInstrument(instrumentId);
        final VendorItem vendor = instrument.vendor;

        final FileDescription[] fileDescriptions = FileUploadHelper.filesReadyToUpload(
                getUserAndCheckToken(token).id,
                instrumentId,
                vendor,
                request.fileDescriptions,
                instrumentManagement,
                dashboardReader
        );

        return new FilesReadyToUploadResponse(fileDescriptions);
    }

    @Override
    public ComposeFilesResponse composeFiles(ComposeFilesRequest request, String token) {
        LOG.debug("Composing files. Instrument: " + request.instrumentId + DOT + token);

        final long instrumentId = request.instrumentId;

        final InstrumentItem instrument = dashboardReader.readInstrument(instrumentId);
        final VendorItem vendor = instrument.vendor;

        final ComposedFileDescription[] composedFileDescriptions = FileUploadHelper.composeFiles(vendor, request.fileDescriptions);

        return new ComposeFilesResponse(composedFileDescriptions);
    }

    @Override
    public Set<DictionaryDTO> getSpecies(String token) {

        LOG.debug("Returning species to a desktop app user. " + token);

        getUserAndCheckToken(token);
        final Set<DictionaryItem> dictionaryItems = experimentCreationHelper.species();

        return toSetDto(dictionaryItems, TO_DICTIONARY);
    }

    @Override
    public DictionaryDTO getDefaultSpecie(String token) {

        LOG.debug("Returning default specie to a desktop app user. " + token);

        getUserAndCheckToken(token);
        final DictionaryItem defaultSpecie = experimentCreationHelper.defaultSpecie();

        return TO_DICTIONARY.apply(defaultSpecie);
    }

    @Override
    public List<FileDTO> getUnfinishedUploads(String token) {

        LOG.debug("Returning unfinished uploads to a desktop app user. " + token);

        final RestHelper.UserDetails userDetails = getUserAndCheckToken(token);
        final Set<FileLine> fileLines =
                dashboardReader.readUnfinishedFiles(userDetails.id);

        return toListDto(fileLines, TO_FILE_DTO);
    }

    @Override
    public DeleteUploadDTO deleteUpload(Long fileId, String token) {

        LOG.debug("Deleting upload for a desktop app user. File: " + fileId + DOT + token);

        final RestHelper.UserDetails userDetails = getUserAndCheckToken(token);
        instrumentManagement.cancelUpload(userDetails.id, fileId);

        return new DeleteUploadDTO(true);
    }

    @Override
    public UploadFilesDTOResponse uploadRequest(UploadFilesDTORequest request, String token) {

        LOG.debug("Handling an composite upload request for a desktop app user. Target instrument: " + request.getInstrument() + DOT + token);

        final RestHelper.UserDetails userDetails = getUserAndCheckToken(token);

        if (!userDetails.hasLaboratories) {
            throw new AccessDenied("permission denied");
        }

        final long userId = userDetails.id;
        final long instrumentId = request.getInstrument();
        final InstrumentItem instrumentItem = dashboardReader.readInstrument(instrumentId);

        long uploadSize = request.getFiles().stream().mapToLong(UploadFilesDTORequest.UploadFile::getSize).sum();
        instrumentManagement.checkCanUploadMore(instrumentId, uploadSize);

        //if billing is enabled
        if (dashboardReader.getFeatures(userId).get(ApplicationFeature.BILLING.getFeatureName())) {
            final UploadLimitCheckResult checkResult = billingManagement.checkUploadLimit(userId, instrumentItem.lab);

            if (checkResult != null && checkResult.isExceeded) {
                throw new UploadLimitException(checkResult.message);
            }

            if (!restHelper.canUploadForInstrument(instrumentId)) {
                return new UploadFilesDTOResponse(
                        instrumentId,
                        new ArrayList<>()
                );
            }
        }


        final List<UploadFilesDTOResponse.UploadFile> responseFiles = newArrayList();
        final List<InstrumentManagement.UploadFileItem> files =
                fromListDto(request.getFiles(), FROM_FILES_REQUEST);

        for (InstrumentManagement.UploadFileItem file : files) {
            final String fileName = FileNameSpotter.replaceInvalidSymbols(file.name);
            final Set<FileLine> uploadedFileWithSameName = dashboardReader.readByNameForInstrument(userId, instrumentId, fileName);

            final boolean uploaded = !uploadedFileWithSameName.isEmpty();
            final String path = storedObjectPaths.rawFilePath(userId, instrumentId, fileName).getPath();

            final long finalFileId;
            if (!uploaded) {
                finalFileId = instrumentManagement.createFile(
                        userId,
                        instrumentId,
                        new FileMetaDataInfo(fileName, file.size, file.labels, path, file.specie, file.archive)
                );
            } else {
                final FileLine fileLine = uploadedFileWithSameName.iterator().next();
                finalFileId = fileLine.id;

                if (fileLine.toReplace) {
                    instrumentManagement.updateFile(
                            userId,
                            fileLine.id,
                            new FileMetaDataInfo(
                                    fileName,
                                    file.size,
                                    file.labels,
                                    path,
                                    file.specie,
                                    file.archive
                            )
                    );
                }
            }

            final UploadFilesDTOResponse.UploadFile uploadFile
                    = new UploadFilesDTOResponse.UploadFile(fileName, finalFileId, path);

            uploadFile.setStarted(uploaded);

            responseFiles.add(uploadFile);

        }

        return new UploadFilesDTOResponse(instrumentId, responseFiles);
    }

    @Override
    public SSEUploadFilesDTOResponse sseUploadRequest(UploadFilesDTORequest request, String token) {

        LOG.debug("Handling an sse upload request for a desktop app user. Target instrument: " + request.getInstrument() + DOT + token);

        final RestHelper.UserDetails userDetails = getUserAndCheckToken(token);

        if (!userDetails.hasLaboratories) {
            throw new AccessDenied("permission denied");
        }

        final long userId = userDetails.id;
        final long instrumentId = request.getInstrument();
        final InstrumentItem instrumentItem = dashboardReader.readInstrument(instrumentId);
        final long uploadSize = request.getFiles().stream().mapToLong(UploadFilesDTORequest.UploadFile::getSize).sum();

        //if billing is enabled
        if (dashboardReader.getFeatures(userId).get(ApplicationFeature.BILLING.getFeatureName())) {
            final UploadLimitCheckResult checkResult = billingManagement.checkUploadLimit(userId, instrumentItem.lab);

            if (checkResult != null && checkResult.isExceeded) {
                throw new UploadLimitException(checkResult.message);
            }

            instrumentManagement.checkCanUploadMore(instrumentId, uploadSize);

            if (!restHelper.canUploadForInstrument(instrumentId)) {
                return new SSEUploadFilesDTOResponse(instrumentId, new ArrayList<>());
            }
        }

        final List<SSEUploadFilesDTOResponse.UploadFileItem> responseFiles = newArrayList();
        final List<InstrumentManagement.UploadFileItem> files =
                fromListDto(request.getFiles(), FROM_FILES_REQUEST);

        for (InstrumentManagement.UploadFileItem file : files) {
            final String fileName = FileNameSpotter.replaceInvalidSymbols(file.name);
            final String path = storedObjectPaths.rawFilePath(userId, instrumentId, fileName).getPath();

            final CorsRequestSignerTemplate.SignedRequest signedRequest = requestSigner.signInitialUploadRequest(userId, path);

            final SSEUploadFilesDTOResponse.UploadFileItem uploadFileItem = new SSEUploadFilesDTOResponse.UploadFileItem(
                    path,
                    signedRequest.authorization,
                    signedRequest.dateAsString,
                    requestSigner.useServerSideEncryption()
            );

            responseFiles.add(uploadFileItem);
        }

        return new SSEUploadFilesDTOResponse(instrumentId, responseFiles);
    }

    @Override
    public CompleteUploadDTO completeUpload(ConfirmMultipartUploadDTO request, String token) {

        LOG.debug("Completing an upload request for a desktop app user. Target file: "
                + request.getFileId()
                + ". Destination: " + request.getRemoteDestination() + DOT
                + token);

        final RestHelper.UserDetails userDetails = getUserAndCheckToken(token);

        try {

            instrumentManagement.completeMultipartUpload(
                    userDetails.id,
                    request.getFileId(),
                    request.getRemoteDestination()
            );

        } catch (UploadUnavailable e) {

            LOG.error("Upload unavailable", e);
            return new CompleteUploadDTO(false);
        }

        return new CompleteUploadDTO(true);
    }

    private RestHelper.UserDetails getUserAndCheckToken(String restToken) throws AccessDenied {

        final RestHelper.UserDetails userDetails = restHelper.checkToken(restToken);

        if (userDetails == null) {
            throw new AccessDenied("permission denied");
        }

        return userDetails;
    }


    private AuthenticateDTO prolongOrCreateNewToken(RestHelper.UserDetails userDetails) {
        RestHelper.Token token = restHelper.findAndProlongToken(userDetails.email);

        if (token == null) {
            token = restHelper.generateToken(userDetails);
        }

        final UploadConfigDTO uploadConfig = new UploadConfigDTO(
                storedObjectPaths.getAmazonKey(),
                storedObjectPaths.getAmazonSecret(),
                storedObjectPaths.getRawFilesBucket()
        );

        LOG.debug("Authentication successful. Returning auth token for user: " + userDetails.email + ". Token: " + token);
        return new AuthenticateDTO(token.token, userDetails.email, uploadConfig);
    }

}
