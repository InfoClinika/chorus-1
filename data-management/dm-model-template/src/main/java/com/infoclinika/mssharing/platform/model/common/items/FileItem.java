package com.infoclinika.mssharing.platform.model.common.items;

import java.util.Date;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FileItem)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        FileItem fileItem = (FileItem) o;
        return copy == fileItem.copy &&
            Objects.equals(uploadDate, fileItem.uploadDate) &&
            Objects.equals(labels, fileItem.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uploadDate, labels, copy);
    }
}
