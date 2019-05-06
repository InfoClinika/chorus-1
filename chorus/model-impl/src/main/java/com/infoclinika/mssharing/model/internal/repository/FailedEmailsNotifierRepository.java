package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.mailing.FailedMailNotificationReceiver;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Herman Zamula
 */
public interface FailedEmailsNotifierRepository extends JpaRepository<FailedMailNotificationReceiver, Long> {
}
