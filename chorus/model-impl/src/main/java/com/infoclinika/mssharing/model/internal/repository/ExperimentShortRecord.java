/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.restorable.AbstractProject;

/**
 * @author Oleksii Tymchenko
 */
public class ExperimentShortRecord {
    public final long id;
    public final String experimentName;
    public final AbstractProject project;
    public final User creator;

    public ExperimentShortRecord(long id, String experimentName, AbstractProject project, User creator) {
        this.id = id;
        this.experimentName = experimentName;
        this.project = project;
        this.creator = creator;
    }
}
