/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.model.helper;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.infoclinika.mssharing.model.GlacierDownloadListeners;
import com.infoclinika.mssharing.model.Notifier;
import com.infoclinika.mssharing.model.PredefinedDataCreator;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.internal.entity.Lab;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.mailing.FailedMailNotificationReceiver;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.write.ExperimentLabelManagement;
import com.infoclinika.mssharing.model.internal.write.ExperimentLabelManagement.ExperimentTypeInfo;
import com.infoclinika.mssharing.model.read.*;
import com.infoclinika.mssharing.model.read.dto.details.ExperimentItem;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.model.write.AnalysisBounds;
import com.infoclinika.mssharing.model.write.AttachmentManagement;
import com.infoclinika.mssharing.model.write.ExperimentInfo;
import com.infoclinika.mssharing.model.write.FeaturesManagement;
import com.infoclinika.mssharing.model.write.FileMovingManager;
import com.infoclinika.mssharing.model.write.FileOperationsManager;
import com.infoclinika.mssharing.model.write.InstrumentDetails;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.model.write.LabHeadManagement;
import com.infoclinika.mssharing.model.write.LabManagement;
import com.infoclinika.mssharing.model.write.ProjectInfo;
import com.infoclinika.mssharing.model.write.ProteinDatabaseManagement;
import com.infoclinika.mssharing.model.write.SharingManagement;
import com.infoclinika.mssharing.model.write.StudyManagement;
import com.infoclinika.mssharing.model.write.UserManagement;
import com.infoclinika.mssharing.model.write.UserPreferencesManagement;
import com.infoclinika.mssharing.model.write.billing.BillingManagement;
import com.infoclinika.mssharing.platform.entity.UserLabMembershipRequestTemplate;
import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.RequestsTemplate;
import com.infoclinika.mssharing.platform.model.common.items.AdditionalExtensionImportance;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.FileExtensionItem;
import com.infoclinika.mssharing.platform.model.helper.ExperimentCreationHelperTemplate.ExperimentTypeItem;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate.PotentialOperator;
import com.infoclinika.mssharing.platform.model.helper.RegistrationHelperTemplate;
import com.infoclinika.mssharing.platform.model.helper.SharingProjectHelperTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.FileItemTemplate;
import com.infoclinika.mssharing.platform.model.read.FileReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.read.GroupsReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.Restriction;
import com.infoclinika.mssharing.platform.model.write.ProjectManagementTemplate;
import com.infoclinika.mssharing.platform.repository.UserLabMembershipRequestRepositoryTemplate;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingChargeType;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.mockito.ArgumentMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.model.helper.Data.AB_SCIEX;
import static com.infoclinika.mssharing.model.helper.Data.AB_SCIEX_EXTENSIONS;
import static com.infoclinika.mssharing.model.helper.Data.AB_SCIEX_INSTRUMENT_MODEL;
import static com.infoclinika.mssharing.model.helper.Data.BRUKER;
import static com.infoclinika.mssharing.model.helper.Data.BRUKER_EXTENSIONS;
import static com.infoclinika.mssharing.model.helper.Data.BRUKER_INSTRUMENT_MODEL;
import static com.infoclinika.mssharing.model.helper.Data.NO_FACTORS;
import static com.infoclinika.mssharing.model.helper.Data.instrumentModel111;
import static com.infoclinika.mssharing.model.helper.Data.instrumentModel121;
import static com.infoclinika.mssharing.model.helper.Data.instrumentModel122;
import static com.infoclinika.mssharing.model.helper.Data.instrumentModel211;
import static com.infoclinika.mssharing.model.helper.Data.instrumentModel212;
import static com.infoclinika.mssharing.model.helper.Data.instrumentType11;
import static com.infoclinika.mssharing.model.helper.Data.instrumentType12;
import static com.infoclinika.mssharing.model.helper.Data.instrumentType21;
import static com.infoclinika.mssharing.model.helper.Data.msStudyType;
import static com.infoclinika.mssharing.model.helper.Data.vendor1;
import static com.infoclinika.mssharing.model.helper.Data.vendor2;
import static com.infoclinika.mssharing.model.helper.Data.vendor3;
import static com.infoclinika.mssharing.model.helper.ExperimentSampleTypeItem.LIGHT;
import static com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState.DISABLED;
import static com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState.ENABLED;
import static com.infoclinika.mssharing.model.internal.entity.Feature.FeatureState.ENABLED_PER_LAB;
import static com.infoclinika.mssharing.model.write.ExperimentCategory.PROTEOMICS;
import static java.util.Collections.emptyList;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.reset;

/**
 * Provides methods for some common scenarios.
 *
 * @author Stanislav Kurilin
 */
@ContextConfiguration(classes = SpringConfig.class)
public class AbstractTest extends AbstractTestNGSpringContextTests {

