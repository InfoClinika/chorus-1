package com.infoclinika.mssharing.platform.model.impl;

import com.google.common.collect.Sets;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.PredefinedDataCreatorTemplate;
import com.infoclinika.mssharing.platform.model.common.items.AdditionalExtensionImportance;
import com.infoclinika.mssharing.platform.model.common.items.FileExtensionItem;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.platform.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Use to create data for initial application startup
 *
 * @author Herman Zamula
 */
@SuppressWarnings("unused")
@Component
@Transactional
public class DefaultPredefinedDataCreator implements PredefinedDataCreatorTemplate {

    @Inject
    protected EntityFactories entityFactories;
    @Inject
    protected UserRepositoryTemplate<UserTemplate> userRepository;
    @Inject
    protected UserManagementTemplate userManagement;
    @Inject
    protected GroupRepositoryTemplate<GroupTemplate> groupRepository;
    @Inject
    protected SpeciesRepositoryTemplate<Species> speciesRepository;
    @Inject
    protected ExperimentTypeRepositoryTemplate<ExperimentType> experimentTypeRepository;
    @Inject
    protected VendorRepositoryTemplate<Vendor> vendorRepository;
    @Inject
    protected InstrumentTypeRepositoryTemplate<InstrumentType> instrumentTypeRepository;
    @Inject
    protected InstrumentStudyTypeRepositoryTemplate<InstrumentStudyType> instrumentStudyTypeRepository;
    @Inject
    protected InstrumentModelRepositoryTemplate<InstrumentModel> instrumentModelRepository;

    @Override
    public long admin(String firstName, String lastName, String email, String password) {
        UserTemplate user = userRepository.findByEmail(email);
        if (user != null) {
            return user.getId();
        }
        long adminId = userManagement.createPersonAndApproveMembership(new UserManagementTemplate.PersonInfo(firstName, lastName, email), password, Sets.newHashSet(), null);
        userManagement.verifyEmail(adminId);
        final UserTemplate entity = userRepository.findOne(adminId);
        entity.setAdmin(true);
        userRepository.save(entity);

        return adminId;
    }

    @Override
    public void allUsersGroup() {
        if (groupRepository.findAllUsersGroup() != null) {
            return;
        }
        final GroupTemplate group = entityFactories.group.get();
        group.setName("All");
        group.setLastModification(new Date());
        group.setIncludesAllUsers(true);
        groupRepository.save(group);

    }

    @Override
    public void species(String... names) {
        for (String name : names) {
            final Species species = entityFactories.species.get();
            species.setName(name);
            speciesRepository.save(species);
        }
    }


    @Override
    public void experimentType(String name, boolean allowed2DLC, boolean allowLabels) {
        final ExperimentType entity = entityFactories.experimentType.get();
        entity.setName(name);
        entity.labelsAllowed = allowLabels;
        entity.allowed2dLC = allowed2DLC;
        experimentTypeRepository.save(entity);
    }

    @Override
    public long instrumentModel(final String vendor, final String type, final String studyType, String name, final boolean isFolderArchiveSupport, final boolean isMultipleFiles, final Set<FileExtensionItem> extensions) {

        final Vendor vendorEntity = fromNullable(vendorRepository.findByName(vendor)).or(() -> {
            final Vendor vendorEntity1 = entityFactories.vendor.get();
            vendorEntity1.setName(vendor);
            return vendorEntity1;
        });

        final InstrumentType typeEntity = fromNullable(instrumentTypeRepository.findByName(type)).or(() -> {
            final InstrumentType entity = entityFactories.instrumentType.get();
            entity.setName(type);
            return entity;
        });

        final InstrumentStudyType studyTypeEntity = fromNullable(instrumentStudyTypeRepository.findByName(studyType)).or(() -> {
            final InstrumentStudyType entity = entityFactories.instrumentStudyType.get();
            entity.setName(studyType);
            return entity;
        });

        final InstrumentModel instrumentModel = entityFactories.instrumentModel.get();
        instrumentModel.setName(name);
        instrumentModel.setVendor(vendorEntity);
        instrumentModel.setType(typeEntity);
        instrumentModel.setStudyType(studyTypeEntity);
        instrumentModel.setFolderArchiveSupport(isFolderArchiveSupport);
        instrumentModel.setAdditionalFiles(isMultipleFiles);

        Set<VendorExtension> fileExtensions = newHashSet();
        for (FileExtensionItem extension : extensions) {
            final Map<String, VendorExtension.Importance> transformed = getStringImportanceMap(extension);
            fileExtensions.add(new VendorExtension(extension.name, extension.zip, transformed));
        }

        instrumentModel.getExtensions().addAll(fileExtensions);

        return instrumentModelRepository.save(instrumentModel).getId();
    }

    private Map<String, VendorExtension.Importance> getStringImportanceMap(FileExtensionItem extension) {
        final Map<String, AdditionalExtensionImportance> additionalExtensions = extension.additionalExtensions;
        final Map<String, VendorExtension.Importance> transformed = newHashMap();
        for (String additional : additionalExtensions.keySet()) {
            transformed.put(additional, getFileExtensionImportance(additionalExtensions.get(additional)));
        }
        return transformed;
    }

    private VendorExtension.Importance getFileExtensionImportance(AdditionalExtensionImportance type) {
        switch (type) {
            case REQUIRED:
                return VendorExtension.Importance.REQUIRED;
            case NOT_REQUIRED:
                return VendorExtension.Importance.NOT_REQUIRED;
            default:
                throw new AssertionError("Unknown additional file extension requirement type: " + type);
        }
    }
}
