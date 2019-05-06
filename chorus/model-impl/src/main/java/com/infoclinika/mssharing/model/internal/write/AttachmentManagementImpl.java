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
package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.AnnotationAttachment;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.Util;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.model.write.AttachmentManagement;
import com.infoclinika.mssharing.platform.entity.Attachment;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.helper.write.AttachmentsManager;
import com.infoclinika.mssharing.platform.model.impl.write.DefaultAttachmentManagement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.Date;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Oleksii Tymchenko
 */
@Service("attachmentManagement")
@Transactional
public class AttachmentManagementImpl extends DefaultAttachmentManagement<Attachment<User>>
    implements AttachmentManagement {

    @Inject
    private ApplicationSettingsRepository applicationSettingsRepository;
    @Inject
    private AnnotationAttachmentRepository annotationAttachmentRepository;
    @Inject
    @Named("validator")
    private RuleValidator validator;
    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private Provider<Date> current;


    @Override
    public long getMaxAttachmentSize() {
        return applicationSettingsRepository.findMaxSize().getValue();
    }

    @Override
    public void discardAnnotationAttachment(long actor, long annotationAttachment) {
        if (!validator.canModifyAnnotationAttachment(actor, annotationAttachment)) {
            throw new AccessDenied(
                "User cannot discard the annotation attachment. User ID = " + actor + ". Annoatation Attachment ID = " +
                    annotationAttachment);
        }
        final AnnotationAttachment toDiscard = annotationAttachmentRepository.findOne(annotationAttachment);

        annotationAttachmentRepository.delete(toDiscard);
    }

    @Override
    public void updateExperimentAnnotationAttachment(long actor, long experiment, Long annotationAttachment) {
        if (!ruleValidator.userHasEditPermissionsOnExperiment(actor, experiment)) {
            throw new AccessDenied(
                "User cannot edit the annotation for the experiment. User ID = " + actor + ". Experiment ID = " +
                    experiment);
        }
        final ActiveExperiment entity = checkNotNull(experimentRepository.findOne(experiment));

        final Long existingId = entity.annotationAttachment != null ? entity.annotationAttachment.getId() : null;


        //discard old attachments as well
        if (existingId != null && !existingId.equals(annotationAttachment)) {
            discardAnnotationAttachment(actor, existingId);
        }

        entity.annotationAttachment =
            annotationAttachment == null ? null : annotationAttachmentRepository.findOne(annotationAttachment);
        //explicit save to avoid implicit merge problems on the future transaction commit
        experimentRepository.save(entity);
    }

    @Override
    public long newAnnotationAttachment(long actor, String fileName, long sizeInBytes) {
        final User creator = Util.USER_FROM_ID.apply(actor);
        fileName = fileName.replaceAll("\\s", AttachmentsManager.WHITE_SPACE_REPLACEMENT);
        final AnnotationAttachment attachment = new AnnotationAttachment(creator, fileName, current.get(), sizeInBytes);
        return annotationAttachmentRepository.save(attachment).getId();
    }
}
