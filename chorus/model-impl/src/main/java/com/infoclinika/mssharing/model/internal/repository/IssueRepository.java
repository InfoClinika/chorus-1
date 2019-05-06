package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Vladislav Kovchug
 */
public interface IssueRepository extends JpaRepository<Issue, Long> {

    @Query("select i from Issue i where i.owner.id = :ownerId")
    Page<Issue> findByOwnerId(@Param("ownerId") long ownerId, Pageable pageable);

}
