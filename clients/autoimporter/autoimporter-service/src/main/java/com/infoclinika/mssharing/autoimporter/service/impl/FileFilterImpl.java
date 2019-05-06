package com.infoclinika.mssharing.autoimporter.service.impl;

import com.infoclinika.mssharing.autoimporter.model.Context;
import com.infoclinika.mssharing.clients.common.InstrumentUtil;
import com.infoclinika.mssharing.clients.common.filtering.ExtensionFileFilter;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import com.infoclinika.mssharing.propertiesprovider.DesktopClientsPropertiesProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.io.FileFilter;
import java.util.List;

/**
 * author Ruslan Duboveckij
 */
@Service
@Scope("prototype")
public class FileFilterImpl implements FileFilter {
    private static final String MAC_OS_DS_STORE_FILE_NAME = ".DS_Store";

    private ExtensionFileFilter fileFilter;

    @Inject
    private DesktopClientsPropertiesProvider desktopClientsPropertiesProvider;

    public FileFilterImpl() {
    }

    public void init(Context context) {
        final InstrumentDTO instrument = context.getInfo().getInstrument();

        fileFilter = new ExtensionFileFilter();
        final List<String> supportedInstrumentExtensions = InstrumentUtil.getSupportedInstrumentExtensions(instrument);

        // also scan folders along with the supported files
        if (desktopClientsPropertiesProvider.isThermoRecursiveScan()) {
            supportedInstrumentExtensions.add("");
        }

        fileFilter.setExtensions(supportedInstrumentExtensions);
        fileFilter.setFileNamesToExclude(MAC_OS_DS_STORE_FILE_NAME);
    }

    @Override
    public boolean accept(File file) {
        return fileFilter.accept(file);
    }

}
