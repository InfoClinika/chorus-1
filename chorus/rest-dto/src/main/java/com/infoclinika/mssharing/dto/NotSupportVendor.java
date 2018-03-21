package com.infoclinika.mssharing.dto;

import com.infoclinika.mssharing.dto.response.VendorDTO;
import org.apache.log4j.Logger;

/**
 * author Ruslan Duboveckij
 */
public class NotSupportVendor extends RuntimeException {
    private static final Logger LOG = Logger.getLogger(NotSupportVendor.class);
    private static final String message = "This vendor is not supported - ";
    private final String vendorName;

    public NotSupportVendor(VendorDTO vendor) {
        super(message + vendor.toString());
        this.vendorName = vendor.name;

        LOG.error(message + vendor.toString());
    }

    public String getVendorName() {
        return vendorName;
    }
}
