package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.AnnotationAttachment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author andrii.loboda
 */
public interface AnnotationAttachmentRepository extends CrudRepository<AnnotationAttachment, Long> {
    @Query("select a from Experiment ex join ex.annotationAttachment a where ex.id=:e")
    AnnotationAttachment findByExperiment(@Param("e") long experimentID);
}
