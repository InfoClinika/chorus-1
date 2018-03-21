/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.helper;

import com.infoclinika.mssharing.platform.model.helper.MailSendingHelperTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Stanislav Kurilin
 */
@Transactional(readOnly = true)
public interface MailSendingHelper extends MailSendingHelperTemplate {

    String fileName(long fileMetaDataId);

    ExperimentDetails experimentDetails(long experiment);

    boolean isSkipSending(String email);

    final class ExperimentDetails{
        public final String name;
        public final String authorEmail;

        public ExperimentDetails(String name, String authorEmail) {
            this.name = name;
            this.authorEmail = authorEmail;
        }
    }

    class ChorusUserDetails extends UserDetails {

        public final boolean skipEmails;

        public ChorusUserDetails(String name, String email, boolean skipEmails) {
            super(name, email);
            this.skipEmails = skipEmails;
        }
    }
}
