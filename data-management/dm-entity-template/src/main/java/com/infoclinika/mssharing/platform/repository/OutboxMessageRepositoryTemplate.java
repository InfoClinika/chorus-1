package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.OutboxMessageTemplate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface OutboxMessageRepositoryTemplate<ENTITY extends OutboxMessageTemplate>
    extends CrudRepository<ENTITY, Long> {

    @Query("from #{#entityName} m where m.from.id = :from")
    List<OutboxMessageTemplate> findByFrom(@Param("from") long from);
}
