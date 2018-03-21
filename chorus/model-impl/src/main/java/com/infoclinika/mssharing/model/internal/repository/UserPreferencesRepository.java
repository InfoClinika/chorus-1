package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Alexander Orlov
 */
@Transactional
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    @Query("select pref from UserPreferences pref join pref.user user where user.id = :userId")
    UserPreferences findByUserId(@Param("userId") long userId);
}