    public static final List<LockMzItem> NO_LOCK_MASSES = emptyList();
    public static final List<Long> NO_OPERATORS = emptyList();
    public static final int DAYS_IN_MONTH = 30;
    public static final int ARCHIVE_PRICE = 450;
    public static final int ANALYSE_PRICE = 450;
    public static final int TRANSLATION_PRICE = 450;
    public static final int DOWNLOAD_PRICE = 0;
    public static final int PROTEIN_SEARCH_PRICE = 1000;
    public static final int PROCESSING_FEATURE_PRICE = 20000;
    public static final int STORAGE_VOLUMES_FEATURE_PRICE = 4000;
    public static final int ARCHIVE_STORAGE_VOLUMES_FEATURE_PRICE = 4000;
    public final List<LockMzItem> lockMasses = emptyList();

    public long createInstrumentAndApproveIfNeeded(long user, long lab) {
        return createInstrumentAndApproveIfNeeded(user, lab, anyInstrumentModelByVendor(anyVendor()), instrumentDetails()).get();
    }

    public Optional<Long> createInstrumentAndApproveIfNeeded(long user,
                                                             long lab,
                                                             long model,
                                                             InstrumentDetails details) {

        final boolean labHead = labManagement.isLabHead(user, lab);
        if (labHead) {
            return Optional.of(instrumentManagement.createInstrument(user, lab, model, details));
        } else {
            final Optional<Long> instrumentRequest =
                    instrumentManagement.newInstrumentRequest(user, lab, model, details, new ArrayList<Long>());
            final LabReaderTemplate.LabLineTemplate labLine = dashboardReader.readLab(lab);
            return Optional.of(instrumentManagement.approveInstrumentCreation(labLine.labHead, instrumentRequest.get()));
        }

    }

    public Optional<Long> createInstrumentCreationRequest(long user, long lab) {
        return instrumentManagement.newInstrumentRequest(
                user,
                lab,
                anyInstrumentModelByVendor(anyVendor()),
                instrumentDetails(),
                NO_OPERATORS
        );
    }

    public void checkHasAccessToFile(long user, long lab, final long file) {
        if (!Iterables.any(fileReader.readFiles(user, Filter.ALL), new Predicate<FileLine>() {
            @Override
            public boolean apply(FileLine input) {
                return input.id == file;
            }
        })) throw new AccessDenied("asserting");
        detailsReader.readFile(user, file);
        reuseFile(user, lab, file);
    }

    public void reuseFile(long actor, long lab, long file) {
        final long privateProject = uc.createProject(actor, lab);
        createInstrumentAndExperiment(actor, lab, privateProject, NO_FACTORS, noFactoredFile(file));
    }

    protected long anyInstrumentModel() {
        return randElement(instrumentCreationHelper.models(anyVendor())).id;
    }

    protected long anyThermoInstrumentModel() {
        return randElement(instrumentCreationHelper.models(thermoVendor())).id;
    }

    protected long anyVendor() {
        return instrumentCreationHelper.vendors().first().id;
    }

    protected long thermoVendor() {
        for (DictionaryItem item : instrumentCreationHelper.vendors()) {
            if (item.name.equals(Data.vendor1)) {
                return item.id;
            }
        }
        return instrumentCreationHelper.vendors().first().id;
    }

    protected long abSciexInstrumentModel() {
        Long model = getInstrumentModelByNames(Data.AB_SCIEX, Data.AB_SCIEX_INSTRUMENT_MODEL);
        if (model == null) {
            //initialize AB SCIEX instrument model
            return predefinedDataCreator.instrumentModel(AB_SCIEX, instrumentType11, msStudyType, AB_SCIEX_INSTRUMENT_MODEL, false, true, AB_SCIEX_EXTENSIONS);
        }
        return model;
    }

    protected long brukerInstrumentModel() {
        Long model = getInstrumentModelByNames(Data.BRUKER, Data.BRUKER_INSTRUMENT_MODEL);
        if (model == null) {
            //initialize Bruker instrument model
            return predefinedDataCreator.instrumentModel(BRUKER, instrumentType11, msStudyType, BRUKER_INSTRUMENT_MODEL, true, false, BRUKER_EXTENSIONS);
        }
        return model;
    }

    protected Long getInstrumentModelByNames(String vendorName, String instrumentName) {
        Long vendor = null;
        Long model = null;
        for (DictionaryItem item : instrumentCreationHelper.vendors()) {
            if (item.name.equals(vendorName)) {
                vendor = item.id;
            }
        }
        if (vendor == null) {
            return null;
        }
        for (DictionaryItem item : instrumentCreationHelper.models(vendor)) {
            if (item.name.equals(instrumentName)) {
                model = item.id;
            }
        }
        return model;
    }

    protected InstrumentDetails instrumentDetails() {
        return new InstrumentDetails(generateString(), generateString(), generateString(), generateString(), lockMasses);
    }

    protected long anyInstrumentModelByVendor(long vendor) {
        return randElement(instrumentCreationHelper.models(vendor)).id;
    }

    protected long anyAvailableInstrumentModel(long user) {
        return randElement(experimentCreationHelper.availableInstrumentModels(user, null)).id;
    }

    protected long anotherInstrumentModel(long vendor, final long model) {
        return Iterables.find(instrumentCreationHelper.models(vendor), new Predicate<DictionaryItem>() {
            @Override
            public boolean apply(DictionaryItem input) {
                return input.id != model;
            }
        }).id;
    }

