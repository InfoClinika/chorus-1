package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.LabCreationRequestTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author : Alexander Serebriyan
 */
public interface LabCreationRequestRepositoryTemplate<T extends LabCreationRequestTemplate>
    extends JpaRepository<T, Long> {

    @Query("select r from #{#entityName} r where r.labName = :labName")
    T findByLabName(@Param("labName") String labName);
}
