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
package com.infoclinika.mssharing.model.helper;

import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.repository.*;
import com.infoclinika.mssharing.platform.entity.InboxMessageTemplate;
import com.infoclinika.mssharing.platform.entity.ProjectSharingRequestTemplate;
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
    private VendorRepository vendorRepository;
    @Inject
    private InstrumentModelRepositoryTemplate instrumentModelRepository;
    @Inject
    private InstrumentTypeRepository instrumentTypeRepository;
    @Inject
    private GroupRepository groupRepository;
    @Inject
    /*package*/ ExperimentRepository experimentRepository;
    @Inject
    private DeletedExperimentRepository deletedExperimentRepository;
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private DeletedProjectRepository deletedProjectRepository;
    @Inject
    protected FileMetaDataRepository fileRepository;
    @Inject
    private AnnotationAttachmentRepository annotationAttachmentRepository;
    @Inject
    private DeletedFileMetaDataRepository deletedFileMetaDataRepository;

    @Inject
    public ChargeableItemRepository chargeableItemRepository;
    @Inject
    public UserPreferencesRepository userPreferencesRepository;
    @Inject
    protected UserRepository userRepository;
    @Inject
    protected LabRepository labRepository;
    @Inject
    InstrumentRepository instrumentRepository;
    @Inject
    PendingOperatorRepository pendingOperatorRepository;
    @Inject
    RawFilesRepository experimentFileRepository;
    @Inject
    private FeaturesRepository featuresRepository;
    @Inject
    FactorRepository factorRepository;
    @Inject
    InstrumentCreationRequestRepository instrumentCreationRequestRepository;

    @Inject
    ExperimentTypeRepositoryTemplate experimentTypeRepository;

    @Inject
    protected SpeciesRepositoryTemplate speciesRepository;

    @Inject
    private UserLabMembershipRepositoryTemplate userLabMembershipRepository;

    @Inject
    private InboxMessageRepositoryTemplate<InboxMessageTemplate<User>> inboxMessageRepository;


    @Inject
    private OutboxMessageRepositoryTemplate outboxMessageRepository;

    @Inject
    protected LabCreationRequestRepository labCreationRequestRepository;

    @Inject
    private UserInvitationLinkRepository userInvitationLinkRepository;
    @Inject
    private FileDownloadJobRepository fileDownloadJobRepository;
    @Inject
    private FileDownloadGroupRepository fileDownloadGroupRepository;
    @Inject
    protected UserLabMembershipRequestRepositoryTemplate<UserLabMembershipRequestTemplate<User, Lab>>
        userLabMembershipRequestRepository;
    @Inject
    private PayPalLogEntryRepository paymentLogEntryRepository;
    @Inject
    private LabPaymentAccountRepository labPaymentAccountRepository;
    @Inject
    private StoreCreditLogEntryRepository storeCreditLogEntryRepository;
    @Inject
    private FeatureLogRepository featureLogRepository;
    @Inject
    private ProjectSharingRequestRepositoryTemplate<ProjectSharingRequestTemplate> projectSharingRequestRepository;
    @Inject
    private TransactionDetailsRepository transactionDetailsRepository;
    @Inject
    private CopyProjectRequestRepository copyProjectRequestRepository;
    @Inject
    protected AttachmentRepository attachmentRepository;
    @Inject
    protected UserProjectAccessRepository userProjectAccessRepository;
    @Inject
    private AdvertisementRepository advertisementRepository;
    @Inject
    private FileAccessLogRepository fileAccessLogRepository;
    @Inject
    private ExperimentLabelToExperimentRepository experimentLabelToExperimentRepository;
    @Inject
    private ExperimentSampleRepository experimentSampleRepository;
    @Inject
    private PrepToExperimentSampleRepository prepToExperimentSampleRepository;
    @Inject
    private ExperimentPreparedSampleRepository experimentPreparedSampleRepository;
    @Inject
    private ExperimentLabelRepository experimentLabelRepository;
    @Inject
    private ExperimentLabelTypeRepository experimentLabelTypeRepository;
    @Inject
    FailedEmailsRecordRepository failedEmailsRecordRepository;
    @Inject
    FailedEmailsNotifierRepository failedEmailsNotifierRepository;
    @Inject
    private BillingPropertyRepository billingPropertyRepository;
    @Inject
    private AdductsRepository adductsRepository;
    @Inject
    private IssueRepository issueRepository;
    @Inject
    /*package*/ NgsExperimentTypeRepository ngsExperimentTypeRepository;
    @Inject
    /*package*/ ExperimentPrepMethodRepository experimentPrepMethodRepository;
    @Inject
    /*package*/ NtExtractionMethodRepository ntExtractionMethodRepository;

    public List<CrudRepository> get() {
        return ImmutableList.of(
            featuresRepository,
            instrumentCreationRequestRepository,
            labCreationRequestRepository,
            userProjectAccessRepository,
            userLabMembershipRequestRepository,
            copyProjectRequestRepository,
            inboxMessageRepository,
            outboxMessageRepository,
            experimentLabelToExperimentRepository,
            experimentRepository,
            deletedExperimentRepository,
            annotationAttachmentRepository,
            experimentFileRepository,
            prepToExperimentSampleRepository,
            experimentPreparedSampleRepository,
            experimentSampleRepository,
            factorRepository,
            fileDownloadGroupRepository,
            fileDownloadJobRepository,
            fileRepository,
            deletedFileMetaDataRepository,
            projectRepository,
            deletedProjectRepository,
            attachmentRepository,
            groupRepository,
            instrumentRepository,
            pendingOperatorRepository,
            userLabMembershipRepository,
            userInvitationLinkRepository,
            storeCreditLogEntryRepository,
            userPreferencesRepository,
            failedEmailsRecordRepository,
            failedEmailsNotifierRepository,
            issueRepository,
            userRepository,
            transactionDetailsRepository,
            labPaymentAccountRepository,
            chargeableItemRepository,
            labRepository,
            instrumentModelRepository,
            vendorRepository,
            instrumentTypeRepository,
            experimentTypeRepository,
            speciesRepository,
            paymentLogEntryRepository,
            featureLogRepository,
            projectSharingRequestRepository,
            advertisementRepository,
            fileAccessLogRepository,
            advertisementRepository,
            experimentLabelRepository,
            experimentLabelTypeRepository,
            billingPropertyRepository,
            adductsRepository,
            ngsExperimentTypeRepository,
            experimentPrepMethodRepository,
            ntExtractionMethodRepository
        );
    }
}
