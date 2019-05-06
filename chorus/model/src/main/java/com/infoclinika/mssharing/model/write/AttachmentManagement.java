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
package com.infoclinika.mssharing.model.write;

import com.infoclinika.mssharing.platform.model.write.AttachmentManagementTemplate;

/**
 * Management of the project and experiment attachments. Also it manages annotation attachments uploaded in csv
 * format to be able to add them to resulted dataCubes of processing run.
 * <p/>
 * As a user I want to be able to manage the supplemental materials for the projects and experiments
 *
 * @author Oleksii Tymchenko
 */
public interface AttachmentManagement extends AttachmentManagementTemplate {

    long getMaxAttachmentSize();

    void discardAnnotationAttachment(long actor, long annotationAttachment);

    void updateExperimentAnnotationAttachment(long actor, long experiment, Long annotationAttachment);

    long newAnnotationAttachment(long actor, String filename, long sizeInBytes);
}
