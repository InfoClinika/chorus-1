// Copyright (c) 2016, NanoString Technologies, Inc.  All rights reserved.
// Use of this file for any purpose requires prior written consent of NanoString Technologies, Inc.

package com.infoclinika.mssharing.web.uploader;

import com.infoclinika.mssharing.dto.ComposedFileDescription;
import com.infoclinika.mssharing.dto.FileDescription;
import com.infoclinika.mssharing.dto.VendorEnum;
import com.infoclinika.mssharing.model.internal.FileNameSpotter;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.platform.model.common.items.FileExtensionItem;
import com.infoclinika.mssharing.platform.model.common.items.VendorItem;
import com.infoclinika.util.FilenameUtil;

import java.util.*;

/**
 * @author Yevhen Panko (yevhen.panko@teamdev.com)
 */
public class FileUploadHelper {
    private static final String ZIP_EXTENSION = ".zip";

    public static FileDescription[] filesReadyToUpload(
        long userId,
        long instrumentId,
        VendorItem vendor,
        FileDescription[] fileDescriptions,
        InstrumentManagement instrumentManagement,
        DashboardReader dashboardReader
    ) {

        for (FileDescription fileDescription : fileDescriptions) {
            final String fileName = generateNameForUpload(
                vendor,
                fileDescription.fileName,
                fileDescription.directory
            );

            boolean uploaded = instrumentManagement.isFileAlreadyUploadedForInstrument(
                userId,
                instrumentId,
                fileName
            );
            if (uploaded) {
                final Set<FileLine> files = dashboardReader.readByNameForInstrument(
                    userId,
                    instrumentId,
                    fileName
                );
                if (files.iterator().hasNext()) {
                    fileDescription.readyToUpload = files.iterator().next().toReplace;
                }
            } else {
                fileDescription.readyToUpload = true;
            }
        }

        return fileDescriptions;
    }

    public static ComposedFileDescription[] composeFiles(VendorItem vendor, FileDescription[] fileDescriptions) {
        final Map<String, List<FileDescription>> baseNameToFilesMap = new HashMap<>();
        for (FileDescription fileDescription : fileDescriptions) {
            final String fileName = fileDescription.fileName;
            final String baseName = FilenameUtil.getBaseName(fileName);
            baseNameToFilesMap.putIfAbsent(baseName, new ArrayList<>());
            baseNameToFilesMap.get(baseName).add(fileDescription);
        }

        int i = 0;
        boolean archivingSupport = vendor.folderArchiveUploadSupport || vendor.multipleFiles;
        final ComposedFileDescription[] composedFileDescriptions =
            new ComposedFileDescription[baseNameToFilesMap.size()];
        for (String baseName : baseNameToFilesMap.keySet()) {
            final List<FileDescription> composedFiles = baseNameToFilesMap.get(baseName);
            final String extension = composedFiles.get(0).fileName.substring(baseName.length());
            final String composedFileName =
                archivingSupport ? baseName + extension + ZIP_EXTENSION : baseName + extension;

            composedFileDescriptions[i] = new ComposedFileDescription(
                composedFileName,
                true,
                composedFiles.toArray(new FileDescription[composedFiles.size()])
            );

            i++;
        }

        return composedFileDescriptions;
    }

    private static String generateNameForUpload(VendorItem vendor, String originalName, boolean directory) {
        final VendorEnum vendorEnum = VendorEnum.getVendorEnum(vendor.name);
        final FileExtensionItem fileExtension = vendor.fileUploadExtensions.iterator().next();
        final String extension = fileExtension.name;
        final String zipSuffix = fileExtension.zip;
        switch (vendorEnum) {
            case BRUKER:
                String filename;
                if (directory) {
                    filename = FilenameUtil.getPartBefore(originalName, extension);
                } else {
                    filename = FilenameUtil.getBaseName(originalName);
                }

                return FileNameSpotter.replaceInvalidSymbols(filename + zipSuffix + ZIP_EXTENSION);
            case WATERS:
            case AGILENT:
                return FileNameSpotter.replaceInvalidSymbols(originalName + ZIP_EXTENSION);
            case AB_SCIEX:
            case WYATT:
                return FileNameSpotter.replaceInvalidSymbols(
                    FilenameUtil.getBaseName(originalName) + zipSuffix + ZIP_EXTENSION);
            case THERMO:
            case MA_AFFYMETRIX:
            case MA_AGILENT:
            case MA_ILLIMUNA:
            case NGS:
            case SOLID:
            case UNKNOWN:
            default:
                return FileNameSpotter.replaceInvalidSymbols(originalName);
        }
    }
}
