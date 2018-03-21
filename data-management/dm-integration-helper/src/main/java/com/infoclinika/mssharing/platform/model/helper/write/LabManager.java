package com.infoclinika.mssharing.platform.model.helper.write;

import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.LabRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author : Alexander Serebriyan
 */
@SuppressWarnings("unchecked")
@Component
public class LabManager<LAB extends LabTemplate, LAB_INFO extends LabManagementTemplate.LabInfoTemplate> {

    @Inject
    private UserRepositoryTemplate<UserTemplate> userRepositoryTemplate;
    @Inject
    private LabRepositoryTemplate<LAB> labRepositoryTemplate;
    @Inject
    private InstrumentRepositoryTemplate<InstrumentTemplate> instrumentRepositoryTemplate;
    @Inject
    private UserManagementTemplate userManagement;
    @Inject
    private EntityFactories factories;
    @Inject
    private Provider<Date> current;

    public LAB createLab(LAB_INFO labInfo, String contactEmail) {

        final UserTemplate head = findOrCreateLabHead(labInfo.labHead);
        LAB lab = (LAB) factories.lab.get();
        lab.setName(labInfo.labName);
        lab.setInstitutionUrl(labInfo.institutionUrl);
        lab.setContactEmail(contactEmail);
        lab.setHead(head);
        final LAB savedLab = saveLab(lab);
        head.addLab(lab);
        userRepositoryTemplate.save(head);

        return savedLab;
    }

    public void editLab(Long labId, LabManagementTemplate.LabInfoTemplate labInfo) {
        final LAB lab = findLab(labId);
        final UserTemplate labHead = findOrCreateLabHead(labInfo.labHead);
        changeLabHead(lab, labHead);
        lab.setName(labInfo.labName);
        lab.setInstitutionUrl(labInfo.institutionUrl);
        final LAB savedLab = saveLab(lab);

        labHead.addLab(savedLab);
        userRepositoryTemplate.save(labHead);
    }


    private LAB saveLab(LAB lab) {
        lab.setLastModification(current.get());
        return labRepositoryTemplate.save(lab);
    }

    private UserTemplate findOrCreateLabHead(UserManagementTemplate.PersonInfo labHead) {
        final UserTemplate existingLabHead = userRepositoryTemplate.findByEmail(labHead.email);
        if (existingLabHead != null) {
            return existingLabHead;
        }

        //todo[tymchenko]: send the Credentials email to this user.
        final UserManagementTemplate.PersonInfo personInfo = new UserManagementTemplate.PersonInfo(labHead.firstName, labHead.lastName, labHead.email);
        final long labHeadUserId = userManagement.createUserWithGeneratedPassword(personInfo, labHead.email);
        return checkNotNull(userRepositoryTemplate.findOne(labHeadUserId));

    }

    private LAB findLab(long labId) {
        return checkNotNull(labRepositoryTemplate.findOne(labId));
    }

    private void changeLabHead(LAB lab, UserTemplate labHead) {
        lab.setHead(labHead);
        List<InstrumentTemplate> instruments = instrumentRepositoryTemplate.findByLab(lab.getId());
        for (InstrumentTemplate i : instruments) {
            i.addOperator(labHead);
        }
        instrumentRepositoryTemplate.save(instruments);
    }
}
