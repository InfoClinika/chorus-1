package com.infoclinika.mssharing.model.internal.repository;


import com.infoclinika.mssharing.model.internal.entity.RestToken;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;

public interface RestTokenRepository extends CrudRepository<RestToken, Long> {
    @Nullable
    @Query("select rt from RestToken rt where rt.token = :token")
    RestToken findByToken(@Param("token") String token);
}
