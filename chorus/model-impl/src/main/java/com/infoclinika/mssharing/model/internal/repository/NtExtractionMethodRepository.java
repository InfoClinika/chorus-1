package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.restorable.NtExtractionMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author timofei.kasianov 8/8/18
 */
public interface NtExtractionMethodRepository extends JpaRepository<NtExtractionMethod, Long> {

    @Query("select m from NtExtractionMethod m where m.title = :title")
    NtExtractionMethod findByTitle(@Param("title") String title);

}
