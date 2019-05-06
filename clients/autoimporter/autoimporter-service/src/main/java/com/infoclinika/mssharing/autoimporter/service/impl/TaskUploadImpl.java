package com.infoclinika.mssharing.autoimporter.service.impl;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.autoimporter.model.Context;
import com.infoclinika.mssharing.autoimporter.model.Session;
import com.infoclinika.mssharing.autoimporter.model.bean.ContextInfo;
import com.infoclinika.mssharing.autoimporter.model.bean.DuplicateItem;
import com.infoclinika.mssharing.autoimporter.model.bean.ItemStatus;
import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.service.api.ObservableList;
import com.infoclinika.mssharing.autoimporter.service.api.TaskUpload;
import com.infoclinika.mssharing.autoimporter.service.api.internal.ObserverList;
import com.infoclinika.mssharing.autoimporter.service.util.Configuration;
import com.infoclinika.mssharing.autoimporter.service.util.ZipUtils;
import com.infoclinika.mssharing.clients.common.Transformers;
import com.infoclinika.mssharing.clients.common.transfer.api.Uploader;
import com.infoclinika.mssharing.clients.common.transfer.impl.UploaderImpl;
import com.infoclinika.mssharing.clients.common.web.api.WebService;
import com.infoclinika.mssharing.clients.common.web.api.exception.RestServiceException;
import com.infoclinika.mssharing.clients.common.web.api.exception.UploadLimitExceededException;
import com.infoclinika.mssharing.dto.ComposedFileDescription;
import com.infoclinika.mssharing.dto.FileDescription;
import com.infoclinika.mssharing.dto.request.ConfirmMultipartUploadDTO;
import com.infoclinika.mssharing.dto.request.UploadFilesDTORequest;
import com.infoclinika.mssharing.dto.response.*;
import com.infoclinika.mssharing.web.rest.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.autoimporter.model.bean.ItemStatus.*;
import static com.infoclinika.mssharing.autoimporter.model.util.UploadTransformer.toUploadFilesDTORequestFile;

/**
 * @author Ruslan Duboveckij
 */
