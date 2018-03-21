package com.infoclinika.mssharing.platform.model.common.items;

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

    public VendorItem(long id, String name, Set<FileExtensionItem> fileUploadExtensions, boolean folderArchiveUploadSupport, boolean multipleFiles, DictionaryItem studyTypeItem) {
        this.id = id;
        this.name = name;
        this.fileUploadExtensions = fileUploadExtensions;
        this.folderArchiveUploadSupport = folderArchiveUploadSupport;
        this.multipleFiles = multipleFiles;
        this.studyTypeItem = studyTypeItem;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VendorItem that = (VendorItem) o;

        if (folderArchiveUploadSupport != that.folderArchiveUploadSupport) return false;
        if (id != that.id) return false;
        if (multipleFiles != that.multipleFiles) return false;
        if (fileUploadExtensions != null ? !fileUploadExtensions.equals(that.fileUploadExtensions) : that.fileUploadExtensions != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (studyTypeItem != null ? !studyTypeItem.equals(that.studyTypeItem) : that.studyTypeItem != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (studyTypeItem != null ? studyTypeItem.hashCode() : 0);
        result = 31 * result + (fileUploadExtensions != null ? fileUploadExtensions.hashCode() : 0);
        result = 31 * result + (folderArchiveUploadSupport ? 1 : 0);
        result = 31 * result + (multipleFiles ? 1 : 0);
        return result;
    }
}
