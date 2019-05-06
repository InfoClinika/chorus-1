package com.infoclinika.mssharing.web.persistence;

import org.springframework.stereotype.Component;

/**
 * @author Pavel Kaplin
 */
@Component
public class MysqlWebPersistenceProvider extends WebPersistenceProvider {

    protected String getDatabaseConfig() {
        return "/mysql.cfg.xml";
    }
}
