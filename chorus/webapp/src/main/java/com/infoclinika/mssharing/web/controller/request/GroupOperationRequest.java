/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.web.controller.request;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents group on create and update group screen.
 *
 * @author Oleksii Tymchenko
 */
public class GroupOperationRequest {
    private final Set<Long> members = new HashSet<Long>();
    private long id;
    private String name;

    public GroupOperationRequest() {
    }

    public Set<Long> getMembers() {
        return members;
    }

    public void setCollaborators(Set<Long> members) {
        this.members.clear();
        this.members.addAll(members);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
