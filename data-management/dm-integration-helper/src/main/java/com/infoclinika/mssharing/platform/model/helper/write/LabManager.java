package com.infoclinika.mssharing.platform.model.helper.write;

import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate.PersonInfo;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.LabRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Date;

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
    private UserManagementTemplate userManagement;
    @Inject
    private EntityFactories factories;
    @Inject
    private Provider<Date> current;

    public LAB createLab(LAB_INFO labInfo, String contactEmail) {
        LAB lab = (LAB) factories.lab.get();
        lab.setContactEmail(contactEmail);
        return setLabInfo(lab, labInfo);
    }

    public void editLab(Long labId, LabManagementTemplate.LabInfoTemplate labInfo) {
        final LAB lab = findLab(labId);
        setLabInfo(lab, labInfo);
    }

    private LAB setLabInfo(LAB lab, LabManagementTemplate.LabInfoTemplate labInfo) {
        final UserTemplate labHead = findOrCreateLabHead(labInfo.labHead);
        lab.setHead(labHead);
        lab.setName(labInfo.labName);
        lab.setInstitutionUrl(labInfo.institutionUrl);
        return saveLab(lab);
    }

    private LAB saveLab(LAB lab) {
        lab.setLastModification(current.get());
        return labRepositoryTemplate.save(lab);
    }

    private UserTemplate findOrCreateLabHead(PersonInfo labHead) {
        final UserTemplate existingLabHead = userRepositoryTemplate.findByEmail(labHead.email);
        if (existingLabHead != null) {
            return existingLabHead;
        }

        //todo[tymchenko]: send the Credentials email to this user.
        final PersonInfo personInfo = new PersonInfo(labHead.firstName, labHead.lastName, labHead.email);
        final long labHeadUserId = userManagement.createUserWithGeneratedPassword(personInfo, labHead.email);
        return checkNotNull(userRepositoryTemplate.findOne(labHeadUserId));

    }

    private LAB findLab(long labId) {
        return checkNotNull(labRepositoryTemplate.findOne(labId));
    }
}