    protected <T> T randElement(Collection<T> all) {
        final Random random = new Random();
        return Iterables.get(all, random.nextInt(all.size()));
    }

    protected String generateString() {
        return UUID.randomUUID().toString();
    }

    protected ImmutableList<com.infoclinika.mssharing.model.write.FileItem> noFactoredFile(long file) {
        return of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0, preparedSample(file)));
    }

    protected ImmutableList<com.infoclinika.mssharing.model.write.FileItem> noFactoredFiles(List<Long> files) {
        return from(transform(files, new Function<Long, com.infoclinika.mssharing.model.write.FileItem>() {
            @Override
            public com.infoclinika.mssharing.model.write.FileItem apply(Long fileId) {
                return new com.infoclinika.mssharing.model.write.FileItem(fileId, false, 0, preparedSample(fileId));
            }
        })).toList();
    }

    protected ImmutableList<com.infoclinika.mssharing.model.write.FileItem> anyFile(long user) {
        final long file = uc.saveFile(user);
        return of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0, preparedSample(file)));
    }


    protected long admin() {
        return adminId;
    }

    protected long otherAdmin() {
        return secondAdminId;
    }

    protected Notifier notificator() {
        return super.applicationContext.getBean(Notifier.class);
    }

    protected StorageService storageService() {
        return super.applicationContext.getBean(StorageService.class);
    }

    public long createInstrumentAndExperiment(long user, long lab, long project, List<ExperimentManagementTemplate.MetaFactorTemplate> noFactors, List<com.infoclinika.mssharing.model.write.FileItem> fileItems) {
        final Optional<Long> lab3Instrument = createInstrumentAndApproveIfNeeded(user, lab, anyInstrumentModelByVendor(anyVendor()), instrumentDetails());
        //todo[tymchenko]: refactor later
        if (lab == uc.getLab3()) {
            uc.onLab3InstrumentCreated(lab3Instrument.get());
        }
        final ExperimentInfo.Builder builder = experimentInfo().project(project).lab(lab).billLab(lab)
                .is2dLc(false).restriction(restriction(user, fileItems.get(0).id)).factors(noFactors).files(fileItems)
                .bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES).experimentLabels(new ExperimentLabelsInfo()).sampleTypesCount(1);
        return studyManagement.createExperiment(user, builder.build());
    }

    public long createInstrumentAndExperimentWithOneFile(long user, long lab, long project) {
        final Optional<Long> lab3Instrument = uc.getLab3Instrument().or(createInstrumentAndApproveIfNeeded(user, lab, anyInstrumentModelByVendor(anyVendor()), instrumentDetails()));
        long file;
        //todo[tymchenko]: refactor later
        if (lab == uc.getLab3()) {
            uc.onLab3InstrumentCreated(lab3Instrument.get());
            file = uc.saveFile(user, lab3Instrument.get());
        } else {
            final Optional<Long> labInstrument = createInstrumentAndApproveIfNeeded(user, lab, anyInstrumentModelByVendor(anyVendor()), instrumentDetails());
            file = uc.saveFile(user, labInstrument.get());
        }
        final ExperimentInfo.Builder builder = experimentInfo()
                .project(project).billLab(lab).lab(lab).is2dLc(false)
                .restriction(restriction(user, file)).factors(NO_FACTORS)
                .experimentLabels(new ExperimentLabelsInfo())
                .files(noFactoredFile(file))
                .bounds(new AnalysisBounds())
                .lockMasses(NO_LOCK_MASSES)
                .sampleTypesCount(1);

        return studyManagement.createExperiment(user, builder.build());
    }

    public long createExperiment(long user, long project, Long lab, List<com.infoclinika.mssharing.model.write.FileItem> fileItems) {

        final ExperimentInfo.Builder builder = experimentInfo()
                .project(project)
                .lab(lab)
                .billLab(lab)
                .is2dLc(false)
                .restriction(restriction(user))
                .factors(NO_FACTORS).files(fileItems)
                .experimentLabels(new ExperimentLabelsInfo())
                .bounds(new AnalysisBounds())
                .lockMasses(NO_LOCK_MASSES)
                .sampleTypesCount(1);

        return studyManagement.createExperiment(user, builder.build());
    }

    public long createExperiment(long user, long project, long model, Long lab, List<com.infoclinika.mssharing.model.write.FileItem> fileItems) {

        final ExperimentInfo.Builder builder = experimentInfo()
                .project(project)
                .lab(lab)
                .billLab(lab)
                .is2dLc(false)
                .restriction(restrictionFromModel(model))
                .factors(NO_FACTORS).files(fileItems)
                .bounds(new AnalysisBounds())
                .experimentLabels(new ExperimentLabelsInfo())
                .lockMasses(NO_LOCK_MASSES)
                .sampleTypesCount(1);

        return studyManagement.createExperiment(user, builder.build());
    }

    protected Restriction restrictionFromModel(long model) {
        return new Restriction(model, Optional.absent());
    }

    protected Restriction restrictionFromModelAndInstrument(long model, long instrument) {
        return new Restriction(model, Optional.of(instrument));
    }

    protected Restriction restrictionForExperiment(ExperimentItem experimentItem) {
        return new Restriction(experimentItem.instrumentModel, experimentItem.instrument);
    }

    public long createExperiment(long user, long project) {
        return createExperiment(user, project, uc.getLab3(), anyExperimentType(), new ExperimentLabelsInfo());
    }

    public long createExperimentWithLabels(long user, long project, long experimentType, ExperimentLabelsInfo experimentLabels) {
        return createExperiment(user, project, uc.getLab3(), experimentType, experimentLabels);
    }

    public long createExperiment(long user, long project, Long lab) {
        return createExperiment(user, project, lab, anyExperimentType(), new ExperimentLabelsInfo());
    }

    public long createExperiment(long user, long project, Long lab, long experimentType, ExperimentLabelsInfo experimentLabels) {

        final long file = uc.saveFile(user);
        final ExperimentInfo.Builder builder = experimentInfo()
                .project(project)
                .lab(lab)
                .billLab(lab)
                .is2dLc(false)
                .restriction(restriction(user, file))
                .factors(NO_FACTORS)
                .experimentType(experimentType)
                .files(noFactoredFile(file))
                .bounds(new AnalysisBounds())
                .experimentLabels(experimentLabels)
                .sampleTypesCount(1)
                .lockMasses(NO_LOCK_MASSES);

        return studyManagement.createExperiment(user, builder.build());
    }

    public long createExperiment(long user, long project, long file, long lab) {
        final ExperimentInfo.Builder builder = experimentInfo().project(project).lab(lab).billLab(lab).is2dLc(false).sampleTypesCount(1)
                .experimentLabels(new ExperimentLabelsInfo()).restriction(restriction(user, file)).factors(NO_FACTORS).files(noFactoredFile(file)).bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES);
        return studyManagement.createExperiment(user, builder.build());
    }

    public long createExperimentWithName(long user, long project, String name) {
        final long file = uc.saveFile(user);
        final Long lab = uc.getLab3();
        final ExperimentInfo.Builder builder = experimentInfo(name).project(project).lab(lab).billLab(lab).is2dLc(false).experimentLabels(new ExperimentLabelsInfo())
                .restriction(restriction(user, file)).factors(NO_FACTORS).files(noFactoredFile(file)).bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES)
                .sampleTypesCount(1);
        return studyManagement.createExperiment(user, builder.build());
    }

    protected Restriction restriction(long user) {
        return restrictionFromModel(anyAvailableInstrumentModel(user));
    }

    protected Restriction restriction(long user, long file) {
        final long model = instrumentModel(user, file);
        return restrictionFromModel(model);
    }

    protected long instrumentModel(long user, long file) {
        final FileItem fileItem = detailsReader.readFile(user, file);
        return detailsReader.readInstrument(user, fileItem.instrumentId).modelId;
    }

    public ExperimentInfo.Builder experimentInfo() {
        return experimentInfo(unspecified());
    }

    public ExperimentInfo.Builder experimentInfo(String name) {
        return experimentInfo(name, unspecified());
    }

    public ExperimentInfo.Builder experimentInfo(long species) {
        return experimentInfo(generateString(), species);
    }

    public ExperimentInfo.Builder experimentInfo(String name, long species) {
        return new ExperimentInfo.Builder().name(name).description(generateString()).experimentType(anyExperimentType()).specie(species);
    }

    protected long anySpecies() {
        Collection<DictionaryItem> filtered = Collections2.filter(experimentCreationHelper.species(), not(DictionaryItem.UNSPECIFIED));
        return randElement(filtered).id;
    }

    protected long unspecified() {
        return Iterables.find(experimentCreationHelper.species(), DictionaryItem.UNSPECIFIED).id;
    }

    protected long anyExperimentType() {
        return experimentCreationHelper.experimentTypes().first().id;
    }

    protected long experimentTypeLabeled() {
        for (ExperimentTypeItem exType : experimentCreationHelper.experimentTypes()) {
            if (exType.allowLabels) {
                return exType.id;
            }
        }
        throw new IllegalStateException("There are no experiment types which allows labels");
    }

    protected long experimentTypeNotLabeled() {
        for (ExperimentTypeItem exType : experimentCreationHelper.experimentTypes()) {
            if (!exType.allowLabels) {
                return exType.id;
            }
        }
        throw new IllegalStateException("There are no experiment types which doesn't allow labels");
    }

    protected long instrumentFromExperimentFile(long actor, long experiment) {
        return Iterables.getFirst(fileReader.readFilesByExperiment(actor, experiment), null).instrumentId;
    }

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected UseCase uc;
    @Inject
    private WriteServices writeServices;
    @Inject
    private Repositories repos;
    @Inject
    protected PredefinedDataCreator predefinedDataCreator;
    @Inject
    private Transformers transformers;

    private long adminId;
    private long secondAdminId;
    private long proteinDatabaseEcoli;
    private long proteinDatabaseEcoliWithWrongReference;

    private long imageProcessingStepType;

    private ImmutableMap<Class, Long> processorToWorkflowStep;
    private long persistProteinDBStepType;

    private long experimentLabelType; //silac
    private long experimentLabelWithRAminoAcid; // experiment label with R aminoAcid
    private long experimentLabelWithKAminoAcid; // experiment label with K aminoAcid

    public void setBilling(boolean value) {
        setFeature(ApplicationFeature.BILLING, value);
    }

    public void setProteinSearch(boolean value) {
        setFeature(ApplicationFeature.PROTEIN_ID_SEARCH, value);
    }

    public void setFeature(ApplicationFeature feature, boolean enabled) {
        repos.featuresRepository.set(feature.getFeatureName(), enabled ? ENABLED : DISABLED);
    }

    public void setProteinSearchFeaturePerLab(final long labId, final boolean value) {
        final ImmutableSet<Long> labs = value ? ImmutableSet.of(labId) : ImmutableSet.of();
        setFeaturePerLab(ApplicationFeature.PROTEIN_ID_SEARCH, labs);
    }

    @Transactional
    public void setFeaturePerLab(ApplicationFeature feature, Collection<Long> labIds) {
        final Set<Lab> labs = labIds.stream()
                .map(labId -> new Lab(labId))
                .collect(Collectors.toSet());
        repos.featuresRepository.set(feature.getFeatureName(), ENABLED_PER_LAB, labs);
    }

    public void updateExperimentFiles(long user, long experiment, long file) {
        final ImmutableList.Builder<com.infoclinika.mssharing.model.write.FileItem> builder = ImmutableList.builder();
        final ExperimentItem experimentItem = detailsReader.readExperiment(user, experiment);
        builder.addAll(transform(experimentItem.files, new Function<FileItemTemplate, com.infoclinika.mssharing.model.write.FileItem>() {
            @Override
            public com.infoclinika.mssharing.model.write.FileItem apply(FileItemTemplate input) {
                return new com.infoclinika.mssharing.model.write.FileItem(input.id, false, 0, preparedSample(file));
            }
        }));
        builder.addAll(noFactoredFile(file));

        final ExperimentInfo.Builder build = new ExperimentInfo.Builder().name(generateString())
                .description(generateString()).experimentType(anyExperimentType()).specie(unspecified())
                .project(experimentItem.project).billLab(uc.createLab3()).is2dLc(false)
                .restriction(restriction(user, file)).factors(NO_FACTORS).lockMasses(NO_LOCK_MASSES).files(builder.build())
                .experimentLabels(new ExperimentLabelsInfo()).sampleTypesCount(1);

        studyManagement.updateExperiment(user, experiment, build.build());
    }

    public void addFilesToExperiment(long user, long experiment) {
        final long file = uc.saveFile(user, instrumentFromExperimentFile(user, experiment));
        updateExperimentFiles(user, experiment, file);
    }

    public void addFilesToExperiment(long user, long experiment, final List<ExperimentManagementTemplate.MetaFactorTemplate> factors,
                                     ImmutableList<com.infoclinika.mssharing.model.write.FileItem> fileItems, final List<String> factorValuesOfNewFactorsForOldSamples) {

        final ImmutableList.Builder<com.infoclinika.mssharing.model.write.FileItem> builder = ImmutableList.builder();
        final ExperimentItem experimentItem = detailsReader.readExperiment(user, experiment);
        final Iterable<com.infoclinika.mssharing.model.write.FileItem> oldFiles = transform(experimentItem.files, new Function<FileItemTemplate, com.infoclinika.mssharing.model.write.FileItem>() {
            @Override
            public com.infoclinika.mssharing.model.write.FileItem apply(FileItemTemplate input) {
                final Set<ExperimentSampleItem> updatedSamples = newHashSet();
                final FileItem fileItem = (FileItem) input;
                for (ExperimentSampleItem sampleItem : fileItem.preparedSample.samples) {
                    final ArrayList<String> updatedFactors = newArrayList(factorValuesOfNewFactorsForOldSamples);
                    updatedSamples.add(new ExperimentSampleItem(sampleItem.name, sampleItem.type, updatedFactors));
                }
                final ExperimentPreparedSampleItem preparedSample = new ExperimentPreparedSampleItem(fileItem.preparedSample.name, updatedSamples);
                return new com.infoclinika.mssharing.model.write.FileItem(input.id, false, fileItem.fractionNumber, preparedSample);
            }
        });
        builder.addAll(fileItems);
        builder.addAll(oldFiles);

        final ExperimentInfo.Builder infoBuilder = new ExperimentInfo.Builder()
                .name(generateString())
                .description(generateString())
                .experimentType(anyExperimentType())
                .specie(unspecified())
                .project(experimentItem.project)
                .billLab(uc.createLab3())
                .is2dLc(false)
                .restriction(restriction(user, experimentItem.files.get(0).id))
                .factors(factors)
                .files(builder.build())
                .experimentLabels(new ExperimentLabelsInfo(experimentItem.labels.lightLabels, experimentItem.labels.mediumLabels, experimentItem.labels.heavyLabels))
                .sampleTypesCount(experimentItem.sampleTypesCount)
                .lockMasses(NO_LOCK_MASSES);

        studyManagement.updateExperiment(user, experiment, infoBuilder.build());
    }

    protected Map<String, String> createPayment(int amount, long labId) {
        final Map<String, String> map = new HashMap<>();
        map.put("txn_id", generateString());
        map.put("custom", String.valueOf(labId));
        map.put("receiver_id", generateString());
        map.put("receiver_email", generateString());
        map.put("payer_email", generateString());
        map.put("payment_status", "completed");
        map.put("payment_gross", String.valueOf(amount / (double) 100));
        return map;
    }


    protected int getDaysInMonth() {
        return new DateTime(DateTimeZone.forTimeZone(transformers.serverTimezone))
                .dayOfMonth()
                .withMaximumValue()
                .getDayOfMonth();
    }


    /**
     * Don't do any db writes if db wasn't empty before test started.
     */
    @BeforeMethod
    public void setUp() {
        reset(super.applicationContext.getBean(Notifier.class));
        for (CrudRepository repo : repos.get()) {
            try {
                checkState(repo.count() == 0, repo.findAll());
            } catch (RuntimeException ex) {
                log.error("Failed on deletion {}", repo);
                throw Throwables.propagate(ex);
            }
        }
        adminId = predefinedDataCreator.admin("Mark", "Thomson", Data.ADMIN_EMAIL, "123");
        secondAdminId = predefinedDataCreator.admin("Mark2", "Thomson2", Data.ADMIN_EMAIL_2, "1234");
        final long chargeableItem = billingManagement.createChargeableItem(ARCHIVE_PRICE, BillingFeature.ARCHIVE_STORAGE, 1, BillingChargeType.PER_GB);//cents
        final long chargeableItem1 = billingManagement.createChargeableItem(ANALYSE_PRICE, BillingFeature.ANALYSE_STORAGE, 1, BillingChargeType.PER_GB);
//        final long chargeableItem2 = billingManagement.createChargeableItem(TRANSLATION_PRICE, BillingFeature.TRANSLATION, 1, BillingChargeType.PER_GB);//cents
        final long chargeableItem3 = billingManagement.createChargeableItem(DOWNLOAD_PRICE, BillingFeature.DOWNLOAD, 1, BillingChargeType.PER_GB);//cents
        final long chargeableItem4 = billingManagement.createChargeableItem(PROTEIN_SEARCH_PRICE, BillingFeature.PROTEIN_ID_SEARCH, 1, BillingChargeType.PER_GB);//cents
        final long chargeableItem5 = billingManagement.createChargeableItem(DOWNLOAD_PRICE, BillingFeature.PUBLIC_DOWNLOAD, 1, BillingChargeType.PER_GB);//cents
        final long processing = billingManagement.createChargeableItem(PROCESSING_FEATURE_PRICE, BillingFeature.PROCESSING, 1, BillingChargeType.PER_GB);//cents
        final long storageVolumes = billingManagement.createChargeableItem(STORAGE_VOLUMES_FEATURE_PRICE, BillingFeature.STORAGE_VOLUMES, 1, BillingChargeType.PER_GB);//cents
        final long archiveStorageVolumes = billingManagement.createChargeableItem(ARCHIVE_STORAGE_VOLUMES_FEATURE_PRICE, BillingFeature.ARCHIVE_STORAGE_VOLUMES, 1, BillingChargeType.PER_GB);//cents


        final HashSet<FileExtensionItem> extensions = newHashSet(new FileExtensionItem(".raw", "", Collections.<String, AdditionalExtensionImportance>emptyMap()));
        predefinedDataCreator.instrumentModel(vendor1, instrumentType11, msStudyType, instrumentModel111, false, false, extensions);
        predefinedDataCreator.instrumentModel(vendor1, instrumentType12, msStudyType, instrumentModel121, false, false, extensions);
        predefinedDataCreator.instrumentModel(vendor1, instrumentType12, msStudyType, instrumentModel122, false, false, extensions);

        predefinedDataCreator.instrumentModel(vendor2, instrumentType21, msStudyType, instrumentModel211, false, false, extensions);
        predefinedDataCreator.instrumentModel(vendor2, instrumentType21, msStudyType, instrumentModel212, false, false, extensions);

        final HashSet<FileExtensionItem> extensions3 = newHashSet(new FileExtensionItem(".raw", ".raw", Collections.<String, AdditionalExtensionImportance>emptyMap()));
        predefinedDataCreator.instrumentModel(vendor3, instrumentType21, msStudyType, instrumentModel212, true, false, extensions3);

        final Set<FileExtensionItem> microArraysExtensions = newHashSet(new FileExtensionItem(".CEL.gz", "", Collections.<String, AdditionalExtensionImportance>emptyMap()));

        predefinedDataCreator.experimentType("Unspecified", false, false);
        predefinedDataCreator.experimentType("Bottom Down Proteomics", true, true);

        predefinedDataCreator.species("Man", "Rat", "Horse", "Cat", "Escherichia coli", "Unspecified");

        predefinedDataCreator.allUsersGroup();
        proteinDatabaseEcoli = predefinedDataCreator.proteinDatabase(adminId, "Escherichia coli", "fasta-dbs/EscherichiaColi_cut.fasta", "Escherichia coli");
        proteinDatabaseEcoliWithWrongReference = predefinedDataCreator.proteinDatabase(adminId, "Escherichia coli", "unspecified", "Escherichia coli");


        ReflectionTestUtils.setField(fileMovingManager, "testMode", true);

        uc = new UseCase(writeServices, adminId, instrumentCreationHelper, experimentCreationHelper, dashboardReader, repos);
        setBilling(false);
        setFeature(ApplicationFeature.TRANSLATION, false);
        setFeature(ApplicationFeature.PROTEIN_ID_SEARCH_RESULTS, false);
        setProteinSearch(false);

        experimentLabelType = experimentLabelManagement.createLabelType(new ExperimentTypeInfo("Silac", 3));
        experimentLabelWithRAminoAcid = experimentLabelManagement.createLabel(new ExperimentLabelManagement.ExperimentLabelInfo("R", "Arg10", experimentLabelType));
        experimentLabelManagement.createLabel(new ExperimentLabelManagement.ExperimentLabelInfo("R", "Arg6", experimentLabelType));
        experimentLabelWithKAminoAcid = experimentLabelManagement.createLabel(new ExperimentLabelManagement.ExperimentLabelInfo("K", "Lys4", experimentLabelType));
        experimentLabelManagement.createLabel(new ExperimentLabelManagement.ExperimentLabelInfo("K", "Lys6", experimentLabelType));

        predefinedDataCreator.billingProperties();
    }

    /**
     * Clean up db after each test.
     */

    @AfterMethod
    @SuppressWarnings("unchecked")
    public void tearDown() {
        for (String name : repos.featuresRepository.get().keySet()) {
            repos.featuresRepository.delete(name);
        }
        final ImmutableList<CrudRepository> repositories = from(repos.get()).toList();
        for (int i = 0; i < repositories.size(); i++) {
            final CrudRepository repo = repositories.get(i);
            for (Object e : repo.findAll()) {
                try {
                    repo.delete(e);
                } catch (RuntimeException ex) {
                    log.error("Failed on deletion {}", e);
                    throw Throwables.propagate(ex);
                }
            }
            repo.deleteAll();
        }

    }

    public long createPublicProject(long user) {
        return createPublicProject(user, null);
    }

    protected long createPublicProject(long user, Long lab) {
        final long project = studyManagement.createProject(user, new ProjectInfo("public project", "area", "", lab));
        sharingManagement.makeProjectPublic(user, project);
        return project;
    }

    protected long createInstrumentByModel(long bob, long lab, long model) {
        return createInstrumentAndApproveIfNeeded(bob, lab, model, instrumentDetails()).get();
    }

    public long proteinDatabase(long user, String dbName, String specie) {
        final long db = proteinDatabaseManagement.createDatabase(user, dbName, getSpecie(specie), 1024, false, false, PROTEOMICS);
        proteinDatabaseManagement.specifyProteinDatabaseContent(user, db, "fastadbs/5/56");
        return db;
    }

    protected static List<String> sameListAs(List<String> strings) {
        class StringListMatcher extends ArgumentMatcher<List<String>> {
            private List<String> expected;

            StringListMatcher(List<String> expected) {
                this.expected = expected;
            }

            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof List)) {
                    return false;
                }
                List actualList = (List) argument;
                if (actualList.size() != expected.size()) {
                    return false;
                }
                for (int i = 0; i < actualList.size(); i++) {
                    if (!actualList.get(i).equals(expected.get(i))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return argThat(new StringListMatcher(strings));
    }

    protected void initFailedEmailsNotifiers() {

        repos.failedEmailsNotifierRepository.save(of(new FailedMailNotificationReceiver("example@email.com"), new FailedMailNotificationReceiver("example2@email.com")));
    }

    @Inject
    protected InstrumentReader instrumentReader;
    @Inject
    protected InstrumentManagement instrumentManagement;
    @Inject
    protected InstrumentCreationHelperTemplate<PotentialOperator> instrumentCreationHelper;
    @Inject
    protected SharingManagement sharingManagement;
    @Inject
    protected StudyManagement studyManagement;
    @Inject
    protected AttachmentManagement attachmentManagement;
    @Inject
    @Named("defaultSharingProjectShortRecordAdapter")
    protected SharingProjectHelperTemplate sharingProjectHelper;
    @Inject
    protected AdministrationToolsReader administrationToolsReader;
    @Inject
    protected ExperimentCreationHelper experimentCreationHelper;
    @Inject
    protected DashboardReader dashboardReader;
    @Inject
    @Named("fileReaderImpl")
    protected FileReaderTemplate<FileLine> fileReader;
    @Inject
    protected GroupsReaderTemplate<GroupsReaderTemplate.GroupLine> groupsReader;
    @Inject
    protected TrashReader trashReader;
    @Inject
    protected PasswordEncoder encoder;
    @Inject
    protected DetailsReader detailsReader;
    @Inject
    protected ProteinDatabaseReader proteinDatabaseReader;
    @Inject
    protected UserManagement userManagement;
    @Inject
    protected LabHeadManagement labHeadManagement;
    @Inject
    protected SecurityHelper securityHelper;
    @Inject
    protected RegistrationHelperTemplate registrationHelper;
    @Inject
    protected LabManagement labManagement;
    @Inject
    protected UserReader userReader;
    @Inject
    protected RequestsReader requestsReader;
    @Inject
    protected ExtendedInfoReader extendedInfoReader;
    @Inject
    protected DownloadFileReader downloadFileReader;
    @Inject
    protected RequestsTemplate requests;
    @Inject
    protected ExperimentDownloadHelper downloadHelper;
    @Inject
    protected FileMetaInfoHelper fileMetaInfoHelper;
    @Inject
    protected FileMovingManager fileMovingManager;
    @Inject
    protected GlacierDownloadListeners<ActiveFileMetaData> glacierDownloadListeners;
    @Inject
    protected PaymentHistoryReader paymentHistoryReader;
    @Inject
    protected BillingHelper billingHelper;
    @Inject
    protected FileOperationsManager fileOperationsManager;
    @Inject
    protected FeaturesManagement featuresManagement;
    @Inject
    protected ProteinDatabaseManagement proteinDatabaseManagement;
    @Inject
    protected BillingManagement billingManagement;
    @Inject
    protected StoredObjectPaths storedObjectPaths;
    @Inject
    protected UserPreferencesReader userPreferencesReader;
    @Inject
    protected UserPreferencesManagement userPreferencesManagement;
    @Inject
    private ExperimentLabelManagement experimentLabelManagement;

    protected long unspecifiedSpecie() {
        return find(experimentCreationHelper.species(), DictionaryItem.UNSPECIFIED).id;
    }

    protected long getSpecie(final String specieName) {
        return find(experimentCreationHelper.species(), new Predicate<DictionaryItem>() {
            @Override
            public boolean apply(@Nullable DictionaryItem input) {
                return input.name.equals(specieName);
            }
        }).id;
    }

    //TODO: Code smell. Consider replacing direct repository usage to test specific service .
    protected UserLabMembershipRequestRepositoryTemplate<UserLabMembershipRequestTemplate<User, Lab>> getUserLabMemebershipRequestRepository() {
        return repos.userLabMembershipRequestRepository;
    }

    protected long getProteinDatabaseEcoli() {
        return proteinDatabaseEcoli;
    }

    protected long getProteinDatabaseEcoliWithWrongWrongReference() {
        return proteinDatabaseEcoliWithWrongReference;
    }

    protected List<String> emptyFactors() {
        return emptyList();
    }

    /* workflow execution getters*/
    protected long getImageProcessingStepType() {
        return imageProcessingStepType;
    }


    protected long getPersistProteinDBStepType() {
        return persistProteinDBStepType;
    }

    protected long getExperimentLabelType() {
        return experimentLabelType;
    }

    protected ExperimentSampleItem sampleWithNoFactors(long id) {
        return new ExperimentSampleItem(String.valueOf(id), LIGHT, emptyFactors());
    }

    protected ExperimentSampleItem sampleWithFactors(long id, List<String> factorValues) {
        return new ExperimentSampleItem(String.valueOf(id), LIGHT, factorValues);
    }

    protected ExperimentPreparedSampleItem preparedSample(long fileId, Set<ExperimentSampleItem> bioSamples) {
        return new ExperimentPreparedSampleItem(String.valueOf(fileId), bioSamples);
    }

    protected ExperimentPreparedSampleItem preparedSample(long id) {
        return new ExperimentPreparedSampleItem(String.valueOf(id), ImmutableSet.of(sampleWithNoFactors(id)));
    }

    protected static ImmutableMap<String, ExperimentSampleItem> extractSampleNameToSample(ExperimentItem experimentItem) {
        Set<ExperimentSampleItem> allSamples = newHashSet();
        for (DetailsReaderTemplate.FileItemTemplate file : experimentItem.files) {
            allSamples.addAll(((FileItem) file).preparedSample.samples);
        }
        return Maps.uniqueIndex(allSamples, new Function<ExperimentSampleItem, String>() {
            @Override
            public String apply(ExperimentSampleItem sampleItem) {
                return composeSampleUniqueKey(sampleItem);
            }
        });
    }

    protected static String composeSampleUniqueKey(ExperimentSampleItem sample) {
        return composeSampleUniqueKey(sample.name, sample.type);
    }

    protected static String composeSampleUniqueKey(String sampleName, ExperimentSampleTypeItem type) {
        return sampleName + type.toString();
    }

    protected long getExperimentLabelKAminoAcid() {
        return experimentLabelWithRAminoAcid;
    }

    protected long getExperimentLabelRAminoAcid() {
        return experimentLabelWithRAminoAcid;
    }
}
