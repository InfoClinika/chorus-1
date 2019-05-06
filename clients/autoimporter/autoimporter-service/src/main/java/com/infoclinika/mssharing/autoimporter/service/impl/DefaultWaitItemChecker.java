package com.infoclinika.mssharing.autoimporter.service.impl;

import com.infoclinika.msdata.dataimport.thermo.ThermoRawFileChecker;
import com.infoclinika.mssharing.autoimporter.model.bean.WaitItem;
import com.infoclinika.mssharing.autoimporter.service.api.WaitItemChecker;
import com.infoclinika.mssharing.dto.VendorEnum;
import com.infoclinika.mssharing.dto.response.VendorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author timofey.kasyanov
 *     06.03.14
 */
@Component
public class DefaultWaitItemChecker implements WaitItemChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWaitItemChecker.class);

    @Override
    public boolean isItemAvailable(VendorDTO vendor, WaitItem item) {
        if (VendorEnum.getVendorEnum(vendor.name) == VendorEnum.THERMO) {
            LOGGER.info("Filtering Thermo file: {}", item.getName());
            try {
                return ThermoRawFileChecker.isFileComplete(item.getFile().getAbsolutePath()) && item.checkSize();
            } catch (Exception e) {
                return item.isAvailable();
            }
        } else {
            return item.isAvailable() && item.checkSize();
        }

    }
}
