package com.infoclinika.mssharing.wizard.upload.service.api;

import com.infoclinika.mssharing.dto.response.InstrumentDTO;

import java.io.FileFilter;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
public interface FileFilterFactory {

    FileFilter createFileFilter(InstrumentDTO instrument);

}
