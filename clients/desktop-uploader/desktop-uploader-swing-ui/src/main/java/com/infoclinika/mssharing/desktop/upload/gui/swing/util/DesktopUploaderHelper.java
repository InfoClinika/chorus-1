package com.infoclinika.mssharing.desktop.upload.gui.swing.util;

import com.amazonaws.services.s3.AmazonS3;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.infoclinika.mssharing.clients.common.InstrumentUtil;
import com.infoclinika.mssharing.clients.common.Transformers;
import com.infoclinika.mssharing.clients.common.filtering.ExtensionFileFilter;
import com.infoclinika.mssharing.clients.common.transfer.impl.UploaderImpl;
import com.infoclinika.mssharing.clients.common.web.api.WebService;
import com.infoclinika.mssharing.clients.common.web.api.exception.RestServiceException;
import com.infoclinika.mssharing.desktop.messages.MessageKey;
import com.infoclinika.mssharing.desktop.messages.MessagesSource;
import com.infoclinika.mssharing.desktop.upload.gui.swing.model.bean.DesktopUploaderSession;
import com.infoclinika.mssharing.desktop.upload.model.*;
import com.infoclinika.mssharing.desktop.upload.service.api.UploadService;
import com.infoclinika.mssharing.desktop.upload.service.api.listener.UploadZipListener;
import com.infoclinika.mssharing.desktop.upload.service.impl.list.EditFileItemList;
import com.infoclinika.mssharing.desktop.upload.service.impl.list.UploadZipList;
import com.infoclinika.mssharing.desktop.upload.service.impl.list.ViewFileItemList;
import com.infoclinika.mssharing.dto.ComposedFileDescription;
import com.infoclinika.mssharing.dto.FileDescription;
import com.infoclinika.mssharing.dto.request.DesktopUploadDoneDTO;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import com.infoclinika.mssharing.dto.response.VendorDTO;
import com.infoclinika.mssharing.web.rest.ComposeFilesRequest;
import com.infoclinika.mssharing.web.rest.ComposeFilesResponse;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

/**
 * @author timofey.kasyanov
 *     date:   30.01.14
 */
