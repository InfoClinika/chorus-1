package com.infoclinika.mssharing.wizard.upload.service.api;

import com.infoclinika.mssharing.dto.response.InstrumentDTO;

import java.io.File;
import java.util.List;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
public interface InstrumentFileComposer {

    /**
     * Check if the file list has all required files by instrument,
     * return true if instrument requires folders or it has main file (file with main extension, specified for instrument vendor)
     * and all required files (files with additional required extensions, specified for instrument vendor),
     */
    boolean checkFiles(InstrumentDTO instrument, List<File> files);

    List<String> toComposedNames(InstrumentDTO instrument, List<File> files);

    boolean fileAcceptable(InstrumentDTO instrument, String baseName, File file);
}
