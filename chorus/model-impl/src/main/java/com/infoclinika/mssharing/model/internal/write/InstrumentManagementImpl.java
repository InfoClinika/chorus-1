/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.write;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.model.UploadUnavailable;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.*;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.RuleValidatorImpl;
import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.internal.entity.restorable.*;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.write.*;
import com.infoclinika.mssharing.model.write.billing.BillingManagement;
import com.infoclinika.mssharing.model.write.dto.UploadLimitCheckResult;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.entity.Species;
import com.infoclinika.mssharing.platform.entity.VendorExtension;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.StoredObject;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.ActionsNotAllowedException;
import com.infoclinika.mssharing.platform.model.InstrumentsDefaults;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate;
import com.infoclinika.mssharing.platform.model.impl.write.DefaultInstrumentManagement;
import com.infoclinika.mssharing.platform.model.write.FileManagementTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate.AccessedInstrument;
import com.infoclinika.mssharing.platform.repository.SpeciesRepositoryTemplate;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.util.*;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Stanislav Kurilin
 */
@Service("instrumentManagement")
@Transactional
public class InstrumentManagementImpl
    extends DefaultInstrumentManagement<Instrument, InstrumentDetails, InstrumentCreationRequest>
    implements InstrumentManagement {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentManagementImpl.class);
    public static final Function<ActiveFileMetaData, Long> FILE_TO_ID = new Function<ActiveFileMetaData, Long>() {
        @Override
        public Long apply(ActiveFileMetaData input) {
            return input.getId();
        }
    };
    private static final String DEFAULT_INSTRUMENT_NAME = InstrumentsDefaults.DEFAULT_INSTRUMENT_NAME;
    private static final String EMPTY_INSTRUMENT_NAME = null;

    @Inject
    private FileMetaDataRepository fileRepository;
    @Inject
    private StorageService fileStorageService;
    @Inject
    private SpeciesRepositoryTemplate<Species> speciesRepository;
    @Inject
    private RawFilesRepository experimentFileRepository;
    @Inject
    private TransactionTemplate transactionTemplate;
    @Inject
    private LabRepository labRepository;
    @Inject
    private StoredObjectPaths storedObjectPaths;
    @Inject
    private BillingFeaturesHelper billingFeaturesHelper;
    @Inject
    private FileMovingManager fileMovingManager;
    @Inject
    private FileDownloadJobRepository fileDownloadJobRepository;
    @Inject
    private FileDownloadGroupRepository fileDownloadGroupRepository;
    @Inject
    private DeletedFileMetaDataRepository deletedFileMetaDataRepository;
    @Inject
    private DeletedExperimentRepository deletedExperimentRepository;
    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private FileAccessLogService fileAccessLogService;
    @Inject
    private FileManagementTemplate<FileMetaDataInfo> fileManagement;
    @Inject
    private FileOperationsManager fileOperationsManager;
    @Inject
    private InstrumentRepository instrumentRepository;
    @Inject
    private InstrumentCreationHelperTemplate instrumentCreationHelper;
    @Inject
    private BillingManagement billingManagement;
    @Inject
    private FeaturesHelper featuresHelper;


    @Override
    protected Instrument onCreateInstrument(long creator, long labId, long model, InstrumentDetails instrumentDetails) {
        return managementHelper.createInstrument(creator, labId, model,
            instrumentDetails, createSetInstrumentPropertiesFn(instrumentDetails)
        );
    }

    private Function<Instrument, Instrument> createSetInstrumentPropertiesFn(
        final InstrumentDetails instrumentDetails
    ) {
        return input -> {
            input.setHplc(instrumentDetails.hplc);
            input.getLockMasses().clear();
            input.getLockMasses().addAll(transformToLockMz(instrumentDetails));
            return input;
        };
    }

    @Override
    protected InstrumentCreationRequest onNewInstrumentRequest(long creator,
                                                               long labId,
                                                               long model,
                                                               InstrumentDetails instrumentDetails) {
        return managementHelper.newCreationRequest(creator,
            labId, model, instrumentDetails,
            createSetInstrumentRequestPropsFn(instrumentDetails)
        );
    }

    private Function<InstrumentCreationRequest, InstrumentCreationRequest> createSetInstrumentRequestPropsFn(
        final InstrumentDetails details
    ) {
        return input -> {
            input.setHplc(details.hplc);
            input.getLockMasses().clear();
            input.getLockMasses().addAll(transformToLockMz(details));
            return input;
        };
    }

    private List<LockMz> transformToLockMz(InstrumentDetails details) {
        return Lists.transform(
            fromNullable(details.lockMasses).or(Collections.<LockMzItem>emptyList()),
            Transformers.LOCK_MZ_FUNCTION
        );
    }

    @Override
    protected InstrumentCreationRequest onUpdateNewInstrumentRequest(
        long model,
        InstrumentDetails instrumentDetails,
        InstrumentCreationRequest instrumentCreationRequest
    ) {
        return managementHelper.updateInstrumentRequest(
            instrumentCreationRequest,
            model,
            instrumentDetails,
            createSetInstrumentRequestPropsFn(instrumentDetails)
        );
    }

    @Override
    protected Instrument onApproveInstrumentCreation(long actor, long requestId) {
        final InstrumentCreationRequest one = instrumentCreationRequestRepository.findOne(requestId);
        return managementHelper.approveInstrumentCreation(actor, requestId, new Function<Instrument, Instrument>() {
            @Override
            public Instrument apply(Instrument input) {
                input.setHplc(one.getHplc());
                input.getLockMasses().addAll(one.getLockMasses());
                return input;
            }
        });
    }

    @Override
    protected void onEditInstrument(long instrumentId, InstrumentDetails instrumentDetails) {
        managementHelper.updateInstrument(
            findInstrument(instrumentId),
            instrumentDetails,
            createSetInstrumentPropertiesFn(instrumentDetails)
        );
    }

    @Override
    public Long findUploadResumableFile(long user, long instrument, final String fileName) {
        final ActiveFileMetaData existingFile =
            Iterables.find(fileRepository.byInstrument(instrument), new Predicate<ActiveFileMetaData>() {
                @Override
                public boolean apply(ActiveFileMetaData input) {
                    return input.getName().equals(fileName);
                }
            }, null);
        return existingFile == null ? null : existingFile.getId();
    }

    @Override
    public long startUploadFile(long actor, long instrument, final FileMetaDataInfo fileMetaDataInfo) {
        long fileId = createFile(actor, instrument, fileMetaDataInfo);
        fileAccessLogService.logFileUploadStart(actor, instrument, fileId);
        return fileId;
    }

    @Override
    public long createFile(long actor, long instrument, final FileMetaDataInfo fileMetaDataInfo) {
        return fileManagement.createFile(actor, instrument, fileMetaDataInfo);
    }

    @Override
    public void cancelUpload(long actor, long file) {
        final ActiveFileMetaData entity = load(file);
        if (!entity.getOwner().equals(Util.USER_FROM_ID.apply(actor))) {
            throw new AccessDenied("Only owner is able to cancel file upload");
        }
        fileRepository.delete(entity);
    }

    @Override
    public void pingUpload(long actor, long file) {
        final ActiveFileMetaData entity = load(file);
        if (!entity.getOwner().equals(Util.USER_FROM_ID.apply(actor))) {
            throw new AccessDenied("Only owner is able to update file upload");
        }
        entity.setLastPingDate(new Date());
        fileRepository.save(entity);
    }

    @Override
    public void setContent(long actor, long file, StoredObject content) {
        final ActiveFileMetaData entity = load(file);
        if (!entity.getOwner().equals(Util.USER_FROM_ID.apply(actor))) {
            throw new AccessDenied("Only owner can set content");
        }
        if (RuleValidatorImpl.IS_UPLOAD_COMPLETE.test(entity)) {
            throw new AccessDenied("Content already set");
        }
        final NodePath path = storedObjectPaths.rawFilePath(actor, entity.getInstrument().getId(), entity.getName());
        fileStorageService.put(path, content);
        entity.setContentId(path.getPath());
        LOGGER.debug(
            "the content for file with ID = {} for user {} has been set. Path = {}",
            file,
            actor,
            path.getPath()
        );
        fileRepository.save(entity);
    }

    @Override
    public void startMultipartUpload(long actor, long file, String uploadId, String destinationPath) {
        final ActiveFileMetaData entity = load(file);
        if (!entity.getOwner().equals(Util.USER_FROM_ID.apply(actor))) {
            throw new AccessDenied("Only owner can set upload ID");
        }
        if (RuleValidatorImpl.IS_UPLOAD_COMPLETE.test(entity)) {
            throw new AccessDenied("Content already set, cannot set upload ID");
        }
        entity.setUploadId(uploadId);
        entity.setDestinationPath(destinationPath);
        LOGGER.debug("The multipart upload ID = {} for user {} has been set: {}", file, actor, uploadId);
        fileRepository.save(entity);
    }

    @Override
    public void completeMultipartUpload(final long actor, final long file, final String contentId) {

        final Instrument instrument = fileRepository.findOne(file).getInstrument();
        final Lab lab = instrument.getLab();
        final boolean storageOn = isStorageEnabled(lab);
        final Long labId = lab.getId();

        if (!storageOn) {
            throw new UploadUnavailable(
                "Storage feature is off for laboratory. Lab id: " + labId + ", Lab name: " + lab.getName());
        }

        final ActiveFileMetaData entity = load(file);

        if (!entity.getOwner().equals(Util.USER_FROM_ID.apply(actor))) {
            throw new AccessDenied("Only owner can set content");
        }

        if (RuleValidatorImpl.IS_UPLOAD_COMPLETE.test(entity) && !entity.isToReplace()) {
            throw new AccessDenied("Content already set");
        }

        final Long id = transactionTemplate.execute(status -> {
            entity.setContentId(contentId);
            LOGGER.debug(
                "The content for file with ID = {} for user {} has been set. Path = {}",
                file,
                actor,
                contentId
            );
            entity.setUploadId(null);
            entity.setDestinationPath(null);
            fileRepository.save(entity);
            return entity.getId();
        });

        fileAccessLogService.logFileUploadConfirm(actor, labId, id);
        fileOperationsManager.checkIsFileConsistent(actor, id);
    }

    private boolean isStorageEnabled(Lab lab) {
        return billingFeaturesHelper.isFeatureEnabled(lab.getId(), BillingFeature.ANALYSE_STORAGE);
    }


    @Override
    public void setContentID(long actor, long file, String contentID) {
        final ActiveFileMetaData entity = load(file);
        if (!entity.getOwner().equals(Util.USER_FROM_ID.apply(actor))) {
            throw new AccessDenied("Only owner can set content");
        }
        if (RuleValidatorImpl.IS_UPLOAD_COMPLETE.test(entity)) {
            throw new AccessDenied("Content already set");
        }
        entity.setContentId(contentID);
        LOGGER.debug("The content for file with ID = {} for user {} has been set. Path = {}", file, actor, contentID);
        fileRepository.save(entity);
    }

    private ActiveFileMetaData load(long file) {
        return checkNotNull(fileRepository.findOne(file));
    }

    @Override
    public void setLabels(long actor, long file, String newLabels) {
        final ActiveFileMetaData entity = load(file);
        if (!entity.getOwner().equals(Util.USER_FROM_ID.apply(actor))) {
            throw new AccessDenied("Only owner can edit file");
        }
        if (!RuleValidatorImpl.IS_UPLOAD_COMPLETE.test(entity)) {
            throw new AccessDenied("Upload file first");
        }
        entity.setLabels(newLabels);
        fileRepository.save(entity);
    }

    @Override
    public void bulkSetLabels(long actor, Set<Long> files, String newLabels, boolean appendExistingLabels) {

        final FluentIterable<ActiveFileMetaData> entities = filterByUserAccessToFile(actor, files);

        //then perform editing
        for (ActiveFileMetaData entity : entities) {
            if (appendExistingLabels) {
                entity.setLabels(entity.getLabels() + " " + newLabels);
            } else {
                entity.setLabels(newLabels);
            }
        }
        fileRepository.save(entities);
        LOGGER.debug("Labels has been set for files: {}", Lists.transform(entities.toList(), FILE_TO_ID));
    }

    @Override
    public void bulkSetSpecies(final long actor, Set<Long> fileIds, long newValue) {

        final Species specie =
            checkNotNull(speciesRepository.findOne(newValue), "Specie with given id not found: " + newValue);

        final List<Long> usedInExperiments = fileRepository.usedInExperiments(fileIds);

        final FluentIterable<ActiveFileMetaData> files =
            filterByUserAccessToFile(actor, fileIds).filter(new Predicate<ActiveFileMetaData>() {
                @Override
                public boolean apply(ActiveFileMetaData input) {
                    return !usedInExperiments.contains(input.getId());
                }
            });

        for (ActiveFileMetaData file : files) {
            file.setSpecie(specie);
        }
        fileRepository.save(files);
        LOGGER.debug("Species has been set for files: {}", Lists.transform(files.toList(), FILE_TO_ID));
    }

    private FluentIterable<ActiveFileMetaData> filterByUserAccessToFile(final long actor, Set<Long> fileIds) {

        return from(fileRepository.findAllByIds(fileIds))
            .filter(or(of(isOperator(actor), isOwner(actor), isLabHead(actor))));
    }

    private Predicate<ActiveFileMetaData> isLabHead(final long actor) {
        return new Predicate<ActiveFileMetaData>() {
            @Override
            public boolean apply(ActiveFileMetaData input) {
                return input.getInstrument().getLab().getHead().getId().equals(actor);
            }
        };
    }

    private Predicate<ActiveFileMetaData> isOwner(final long actor) {
        return new Predicate<ActiveFileMetaData>() {
            @Override
            public boolean apply(ActiveFileMetaData input) {
                return input.getOwner().getId().equals(actor);
            }
        };
    }

    private Predicate<ActiveFileMetaData> isOperator(final long actor) {
        return new Predicate<ActiveFileMetaData>() {
            @Override
            public boolean apply(ActiveFileMetaData entity) {
                if (Util.USER_FROM_ID.apply(actor).getLabs().contains(entity.getInstrument().getLab())) {
                    LOGGER.warn(
                        "Cannot perform operation for file ID = {}. User is not operator of instrument of file; user " +
                            "ID = {}",
                        entity.getId(),
                        actor
                    );
                    return false;
                }
                return true;
            }
        };
    }

    @Override
    public void moveFilesToTrash(long user, List<Long> files) {
        for (Long file : files) {
            moveFileToTrash(user, file);
        }
    }

    @Override
    public void deleteFile(long file) {
        fileAccessLogService.logFileDelete(file);
        removeFile(deletedFileMetaDataRepository, file);
    }

    @Override
    public void deleteFile(long actor, long file, boolean permanently) {

    }

    public void removeFile(final CrudRepository<? extends AbstractFileMetaData, Long> fileRepository, final long file) {

        final AbstractFileMetaData fileMetaData = fileRepository.findOne(file);
        final List<RawFile> rawFiles = experimentFileRepository.findByMetaData(fileMetaData);

        for (RawFile rawFile : rawFiles) {
            experimentFileRepository.delete(rawFile);
        }

        removeAllDownloadJobs(fileMetaData);
        fileRepository.delete(file);
        fileMovingManager.deleteFromStorage(fileMetaData.getContentId());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_COMMITTED) {
                    fileMovingManager.deleteFromStorage(fileMetaData.getContentId());
                    fileMovingManager.deleteFromArchiveStorage(fileMetaData.getArchiveId());
                }
            }
        });
    }

    private void removeAllDownloadJobs(AbstractFileMetaData fileMetaData) {

        final Optional<FileDownloadJob> downloadJob =
            fromNullable(fileDownloadJobRepository.findByFileMetaDataId(fileMetaData.getId()));

        if (!downloadJob.isPresent()) {
            return;
        }

        final List<FilesDownloadGroup> downloadGroups = fileDownloadGroupRepository.findByJob(of(downloadJob.get()));
        for (FilesDownloadGroup downloadGroup : downloadGroups) {
            downloadGroup.getJobs().remove(downloadJob.get());
        }
        fileDownloadGroupRepository.save(downloadGroups);
        fileDownloadJobRepository.delete(downloadJob.get());

    }

    @Override
    public void removeFilesPermanently(final long actor, Set<Long> files) {
        for (Long file : files) {
            removeFilePermanently(actor, file);
        }
    }

    private void removeFilePermanently(long actor, Long file) {
        if (!ruleValidator.canRemoveFile(actor, file)) {
            throw new AccessDenied("Cannot remove file. File id " + file + ", actor: " + actor);
        }
        LOGGER.warn("User ID = {} has initiated permanent file deletion. File ID = {}", actor, file);
        fileAccessLogService.logFileDeletePermanently(actor, file);
        removeFile(fileRepository, file);
    }

    @Override
    public long moveFileToTrash(long actor, long file) {
        if (!ruleValidator.canRemoveFile(actor, file)) {
            throw new AccessDenied("Cannot move file to trash");
        }
        final ActiveFileMetaData fileMetaData = fileRepository.findOne(file);
        DeletedFileMetaData deleted = new DeletedFileMetaData((ActiveFileMetaData) fileMetaData.copy(
            fileMetaData.getName(),
            fileMetaData.getOwner()
        ));
        deleted = deletedFileMetaDataRepository.save(deleted);
        final List<RawFile> rawFiles = experimentFileRepository.findByMetaData(fileMetaData);

        for (RawFile rawFile : rawFiles) {
            rawFile.setFileMetaData(deleted);
            experimentFileRepository.save(rawFile);
        }
        removeDownloadingGlacierInfo(fileMetaData);
        fileRepository.delete(file);
        return deleted.getId();
    }

    @Override
    public long restoreFile(long actor, long file) {
        final DeletedFileMetaData deleted = deletedFileMetaDataRepository.findOne(file);

        if (ruleValidator.fileHasDuplicateName(deleted)) {
            throw new AccessDenied("Couldn't restore file");
        }
        ActiveFileMetaData fileMetaData = (ActiveFileMetaData) deleted.copy(deleted.getName(), deleted.getOwner());
        fileMetaData = fileRepository.save(fileMetaData);
        final List<RawFile> rawFiles = experimentFileRepository.findByMetaData(deleted);

        for (RawFile rawFile : rawFiles) {
            rawFile.setFileMetaData(fileMetaData);
            experimentFileRepository.save(rawFile);
        }
        deletedFileMetaDataRepository.delete(deleted);
        return fileMetaData.getId();
    }

    private void removeDownloadingGlacierInfo(ActiveFileMetaData fileMetaData) {
        final FileDownloadJob job = fileDownloadJobRepository.findByMetaData(fileMetaData);
        if (job != null) {
            final List<FilesDownloadGroup> groups = fileDownloadGroupRepository.findByJob(newHashSet(job));
            for (FilesDownloadGroup group : groups) {
                group.getJobs().remove(job);
                if (group.getJobs().isEmpty()) {
                    fileDownloadGroupRepository.delete(group);
                } else {
                    fileDownloadGroupRepository.save(group);
                }
            }
            fileDownloadJobRepository.delete(job);
        }
    }

    @Override
    protected void beforeDeleteInstrument(long actor, long instrumentId) {
        super.beforeDeleteInstrument(actor, instrumentId);

        Collection<DeletedFileMetaData> deletedFileMetaDatas = deletedFileMetaDataRepository.byInstrument(instrumentId);
        for (DeletedFileMetaData data : deletedFileMetaDatas) {
            Collection<DeletedExperiment> deletedExperiments = deletedExperimentRepository.findByFile(data.getId());
            for (DeletedExperiment experiment : deletedExperiments) {
                removeExperiment(experiment);
            }
            deleteFile(data.getId());
        }
    }

    private void removeExperiment(DeletedExperiment experiment) {
        deletedExperimentRepository.delete(experiment);
    }

    @Override
    public long createDefaultInstrument(long actor, long labId, long modelId) {
        final AccessedInstrument<Instrument> instrument =
            instrumentRepository.findByLabModelAndNameAccessed(actor, labId, modelId, DEFAULT_INSTRUMENT_NAME);

        if (instrument != null) {
            throw new IllegalStateException(
                "Default instrument for lab with ID: " + labId + " and model with ID: " + modelId + " already exists.");
        }

        final Lab laboratory = labRepository.findOne(labId);
        final User labHead = laboratory.getHead();

        return createInstrument(labHead.getId(), labId, modelId, getDefaultInstrumentDetails());
    }

    @Override
    public long createDefaultInstrument(long actor, long labId, long modelId, String instrumentName) {

        final AccessedInstrument<Instrument> instrument =
            instrumentRepository.findByLabModelAndNameAccessed(actor, labId, modelId, DEFAULT_INSTRUMENT_NAME);

        if (instrument != null) {
            throw new IllegalStateException(
                "Default instrument for lab with ID: " + labId + " and model with ID: " + modelId + " already exists.");
        }

        final Lab laboratory = labRepository.findOne(labId);
        final User labHead = laboratory.getHead();

        final InstrumentDetails defaultDetails = getDefaultInstrumentDetails();
        final InstrumentDetails details = new InstrumentDetails(
            instrumentName,
            defaultDetails.serialNumber,
            defaultDetails.peripherals,
            defaultDetails.hplc,
            defaultDetails.lockMasses
        );
        return createInstrument(labHead.getId(), labId, modelId, details);
    }

    @Override
    public UploadLimitCheckResult checkUploadLimit(long actor, long labId, long sizeInBytes) {

        if (featuresHelper.isEnabledForLab(ApplicationFeature.BILLING, labId)) {

            final long availableStorageSize = billingManagement.availableStorageSize(actor, labId);

            if (availableStorageSize < 0) {
                final String limitExceededMessage = "The lab storage limit has already been exceeded by "
                    + FileUtils.byteCountToDisplaySize(availableStorageSize)
                    + ". Please upgrade your lab subscription to Enterprise to have an unlimited storage.";
                return UploadLimitCheckResult.uploadLimitExceeded(limitExceededMessage);
            }

            final long availableStorageSizeAfterUpload = availableStorageSize - sizeInBytes;

            if (availableStorageSizeAfterUpload < 0) {
                final String limitExceededMessage = "The lab storage has insufficient free space. "
                    + "Required: " + FileUtils.byteCountToDisplaySize(sizeInBytes) + ", "
                    + "available: " + FileUtils.byteCountToDisplaySize(availableStorageSize)
                    + ". Please upgrade your lab subscription to Enterprise to have an unlimited storage.";
                return UploadLimitCheckResult.uploadLimitExceeded(limitExceededMessage);
            }
        }

        return UploadLimitCheckResult.uploadAvailable();
    }

    @Override
    public boolean checkMultipleFilesValidForUpload(long instrument, List<String> files) {
        final Instrument instr = checkNotNull(instrumentRepository.findOne(instrument));

        if (!instr.getModel().isAdditionalFiles()) {
            throw new IllegalArgumentException(
                "Vendor of the instrument '" + instrument + "' does not support multiple files.");
        }
        return new MultipleFilesForUploadValidator(instr.getModel()).checkMultipleFilesValid(files);
    }

    @Override
    public boolean isFileAlreadyUploadedForInstrument(long actor, long instrument, String fileName) {
        return fileRepository.isFileAlreadyUploadedForInstrument(actor, instrument, fileName);
    }

    @Override
    public void discard(long actor, long file) {
        final ActiveFileMetaData entity = load(file);
        if (!entity.getOwner().equals(Util.USER_FROM_ID.apply(actor))) {
            throw new AccessDenied("Only owner can discard");
        }
        if (RuleValidatorImpl.IS_UPLOAD_COMPLETE.test(entity)) {
            throw new AccessDenied("Content already set");
        }
        fileRepository.delete(entity);
    }

    private Instrument findInstrument(long instrumentId) {
        return checkNotNull(instrumentRepository.findOne(instrumentId), "Couldn't find such instrument");
    }

    @Override
    public void updateFile(long actor, long file, FileMetaDataInfo fileMetaData) {
        fileManagement.updateFile(actor, file, fileMetaData);
    }

    @Override
    protected void beforeCreateInstrument(long creator, long labId, InstrumentDetails instrumentDetails) {
        if (!InstrumentsDefaults.DEFAULT_INSTRUMENT_NAME.equals(instrumentDetails.name)) {
            super.beforeCreateInstrument(creator, labId, instrumentDetails);
        } else { // skip name verification to allow several "Default" instruments per lab
            if (!ruleValidator.canUserPerformActions(creator)) {
                throw new ActionsNotAllowedException(creator);
            }
            if (!ruleValidator.canUserCreateInstrumentInLab(creator, labId)) {
                throw new AccessDenied("Only lab head and admins can create instrument in the laboratory");
            } else if (!ruleValidator.canUserCreateInstrument(creator)) {
                throw new AccessDenied("User isn\'t permitted to create experiment - laboratory is not specified");
            } else if (!ruleValidator.canInstrumentBeCreated(
                labId,
                EMPTY_INSTRUMENT_NAME,
                instrumentDetails.serialNumber
            )) {
                throw new AccessDenied("Couldn\'t create instrument");
            }
        }
    }

    private InstrumentDetails getDefaultInstrumentDetails() {
        return new InstrumentDetails(
            DEFAULT_INSTRUMENT_NAME,
            UUID.randomUUID().toString(),
            "",
            "",
            new ArrayList<>()
        );
    }

    private static final class MultipleFilesForUploadValidator {

        private VendorExtensionCombination vendorExtensionCombination;

        //throws IllegalArgumentException
        //if class's hardcoded vendor names or extensions is not up to date with it's database versions
        MultipleFilesForUploadValidator(InstrumentModel instrumentModel) {
            boolean supportVendor = false;
            for (VendorExtensionCombination vendorExtensionCombination : VendorExtensionCombination.values()) {
                if (vendorExtensionCombination.vendorName.equals(instrumentModel.getVendor().getName())) {
                    supportVendor = true;
                    this.vendorExtensionCombination = vendorExtensionCombination;
                }
            }
            if (!supportVendor) {
                throw new IllegalArgumentException(MultipleFilesForUploadValidator.class.getName()
                    + " does not support specified vendor. Vendor name is not known.");
            }
            for (String extension : this.vendorExtensionCombination.getUsedExtensions()) {
                if (!extensionValid(instrumentModel, extension)) {
                    throw new IllegalArgumentException(MultipleFilesForUploadValidator.class.getName()
                        + " does not support extension: " + extension);
                }
            }
        }

        boolean checkMultipleFilesValid(List<String> files) {
            Map<String, Set<String>> combinedFilesExtensionMap = newHashMap();

            final String[][] extensionCombinations = vendorExtensionCombination.getAllowedExtensionCombinations();
            for (String[] combination : extensionCombinations) {
                for (String file : files) {
                    fillCombinedFilesExtensionsMap(combinedFilesExtensionMap, combination, file);
                }
            }
            if (combinedFilesExtensionMap.isEmpty()) {
                return false;
            }
            for (String fileName : combinedFilesExtensionMap.keySet()) {
                boolean combinedFileValid = false;
                for (String[] combination : extensionCombinations) {
                    //if file match any of extension combination it is valid
                    List<String> extensions = Arrays.asList(combination);
                    if (combinedFilesExtensionMap.get(fileName).size() == extensions.size()
                        && combinedFilesExtensionMap.get(fileName).containsAll(extensions)) {
                        combinedFileValid = true;
                        break;
                    }
                }
                if (!combinedFileValid) {
                    return false;
                }
            }
            return true;
        }

        private enum VendorExtensionCombination {
            AB_SCIEX("Sciex"), BRUKER("Bruker");

            final String vendorName;

            VendorExtensionCombination(String name) {
                this.vendorName = name;
            }

            private String[][] getAllowedExtensionCombinations() {
                switch (this) {
                    case AB_SCIEX:
                        return new String[][] {
                            {".wiff", ".wiff.mtd", ".wiff.scan"},
                            {".wiff", ".wiff.scan"},
                            {".wiff"}
                        };
                    default:
                        return new String[][] {};
                }
            }

            private Set<String> getUsedExtensions() {
                Set<String> usedExtensions = newHashSet();
                for (String[] combination : getAllowedExtensionCombinations()) {
                    usedExtensions.addAll(Arrays.asList(combination));
                }
                return usedExtensions;
            }
        }

        private void fillCombinedFilesExtensionsMap(Map<String, Set<String>> combinedFiles,
                                                    String[] combination,
                                                    String file) {
            for (String extension : combination) {
                if (file.endsWith(extension)) {
                    final String clearFileName = file.substring(0, file.toLowerCase().lastIndexOf(extension));

                    if (!combinedFiles.containsKey(clearFileName)) {
                        combinedFiles.put(clearFileName, new HashSet<String>());
                    }
                    if (!combinedFiles.get(clearFileName).contains(clearFileName)) {
                        combinedFiles.get(clearFileName).add(extension);
                    }
                }
            }
        }

        private boolean extensionValid(InstrumentModel instrumentModel, String extension) {
            for (VendorExtension vendorExtension : instrumentModel.getExtensions()) {
                if (vendorExtension.getExtension().equals(extension)
                    || vendorExtension.getAdditionalFilesExtensions().keySet().contains(extension)) {
                    return true;
                }
            }
            return false;
        }
    }

}
