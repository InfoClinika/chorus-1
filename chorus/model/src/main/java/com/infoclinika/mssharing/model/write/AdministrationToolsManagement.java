package com.infoclinika.mssharing.model.write;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Herman Zamula
 */
@Transactional
public interface AdministrationToolsManagement {

    void broadcastNotification(long actor, String title, String body);

    void unarchiveInconsistentFiles(long actor);

}
