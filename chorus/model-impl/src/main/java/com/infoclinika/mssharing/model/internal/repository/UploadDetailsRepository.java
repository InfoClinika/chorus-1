package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.upload.UploadDetails;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadStatus;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Vitalii Petkanych
 */
@Repository
@Transactional
public interface UploadDetailsRepository extends CrudRepository<UploadDetails, Long> {
    List<UploadDetails> findByUserIdAndStatus(long userId, UploadStatus status);

    List<UploadDetails> findByTypeAndStatus(UploadType type, UploadStatus status);
}
