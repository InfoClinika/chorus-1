/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.entity.UserLabMembership;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author andrii.loboda
 */
public interface UserLabMembershipRepositoryTemplate<USER extends UserTemplate<?>, LAB extends LabTemplate<?>> extends JpaRepository<UserLabMembership<USER, LAB>, Long> {
    @Query("select r from #{#entityName} r where r.lab.id = :labId and r.user.id = :userId")
    UserLabMembership<USER, LAB> findByLabAndUser(@Param("labId") long labId, @Param("userId") long userId);
}
