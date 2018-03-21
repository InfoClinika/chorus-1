package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.InboxMessageTemplate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface InboxMessageRepositoryTemplate<ENTITY extends InboxMessageTemplate> extends CrudRepository<ENTITY, Long> {
    @Query("from #{#entityName} where to.id = :to")
    List<ENTITY> findByTo(@Param("to") long to);
}
