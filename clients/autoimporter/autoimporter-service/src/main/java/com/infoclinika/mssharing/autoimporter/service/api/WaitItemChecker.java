package com.infoclinika.mssharing.autoimporter.service.api;

import com.infoclinika.mssharing.autoimporter.model.bean.WaitItem;
import com.infoclinika.mssharing.dto.response.VendorDTO;

/**
 * @author timofey.kasyanov
 *     06.03.14
 */
public interface WaitItemChecker {

    boolean isItemAvailable(VendorDTO vendor, WaitItem item);

}