@Component
public class DesktopUploaderHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopUploaderHelper.class);
    public static final Function<ViewFileItem, File> VIEW_FILE_ITEM_TO_FILE = new Function<ViewFileItem, File>() {
        @Override
        public File apply(ViewFileItem input) {
            return input.getFile();
        }
    };

    private ComposedFileDescription[] composedFileDescriptions;
    private List<String> extensions = new ArrayList<>();

    @Inject
    private ViewFileItemList viewFileItemList;

    @Inject
    private EditFileItemList editFileItemList;

    @Inject
    private UploadZipList uploadZipList;

    @Inject
    private DesktopUploaderSession desktopUploaderSession;

    @Inject
    private UploadService uploadService;

    @Inject
    private WebService webService;

    @Inject
    private UploadZipListener listener;

    @Inject
    private DisplayMessageHelper messageHelper;

    public List<String> getInstrumentDefaultExtensions() {
        return InstrumentUtil
            .getSupportedInstrumentExtensions(desktopUploaderSession.getDesktopUploaderContext().getInstrument());
    }

    public InstrumentFileFilter createFileFilter(List<String> extensions) {
        final InstrumentDTO instrument = desktopUploaderSession.getDesktopUploaderContext().getInstrument();

        checkNotNull(instrument);

        final ExtensionFileFilter fileFilter = new ExtensionFileFilter();
        fileFilter.setExtensions(extensions);

        return new InstrumentFileFilter(fileFilter, instrument.getName() + " " + extensions.toString());
    }

    public InstrumentDTO getInstrument() {
        return desktopUploaderSession.getDesktopUploaderContext().getInstrument();
    }

    public ViewFileItemList getViewFileItemList() {
        return viewFileItemList;
    }

    public EditFileItemList getEditFileItemList() {
        return editFileItemList;
    }

    public UploadZipList getUploadZipList() {
        return uploadZipList;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<String> extensions) {
        this.extensions.clear();
        this.extensions.addAll(extensions);
    }

    private List<EditFileItem> getEditFileItems() {
        checkNotNull(viewFileItemList);

        final InstrumentDTO instrument = desktopUploaderSession.getDesktopUploaderContext().getInstrument();
        final List<File> files = Lists.transform(viewFileItemList, VIEW_FILE_ITEM_TO_FILE);
        final List<FileDescription> fileDescriptions = Lists.transform(files, Transformers.FILE_TO_FILE_DESCRIPTION);

        final ComposeFilesResponse composeFilesResponse = webService.composeFiles(
            new ComposeFilesRequest(
                instrument.getId(),
                fileDescriptions.toArray(new FileDescription[fileDescriptions.size()])
            )
        );

        composedFileDescriptions = composeFilesResponse.composedFileDescriptions;

        final List<String> composedNames = new ArrayList<>();
        for (ComposedFileDescription composedFileDescription : composeFilesResponse.composedFileDescriptions) {
            if (composedFileDescription.allRequiredFilesPresented) {
                composedNames.add(composedFileDescription.fileName);
            }
        }
        final DictionaryDTO specie = desktopUploaderSession.getDesktopUploaderContext().getSpecie();

        return composedNames.stream().map(new java.util.function.Function<String, EditFileItem>() {
            @Override
            public EditFileItem apply(String input) {
                return new EditFileItem(input, specie, "");
            }
        }).collect(Collectors.toList());
    }

    public List<UploadFileItem> getUploadFileItems() {
        checkNotNull(viewFileItemList);
        checkNotNull(editFileItemList);

        final boolean archive = isArchive();

        final Map<String, File> fileNameToFileMap = new HashMap<>();
        for (ViewFileItem viewFileItem : viewFileItemList) {
            fileNameToFileMap.put(viewFileItem.getName().toLowerCase(), viewFileItem.getFile());
        }

        final Map<String, EditFileItem> fileNameToEditFileItemMap = new HashMap<>();
        for (EditFileItem editFileItem : editFileItemList) {
            fileNameToEditFileItemMap.put(editFileItem.getName().toLowerCase(), editFileItem);
        }

        final List<UploadFileItem> uploadFileItems = new ArrayList<>();
        for (ComposedFileDescription composedFileDescription : composedFileDescriptions) {
            final EditFileItem editFileItem =
                fileNameToEditFileItemMap.get(composedFileDescription.fileName.toLowerCase());
            if (editFileItem != null) {
                final UploadFileItem item = new UploadFileItem();
                item.setName(editFileItem.getName());
                item.setNeedZipping(archive);
                item.setSpecieId(editFileItem.getSpecie().getId());
                item.setLabels(editFileItem.getLabels());

                if (archive) {
                    final List<File> filesToZip = new ArrayList<>();
                    for (FileDescription fileDescription : composedFileDescription.fileDescriptions) {
                        final File file = fileNameToFileMap.get(fileDescription.fileName.toLowerCase());
                        if (file != null) {
                            filesToZip.add(file);
                        }
                    }

                    item.setFilesToZip(filesToZip);

                    final long filesSize = getFilesSize(filesToZip);
                    item.setZipSize(filesSize);
                    item.setFileSize(filesSize);
                } else {
                    final File file = fileNameToFileMap.get(editFileItem.getName().toLowerCase());
                    if (file != null) {
                        item.setResultFile(file);
                        item.setFileSize(FileUtils.sizeOf(file));
                    } else {
                        throw new RuntimeException("Can't find file by name: " + editFileItem.getName());
                    }
                }

                uploadFileItems.add(item);
            }
        }

        return uploadFileItems;
    }

    public void updateEditFileItemList() {
        //current list of items
        final EditFileItemList list = getEditFileItemList();
        //fluent iterable from current list of items
        final FluentIterable<EditFileItem> fluentList = FluentIterable.from(list);
        //new list of items
        final List<EditFileItem> editFileItems = getEditFileItems();

        //transformed new list of items so if new item is present in current list,
        //then set specie and labels values from current item to new items
        final List<EditFileItem> transformed =
            editFileItems.stream().map(new java.util.function.Function<EditFileItem, EditFileItem>() {
                @Override
                public EditFileItem apply(EditFileItem input) {
                    final Optional<EditFileItem> match = fluentList.firstMatch(
                        new com.google.common.base.Predicate<EditFileItem>() {
                            @Override
                            public boolean apply(@Nullable EditFileItem input2) {
                                return input2.getName().equals(input.getName());
                            }
                        });

                    if (match.isPresent()) {
                        input.setSpecie(match.get().getSpecie());
                        input.setLabels(match.get().getLabels());
                    }

                    return input;
                }
            }).collect(Collectors.toList());

        list.clear();
        list.addAll(transformed);
    }

    public void clearAllLists() {
        getViewFileItemList().clear();
        getEditFileItemList().clear();
        getUploadZipList().clear();
    }

    private long getFilesSize(List<File> files) {
        long size = 0;

        for (File file : files) {
            size += FileUtils.sizeOf(file);
        }

        return size;
    }

    public void updateUploadList() {
        final UploadZipList uploadZipList = getUploadZipList();

        uploadZipList.clear();
        uploadZipList.addAll(getUploadFileItems());
    }

    public boolean isArchive() {
        final InstrumentDTO instrument = desktopUploaderSession.getDesktopUploaderContext().getInstrument();
        final VendorDTO vendor = instrument.getVendor();

        return vendor.folderArchiveUploadSupport || vendor.multipleFiles;
    }

    public void startUpload() {

        getViewFileItemList().clear();
        getEditFileItemList().clear();

        final UploadZipList uploadZipList = getUploadZipList();
        final UploadConfig uploadConfig = desktopUploaderSession.getUploadConfig();

        uploadService.upload(uploadZipList, uploadConfig, listener);
    }

    public void showUploadLimitExceededErrorMessage() {
        messageHelper.showMainWindowMessage(
            MessagesSource.getMessage(MessageKey.APP_ERROR_UPLOAD_LIMIT_EXCEEDED),
            MessagesSource.getMessage(MessageKey.MODALS_ERROR_TITLE),
            JOptionPane.ERROR_MESSAGE
        );
    }

    public void handleFilesUploadCompleted(List<UploadFileItem> fileItems) {
        try {
            List<String> files = new ArrayList<String>();
            for (UploadFileItem item : fileItems) {
                files.add(item.getContentId());
            }

            DesktopUploadDoneDTO uploadDTO = new DesktopUploadDoneDTO();
            uploadDTO.setInstrumentId(getInstrument().getId());
            uploadDTO.setDone(true);
            uploadDTO.setFiles(files);

            String id = md5Hex(String.join("|", files));

            webService.finalizeUpload(id, uploadDTO);
        } catch (Exception e) {
            LOGGER.error("Error. Can't handle files upload complete.", e);

            messageHelper.showMainWindowMessage(
                "Upload error!",
                "Restart upload!",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void cancelUpload() {
        LOGGER.info("Canceling upload...");

        final UploadZipList uploadZipList = this.uploadZipList;
        for (UploadFileItem item : uploadZipList) {
            item.setCanceled(true);
        }

        desktopUploaderSession.shutdownUploading();

        final List<UploadFileItem> items = uploadZipList.stream().filter(new Predicate<UploadFileItem>() {
            @Override
            public boolean test(UploadFileItem itemInput) {
                final UploadStatus status = itemInput.getStatus();
                return status == UploadStatus.UPLOADING
                    || status == UploadStatus.WAITING;
            }
        }).collect(Collectors.toList());

        for (UploadFileItem item : items) {
            deleteZipIfNeeded(item);
            deleteUpload(item.getFileId());
            deleteS3Object(item.getContentId());
        }

        LOGGER.info("Upload has been canceled");
    }

    public void deleteZipIfNeeded(UploadFileItem item) {
        if (item.getZipSize() > 0 && item.getResultFile() != null) {
            LOGGER.info("Try to delete zip file: {}", item.getName());
            try {
                FileUtils.forceDelete(item.getResultFile());
                LOGGER.info("Zip file deleted successfully. File: {}", item.getName());
            } catch (IOException ex) {
                LOGGER.info("Cannot delete zip file: {}", item.getName());
            }
        }
    }

    public void deleteUpload(long fileId) {
        if (fileId == 0) {
            return;
        }

        try {
            LOGGER.info("Delete unfinished upload. File ID: {}", fileId);
            webService.deleteUpload(fileId);
            LOGGER.info("Unfinished upload deleted successfully. File ID: {}", fileId);
        } catch (RestServiceException ex) {
            LOGGER.info("Cannot delete unfinished upload from server. File ID: {}", fileId);
        }
    }

    public void deleteS3Object(String key) {
        if (key == null) {
            return;
        }

        final UploadConfig uploadConfig = desktopUploaderSession.getUploadConfig();
        final AmazonS3 amazonS3 = ((UploaderImpl) uploadConfig.getUploader()).getAmazonS3();
        final String bucket = uploadConfig.getBucket();
        try {
            LOGGER.info("Delete object from S3. Object key: {}", key);
            amazonS3.deleteObject(bucket, key);
            LOGGER.info("S3 object deleted successfully. Object key: {}", key);
        } catch (Exception ex) {
            LOGGER.info("Cannot delete object from S3. Object key: {}", key);
        }
    }

    public void pauseUpload() {
        final UploadConfig uploadConfig = desktopUploaderSession.getUploadConfig();
        uploadService.pause(uploadConfig);
    }

    public void resumeUpload() {
        final UploadConfig uploadConfig = desktopUploaderSession.getUploadConfig();
        uploadService.resume(uploadConfig);
    }

}
