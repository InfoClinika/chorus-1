/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.helper;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.platform.fileserver.model.StoredFile;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.write.*;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate.PotentialOperator;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;

import javax.swing.*;
import java.io.InputStream;
import java.util.*;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Maps.newHashMap;
import static com.infoclinika.mssharing.model.helper.Data.*;
import static org.mockito.Mockito.mock;

/**
 * Bob is the main actor. He works in Lab3. His head is Poll.
 * Lab3 suited in Harvard. Bob works on BigApple project.
 * <p/>
 * Kate works in Lab2. Her head isn't system user.
 * <p/>
 * Mark is an admin.
 *
 * @author Stanislav Kurilin
 */

public class UseCase {
    private ImmutableSet<Long> emptyGroups = of();
    private ImmutableSet<Long> emptyCollaborators = of();

    private final WriteServices ws;
    private final Repositories repositories;

    private final long adminId;
    private final InstrumentCreationHelperTemplate<PotentialOperator> instrumentCreationHelper;
    private final ExperimentCreationHelper experimentCreationHelper;
    private final DashboardReader dashboardReader;

    private Optional<Long> labRequest2 = Optional.absent();
    private Optional<Long> labRequest3 = Optional.absent();
    private Optional<Long> labRequest4 = Optional.absent();

    //absent if wasn't created yet
    private Optional<Long> lab4 = Optional.absent();
    private Optional<Long> lab3 = Optional.absent();
    private Optional<Long> lab2 = Optional.absent();

    private Optional<Long> bob = Optional.absent();
    private Optional<Long> kate = Optional.absent();
    private Optional<Long> paul = Optional.absent(); //paul is the head of lab 3 and lab 2
    private Optional<Long> john = Optional.absent(); //john is the user with no lab
    private Optional<Long> jake = Optional.absent(); // jake is a user with expired email verification and password reset links


    private Optional<Long> lab3Instrument = Optional.absent();


    private final List<Long> EMPTY_PERIPHERALS = ImmutableList.of();

    private final UserManagement.LabMembershipRequestActions APPROVE = UserManagementTemplate.LabMembershipRequestActions.APPROVE;
    private final UserManagement.LabMembershipRequestActions REFUSE = UserManagementTemplate.LabMembershipRequestActions.REFUSE;

    public UseCase(WriteServices ws, long adminId, InstrumentCreationHelperTemplate instrumentCreationHelper,
                   ExperimentCreationHelper experimentCreationHelper, DashboardReader dashboardReader, Repositories repositories) {
        this.ws = ws;
        this.adminId = adminId;
        this.instrumentCreationHelper = instrumentCreationHelper;
        this.experimentCreationHelper = experimentCreationHelper;
        this.dashboardReader = dashboardReader;
        this.repositories = repositories;
    }


    public long requestLab2creation() {
        if (!lab2.isPresent())
            labRequest2 = Optional.of(ws.labManagement.requestLabCreation(new LabManagementTemplate.LabInfoTemplate(Data.HARVARD_URL, Data.L_KATE_INFO, "lab2"), "me@c.com"));
        return labRequest2.get();
    }

    public long requestLab3creation() {
        if (!labRequest3.isPresent()) {
            labRequest3 = Optional.of(ws.labManagement.requestLabCreation(Data.LAB_3_DATA, "a.a@com"));
        }
        return labRequest3.get();
    }

    public long requestLab4creation() {
        if (!labRequest4.isPresent()) {
            labRequest4 = Optional.of(ws.labManagement.requestLabCreation(Data.LAB_4_DATA, "a.a@com"));
        }
        return labRequest4.get();
    }

    public void requestJohnLab3Membership() {
        ws.userManagement.updatePerson(john.get(), new UserManagement.PersonInfo("Joe", "J", "jjj@j.com"), ImmutableSet.of(lab3.get()));
    }

    private long activateLab(long actor, long request) {
        return ws.labManagement.confirmLabCreation(actor, request);
    }

    /**
     * By create I mean request and activate
     */
    public void createLab2() {
        if (!lab2.isPresent()) {
            final long request = requestLab2creation();
            lab2 = Optional.of(activateLab(adminId, request));
        }
    }

    public long createLab3() {
        if (lab3.isPresent()) {
            return lab3.get();
        }
        long request = requestLab3creation();
        lab3 = Optional.of(activateLab(adminId, request));
        return lab3.get();
    }

    public long createLab4() {
        if (lab4.isPresent()) {
            return lab4.get();
        }
        long request = requestLab4creation();
        lab4 = Optional.of(activateLab(adminId, request));
        return lab4.get();
    }

    public final long createLab3AndBob() {
        createLab3();
        return tryBobCreation();
    }

    public final long tryBobCreation() {
        checkState(!bob.isPresent());
        bob = Optional.of(ws.userManagement.createPersonAndApproveMembership(BOB_INFO, BOBS_PASS, lab3.get(), null));
        return bob.get();
    }

