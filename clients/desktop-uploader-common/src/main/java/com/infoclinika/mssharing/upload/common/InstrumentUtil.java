// Copyright (c) 2016, NanoString Technologies, Inc.  All rights reserved.
// Use of this file for any purpose requires prior written consent of NanoString Technologies, Inc.

package com.infoclinika.mssharing.upload.common;

import com.infoclinika.mssharing.dto.response.FileExtensionDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import com.infoclinika.mssharing.dto.response.VendorDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Yevhen Panko (yevhen.panko@teamdev.com)
 */
public class InstrumentUtil {
    private static final String DEFAULT = "Default";

    public static List<String> getSupportedInstrumentExtensions(InstrumentDTO instrument) {
        final VendorDTO vendor = instrument.getVendor();
        final Set<FileExtensionDTO> fileUploadExtensions = vendor.fileUploadExtensions;

        final List<String> supportedExtensions = new ArrayList<>();
        for (FileExtensionDTO fileUploadExtension : fileUploadExtensions) {
            supportedExtensions.add(fileUploadExtension.name);

            for (String additionalExtension : fileUploadExtension.additionalExtensions.keySet()) {
                supportedExtensions.add(additionalExtension);
            }
        }

        return supportedExtensions;
    }

    public static boolean containsNonDefaultInstrument(List<InstrumentDTO> instruments){
        for (InstrumentDTO instrument : instruments) {
            if (!instrument.getName().equalsIgnoreCase(DEFAULT)){
                return true;
            }
        }

        return false;
    }

    public static boolean containsOnlyDefaultInstrument(List<InstrumentDTO> instruments){
        if (instruments.isEmpty()){
            return false;
        }

        for (InstrumentDTO instrument : instruments) {
            if (!instrument.getName().equalsIgnoreCase(DEFAULT)){
                return false;
            }
        }

        return true;
    }
}
