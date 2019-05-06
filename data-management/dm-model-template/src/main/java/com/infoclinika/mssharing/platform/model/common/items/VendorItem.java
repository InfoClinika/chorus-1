package com.infoclinika.mssharing.platform.model.common.items;

import java.util.Objects;
import java.util.Set;

/**
 * @author Herman Zamula
 */
public class VendorItem {
    public final long id;
    public final String name;
    public final Set<FileExtensionItem> fileUploadExtensions;
    public final boolean folderArchiveUploadSupport;
    public final boolean multipleFiles;
    public final DictionaryItem studyTypeItem;

    public VendorItem(long id,
                      String name,
                      Set<FileExtensionItem> fileUploadExtensions,
                      boolean folderArchiveUploadSupport,
                      boolean multipleFiles,
                      DictionaryItem studyTypeItem) {
        this.id = id;
        this.name = name;
        this.fileUploadExtensions = fileUploadExtensions;
        this.folderArchiveUploadSupport = folderArchiveUploadSupport;
        this.multipleFiles = multipleFiles;
        this.studyTypeItem = studyTypeItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VendorItem)) {
            return false;
        }
        VendorItem that = (VendorItem) o;
        return id == that.id &&
            folderArchiveUploadSupport == that.folderArchiveUploadSupport &&
            multipleFiles == that.multipleFiles &&
            Objects.equals(name, that.name) &&
            Objects.equals(fileUploadExtensions, that.fileUploadExtensions) &&
            Objects.equals(studyTypeItem, that.studyTypeItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fileUploadExtensions, folderArchiveUploadSupport, multipleFiles, studyTypeItem);
    }
}
