package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.mailing.FailedMailRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Herman Zamula
 */
public interface FailedEmailsRecordRepository extends JpaRepository<FailedMailRecord, Long> {
}
