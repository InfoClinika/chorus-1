package com.infoclinika.mssharing.dto.response;

import java.io.Serializable;
import java.util.Set;

/**
 * @author Ruslan Duboveckij
 */
public class VendorDTO implements Serializable {
    public long id;
    public String name;
    public Set<FileExtensionDTO> fileUploadExtensions;
    public boolean folderArchiveUploadSupport;
    public boolean multipleFiles;
    public DictionaryDTO studyTypeItem;

    public VendorDTO() {
    }

    public VendorDTO(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public VendorDTO(
        long id,
        String name,
        Set<FileExtensionDTO> fileUploadExtensions,
        boolean folderArchiveUploadSupport,
        boolean multipleFiles,
        DictionaryDTO studyTypeItem
    ) {
        this.id = id;
        this.name = name;
        this.fileUploadExtensions = fileUploadExtensions;
        this.folderArchiveUploadSupport = folderArchiveUploadSupport;
        this.multipleFiles = multipleFiles;
        this.studyTypeItem = studyTypeItem;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            VendorDTO that = (VendorDTO) o;
            if (this.folderArchiveUploadSupport != that.folderArchiveUploadSupport) {
                return false;
            } else if (this.id != that.id) {
                return false;
            } else if (this.multipleFiles != that.multipleFiles) {
                return false;
            } else {
                if (this.fileUploadExtensions != null) {
                    if (!this.fileUploadExtensions.equals(that.fileUploadExtensions)) {
                        return false;
                    }
                } else if (that.fileUploadExtensions != null) {
                    return false;
                }

                if (this.name != null) {
                    if (!this.name.equals(that.name)) {
                        return false;
                    }
                } else if (that.name != null) {
                    return false;
                }

                if (this.studyTypeItem != null) {
                    if (!this.studyTypeItem.equals(that.studyTypeItem)) {
                        return false;
                    }
                } else if (that.studyTypeItem != null) {
                    return false;
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = (int) (this.id ^ this.id >>> 32);
        result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
        result = 31 * result + (this.studyTypeItem != null ? this.studyTypeItem.hashCode() : 0);
        result = 31 * result + (this.fileUploadExtensions != null ? this.fileUploadExtensions.hashCode() : 0);
        result = 31 * result + (this.folderArchiveUploadSupport ? 1 : 0);
        result = 31 * result + (this.multipleFiles ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VendorDTO{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", fileUploadExtensions=" + fileUploadExtensions +
            ", folderArchiveUploadSupport=" + folderArchiveUploadSupport +
            ", multipleFiles=" + multipleFiles +
            ", studyTypeItem=" + studyTypeItem +
            '}';
    }
}