    public final long createPaul() {
        createLab3();
        if (paul.isPresent()) {
            return paul.get();
        }
        paul = Optional.of(ws.userManagement.createPersonAndApproveMembership(PAUL_INFO, "1231", lab3.get(), null));
        return paul.get();
    }

    public final long createLab3AndGetPaul() {
        return createPaul();
    }

    public final long createJoe() {
        return ws.userManagement.createPersonAndApproveMembership(new UserManagement.PersonInfo("Joe", "J", "jjj@j.com"), "1231", lab3.get(), null);
    }

    public final long createKateAndLab2() {
        createLab2();
        checkState(!kate.isPresent());
        kate = Optional.of(ws.userManagement.createPersonAndApproveMembership(KATE_INFO, "1231", lab2.get(), null));
        return kate.get();
    }

    public final long kateLab() {
        return lab2.get();
    }

    public final void addKateToLab3() {
        createLab3();
        checkState(kate.isPresent());
        ws.userManagement.updatePersonAndApproveMembership(kate.get(), KATE_INFO, ImmutableSet.of(lab2.get(), lab3.get()));
    }

    public final void addKateToLab4() {
        createLab4();
        checkState(kate.isPresent());
        ws.userManagement.updatePersonAndApproveMembership(kate.get(), KATE_INFO, ImmutableSet.of(lab2.get(), lab3.get(), lab4.get()));
    }

    public Optional<Long> createInstrumentAndApproveIfNeeded(long user, long lab) {
        return createInstrumentAndApproveIfNeeded(user, lab, anyInstrumentModel());
    }

    public Optional<Long> createInstrumentAndApproveIfNeeded(long user, long lab, long model) {
        return createInstrumentAndApproveIfNeeded(user, lab, model, new InstrumentDetails(anyStr(), anyStr(), anyStr(), anyStr(), Collections.<LockMzItem>emptyList()));
    }

    private Optional<Long> createInstrumentAndApproveIfNeeded(long user,
                                                             long lab,
                                                             long model,
                                                             InstrumentDetails details){

        final boolean labHead = ws.labManagement.isLabHead(user, lab);
        if(labHead){
            return Optional.of(ws.instrumentManagement.createInstrument(user, lab, model, details));
        } else {
            final Optional<Long> instrumentRequest =
                    ws.instrumentManagement.newInstrumentRequest(user, lab, model, details, new ArrayList<Long>());
            final LabReaderTemplate.LabLineTemplate labLine = dashboardReader.readLab(lab);
            return Optional.of(ws.instrumentManagement.approveInstrumentCreation(labLine.labHead, instrumentRequest.get()));
        }

    }

    public String anyStr() {
        return UUID.randomUUID().toString();
    }

    public final long createProject(long bobsId, Long lab) {
        return ws.studyManagement.createProject(bobsId, new ProjectInfo(anyStr(), "DNA", "Some proj", lab));
    }

    public final long createProject(long userId) {
        return createProject(userId, null);
    }

    public void onLab3InstrumentCreated(long instrumentId) {
        lab3Instrument = Optional.of(instrumentId);
    }

    public Optional<Long> getLab3Instrument() {
        return lab3Instrument;
    }

    public long saveFile(long userId) {
        if (!lab3Instrument.isPresent())
            lab3Instrument = createInstrumentAndApproveIfNeeded(userId, lab3.get(), anyThermoInstrumentModel());
        return saveFile(userId, lab3Instrument.get());
    }

    public long saveFile(long userId, long instrument) {
        final long file = ws.instrumentManagement.createFile(userId, instrument, new FileMetaDataInfo(UUID.randomUUID().toString(), 0, "", null, unspecified(), false));
        updateFileContent(userId, file);
        return file;
    }

    public long saveFileWithSize(long userId, long instrument, long size) {
        final long file = ws.instrumentManagement.createFile(userId, instrument, new FileMetaDataInfo(UUID.randomUUID().toString(), size, "", null, unspecified(), false));
        updateFileContent(userId, file);
        return file;
    }

    public long saveFileWithName(long userId, long instrument, String name) {
        final long file = ws.instrumentManagement.createFile(userId, instrument, new FileMetaDataInfo(name, 0, "", null, unspecified(), false));
        updateFileContent(userId, file);
        return file;
    }

    public long saveFileAvailableForTranslation(long user) {
        ImmutableSet<DictionaryItem> models = instrumentCreationHelper.models(thermoVendor());
        return saveFile(user, createInstrumentAndApproveIfNeeded(user, getLab3(), models.iterator().next().id).get());
    }

    protected long thermoVendor() {
        for (DictionaryItem item : instrumentCreationHelper.vendors()) {
            if (item.name.equals(Data.vendor1)) {
                return item.id;
            }
        }
        return instrumentCreationHelper.vendors().first().id;
    }

