package com.infoclinika.mssharing.model.internal;

import com.infoclinika.mssharing.platform.model.mailing.EmailerTemplate;
import org.apache.log4j.Logger;

/**
 * @author Pavel Kaplin
 */
class MockEmailer implements EmailerTemplate {

    private static final Logger log = Logger.getLogger(MockEmailer.class);

    @Override
    public void send(String to, String title, String message) {
        log.info("Skipping email to " + to + ": " + title);
    }
}
