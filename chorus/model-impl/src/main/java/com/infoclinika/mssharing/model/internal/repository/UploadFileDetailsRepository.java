package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.upload.FileDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Vitalii Petkanych
 */
@Repository
@Transactional
public interface UploadFileDetailsRepository extends CrudRepository<FileDetails, Long> {
}