    public long updateFileContent(long actor, long file) {
        ws.instrumentManagement.setContent(actor, file, new StoredFile(mock(InputStream.class)));
        ws.fileMetaInfoHelper.updateFileMeta(file, new FileMetaInfoHelper.MetaInfo());
        return file;
    }

    public void sharing(long actor, long project, Set<Long> collaborator, Set<Long> groups) {
        ws.sharingManagement.updateSharingPolicy(actor, project, addAccessLevel(collaborator), addAccessLevel(groups), false);
    }

    private Map<Long, SharingManagementTemplate.Access> addAccessLevel(Set<Long> collaborator) {
        return addAccessLevel(collaborator, SharingManagementTemplate.Access.WRITE);
    }

    private Map<Long, SharingManagementTemplate.Access> addAccessLevel(Set<Long> collaborator, SharingManagementTemplate.Access access) {
        Map<Long, SharingManagementTemplate.Access> result = newHashMap();
        for (Long id : collaborator) {
            result.put(id, access);
        }
        return result;
    }

    public void sharingWithCollaborator(long actor, long project, Long collaborator) {
        sharing(actor, project, ImmutableSet.of(collaborator), emptyGroups);
    }

    public void shareProjectThrowGroup(long owner, long collaborator, long project) {
        shareProjectThrowGroup(owner, collaborator, project, SharingManagementTemplate.Access.WRITE);
    }

    public void shareProjectThrowGroup(long owner, long collaborator, long project, SharingManagementTemplate.Access access) {
        final long paul = createPaul();
        final long group = ws.sharingManagement.createGroup(owner, "kates lab", ImmutableSet.of(paul));
        ws.sharingManagement.setCollaborators(owner, group, ImmutableSet.of(collaborator), false);

        ws.sharingManagement.updateSharingPolicy(owner, project, addAccessLevel(emptyCollaborators), addAccessLevel(ImmutableSet.of(group), access), false);
    }

    public void shareProjectToKateInGroup(long owner, long project) {
        final long paul = createPaul();
        final long group = ws.sharingManagement.createGroup(owner, "kates lab", ImmutableSet.of(paul));
        ws.sharingManagement.setCollaborators(owner, group, ImmutableSet.of(createKateAndLab2()), false);
        ws.sharingManagement.updateSharingPolicy(owner, project, addAccessLevel(emptyCollaborators), addAccessLevel(ImmutableSet.of(group)), false);
    }

    //TODO: [stanislav.kurilin] shouldn't be placed here
    private long anyInstrumentModel() {
        //TODO: [stanislav.kurilin] return random instead of first
        return instrumentCreationHelper.models(anyVendor()).first().id;
    }

    private long anyThermoInstrumentModel() {
        return instrumentCreationHelper
                .models(thermoVendor())
                .stream()
                .findAny()
                .orElseThrow(() -> new RuntimeException("Instrument model is not found."))
                .id;
    }

    private long anyVendor() {
        return instrumentCreationHelper.vendors().first().id;
    }

    private long anyVendorWithFolderArchiveUploadSupport() {
        return instrumentCreationHelper.vendorsWithFolderArchiveUploadSupport().first().id;
    }

    public long anySpecie() {
        return experimentCreationHelper.species().iterator().next().id;
    }

    public long unspecified() {
        return find(experimentCreationHelper.species(), DictionaryItem.UNSPECIFIED).id;
    }

    public Long getLab4() {
        return lab4.get();
    }

    public Long getLab3() {
        return lab3.get();
    }

    public Long getLab2() {
        return lab2.get();
    }

    public UserManagement.LabMembershipRequestActions getApprove() {
        return APPROVE;
    }

    public UserManagement.LabMembershipRequestActions getRefuse() {
        return REFUSE;
    }

    public long createJohnWithoutLab() {
        if (john.isPresent()) {
            return john.get();
        }
        john = Optional.of(ws.userManagement.createPersonAndApproveMembership(JOHN_INFO, "1231", ImmutableSet.<Long>of(), null));
        return john.get();
    }

    public long getInstrumentModelWhichSupportArchiveUpload() {
        return instrumentCreationHelper.models(anyVendorWithFolderArchiveUploadSupport()).first().id;
    }


    public long getJake() {
        if (jake.isPresent()) {
            return jake.get();
        }
        final long jakeId = ws.userManagement.createPersonAndApproveMembership(JAKE_INFO, "1231", ImmutableSet.<Long>of(), null);
        final User jake = repositories.userRepository.findOne(jakeId);
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH, -7);
        final Date lastWeek = now.getTime();
        jake.setEmailVerificationSentOnDate(lastWeek);
        jake.setPasswordResetSentOnDate(lastWeek);
        repositories.userRepository.save(jake);

        this.jake = Optional.of(jakeId);

        return this.jake.get();
    }
}
