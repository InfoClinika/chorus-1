package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.UploadAppConfiguration;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author Ruslan Duboveckij
 */
public interface UploadAppConfigurationRepository extends CrudRepository<UploadAppConfiguration, Long> {
    @Query("select c from UploadAppConfiguration c where c.user.id = :user")
    List<UploadAppConfiguration> findByUser(@Param("user") long user);
}
