package com.infoclinika.mssharing.dto;

import com.infoclinika.mssharing.dto.response.VendorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * author Ruslan Duboveckij
 */
public class NotSupportVendor extends RuntimeException {
    private static final Logger LOG = LoggerFactory.getLogger(NotSupportVendor.class);
    private static final String THIS_VENDOR_IS_NOT_SUPPORTED = "This vendor is not supported - ";
    private final String vendorName;

    public NotSupportVendor(VendorDTO vendor) {
        super(THIS_VENDOR_IS_NOT_SUPPORTED + vendor.toString());
        this.vendorName = vendor.name;

        LOG.error(THIS_VENDOR_IS_NOT_SUPPORTED + vendor.toString());
    }

    public String getVendorName() {
        return vendorName;
    }
}
