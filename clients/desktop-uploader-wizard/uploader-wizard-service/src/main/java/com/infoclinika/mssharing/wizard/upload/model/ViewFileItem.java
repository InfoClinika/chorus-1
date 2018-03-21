package com.infoclinika.mssharing.wizard.upload.model;

import com.google.common.base.Optional;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author timofey.kasyanov
 *         date:   27.01.14
 */
public class ViewFileItem {

    private final File file;
    private final String name;
    private final Long size;
    private final Date date;
    private Optional<String> sizeString = Optional.absent();

    public ViewFileItem(File file) {

        checkNotNull(file);

        this.file = file;
        this.name = file.getName();
        this.size = FileUtils.sizeOf(file);
        this.date = new Date(file.lastModified());

    }

    public String getSizeString() {
        if (!sizeString.isPresent()) {
            sizeString = Optional.of(FileUtils.byteCountToDisplaySize(getSize()));
        }
        return sizeString.get();
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ViewFileItem that = (ViewFileItem) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (size != null ? !size.equals(that.size) : that.size != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (size != null ? size.hashCode() : 0);
        return result;
    }
}
