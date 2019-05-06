package com.infoclinika.mssharing.autoimporter.model.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.Serializable;

/**
 * author Ruslan Duboveckij
 */
public abstract class AbstractUploadFile implements Serializable {

    private final File file;
    private final String name;
    private long size;

    public AbstractUploadFile(File file) {
        this.file = file;
        name = file.getName();

        if (file.exists()) {
            size = FileUtils.sizeOf(file);
        } else {
            size = 0;
        }

    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractUploadFile that = (AbstractUploadFile) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
