package com.infoclinika.mssharing.web.transform;

import com.google.common.base.Function;
import com.infoclinika.mssharing.dto.FunctionTransformerAbstract;
import com.infoclinika.mssharing.dto.request.UploadFilesDTORequest;
import com.infoclinika.mssharing.dto.response.*;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.model.read.InstrumentLine;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.platform.model.common.items.*;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * author: Ruslan Duboveckij
 */
public class DtoTransformer extends FunctionTransformerAbstract {

    // From DTO
    public static final Function<UploadFilesDTORequest.UploadFile,
            InstrumentManagement.UploadFileItem> FROM_FILES_REQUEST = new Function<UploadFilesDTORequest.UploadFile, InstrumentManagement.UploadFileItem>() {
        @Nullable
        @Override
        public InstrumentManagement.UploadFileItem apply(@Nullable UploadFilesDTORequest.UploadFile item) {
            return new InstrumentManagement.UploadFileItem(item.getName(),
                    item.getLabels(), item.getSize(), item.getSpecie(), item.isArchive());
        }
    };


    // To DTO
    public static final Function<DictionaryItem, DictionaryDTO> TO_DICTIONARY = new Function<DictionaryItem, DictionaryDTO>() {
        @Nullable
        @Override
        public DictionaryDTO apply(@Nullable DictionaryItem item) {
            return new DictionaryDTO(item.id, item.name);
        }
    };

    public static final Function<FileLine, FileDTO> TO_FILE_DTO = new Function<FileLine, FileDTO>() {
        @Nullable
        @Override
        public FileDTO apply(@Nullable FileLine item) {
            DashboardReader.FileColumns columns = item.columns;
            // From Annotations ignore
            FileColumnsDTO columnsDTO = new FileColumnsDTO(columns.name,
                    columns.sizeInBytes, columns.instrument, columns.laboratory,
                    columns.uploadDate, columns.labels);

            return new FileDTO(item.id, item.name, item.instrumentId,
                    item.specieId, item.contentId, item.uploadId,
                    item.destinationPath, item.isArchive,
                    FileDTO.AccessLevel.valueOf(item.accessLevel.name()),
                    item.usedInExperiments, item.owner, item.lastPingDate,
                    columnsDTO, item.invalid);
        }
    };

    public static final Function<InstrumentItem, InstrumentDTO> TO_INSTRUMENT_DTO = new Function<InstrumentItem, InstrumentDTO>() {
        @Nullable
        @Override
        public InstrumentDTO apply(@Nullable InstrumentItem item) {
            final VendorItem vendor = item.vendor;
            final Set<FileExtensionItem> fileUploadExtensions = vendor.fileUploadExtensions;
            final Set<FileExtensionDTO> fileExtensions = new HashSet<>();

            for (FileExtensionItem fileUploadExtension : fileUploadExtensions) {
                final Map<String, FileExtensionDTO.AdditionalExtensionImportance> additionalExtensions = new HashMap<>();
                final Map<String, AdditionalExtensionImportance> importanceMap = fileUploadExtension.additionalExtensions;

                for (String key : importanceMap.keySet()) {
                    final FileExtensionDTO.AdditionalExtensionImportance extensionImportance =
                            FileExtensionDTO.AdditionalExtensionImportance.valueOf(importanceMap.get(key).name());
                    additionalExtensions.put(key, extensionImportance);
                }

                fileExtensions.add(new FileExtensionDTO(
                        fileUploadExtension.name,
                        fileUploadExtension.zip,
                        additionalExtensions
                ));
            }
            final VendorDTO vendorDTO = new VendorDTO(
                    vendor.id,
                    vendor.name,
                    fileExtensions,
                    vendor.folderArchiveUploadSupport,
                    vendor.multipleFiles,
                    new DictionaryDTO(
                            vendor.studyTypeItem.id,
                            vendor.studyTypeItem.name
                    )
            );

            return new InstrumentDTO(item.id, item.name,
                    vendorDTO, item.lab, item.serial, item.creator);
        }
    };

    public static final Function<InstrumentLine, InstrumentDTO> TO_SIMPLE_INSTRUMENT_DTO = new Function<InstrumentLine, InstrumentDTO>() {
        @Nullable
        @Override
        public InstrumentDTO apply(@Nullable InstrumentLine input) {
            return new InstrumentDTO(
                    input.id,
                    input.name
            );
        }
    };

    private DtoTransformer() {
    }
}
