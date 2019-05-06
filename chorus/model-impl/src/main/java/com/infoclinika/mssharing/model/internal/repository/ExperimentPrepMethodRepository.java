package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.restorable.ExperimentPrepMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Vitalii Petkanych
 */
public interface ExperimentPrepMethodRepository extends JpaRepository<ExperimentPrepMethod, Integer> {

    @Query("select m from ExperimentPrepMethod m where m.title = :title")
    ExperimentPrepMethod findByTitle(@Param("title") String title);

}
