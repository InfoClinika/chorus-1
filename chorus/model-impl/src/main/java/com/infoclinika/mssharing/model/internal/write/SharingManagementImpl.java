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

import com.google.common.collect.Collections2;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.Group;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.write.FileMovingManager;
import com.infoclinika.mssharing.model.write.SharingManagement;
import com.infoclinika.mssharing.platform.entity.EntityUtil;
import com.infoclinika.mssharing.platform.model.impl.write.sharing.DefaultSharingManagement;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

/**
 * @author Stanislav Kurilin
 */
@Lazy(true)
@Service("sharingManagement")
@Transactional
public class SharingManagementImpl extends DefaultSharingManagement<Group> implements SharingManagement {

    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private FileMovingManager fileMovingManager;
    @Inject
    private RuleValidator ruleValidator;

    @Override
    protected void afterUpdateSharingPolicy(long actor,
                                            long project,
                                            Map<Long, Access> colleagues,
                                            Map<Long, Access> groups,
                                            boolean withEmailNotification,
                                            Map newAllCollaborators) {
        makeExperimentsActiveForDownload(experimentRepository.findByProject(project), newAllCollaborators.keySet());
    }


    private void makeExperimentsActiveForDownload(Collection<ActiveExperiment> experiments, Collection<User> users) {
        final Collection<Long> ids = Collections2.transform(users, EntityUtil.ENTITY_TO_ID);
        for (ActiveExperiment experiment : experiments) {
            if (!ruleValidator.isExperimentReadyToDownload(experiment)) {
                fileMovingManager.requestExperimentFilesUnarchiving(experiment.getId(), ids);
            } else {
                fileMovingManager.updateAccessForExperiment(experiment.getId());
            }
        }
    }


}
