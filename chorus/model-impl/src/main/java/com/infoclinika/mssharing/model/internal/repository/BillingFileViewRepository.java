package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.view.BillingFileView;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Herman Zamula
 */
public interface BillingFileViewRepository extends CrudRepository<BillingFileView, Long> {

    @Query("SELECT distinct f FROM BillingFileView f WHERE (f.contentId in (:contentIds))")
    List<BillingFileView> findByContentId(@Param("contentIds") Set<String> contentIds);

    @Query("SELECT distinct f FROM BillingFileView f WHERE (f.archiveId in (:archiveIds))")
    List<BillingFileView> findAllByArchiveId(@Param("archiveIds") Iterable<String> strings);
}