@Service
public class TaskUploadImpl implements TaskUpload {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskUploadImpl.class);
    private static final int CHECK_SIZE_TRY_COUNT = 10;
    private static final long CHECK_SIZE_RETRY_DELAY = 1000;
    private static final int CHECK_FOR_DUPLICATES_PAGE_SIZE = 100;

    @Inject
    private WebService webService;

    @Inject
    private ZipUtils zipUtils;

    @Inject
    private Configuration configuration;


    @Async
    public void start(Context context, List<File> files) {
        LOGGER.info("Task upload start");

        LOGGER.info("Filter duplicated files");
        final List<File> filesWithoutDuplicates = filterDuplicates(context, files);
        LOGGER.info("Number of duplicates: " + (files.size() - filesWithoutDuplicates.size()));

        LOGGER.info("Composing items...");
        final List<UploadItem> uploadItems = getUploadItems(context, filesWithoutDuplicates);
        LOGGER.info("Items composed");

        addItemsToList(context, uploadItems);
        findRepeatItems(context, uploadItems);
        findItemsToRetry(context, uploadItems);

        if (webService.isArchivingRequired(context.getInstrument())) {
            for (UploadItem item : uploadItems) {
                zipItem(context, item);
                if (item.getStatus().equals(WAITING)) {
                    item.setArchive(true);
                    prepareUploadAndStart(context, newArrayList(item));
                }
            }
        } else {
            prepareUploadAndStart(context, uploadItems);
        }
    }

    public List<UploadItem> getUploadItems(Context context, List<File> files) {
        final boolean archivingRequired = webService.isArchivingRequired(context.getInstrument());

        final Map<String, File> fileNameToFileMap = new HashMap<>();
        for (File file : files) {
            fileNameToFileMap.put(file.getName().toLowerCase(), file);
        }

        final InstrumentDTO instrument = context.getInstrument();
        final List<FileDescription> fileDescriptions = Lists.transform(files, Transformers.FILE_TO_FILE_DESCRIPTION);
        final ComposeFilesResponse composeFilesResponse = webService.composeFiles(
            new ComposeFilesRequest(
                instrument.getId(),
                fileDescriptions.toArray(new FileDescription[fileDescriptions.size()])
            )
        );
        final ComposedFileDescription[] composedFileDescriptions = composeFilesResponse.composedFileDescriptions;

        final List<UploadItem> uploadItems = new ArrayList<>();
        for (ComposedFileDescription composedFileDescription : composedFileDescriptions) {

            if (archivingRequired) {
                final String folderPrefix = configuration.getZipFolderPath() + File.separator;
                final String filePath = folderPrefix + composedFileDescription.fileName;
                final File archiveFile = new File(filePath);
                final UploadItem item = new UploadItem(archiveFile);

                final List<File> filesToZip = new ArrayList<>();
                for (FileDescription fileDescription : composedFileDescription.fileDescriptions) {
                    final File file = fileNameToFileMap.get(fileDescription.fileName.toLowerCase());
                    if (file != null) {
                        filesToZip.add(file);
                    }
                }

                item.setFilesToZip(filesToZip);

                final long filesSize = getFilesSize(filesToZip);
                item.setSize(filesSize);
                item.setZipSize(filesSize);

                uploadItems.add(item);
            } else {
                final File file = fileNameToFileMap.get(composedFileDescription.fileName.toLowerCase());
                if (file != null) {
                    final UploadItem item = new UploadItem(file);
                    item.setSize(FileUtils.sizeOf(file));

                    uploadItems.add(item);
                } else {
                    throw new RuntimeException("Can't find file by name: " + composedFileDescription.fileName);
                }
            }
        }

        return uploadItems;
    }

    private long getFilesSize(List<File> files) {
        long size = 0;
        for (File file : files) {
            size += FileUtils.sizeOf(file);
        }

        return size;
    }

    private void findRepeatItems(Context context, List<UploadItem> uploadItems) {
        final ObservableList<UploadItem> items = context.getUploadList();
        for (UploadItem item : items) {
            if (item.isRepeat()) {
                item.setRepeat(false);
                item.setStatus(WAITING);
                uploadItems.add(item);
            }
        }
    }

    private void zipItem(Context context, UploadItem item) {
        zipUtils.zip(item, context.getUploadList());
    }

    private void addItemsToList(Context context, List<UploadItem> items) {
        for (UploadItem item : items) {
            context.addUploadItem(item);
        }
    }

    private FilesReadyToUploadResponse isFilesReadyToUpload(List<File> files, long instrumentId) {
        final FilesReadyToUploadRequest request = new FilesReadyToUploadRequest();

        final FileDescription[] fileDescriptions = files.stream()
            .map(f -> new FileDescription(f.getName(), f.isDirectory(), false))
            .toArray(size -> new FileDescription[files.size()]);

        request.instrumentId = instrumentId;
        request.fileDescriptions = fileDescriptions;
        return webService.isReadyToUpload(request);
    }

    private List<File> filterDuplicates(Context context, List<File> files) {
        if (files.isEmpty()) {
            return new ArrayList<>();
        }
        LOGGER.info("Files to filter: {}", files.size());

        final InstrumentDTO instrument = context.getInfo().getInstrument();
        final HashMap<String, File> filesMap = new HashMap<>(files.size());
        files.forEach(f -> filesMap.put(f.getName(), f));

        final List<File> result = new ArrayList<>();

        final List<List<File>> partitions = Lists.partition(files, CHECK_FOR_DUPLICATES_PAGE_SIZE);

        final AtomicInteger pageCounter = new AtomicInteger(0);
        partitions.forEach(filesPage -> {
            final FilesReadyToUploadResponse readyToUploadResponse =
                isFilesReadyToUpload(filesPage, instrument.getId());
            for (int i = 0; i < readyToUploadResponse.fileDescriptions.length; i++) {
                FileDescription fileDescription = readyToUploadResponse.fileDescriptions[i];
                final File file = filesMap.get(fileDescription.fileName);
                if (fileDescription.readyToUpload) {
                    result.add(file);
                } else {
                    addDuplicatedItemToList(context, file);
                }
            }
            final int numberOfFilteredFiles = pageCounter.incrementAndGet() * CHECK_FOR_DUPLICATES_PAGE_SIZE;
            LOGGER.debug("Filtered {} files of {}", numberOfFilteredFiles, files.size());
        });

        return result;
    }

    private void addDuplicatedItemToList(Context context, File file) {
        final DuplicateItem duplicateItem = new DuplicateItem(file);
        context.addDuplicateItem(duplicateItem);
    }

    private void prepareUploadAndStart(Context context, final List<UploadItem> files) {
        if (files.size() == 0) {
            return;
        }

        final ContextInfo info = context.getInfo();
        final List<UploadFilesDTORequest.UploadFile> uploadFiles =
            toUploadFilesDTORequestFile(info.getSpecie(), FluentIterable.from(files), info.getLabels());

        final UploadFilesDTORequest request = new UploadFilesDTORequest(
            info.getInstrument().getId(),
            uploadFiles
        );

        SSEUploadFilesDTOResponse response;

        try {

            response = webService.postStartSSEUploadRequest(request);

        } catch (RestServiceException ex) {
            LOGGER.info("Cannot post start upload request", ex);

            ItemStatus itemStatus;
            final RestExceptionType exceptionType = ex.getExceptionType();

            if (exceptionType == RestExceptionType.UPLOAD_UNAVAILABLE
                || exceptionType == RestExceptionType.UPLOAD_LIMIT_EXCEEDED) {
                itemStatus = UPLOAD_UNAVAILABLE;
            } else {
                itemStatus = ERROR;
            }

            for (UploadItem item : files) {
                notifyItemStatusChanged(context, item, itemStatus);
            }

            if (exceptionType == RestExceptionType.UPLOAD_LIMIT_EXCEEDED) {
                throw new UploadLimitExceededException(ex.getMessage());
            }

            return;
        }

        for (int i = 0; i < files.size(); ++i) {
            final UploadItem item = files.get(i);
            final SSEUploadFilesDTOResponse.UploadFileItem uploadFileItem = response.getFiles().get(i);

            item.setContentId(uploadFileItem.getPath());
            item.setAuthorization(uploadFileItem.getAuthorization());
            item.setFormattedDate(uploadFileItem.getFormattedDate());
            item.setSseEnabled(uploadFileItem.isSseEnabled());

        }

        upload(context, files);
    }

    private void notifyItemStatusChanged(Context context, UploadItem uploadItem, ItemStatus status) {
        final String folder = context.getInfo().getFolder();
        final ObserverList<UploadItem> observer = context.getUploadList().getObserver();

        uploadItem.setStatus(status);
        switch (status) {
            case SIZE_MISMATCH:
            case COMPLETE:
            case UPLOAD_UNAVAILABLE:
            case CANCELED:
            case ERROR:
                handleFileUploadFinished(uploadItem);
                break;
            default:
                throw new IllegalArgumentException("Unknown status: " + status);
        }

        observer.notify(NotificationType.STATUS_VALUE, folder, uploadItem);
    }

    private void handleFileUploadFinished(UploadItem item) {
        if (item.getStatus().equals(ERROR) && item.getNumberOfTries() < configuration.getNumberOfTriesToUpload()) {
            item.setStatus(RETRYING);
            item.setLastErrorDate(new Date());
            item.incrementNumberOfTries();
        } else {
            removeZipIfNeeded(item);
        }
    }

    private void findItemsToRetry(Context context, List<UploadItem> uploadItems) {
        final Date currentDate = new Date();
        context.getUploadList().forEach(item -> {
            if (!item.getStatus().equals(RETRYING)) {
                return;
            }

            final long lastErrorTime = item.getLastErrorDate().getTime();
            final long interval = currentDate.getTime() - lastErrorTime;
            if (interval >= configuration.getTimeToWaitBeforeRetry()) {
                item.setStatus(WAITING);
                uploadItems.add(item);
            }
        });
    }

    private void notifyItemChanged(Context context, UploadItem item, NotificationType type) {
        final String folder = context.getInfo().getFolder();
        final ObserverList<UploadItem> observer = context.getUploadList().getObserver();
        observer.notify(type, folder, item);
    }

    private void upload(Context context, final List<UploadItem> files) {
        LOGGER.info("Start uploading. Context: {}", context.getInfo().getFolder());

        final Uploader uploader = context.getUploader();
        for (final UploadItem uploadItem : files) {
            if (uploader.isCanceled()) {
                return;
            }

            final File file = uploadItem.getFile();
            final com.infoclinika.mssharing.clients.common.transfer.impl.UploadItem item =
                new com.infoclinika.mssharing.clients.common.transfer.impl.UploadItem(
                    file,
                    uploadItem.getContentId(),
                    uploadItem.getAuthorization(),
                    uploadItem.getFormattedDate(),
                    uploadItem.isSseEnabled()
                );

            final com.amazonaws.event.ProgressListener progressListener =
                new InternalProgressListener(uploadItem, context);

            uploader.upload(item, progressListener);
        }
    }

    private void completeItemUpload(Context context, UploadItem item) {
        final boolean result = postStartUploadBeforeComplete(context, item);
        if (result) {
            postUploadComplete(context, item);
            if (item.getStatus() == COMPLETE) {
                context.itemUploadComplete(item);
            }
        }
    }

    private boolean postStartUploadBeforeComplete(Context context, UploadItem item) {
        final ContextInfo contextInfo = context.getInfo();
        LOGGER.info("Posting request before complete upload. Item: {}", item.getName());

        final UploadFilesDTORequest.UploadFile uploadFile = new UploadFilesDTORequest.UploadFile(
            item.getName(),
            contextInfo.getLabels(),
            item.getSize(),
            contextInfo.getSpecie().getId(),
            item.isArchive()
        );
        final UploadFilesDTORequest request =
            new UploadFilesDTORequest(contextInfo.getInstrument().getId(), newArrayList(uploadFile));
        try {
            final UploadFilesDTOResponse response
                = webService.postStartUploadRequestBeforeFinish(request);
            LOGGER.info("Upload file before complete request posted successfully");
            //upload is unavailable
            if (response.getFiles().size() == 0) {
                notifyItemStatusChanged(context, item, UPLOAD_UNAVAILABLE);
                return false;
            }

            final UploadFilesDTOResponse.UploadFile responseFile = response.getFiles().get(0);
            item.setFileId(responseFile.getFileId());

            return true;
        } catch (RestServiceException ex) {
            LOGGER.info("Cannot post request before complete upload. Item name: {}", item.getName());
            notifyItemStatusChanged(context, item, ERROR);
            return false;
        }
    }

    private void postUploadComplete(Context context, UploadItem item) {
        LOGGER.info("Upload complete, posting confirm request... Item name: {}", item.getName());

        final ConfirmMultipartUploadDTO confirmRequest = new ConfirmMultipartUploadDTO(
            item.getFileId(),
            item.getContentId()
        );

        try {
            final CompleteUploadDTO confirmResponse =
                webService.postCompleteUploadRequest(confirmRequest);
            LOGGER.info("Confirmation response for item:" + item.getName()
                + " -> confirmed - " + confirmResponse.isConfirmed());
            if (!confirmResponse.isConfirmed()) {
                notifyItemStatusChanged(context, item, UPLOAD_UNAVAILABLE);
            }
        } catch (RestServiceException ex) {
            LOGGER.info("Cannot post request to finish upload. Item name: {}", item.getName());
            notifyItemStatusChanged(context, item, ERROR);
        }
    }

    private void removeZipIfNeeded(UploadItem uploadItem) {
        if (uploadItem.isArchive() && uploadItem.getFile().exists()) {
            LOGGER.info("Try to delete zip file: " + uploadItem.getFile().getName());

            try {
                FileUtils.forceDelete(uploadItem.getFile());
                LOGGER.info("Zip file deleted successfully. File: {}", uploadItem.getFile().getName());
            } catch (Exception ex) {
                LOGGER.info("Cannot delete zip file: {}", uploadItem.getFile().getName());
            }
        }
    }

    private boolean checkSizeMatch(UploadItem uploadItem, Context context) {
        for (int i = 1; i <= CHECK_SIZE_TRY_COUNT; i++) {
            try {
                final UploaderImpl uploader = (UploaderImpl) context.getUploader();
                final AmazonS3 s3 = uploader.getAmazonS3();
                final Session session = context.getSession();
                final AuthenticateDTO authenticate = session.getAuthenticate();
                final UploadConfigDTO uploadConfig = authenticate.getUploadConfig();
                final String bucket = uploadConfig.getActiveBucket();
                final ObjectMetadata metadata = s3.getObjectMetadata(bucket, uploadItem.getContentId());
                final long contentLength = metadata.getContentLength();

                final boolean result = contentLength == uploadItem.getSize();
                LOGGER.info("Size match checking. Size match: {}", result);

                return result;
            } catch (Exception e) {
                LOGGER.error("Error occurred while checking size match. Item: {}", uploadItem.getName()
                    + "Try number: " + i + " of " + CHECK_SIZE_TRY_COUNT, e);
                try {
                    Thread.sleep(CHECK_SIZE_RETRY_DELAY);
                } catch (InterruptedException e1) {
                    LOGGER.error(String.valueOf(e1));
                }
            }
        }

        return true;
    }

    private void processSizeMismatch(UploadItem uploadItem) {
        uploadItem.setRepeat(true);
    }

    private class InternalProgressListener implements ProgressListener {
        private final UploadItem uploadItem;
        private long currentTransferred = 0;
        private final Context context;

        private InternalProgressListener(UploadItem uploadItem,
                                         Context context) {
            this.context = context;
            this.uploadItem = uploadItem;
        }

        @Override
        public void progressChanged(ProgressEvent progressEvent) {
            if (uploadItem.isCanceled()) {
                LOGGER.info("Upload progress changed, but uploading is canceled. Item: {}", uploadItem.getName());
                return;
            }

            final ProgressEventType eventType = progressEvent.getEventType();
            switch (eventType) {
                case TRANSFER_STARTED_EVENT:
                    LOGGER.info("Upload started for item: {}", uploadItem.getName());
                    notifyItemStatusChanged(context, uploadItem, UPLOADING);
                    break;

                case TRANSFER_CANCELED_EVENT:
                    LOGGER.info("Upload canceled for item: {}", uploadItem.getName());
                    uploadItem.setCanceled(true);
                    notifyItemStatusChanged(context, uploadItem, CANCELED);
                    break;

                case TRANSFER_FAILED_EVENT:
                    LOGGER.info("Upload failed for item: {}", uploadItem.getName());
                    notifyItemStatusChanged(context, uploadItem, ERROR);
                    break;

                case HTTP_REQUEST_CONTENT_RESET_EVENT:
                    LOGGER.info("Upload reset for item: {}", uploadItem.getName());
                    currentTransferred = 0;
                    uploadItem.setUploadedValue(0);
                    notifyItemChanged(context, uploadItem, NotificationType.UPLOAD_VALUE);
                    break;

                case TRANSFER_COMPLETED_EVENT:
                    LOGGER.info("Upload completed for item: {}", uploadItem.getName());
                    final boolean sizeMatch = checkSizeMatch(uploadItem, context);

                    if (sizeMatch) {
                        uploadItem.setUploadedValue(uploadItem.getSize());
                        notifyItemChanged(context, uploadItem, NotificationType.UPLOAD_VALUE);
                        notifyItemStatusChanged(context, uploadItem, COMPLETE);
                        completeItemUpload(context, uploadItem);
                    } else {
                        uploadItem.setUploadedValue(uploadItem.getSize());
                        notifyItemChanged(context, uploadItem, NotificationType.UPLOAD_VALUE);
                        notifyItemStatusChanged(context, uploadItem, SIZE_MISMATCH);
                        processSizeMismatch(uploadItem);
                    }

                    break;
                default:
                    currentTransferred += progressEvent.getBytesTransferred();
                    uploadItem.setUploadedValue(currentTransferred);
                    notifyItemChanged(context, uploadItem, NotificationType.UPLOAD_VALUE);
                    break;
            }
        }
    }
}
