package com.infoclinika.mssharing.wizard.upload.gui.swing.util;

import javax.swing.filechooser.FileFilter;
import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author timofey.kasyanov
 *         date:   30.01.14
 */
public class InstrumentFileFilter extends FileFilter {
    private final java.io.FileFilter fileFilter;
    private final String description;

    public InstrumentFileFilter(java.io.FileFilter fileFilter, String description) {
        checkNotNull(fileFilter);

        this.fileFilter = fileFilter;
        this.description = description;
    }

    @Override
    public boolean accept(File f) {
        return f.isDirectory() || fileFilter.accept(f);
    }

    @Override
    public String getDescription() {
        return description;
    }

    public java.io.FileFilter getFileFilter() {
        return fileFilter;
    }
}
