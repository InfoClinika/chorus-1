package com.infoclinika.mssharing.platform.model.impl.write;

import com.infoclinika.mssharing.platform.entity.LabCreationRequestTemplate;
import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.entity.PersonData;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.*;
import com.infoclinika.mssharing.platform.model.helper.write.LabManager;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.platform.repository.LabCreationRequestRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.LabRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.infoclinika.mssharing.platform.model.DefaultTransformers.personalInfoToData;

/**
 * @author : Alexander Serebriyan
 */
@Transactional
@Component
public class DefaultLabManagement<LAB extends LabTemplate, LAB_INFO extends LabManagementTemplate.LabInfoTemplate> implements LabManagementTemplate<LAB_INFO> {

    @Inject
    private RuleValidator ruleValidator;
    @Inject
    private LabManager<LAB, LAB_INFO> labManager;
    @Inject
    private LabCreationRequestRepositoryTemplate<LabCreationRequestTemplate> labCreationRequestRepository;
    @Inject
    private LabRepositoryTemplate<LAB> labRepository;
    @Inject
    private UserRepositoryTemplate<UserTemplate> userRepository;
    @Inject
    private Provider<Date> current;
    @Inject
    private RequestsTemplate requests;
    @Inject
    private NotifierTemplate notifier;
    @Inject
    private InboxNotifierTemplate inboxNotifier;

    @Override
    public void editLab(Long actor, Long lab, LAB_INFO labInfo) {
        if (!ruleValidator.canEditLabDetails(actor, lab)) throw new AccessDenied("Couldn't edit lab");
        labManager.editLab(lab, labInfo);
        afterEditLab(actor, lab, labInfo);
    }

    private void afterEditLab(Long actor, Long lab, LAB_INFO labInfo) {
    }

    @Override
    public Long createLab(Long actor, LAB_INFO labInfo, String contactEmail) {
        if (!ruleValidator.canCreateLabs(actor)) throw new AccessDenied("Couldn't create labs");
        LAB lab = labManager.createLab(labInfo, contactEmail);
        LAB labAfterCreate = afterCreateLab(actor, lab, labInfo);
        return labAfterCreate.getId();
    }

    protected LAB afterCreateLab(Long actor, LAB lab, LAB_INFO labInfo) {
        return lab;
    }


    @Override
    public Long requestLabCreation(LabInfoTemplate labInfo, String contactEmail) {
        final LabCreationRequestTemplate existingRequest = labCreationRequestRepository.findByLabName(labInfo.labName);
        if (existingRequest != null) {
            //todo[tymchenko]: discuss what happens if several users requested the same laboratory creation
            return existingRequest.getId();
        }
        final LAB lab = labRepository.findByName(labInfo.labName);
        if (lab != null) {
            throw new IllegalArgumentException("Such laboratory name already used");
        }
        final LabCreationRequestTemplate labCreationRequest = new LabCreationRequestTemplate(labInfo.labName, labInfo.institutionUrl,
                personalInfoToData(labInfo.labHead), contactEmail, current.get());
        UserTemplate requester = userRepository.findByEmail(contactEmail);
        if (requester != null) {
            requests.addOutboxItem(requester.getId(), "Administrators", "You requested creation of lab " + labInfo.labName, current.get());
        }
        notifyAdmins(labInfo, contactEmail);
        return saveLabCreationRequest(labCreationRequest).getId();
    }

    private void notifyAdmins(LabInfoTemplate labInfo, String contactEmail) {
        final String headName = labInfo.labHead.firstName + " " + labInfo.labHead.lastName;
        for (UserTemplate user : userRepository.findAdmins()) {
            notifier.sendLabCreationRequestNotification(user.getId(), contactEmail, headName, labInfo.labName, labInfo.labHead.email);
        }
    }

    private LabCreationRequestTemplate saveLabCreationRequest(LabCreationRequestTemplate request) {
        request.setLastModification(current.get());
        return labCreationRequestRepository.save(request);
    }

    @Override
    public void editLabRequestInfo(Long actor, Long requestId, LabInfoTemplate labInfo) {
        if (!ruleValidator.canEditLabCreationRequests(actor))
            throw new AccessDenied("Couldn't edit lab creation request");
        final LabCreationRequestTemplate request = findLabCreationRequest(requestId);
        request.setInstitutionUrl(labInfo.institutionUrl);
        request.setLabName(labInfo.labName);
        request.setHeadData(personalInfoToData(labInfo.labHead));
        labCreationRequestRepository.save(request);
    }

    private LabCreationRequestTemplate findLabCreationRequest(long requestId) {
        return checkNotNull(labCreationRequestRepository.findOne(requestId));
    }

    @Override
    public Long confirmLabCreation(Long actor, Long labCreationRequestId) {

        if (!ruleValidator.canProcessLabRequests(actor)) throw new AccessDenied("Can't process lab requests");

        final LabCreationRequestTemplate request = labCreationRequestRepository.findOne(labCreationRequestId);
        if (request == null) {
            notifier.staleOnLabRequest(actor, labCreationRequestId);
            throw new StaleLabCreationRequestException(labCreationRequestId);
        }
        final PersonData headData = request.getHeadData();
        final UserManagementTemplate.PersonInfo personInfo = DefaultTransformers.personDataToPersonInfo(headData);
        final LAB_INFO labInfo = (LAB_INFO) new LabInfoTemplate(request.getInstitutionUrl(), personInfo, request.getLabName());
        final LAB lab = labRepository.findOne(createLab(actor, labInfo, request.getContactEmail()));

        sendLabCreationEmailNotifcations(lab);

        labCreationRequestRepository.delete(request);
        UserTemplate requester = userRepository.findByEmail(request.getContactEmail());
        if (requester != null) {
            inboxNotifier.notify(actor, requester.getId(), "Your request for creation lab " + request.getLabName() + " was approved");
        }
        return lab.getId();
    }

    protected void sendLabCreationEmailNotifcations(LAB lab) {
        final UserTemplate head = lab.getHead();
        notifier.labCreationApproved(lab.getContactEmail(), lab.getId());
        notifier.sendLabCreatedNotification(head.getId(), lab.getName());
    }

    @Override
    public void rejectLabCreation(Long actor, Long labCreationRequestId, String rejectComment) {
        if (!ruleValidator.canProcessLabRequests(actor)) throw new AccessDenied("Cannot update lab creation request");
        final LabCreationRequestTemplate request = labCreationRequestRepository.findOne(labCreationRequestId);
        if (request != null) {
            notifier.labCreationRejected(request.getContactEmail(), rejectComment, request.getLabName());
            labCreationRequestRepository.delete(request);
            UserTemplate requester = userRepository.findByEmail(request.getContactEmail());
            if (requester != null) {
                inboxNotifier.notify(actor, requester.getId(), "Your request for creation lab " + request.getLabName() + " was rejected: " + rejectComment);
            }
        } else {
            notifier.staleOnLabRequest(actor, labCreationRequestId);
        }
    }

    @Override
    public boolean isLabHead(long actor, long lab) {
        return labRepository.findOne(lab).getHead().getId().equals(actor);
    }
}
