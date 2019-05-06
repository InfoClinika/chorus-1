package com.infoclinika.mssharing.autoimporter.model.bean;

import com.infoclinika.mssharing.autoimporter.model.util.AbstractUploadFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * author Ruslan Duboveckij
 */
public class WaitItem extends AbstractUploadFile implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitItem.class);
    private final boolean isFolder;
    private boolean wasUnavailable = false;
    private long lastSize = -9;

    public WaitItem(File file) {
        super(checkNotNull(file));
        isFolder = file.isDirectory();
    }

    public boolean isFolder() {
        return isFolder;
    }

    public boolean isAvailable() {
        boolean fileAvailable = isFileAvailable(getFile());
        if (fileAvailable && wasUnavailable) {
            final long size = FileUtils.sizeOf(getFile());
            setSize(size);
        } else if (!fileAvailable) {
            wasUnavailable = true;
            LOGGER.info("Wait item is not available. Name: {}", getName());
        }

        return fileAvailable;
    }

    /**
     * NOTE! First invocation always returns false
     *
     * @return Return true if file size has not been changed since last invocation.
     */
    public boolean checkSize() {
        try {
            if (lastSize == -9) {
                lastSize = FileUtils.sizeOf(getFile());
                return false;
            } else {
                final long tempSize = FileUtils.sizeOf(getFile());
                if (tempSize != lastSize) {
                    lastSize = tempSize;
                    return false;
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isFileAvailable(File f) {
        if (f.isDirectory()) {
            final File[] files = f.listFiles();
            List<File> fileList = files == null ? new ArrayList<>() : newArrayList(files);
            for (File fileItem : fileList) {
                final boolean fileAvailable = isFileAvailable(fileItem);
                if (!fileAvailable) {
                    return false;
                }
            }

            return true;
        } else {
            try {
                final FileInputStream stream = new FileInputStream(f);
                try {
                    stream.close();
                } catch (Exception e) {
                    LOGGER.warn("An error occurred during a stream close.", e);
                }

                return true;
            } catch (Exception ex) {
                return false;
            }
        }
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
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "WaitItem{" +
            "file=" + getFile() +
            '}';
    }
}
