package com.infoclinika.mssharing.desktop.upload.gui.swing.util;

import com.infoclinika.mssharing.clients.common.web.api.WebService;
import com.infoclinika.mssharing.desktop.upload.gui.swing.controller.steps.SecondStepController;
import com.infoclinika.mssharing.dto.FileDescription;
import com.infoclinika.mssharing.web.rest.FilesReadyToUploadRequest;
import com.infoclinika.mssharing.web.rest.FilesReadyToUploadResponse;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.swing.*;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.desktop.messages.MessageKey.MODALS_FILES_FILTERED_TEXT;
import static com.infoclinika.mssharing.desktop.messages.MessageKey.MODALS_WARNING_TITLE;
import static com.infoclinika.mssharing.desktop.messages.MessagesSource.getMessage;

/**
 * @author timofey.kasyanov
 *     date:   06.02.14
 */
@Component
public class SecondStepHelper {

    @Inject
    private SecondStepController secondStepController;

    @Inject
    private DisplayMessageHelper messageHelper;

    @Inject
    private DesktopUploaderHelper helper;

    @Inject
    private WebService webService;

    private InstrumentFileFilter instrumentFileFilter;

    public void setInstrumentFileFilter(InstrumentFileFilter instrumentFileFilter) {
        this.instrumentFileFilter = instrumentFileFilter;
    }

    public void filesDropped(List<File> files) {
        doFiltering(getFilesToDrop(files));
    }

    private void doFiltering(FilesInfo filesInfo) {
        boolean filtered = filesInfo.filtered;
        boolean added = false;

        final FileDescription[] fileDescriptions = new FileDescription[filesInfo.files.size()];
        for (int i = 0; i < filesInfo.files.size(); i++) {
            final FileDescription fileDescription = new FileDescription();
            final File file = filesInfo.files.get(i);
            fileDescription.fileName = file.getName();
            fileDescription.directory = file.isDirectory();
            fileDescriptions[i] = fileDescription;
        }

        final FilesReadyToUploadRequest request = new FilesReadyToUploadRequest();
        request.instrumentId = helper.getInstrument().getId();
        request.fileDescriptions = fileDescriptions;

        final FilesReadyToUploadResponse response = webService.isReadyToUpload(request);
        final Map<String, FileDescription> fileNameToDescriptionMap = new HashMap<>();
        for (FileDescription fileDescription : response.fileDescriptions) {
            fileNameToDescriptionMap.put(fileDescription.fileName, fileDescription);
        }

        for (File file : filesInfo.files) {
            final FileDescription fileDescription = fileNameToDescriptionMap.get(file.getName());
            if (fileDescription != null) {
                if (fileDescription.readyToUpload) {
                    secondStepController.addItem(file);
                    added = true;
                } else {
                    filtered = true;
                }
            }
        }

        if (filtered) {
            messageHelper.showMainWindowMessage(
                getMessage(MODALS_FILES_FILTERED_TEXT),
                getMessage(MODALS_WARNING_TITLE),
                JOptionPane.WARNING_MESSAGE
            );
        }

        if (added) {
            secondStepController.filesChanged();
        }
    }

    private FilesInfo getFilesToDrop(List<File> files) {
        final List<File> list = newArrayList();
        checkNotNull(instrumentFileFilter);
        final FileFilter fileFilter = instrumentFileFilter.getFileFilter();
        boolean filtered = false;

        for (File file : files) {
            final boolean acceptableType = fileFilter.accept(file);

            if (!acceptableType && file.isDirectory()) {
                final File[] filesArray = file.listFiles(fileFilter);
                if (filesArray != null) {
                    list.addAll(Arrays.asList(filesArray));
                }

                final File[] directories = file.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
                if (directories != null) {
                    final List<File> fileList = Arrays.asList(directories);
                    final FilesInfo filesToDrop = getFilesToDrop(fileList);
                    list.addAll(filesToDrop.files);
                    filtered = filesToDrop.filtered;
                    if (fileList.size() > 0 && filesToDrop.files.size() == 0) {
                        filtered = true;
                    }
                }
                continue;
            }

            if (acceptableType) {
                list.add(file);
            } else {
                filtered = true;
            }
        }

        return new FilesInfo(list, filtered);
    }

    private static class FilesInfo {
        private final List<File> files;
        private final boolean filtered;

        private FilesInfo(List<File> files, boolean filtered) {
            this.files = files;
            this.filtered = filtered;
        }
    }

}
