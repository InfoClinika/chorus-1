package com.infoclinika.mssharing.web;

import com.infoclinika.mssharing.web.persistence.WebPersistenceProvider;
import org.springframework.stereotype.Component;

/**
 * @author Pavel Kaplin
 */
@Component
public class H2WebPersistenceProvider extends WebPersistenceProvider {
    @Override
    protected String getDatabaseConfig() {
        return "/h2.cfg.xml";
    }
}
