package com.infoclinika.mssharing.model.internal;

import com.infoclinika.mssharing.platform.model.mailing.EmailerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Pavel Kaplin
 */
class MockEmailer implements EmailerTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockEmailer.class);

    @Override
    public void send(String to, String title, String message) {
        LOGGER.info("Skipping email to {}: {} ", to, title);
    }

    @Override
    public void send(List<String> to, String title, String message) {
        LOGGER.info("Skipping email to {}: {}", to, title);
    }
}
