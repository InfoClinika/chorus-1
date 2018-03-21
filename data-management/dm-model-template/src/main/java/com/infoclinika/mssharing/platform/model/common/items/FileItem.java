package com.infoclinika.mssharing.platform.model.common.items;

import java.util.Date;

/**
 * @author Herman Zamula
 */
public class FileItem extends DictionaryItem {
    public final Date uploadDate;
    public final String labels;
    public final boolean copy;

    public FileItem(long id, String name, Date uploadDate, String labels, boolean copy) {
        super(id, name);
        this.uploadDate = uploadDate;
        this.labels = labels;
        this.copy = copy;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FileItem fileItem = (FileItem) o;

        if (copy != fileItem.copy) return false;
        if (labels != null ? !labels.equals(fileItem.labels) : fileItem.labels != null) return false;
        if (uploadDate != null ? !uploadDate.equals(fileItem.uploadDate) : fileItem.uploadDate != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (uploadDate != null ? uploadDate.hashCode() : 0);
        result = 31 * result + (labels != null ? labels.hashCode() : 0);
        result = 31 * result + (copy ? 1 : 0);
        return result;
    }
}
