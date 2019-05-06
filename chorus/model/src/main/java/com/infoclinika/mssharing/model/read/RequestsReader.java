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
package com.infoclinika.mssharing.model.read;

import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Stanislav Kurilin
 */
public interface RequestsReader extends RequestsReaderTemplate {

    ImmutableSortedSet<ProjectCopyRequest> myCopyProjectInbox(Long actor);

    class ProjectCopyRequest extends ComparableRequest<ProjectCopyRequest> {
        public final Long id;
        public final String fullName;
        public final String senderEmail;
        public final Long receiver;
        public final Long sender;
        public final Long project;
        public final String projectName;
        public final Date dateSent;

        public ProjectCopyRequest(Long id, String fullName, Long sender, String senderEmail, Long receiver,
                                  Long project, String projectName, Date dateSent) {
            this.id = id;
            this.fullName = fullName;
            this.senderEmail = senderEmail;
            this.receiver = receiver;
            this.sender = sender;
            this.project = project;
            this.projectName = projectName;
            this.dateSent = dateSent;
        }

        @Override
        public int compareTo(@NotNull ProjectCopyRequest o) {
            return compareAllFields(
                id.compareTo(o.id)
            );
        }
    }
}
