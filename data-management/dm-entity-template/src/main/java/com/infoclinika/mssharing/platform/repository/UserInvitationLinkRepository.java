package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.UserInvitationLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;

/**
 * @author : Alexander Serebriyan
 */
public interface UserInvitationLinkRepository extends JpaRepository<UserInvitationLink, Long> {
    @Query("select u from UserInvitationLink u where u.user.id = :userId")
    @Nullable
    UserInvitationLink findByUser(@Param("userId") Long userId);

    @Query("select u from UserInvitationLink u where u.invitationLink = :link")
    @Nullable
    UserInvitationLink findByLink(@Param("link") String link);
}
