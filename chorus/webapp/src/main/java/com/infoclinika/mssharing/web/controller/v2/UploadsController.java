package com.infoclinika.mssharing.web.controller.v2;

import com.infoclinika.mssharing.model.UploadLimitException;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.upload.FileDetails;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadDetails;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadType;
import com.infoclinika.mssharing.model.internal.repository.InstrumentRepository;
import com.infoclinika.mssharing.model.internal.repository.UploadDetailsRepository;
import com.infoclinika.mssharing.model.internal.s3client.AWSConfigService;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.model.write.dto.UploadLimitCheckResult;
import com.infoclinika.mssharing.platform.model.FileTransferNotifier;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import com.infoclinika.mssharing.web.controller.request.CheckUploadLimitRequest;
import com.infoclinika.mssharing.web.controller.response.ValueResponse;
import com.infoclinika.mssharing.web.controller.v2.dto.*;
import com.infoclinika.mssharing.web.controller.v2.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.model.internal.entity.upload.UploadFileStatus.*;
import static com.infoclinika.mssharing.model.internal.entity.upload.UploadStatus.STARTED;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

/**
 * @author Oleksii Stotskyi
 */
@RestController
@RequestMapping("/v2/uploads")
public class UploadsController {

    @Inject
    private InstrumentRepository instrumentRepository;
    @Inject
    @Named("InboxFileTransferNotifier")
    private FileTransferNotifier inboxNotifier;
    @Inject
    private UploadDetailsRepository uploadDetailsRepository;
    @Inject
    private ImportServiceFactory importServiceFactory;
    @Inject
    private TrackingUploadService trackingService;
    @Inject
    private ActiveBucketService activeBucketService;
    @Inject
    private AWSConfigService awsConfigService;
    @Inject
    private S3ReferencesService s3ReferencesService;
    @Inject
    private FileImportHelper fileImportHelper;
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private S3PolicyProvider s3PolicyProvider;
    @Inject
    private InstrumentManagement instrumentManagement;
    @Inject
    private DashboardReader dashboardReader;

