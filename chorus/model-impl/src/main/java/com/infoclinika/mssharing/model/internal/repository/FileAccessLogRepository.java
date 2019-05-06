package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.FileAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author vladislav.kovchug
 */

@Repository
public interface FileAccessLogRepository extends JpaRepository<FileAccessLog, Long> {

}
