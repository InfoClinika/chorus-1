package com.infoclinika.mssharing.services.billing.persistence.helper;

import java.util.Date;

/**
 * @author Elena Kurilina
 */
public interface StorageLogHelper {

    void log(long logInterval);

    void sumLogs(Date dayToLog);

}
