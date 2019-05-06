package com.infoclinika.mssharing.platform.model;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.common.items.*;
import com.infoclinika.mssharing.platform.model.read.AccessLevel;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate.LabLineTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate.PersonInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * @author : Alexander Serebriyan, Herman Zamula
 */
public class DefaultTransformers implements TransformersTemplate {

    public DefaultTransformers() {
    }

    public static PageRequest toPageRequest(PagedItemInfo pagedInfo) {
        return new PageRequest(pagedInfo.page, pagedInfo.items, new Sort(
            new Sort.Order(pagedInfo.isSortingAsc ? ASC : DESC, pagedInfo.sortingField)
        ));
    }

    public static <F, T> PagedItem<T> toPagedItem(Page<F> page, Function<F, T> transformFn) {
        return new PagedItem<>(
            page.getTotalPages(),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize(),
            from(page).transform(transformFn).toList()
        );
    }

    public static PersonData personalInfoToData(PersonInfo user) {
        return new PersonData(user.email, user.firstName, user.lastName);
    }

    public static PersonInfo personDataToPersonInfo(PersonData headData) {
        return new PersonInfo(headData.getFirstName(), headData.getLastName(), headData.getEmail());
    }

    public static AccessLevel fromSharingType(Sharing.Type type) {
        AccessLevel accessLevel;
        switch (type) {
            case PUBLIC:
                accessLevel = AccessLevel.PUBLIC;
                break;
            case PRIVATE:
                accessLevel = AccessLevel.PRIVATE;
                break;
            case SHARED:
                accessLevel = AccessLevel.SHARED;
                break;
            default:
                throw new IllegalStateException("Illegal sharing type: " + type);
        }
        return accessLevel;
    }

    public static <LAB extends LabTemplate> Function<LAB, LabLineTemplate> labLineTemplateTransformer() {
        return input -> {
            if (input == null) {
                return null;
            }
            return new LabLineTemplate(
                input.getId(),
                input.getName(),
                input.getHead().getId(),
                input.getInstitutionUrl(),
                input.getHead().getFullName(),
                input.getLastModification()
            );
        };
    }

    private static Set<FileExtensionItem> getFileExtensionItems(InstrumentModel instrumentModel) {
        final Set<VendorExtension> extensions = instrumentModel.getExtensions();

        return extensions.stream().map(input -> {
            final Map<String, AdditionalExtensionImportance> additionalFilesExtensions = newHashMap();
            final Map<String, VendorExtension.Importance> importanceMap = input.getAdditionalFilesExtensions();
            for (String extension : importanceMap.keySet()) {
                additionalFilesExtensions.put(extension, getType(importanceMap.get(extension)));
            }
            return new FileExtensionItem(input.getExtension(), input.getZipExtension(), additionalFilesExtensions);
        }).collect(Collectors.toSet());
    }

    private static AdditionalExtensionImportance getType(VendorExtension.Importance type) {
        switch (type) {
            case REQUIRED:
                return AdditionalExtensionImportance.REQUIRED;
            case NOT_REQUIRED:
                return AdditionalExtensionImportance.NOT_REQUIRED;
            default:
                return null;
        }
    }

    @Override
    public Function<Dictionary, DictionaryItem> dictionaryItemTransformer() {
        return input -> new DictionaryItem(input.getId(), input.getName());
    }

    @Override
    public Comparator<? super DictionaryItem> dictionaryItemComparator() {
        return Comparator.comparing(o -> o.name);
    }

    @Override
    public <T extends FileMetaDataTemplate> Function<T, FileItem> fileTransformer() {
        return input -> new FileItem(
            input.getId(), input.getName(), input.getUploadDate(), input.getLabels(), input.isCopy()
        );
    }

    @Override
    public <T extends InstrumentTemplate> Function<T, InstrumentItem> instrumentItemTransformer() {
        return input -> {
            final InstrumentModel model = input.getModel();
            final Vendor vendor = model.getVendor();
            final Set<FileExtensionItem> items = getFileExtensionItems(model);
            final boolean archive = model.isFolderArchiveSupport();
            final boolean additionalFiles = model.isAdditionalFiles();
            final DictionaryItem studyTypeItem =
                new DictionaryItem(model.getStudyType().getId(), model.getStudyType().getName());
            return new InstrumentItem(
                input.getId(),
                input.getName(),
                new VendorItem(vendor.getId(), vendor.getName(), items, archive, additionalFiles, studyTypeItem),
                input.getLab().getId(),
                input.getSerialNumber(),
                input.getCreator().getId()
            );
        };
    }

    @Override
    public Comparator<InstrumentItem> instrumentItemComparator() {
        return (o1, o2) -> {
            if (o1.id == o2.id) {
                return 0;
            }
            final int i = o1.name.compareTo(o2.name);
            if (i == 0) {
                return o1.hashCode() - o2.hashCode();
            }
            return i;
        };
    }

    @Override
    public Comparator<? super NamedItem> namedItemComparator() {
        return Comparator.comparing(o -> o.name);
    }
}
