package com.infoclinika.msdata.dataimport.thermo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThermoRawFileChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThermoRawFileChecker.class);
    private static final Object LOCKER = new Object();
    private static ThermoRawFileChecker instance;

    private ThermoRawFileChecker() {
    }

    public static boolean isFileComplete(String file) throws Exception {
        synchronized (LOCKER) {
            if (instance == null) {
                instance = new ThermoRawFileChecker();
            }
            return instance.checkFile(file);
        }
    }

    public static boolean isCheckingAvailable() {
        LOGGER.info("Check if Thermo file checking is available");
        synchronized (LOCKER) {
            try {
                XRawOCXWrapperImpl.create();
                return true;
            } catch (Throwable e) {
                LOGGER.info("Thermo file checking is not available");
                return false;
            }
        }
    }

    private boolean checkFile(String file) throws Exception {
        LOGGER.info("Checking thermo file: {}", file);

        final XRawOCXWrapperImpl wrapper = new XRawOCXWrapperImpl();
        boolean available;
        try {
            wrapper.open(file);
            available = !wrapper.inAcquisition();
        } catch (Exception e) {
            LOGGER.warn("File is not available: {}", file, e);
            return false;
        } finally {
            try {
                wrapper.close();
            } catch (Exception e) {
                LOGGER.error("Exception during wrapper close", e);
            }
        }

        return available;
    }
}


