package com.infoclinika.mssharing.autoimporter.model.bean;

import com.google.common.base.Optional;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.Serializable;

/**
 * @author timofey.kasyanov
 *     21.01.14.
 */
public class DuplicateItem implements Serializable {

    private String name;
    private long size;
    private File file;
    private Optional<String> sizeString = Optional.absent();

    public DuplicateItem() { }

    public DuplicateItem(File file) {
        this.name = file.getName();
        this.size = FileUtils.sizeOf(file);
        this.file = file;
    }

    public String getSizeString() {
        if (!sizeString.isPresent()) {
            sizeString = Optional.of(FileUtils.byteCountToDisplaySize(getSize()));
        }
        return sizeString.get();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DuplicateItem that = (DuplicateItem) o;

        if (size != that.size) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (int) (size ^ (size >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "DuplicateItem{" +
            "name='" + name + '\'' +
            ", size=" + size +
            '}';
    }
}
