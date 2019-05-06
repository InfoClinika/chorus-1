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
package com.infoclinika.mssharing.platform.model.testing.helper;

import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.platform.entity.InboxMessageTemplate;
import com.infoclinika.mssharing.platform.entity.UserLabMembershipRequestTemplate;
import com.infoclinika.mssharing.platform.repository.*;
import org.springframework.data.repository.CrudRepository;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Stanislav Kurilin
 */
class Repositories {
    @Inject
    protected LabRepositoryTemplate labRepository;
    @Inject
    protected InstrumentRepositoryTemplate instrumentRepository;
    @Inject
    protected ExperimentFileRepositoryTemplate experimentFileRepository;
    @Inject
    protected FactorRepositoryTemplate factorRepository;
    @Inject
    protected InstrumentCreationRequestRepositoryTemplate instrumentCreationRequestRepository;
    @Inject
    protected ExperimentTypeRepositoryTemplate experimentTypeRepository;
    @Inject
    protected SpeciesRepositoryTemplate speciesRepository;
    @Inject
    protected LabCreationRequestRepositoryTemplate labCreationRequestRepository;
    @Inject
    protected UserLabMembershipRequestRepositoryTemplate<UserLabMembershipRequestTemplate>
        userLabMembershipRequestRepository;
    @Inject
    protected AttachmentRepositoryTemplate attachmentRepository;
    @Inject
    protected UserProjectAccessRepository userProjectAccessRepository;
    @Inject
    private VendorRepositoryTemplate vendorRepository;
    @Inject
    private InstrumentModelRepositoryTemplate instrumentModelRepository;
    @Inject
    private InstrumentTypeRepositoryTemplate instrumentTypeRepository;
    @Inject
    private InstrumentStudyTypeRepositoryTemplate instrumentStudyTypeRepository;
    @Inject
    private GroupRepositoryTemplate groupRepository;
    @Inject
    private ExperimentRepositoryTemplate experimentRepository;
    @Inject
    private ProjectRepositoryTemplate projectRepository;
    @Inject
    private FileRepositoryTemplate fileRepository;
    @Inject
    private UserRepositoryTemplate userRepository;
    @Inject
    private UserLabMembershipRepositoryTemplate userLabMembershipRepository;
    @Inject
    private InboxMessageRepositoryTemplate<InboxMessageTemplate> inboxMessageRepository;
    @Inject
    private OutboxMessageRepositoryTemplate outboxMessageRepository;
    @Inject
    private UserInvitationLinkRepository userInvitationLinkRepository;
    @Inject
    private ProjectSharingRequestRepositoryTemplate projectSharingRequestRepository;


    public List<CrudRepository> get() {
        return ImmutableList.of(
            instrumentCreationRequestRepository,
            labCreationRequestRepository,
            userProjectAccessRepository,
            userLabMembershipRequestRepository,
            inboxMessageRepository,
            outboxMessageRepository,
            experimentRepository,
            experimentFileRepository,
            factorRepository,
            fileRepository,
            projectRepository,
            attachmentRepository,
            groupRepository,
            instrumentRepository,
            userLabMembershipRepository,
            userInvitationLinkRepository,
            userRepository,
            labRepository,
            instrumentModelRepository,
            vendorRepository,
            instrumentTypeRepository,
            instrumentStudyTypeRepository,
            experimentTypeRepository,
            speciesRepository,
            projectSharingRequestRepository
        );
    }
}
