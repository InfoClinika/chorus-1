package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author : Alexander Serebriyan
 */
public interface AttachmentRepositoryTemplate<T extends Attachment> extends JpaRepository<T, Long> {

    @Query("select a from ProjectTemplate pr join pr.attachments a where pr.id=:p")
    List<T> findByProject(@Param("p") long project);

    @Query("select a from ExperimentTemplate ex join ex.attachments a where ex.id=:e")
    List<T> findByExperiment(@Param("e") long experiment);

}
