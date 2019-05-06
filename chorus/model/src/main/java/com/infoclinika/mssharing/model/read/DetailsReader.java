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

import com.infoclinika.mssharing.model.api.MSFunctionMassAnalyzerType;
import com.infoclinika.mssharing.model.api.MSFunctionType;
import com.infoclinika.mssharing.model.api.MSResolutionType;
import com.infoclinika.mssharing.model.helper.LockMzItem;
import com.infoclinika.mssharing.model.read.dto.details.*;
import com.infoclinika.mssharing.model.write.ExperimentCategory;
import com.infoclinika.mssharing.platform.model.read.AttachmentsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.GroupItemTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.LabItemTemplateDetailed;
import com.infoclinika.mssharing.platform.model.read.RequestsDetailsReaderTemplate;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Stanislav Kurilin
 */
public interface DetailsReader
    extends DetailsReaderTemplate<FileItem, ExperimentItem, ProjectItem, InstrumentItem, LabItemTemplateDetailed,
    GroupItemTemplate> {

    InstrumentCreationItem readInstrumentCreation(long actor, long request);

    LabItemTemplate readLabRequestDetails(long actor, long lab);

    FileItem readFileDetailsWithConditions(long actor, long fileId, long experimentId);

    List<ShortFileWithConditions> readFilesAndConditionsForExperiment(long actor, long experimentId);

    List<ShortExperimentFileItem> readFilesInOtherExperiments(long actor, long experiment);

    AttachmentsReaderTemplate.AttachmentItem readExperimentAnnotationAttachment(long actor, long experiment);

    AttachmentsReaderTemplate.AttachmentItem readAnnotationAttachmentDetails(long actor, long attachmentId);

    class InstrumentCreationItem extends RequestsDetailsReaderTemplate.InstrumentCreationItemTemplate {

        public final String hplc;
        public final List<LockMzItem> lockMasses;

        public InstrumentCreationItem(RequestsDetailsReaderTemplate.InstrumentCreationItemTemplate other, String hplc,
                                      List<LockMzItem> lockMasses) {
            super(other);
            this.hplc = hplc;
            this.lockMasses = lockMasses;
        }
    }

    class ExperimentShortInfoDetailed extends ExperimentShortInfo {

        public final ExperimentCategory category;

        public ExperimentShortInfoDetailed(ExperimentShortInfo info, ExperimentCategory category) {
            super(info.id, info.labName, info.name, info.description, info.projectName,
                info.species, info.files, info.attachments, info.ownerEmail
            );
            this.category = category;
        }
    }
}
