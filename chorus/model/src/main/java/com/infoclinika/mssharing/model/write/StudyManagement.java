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

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.ProjectManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.ProjectSharingRequestManagement;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;


/**
 * Manage operations related to studying.
 * Users can create Experiments. Experiments are grouped in Projects.
 * <p/>
 * User can attach files to his experiment. Files are result of working with Instruments.
 * User can assign labels to files. Labels are just plain strings that would help on file search.
 * <p/>
 * One file can be attached to several experiments. On each attachment user can specify factors to each file.
 * Factor are...   //TODO: [stanislav.kurilin]
 *
 * @author Stanislav Kurilin
 */
public interface StudyManagement extends
    ProjectManagementTemplate<ProjectInfo>,
    ExperimentManagementTemplate<ExperimentInfo>,
    ProjectSharingRequestManagement {


    void removeProject(long project);

    void removeProject(long actor, long project, boolean permanently);

    long moveProjectToTrash(long actor, long projectId);

    long restoreProject(long actor, long projectId);

    /**
     * For internal use
     *
     * @param experiment - private experiment
     * @see #deleteExperiment(long, long)
     */
    @Deprecated
    void removeExperiment(long experiment);

    long moveExperimentToTrash(long actor, long experimentId);

    long restoreExperiment(long actor, long experimentId);

    long newProjectCopyRequest(long actor, long copyTo, long project);

    void approveCopyProjectRequest(long actor, long project, long billLaboratory);

    void refuseCopyProjectRequest(long actor, long project);

    void runPreCacheViewers(long actor, long experimentId);

    void setBlogEnabled(long userId, long project, boolean blogEnabled);

    class CopyProjectInfo extends CopyProjectInfoTemplate {
        private final long billLab;

        public CopyProjectInfo(long project, long newOwner, long owner, long billLab, boolean notification) {
            super(project, newOwner, owner, notification);
            this.billLab = billLab;
        }

        public long getBillLab() {
            return billLab;
        }

    }
}
