package com.infoclinika.sso.model.internal.repository;

import com.infoclinika.sso.model.internal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author andrii.loboda
 */
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u from User u where u.chorusUsername=:chorusUsername")
    User findByChorusUsername(@Param("chorusUsername") String username);

    @Query("select u from User u where u.panoramaUsername=:panoramaUsername")
    User findByPanoramaUsername(@Param("panoramaUsername") String username);
}