    @RequestMapping(value = "/types", method = RequestMethod.GET)
    public UploadTypeDTO[] uploadTypes() {
        return Arrays.stream(UploadType.class.getEnumConstants())
            .filter(UploadType::isWebEnabled)
            .filter(t -> t == UploadType.S3_COPY || t == UploadType.S3_LINK)
            .map(ut -> new UploadTypeDTO(ut.name(), ut.getTitle()))
            .toArray(UploadTypeDTO[]::new);
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String userArn(Principal principal) {
        getUserId(principal);
        return awsConfigService.getUserArn();
    }

    @RequestMapping(value = "/workflowArn", method = RequestMethod.GET)
    public String workflowArn(Principal principal) {
        getUserId(principal);
        return awsConfigService.getWorkflowArn();
    }

    @RequestMapping(value = "/getBucketPolicy", method = RequestMethod.GET)
    public String getBucketPolicy(@RequestParam("s3Url") String s3Url, Principal principal) {
        getUserId(principal);
        return s3PolicyProvider.getBucketPolicy(s3Url);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<UploadDetailsDTO> usersActiveUploads() {
        return uploadDetailsRepository.findByUserIdAndStatus(RichUser.getCurrentUserId(), STARTED)
            .stream()
            .map(UploadDetailsDTO::of)
            .collect(toList());
    }

    @RequestMapping(method = RequestMethod.POST)
    public UploadDetailsDTO uploadCreate(@RequestBody UploadCreateDTO src) {
        final long userId = RichUser.getCurrentUserId();

        if (!ruleValidator.userCanUploadFileOfInstrument(userId, src.getInstrumentId())) {
            throw new ForbiddenException();
        }

        final UploadDetailsDTO upload = new UploadDetailsDTO();
        upload.setType(src.getType());

        final UploadDetails uploadDetails = new UploadDetails();
        uploadDetails.setUserId(userId);
        uploadDetails.setType(src.getType());
        uploadDetails.setUrl(src.getUrl());
        uploadDetails.setMasks(src.getMasks());
        uploadDetails.setRecursive(src.isRecursive());
        uploadDetails.setInstrumentId(src.getInstrumentId());

        if (src.getType() != UploadType.S3_LINK) {
            uploadDetails.setLogin(src.getUser());
            uploadDetails.setPassword(src.getPass());
        }

        if (src.getType() != UploadType.DIRECT) {

            final FileImportService importService = importServiceFactory.ofType(src.getType());
            final Predicate<String> filter = importService.buildFilter(Arrays.asList(src.getMasks()));
            final List<FileDetails> files;
            try {
                files = importService.listFiles(uploadDetails.getLogin(),
                    uploadDetails.getPassword(),
                    src.getUrl(),
                    src.isRecursive(),
                    filter
                );
            } catch (Exception e) {
                throw new ApiException(e);
            }

            files.stream()
                .filter(f -> fileImportHelper.isFileAlreadyUploaded(userId, src.getInstrumentId(), f.getFileName()))
                .forEach(f -> f.setStatus(REGISTERED));

            uploadDetails.setFiles(files);

            final List<UploadFileDTO> filesDto = files
                .stream()
                .map(UploadFileDTO::of)
                .collect(toList());
            upload.setFiles(filesDto);
        }

        uploadDetailsRepository.save(uploadDetails);
        upload.setId(uploadDetails.getId());

        return upload;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public UploadDetailsDTO uploadDetails(@PathVariable("id") long id) {
        return UploadDetailsDTO.of(Optional.ofNullable(uploadDetailsRepository.findOne(id))
            .orElseThrow(NotFoundException::new));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void uploadUpdate(
        @PathVariable("id") String id,
        @RequestBody UploadUpdateDTO dto
    ) {
        String join = String.join("|", dto.getFiles());
        if (!md5Hex(join).equals(id)) {
            throw new ApiException("Wrong params");
        }

        if (dto.isDone()) {
            Lab lab = instrumentRepository.labOfInstrument(dto.getInstrumentId());
            List<DictionaryItem> files = dto.getFiles()
                .stream()
                .map(f -> new DictionaryItem(0, f))
                .collect(toList());
            inboxNotifier.notifyFileTransferCompleted(RichUser.getCurrentUserId(), lab.getId(), files);
        }
    }

    @RequestMapping(value = "/checkUploadLimit", method = RequestMethod.GET)
    @ResponseBody
    public ValueResponse checkUploadLimit(CheckUploadLimitRequest request) {

        final long userId = RichUser.getCurrentUserId();
        final UploadLimitCheckResult uploadLimit =
            instrumentManagement.checkUploadLimit(userId, request.getLabId(), request.getBytesToUpload());
        final boolean uploadAvailable = !uploadLimit.exceeded;

        return new ValueResponse<>(uploadAvailable);
    }

    @RequestMapping(value = "/{id}/files", method = RequestMethod.PUT)
    public void uploadFiles(@PathVariable("id") long id, @RequestBody List<UploadFileDTO> dto) {

        final Map<String, UploadFileDTO> map = dto
            .stream()
            .collect(Collectors.toMap(UploadFileDTO::getFullName, Function.identity()));
        final long userId = RichUser.getCurrentUserId();
        final UploadDetails upload = uploadDetailsRepository.findOne(id);
        final long instrumentId = upload.getInstrumentId();
        final InstrumentItem instrumentItem = dashboardReader.readInstrument(instrumentId);
        final List<FileDetails> files = upload.getFiles()
            .stream()
            .filter(f -> map.containsKey(f.getFileName()))
            .filter(f -> !fileImportHelper.isFileAlreadyUploaded(userId, instrumentId, f.getFileName()))
            .map(f -> {
                final UploadFileDTO src = map.get(f.getFileName());
                f.setLabels(src.getLabels());
                f.setSpecieId(src.getSpecieId());
                f.setStatus(QUEUE);
                return f;
            })
            .collect(toList());
        final long uploadSize = files.stream().mapToLong(FileDetails::getSize).sum();
        final UploadLimitCheckResult uploadLimit =
            instrumentManagement.checkUploadLimit(userId, instrumentItem.lab, uploadSize);

        if (uploadLimit.exceeded) {
            throw new UploadLimitException(uploadLimit.message);
        }

        upload.setFiles(files);

        final List<String> fileNames = upload.getFiles()
            .stream()
            .filter(f -> f.getStatus() != REGISTERED)
            .map(FileDetails::getFileName)
            .collect(toList());

        trackingService.startTracking(upload);
        final FileImportService importService = importServiceFactory.ofType(upload.getType());
        try {
            if (upload.getType() == UploadType.S3_LINK) {
                s3ReferencesService.createReferences(userId, instrumentId, upload.getUrl(), upload.getFiles());
            } else {
                final Function<String, String> dstKeyGenerator =
                    activeBucketService.importKeyGenerator(userId, instrumentId);
                importService.copyFiles(
                    upload.getLogin(),
                    upload.getPassword(),
                    upload.getUrl(),
                    fileNames,
                    dstKeyGenerator
                );
            }
            upload.setStatus(STARTED);
            uploadDetailsRepository.save(upload);
        } catch (FileImportException e) {
            throw new InternalServerErrorException("Can't start upload");
        }
    }

    @RequestMapping(value = "/{id}/files", method = RequestMethod.GET)
    public List<UploadFileDTO> uploadStatus(@PathVariable("id") long id) {
        try {
            final UploadDetails upload = trackingService.getTrackingDetails(id);

            return upload.getFiles()
                .stream()
                .filter(f -> f.getStatus() != CANCELED)
                .map(UploadFileDTO::of)
                .collect(toList());
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    @RequestMapping(value = "/{uploadId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void cancelUpload(@PathVariable("uploadId") long uploadId) {
        final UploadDetails upload = trackingService.getTrackingDetails(uploadId);

        if (upload == null) {
            throw new NotFoundException();
        }

        if (RichUser.getCurrentUserId() != upload.getUserId()) {
            throw new ForbiddenException();
        }

        try {
            trackingService.cancelUpload(uploadId);
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    @RequestMapping(value = "/{uploadId}/files/{fileId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void cancelUploadFile(@PathVariable("uploadId") long uploadId, @PathVariable("fileId") long fileId) {
        final UploadDetails upload = trackingService.getTrackingDetails(uploadId);

        if (upload == null) {
            throw new NotFoundException();
        }
        upload.getFiles()
            .stream()
            .filter(f -> f.getFileId() == fileId)
            .findAny()
            .orElseThrow(NotFoundException::new);

        if (RichUser.getCurrentUserId() != upload.getUserId()) {
            throw new ForbiddenException();
        }

        try {
            trackingService.cancelFile(uploadId, fileId);
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }
}
