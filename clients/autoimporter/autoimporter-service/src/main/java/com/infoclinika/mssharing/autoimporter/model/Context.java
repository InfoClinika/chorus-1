package com.infoclinika.mssharing.autoimporter.model;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.autoimporter.model.bean.ContextInfo;
import com.infoclinika.mssharing.autoimporter.model.bean.DuplicateItem;
import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.model.bean.WaitItem;
import com.infoclinika.mssharing.autoimporter.service.api.*;
import com.infoclinika.mssharing.autoimporter.service.impl.FileFilterImpl;
import com.infoclinika.mssharing.autoimporter.service.util.Configuration;
import com.infoclinika.mssharing.autoimporter.service.util.MonitorFactory;
import com.infoclinika.mssharing.autoimporter.service.util.ObservableListFactory;
import com.infoclinika.mssharing.clients.common.transfer.api.Uploader;
import com.infoclinika.mssharing.clients.common.transfer.impl.S3ClientProvider;
import com.infoclinika.mssharing.clients.common.transfer.impl.UploaderConfiguration;
import com.infoclinika.mssharing.clients.common.transfer.impl.UploaderImpl;
import com.infoclinika.mssharing.clients.common.util.PauseSemaphore;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.autoimporter.model.bean.ItemStatus.*;

/**
 * author Ruslan Duboveckij
 */
@Component
@Scope("prototype")
public abstract class Context implements DefaultInitUtil<ContextInfo> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Context.class);


    private final ObservableListFactory observableListFactory;
    private final TaskUpload taskUpload;
    private final Session session;
    private final MonitorFactory monitorFactory;
    private ObservableList<WaitItem> waitList;
    private ObservableList<UploadItem> uploadList;
    private ObservableList<DuplicateItem> duplicateList;
    private ContextInfo info;
    private long currentCount;
    private FileFilterImpl fileFilterImpl;
    private InstrumentDTO instrument;
    private Uploader uploader;
    private WaitItemChecker waitItemChecker;
    private final Configuration configuration;

    @Inject
    public Context(TaskUpload taskUpload,
                   Session session,
                   MonitorFactory monitorFactory,
                   WaitItemChecker waitItemChecker,
                   ObservableListFactory observableListFactory,
                   Configuration configuration) {
        this.taskUpload = taskUpload;
        this.session = session;
        this.monitorFactory = monitorFactory;
        this.waitItemChecker = waitItemChecker;
        this.observableListFactory = observableListFactory;
        this.configuration = configuration;
    }

    @Override
    public void init(ContextInfo info) {
        this.info = info;
        final String folder = info.getFolder();

        instrument = info.getInstrument();
        fileFilterImpl = newFileFilter();

        waitList = observableListFactory.newObservableWaitList(folder);
        uploadList = observableListFactory.newObservableUploadList(folder);
        duplicateList = observableListFactory.newObservableDuplicateList(folder);
    }

    public Session getSession() {
        return session;
    }

    public void addWaitFile(File file) {
        LOGGER.info("Adding file to wait list. File: {}", file.getAbsolutePath());

        final Optional<WaitItem> waitFile = findWaitFile(file);
        if (!waitFile.isPresent()) {
            final WaitItem waitItem = new WaitItem(file);
            waitList.add(waitItem);
        } else {
            LOGGER.info("File already present, it won't be added to the list. File: {}", file.getAbsolutePath());
        }
    }

    @Lookup
    protected abstract S3ClientProvider getS3ClientProvider();

    private Optional<WaitItem> findWaitFile(final File file) {
        return Iterables.tryFind(waitList.getList(), new Predicate<WaitItem>() {
            @Override
            public boolean apply(WaitItem input) {
                return input.getFile().equals(file);
            }
        });
    }

    public void removeWaitFile(File file) {
        Optional<WaitItem> waitFile = findWaitFile(file);
        if (waitFile.isPresent()) {
            waitList.remove(waitFile.get());
        }
    }

    public void start() {
        LOGGER.info("Context start: {}", info.getName());
        if (uploader == null) {
            uploader = createUploader();
        }

        info.onStarted();
    }

    private Uploader createUploader() {
        final String bucket = session.getAuthenticate().getUploadConfig().getActiveBucket();
        final UploaderConfiguration configuration =
            UploaderConfiguration.getDefaultConfiguration(getS3ClientProvider(), bucket);
        final PauseSemaphore pauseSemaphore = new PauseSemaphore();
        return new UploaderImpl(configuration, pauseSemaphore);
    }

    public void stop() {
        LOGGER.info("Context stop: {}", info.getName());
        for (UploadItem item : getUploadList()) {
            item.setCanceled(true);
        }

        uploader.cancel();

        uploader = createUploader();
        for (UploadItem item : getUploadList()) {
            removeZipIfNeeded(item);
        }

        uploadList.clear();
        info.onStopped();
    }

    private void removeZipIfNeeded(UploadItem uploadItem) {
        if (uploadItem.isArchive()) {
            LOGGER.info("Try to delete zip file: {}", uploadItem.getFile().getName());
            try {
                FileUtils.forceDelete(uploadItem.getFile());
                LOGGER.info("Zip file deleted successfully. File: {}", uploadItem.getFile().getName());
            } catch (Exception ex) {
                LOGGER.info("Cannot delete zip file: {}", uploadItem.getFile().getName());
            }
        }
    }

    public void incCurrentCountAndUpload() {
        upload();
        currentCount++;
    }

    private List<File> getFileList(long limit) {
        final List<WaitItem> list = newArrayList(waitList);
        final List<File> filesToUpload = newArrayList();
        for (WaitItem item : list) {
            if (filesToUpload.size() >= limit) {
                break;
            }

            if (item.isAvailable() && item.checkSize()) {
                filesToUpload.add(item.getFile());
                waitList.remove(item);
            }
        }

        return filesToUpload;

    }

    private List<File> getDuplicatedFiles(long limit) {
        final List<DuplicateItem> list = newArrayList(duplicateList);
        final List<File> filesToUpload = newArrayList();
        for (DuplicateItem item : list) {
            if (filesToUpload.size() >= limit) {
                break;
            }
            filesToUpload.add(item.getFile());
            duplicateList.remove(item);
        }

        return filesToUpload;
    }

    private long getNumberOfFilesInProgress() {
        final AtomicInteger numberOfFilesInProgress = new AtomicInteger();
        uploadList.forEach(uploadItem -> {
            if (uploadItem.isRepeat() || uploadItem.getStatus() == WAITING || uploadItem.getStatus() == ZIPPING ||
                uploadItem.getStatus() == UPLOADING || uploadItem.getStatus() == RETRYING) {
                numberOfFilesInProgress.incrementAndGet();
            }
        });

        return numberOfFilesInProgress.get();
    }

    private synchronized void upload() {
        LOGGER.info("Starting autoimporter. Context started: {}", info.isStarted());
        if (info.isStarted()) {

            final long numberOfFilesInProgress = getNumberOfFilesInProgress();
            long numberOfFreeSlotsInQueue = configuration.getUploadingInProgressFilesLimit() - numberOfFilesInProgress;
            LOGGER.info("Number of files in progress: {}", numberOfFilesInProgress);
            LOGGER.info("Files in progress limit: {}", configuration.getUploadingInProgressFilesLimit());
            LOGGER.info("Number of free slots in queue: {}", numberOfFreeSlotsInQueue);

            final List<File> files = getFileList(numberOfFreeSlotsInQueue);
            numberOfFreeSlotsInQueue -= files.size();
            LOGGER.info("Got {} files from wait list. Free slots left: {}", files.size(), numberOfFreeSlotsInQueue);

            files.addAll(getDuplicatedFiles(numberOfFreeSlotsInQueue));
            LOGGER.info("Files list before autoimporter: {}", files);
            taskUpload.start(Context.this, files);
        }
    }

    public void addUploadItem(UploadItem uploadItem) {
        uploadList.add(uploadItem);
    }

    public void addDuplicateItem(DuplicateItem duplicateItem) {
        if (!duplicateList.getList().contains(duplicateItem)) {
            duplicateList.add(duplicateItem);
        }
    }

    public FileFilterImpl getFileFilter() {
        return fileFilterImpl;
    }

    public FileFilterImpl newFileFilter() {
        return fileFilterImpl = monitorFactory.createFileFilter(this);
    }

    public ContextInfo getInfo() {
        return info;
    }

    public InstrumentDTO getInstrument() {
        return instrument;
    }

    public ObservableList<WaitItem> getWaitList() {
        return waitList;
    }

    public ObservableList<UploadItem> getUploadList() {
        return uploadList;
    }

    public ObservableList<DuplicateItem> getDuplicateList() {
        return duplicateList;
    }

    public Uploader getUploader() {
        return uploader;
    }

    @Override
    public String toString() {
        return "Context{" +
            "waitList=" + waitList +
            ", info=" + info +
            ", currentCount=" + currentCount +
            '}';
    }

    public void itemUploadComplete(UploadItem uploadItem) {
        final UploadConfigurationService.CompleteAction completeAction = info.getCompleteAction();
        if (completeAction == UploadConfigurationService.CompleteAction.DELETE_FILE) {
            LOGGER.info("Delete files after autoimporter complete. Upload item: {}", uploadItem.getName());
            final List<File> filesToDelete = uploadItem.isArchive() ?
                newArrayList(uploadItem.getFilesToZip()) :
                newArrayList(uploadItem.getFile());

            for (File file : filesToDelete) {
                try {
                    LOGGER.info("Try to delete file: {}", file.getAbsolutePath());
                    FileUtils.forceDelete(file);
                    LOGGER.info("File deleted successfully. File: {}", file.getAbsolutePath());
                } catch (Exception ex) {
                    LOGGER.info("Cannot delete file: {}", file.getAbsolutePath());
                }
            }
        } else if (completeAction == UploadConfigurationService.CompleteAction.MOVE_FILE) {
            final File folderToMoveTo = new File(info.getFolderToMoveFiles());
            final List<File> filesToMove = uploadItem.isArchive() ?
                newArrayList(uploadItem.getFilesToZip()) :
                newArrayList(uploadItem.getFile());

            for (File file : filesToMove) {
                final String relativePath = getFileRelativePath(file, info.getFolder()).or(file.getName());

                final File destination = new File(folderToMoveTo, relativePath);
                if (file.isDirectory()) {
                    try {
                        LOGGER.info("Try to move directory from: {} to: {}", file.getAbsolutePath(),
                            destination.getAbsolutePath());
                        FileUtils.moveDirectory(file, destination);
                        LOGGER.info("Directory moved successfully from: {} to: {}", file.getAbsolutePath(),
                            destination.getAbsolutePath()
                        );
                    } catch (Exception ex) {
                        LOGGER.info("Cannot move directory from: {} to: {}", file.getAbsolutePath(),
                            destination.getAbsolutePath());
                        LOGGER.info(String.valueOf(ex));
                    }
                } else {
                    try {
                        LOGGER.info("Try to move file from: {} to: {}", file.getAbsolutePath(),
                            destination.getAbsolutePath()
                        );
                        FileUtils.moveFile(file, destination);

                        LOGGER.info("File moved successfully from: {} to: {}", file.getAbsolutePath(),
                            destination.getAbsolutePath()
                        );
                    } catch (Exception ex) {
                        LOGGER.info("Cannot move file from: {} to: {}", file.getAbsolutePath(),
                            destination.getAbsolutePath()
                        );
                        LOGGER.info(String.valueOf(ex));
                    }
                }
            }
        }
    }

    private Optional<String> getFileRelativePath(File file, String parentPath) {
        final String filePath = file.getAbsolutePath();

        if (!filePath.startsWith(parentPath)) {
            return Optional.absent();
        }

        final String relativePath = filePath.substring(parentPath.length());
        return Optional.of(relativePath);
    }
}
