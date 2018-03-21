/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.model.testing.helper;

import com.google.common.base.*;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.platform.entity.UserLabMembershipRequestTemplate;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.NotifierTemplate;
import com.infoclinika.mssharing.platform.model.PredefinedDataCreatorTemplate;
import com.infoclinika.mssharing.platform.model.RequestsTemplate;
import com.infoclinika.mssharing.platform.model.common.items.AdditionalExtensionImportance;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.FileExtensionItem;
import com.infoclinika.mssharing.platform.model.helper.*;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate.ExperimentDownloadDataTemplate;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate.ExperimentItemTemplate;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate.FileDataTemplate;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate.PotentialOperator;
import com.infoclinika.mssharing.platform.model.helper.SecurityHelperTemplate.UserDetails;
import com.infoclinika.mssharing.platform.model.read.*;
import com.infoclinika.mssharing.platform.model.read.DetailsReaderTemplate.FileItemTemplate;
import com.infoclinika.mssharing.platform.model.read.FileReaderTemplate.FileLineTemplate;
import com.infoclinika.mssharing.platform.model.write.*;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.AnnotationTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.MetaFactorTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.Restriction;
import com.infoclinika.mssharing.platform.model.write.InstrumentManagementTemplate.InstrumentDetailsTemplate;
import com.infoclinika.mssharing.platform.model.write.ProjectManagementTemplate.ProjectInfoTemplate;
import com.infoclinika.mssharing.platform.repository.UserLabMembershipRequestRepositoryTemplate;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;

/**
 * Provides methods for some common scenarios.
 *
 * @author Stanislav Kurilin
 */
@SuppressWarnings("unchecked")
public class AbstractTestTemplate extends AbstractTestNGSpringContextTests {

    public static final List<Long> NO_OPERATORS = emptyList();
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    public UseCase uc;
    @Inject
    protected ProjectManagementTemplate<ProjectInfoTemplate> projectManagement;
    @Inject
    protected InstrumentManagementTemplate<InstrumentDetailsTemplate> instrumentManagement;
    @Inject
    protected InstrumentCreationHelperTemplate<PotentialOperator> instrumentCreationHelper;
    @Inject
    protected SharingManagementTemplate sharingManagement;
    @Inject
    protected AttachmentManagementTemplate attachmentManagement;
    @Inject
    @Named("defaultSharingProjectShortRecordAdapter")
    protected SharingProjectHelperTemplate sharingProjectHelper;
    @Inject
    protected ExperimentCreationHelperTemplate experimentCreationHelper;
    @Inject
    protected FileReaderTemplate<FileLineTemplate> fileReader;
    @Inject
    protected GroupsReaderTemplate<GroupsReaderTemplate.GroupLine> groupsReader;
    @Inject
    protected PasswordEncoder encoder;
    @Inject
    protected DetailsReaderTemplate detailsReader;
    @Inject
    protected RequestsDetailsReaderTemplate requestsDetailsReader;
    @Inject
    protected UserManagementTemplate userManagement;
    @Inject
    protected LabHeadManagementTemplate labHeadManagement;
    @Inject
    protected SecurityHelperTemplate<UserDetails> securityHelper;
    @Inject
    protected RegistrationHelperTemplate registrationHelper;
    @Inject
    protected LabManagementTemplate<LabManagementTemplate.LabInfoTemplate> labManagement;
    @Inject
    protected UserTestHelper userTestHelper;
    @Inject
    protected UserReaderTemplate<UserReaderTemplate.UserLineTemplate> userReader;
    @Inject
    protected RequestsReaderTemplate requestsReader;
    @Inject
    protected UploadHelperTemplate uploadHelper;
    @Inject
    protected RequestsTemplate requests;
    @Inject
    protected ExperimentManagementTemplate<ExperimentManagementTemplate.ExperimentInfoTemplate<?, ?>> experimentManagement;
    @Inject
    protected LabReaderTemplate labReader;
    @Inject
    protected InstrumentReaderTemplate<?> instrumentReader;
    @Inject
    protected FileManagementTemplate fileManagement;
    @Inject
    protected ExperimentReaderTemplate<?> experimentReader;
    @Inject
    protected ProjectReaderTemplate<?> projectReader;
    @Inject
    protected ProjectSharingRequestManagement projectSharingRequestManagement;
    @Inject
    protected ExperimentDownloadHelperTemplate<
            ExperimentItemTemplate,
            ExperimentDownloadDataTemplate,
            FileDataTemplate> downloadHelper;
    @Inject
    protected FileUploadManagementTemplate fileUploadManagement;
    @Inject
    private WriteServices writeServices;
    @Inject
    private ReadServices readServices;
    @Inject
    private Repositories repos;
    @Inject
    private PredefinedDataCreatorTemplate predefinedDataCreator;
    private long adminId;
    private long secondAdminId;
    @Value("${protein.dbs.target.folder}")
    private String proteinDatabasesPrefix;

    public static List<String> sameListAs(List<String> strings) {
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
        return Matchers.argThat(new StringListMatcher(strings));
    }

    public void setContent(long bob, long file) {
        fileUploadManagement.completeMultipartUpload(bob, file, generateString());
    }

    public long createInstrumentByModel(long bob, long lab, long model) {
        return createInstrumentAndApproveIfNeeded(bob, lab, model, instrumentDetails()).get();
    }

    public long createInstrumentAndApproveIfNeeded(long user, long lab) {
        return createInstrumentAndApproveIfNeeded(user, lab, anyInstrumentModelByVendor(anyVendor()), instrumentDetails()).get();
    }

    public Optional<Long> createInstrumentAndApproveIfNeeded(long user,
                                                             long lab,
                                                             long model,
                                                             InstrumentDetailsTemplate details) {

        final boolean labHead = labManagement.isLabHead(user, lab);
        if (labHead) {
            return Optional.of(instrumentManagement.createInstrument(user, lab, model, details));
        } else {
            final Optional<Long> instrumentRequest =
                    instrumentManagement.newInstrumentRequest(user, lab, model, details, new ArrayList<Long>());
            final LabReaderTemplate.LabLineTemplate labLine = labReader.readLab(lab);
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
        if (!Iterables.any(fileReader.readFiles(user, Filter.ALL), new Predicate<FileLineTemplate>() {
            @Override
            public boolean apply(FileLineTemplate input) {
                return input.id == file;
            }
        })) throw new AccessDenied("asserting");
        detailsReader.readFile(user, file);
        reuseFile(user, lab, file);
    }

    public void reuseFile(long actor, long lab, long file) {
        final long privateProject = uc.createProject(actor, lab);
        createInstrumentAndExperiment(actor, lab, privateProject, Data.NO_FACTORS, noFactoredFile(file));
    }

    public long anyInstrumentModel() {
        return randElement(instrumentCreationHelper.models(anyVendor())).id;
    }

    public long anyVendor() {
        return instrumentCreationHelper.vendors().first().id;
    }

    public long anyTechnologyType() {
        return instrumentCreationHelper.studyTypes().first().id;
    }

    public long thermoVendor() {
        for (DictionaryItem item : instrumentCreationHelper.vendors()) {
            if (item.name.equals(Data.vendor1)) {
                return item.id;
            }
        }
        return instrumentCreationHelper.vendors().first().id;
    }

    public long abSciexInstrumentModel() {
        Long model = getInstrumentModelByNames(Data.AB_SCIEX, Data.AB_SCIEX_INSTRUMENT_MODEL);
        if (model == null) {
            //initialize AB SCIEX instrument model
            return predefinedDataCreator.instrumentModel(Data.AB_SCIEX, Data.instrumentType11, Data.studyType1, Data.AB_SCIEX_INSTRUMENT_MODEL, false, true, Data.AB_SCIEX_EXTENSIONS);
        }
        return model;
    }

    public long brukerInstrumentModel() {
        Long model = getInstrumentModelByNames(Data.BRUKER, Data.BRUKER_INSTRUMENT_MODEL);
        if (model == null) {
            //initialize Bruker instrument model
            return predefinedDataCreator.instrumentModel(Data.BRUKER, Data.instrumentType11, Data.studyType1, Data.BRUKER_INSTRUMENT_MODEL, true, false, Data.BRUKER_EXTENSIONS);
        }
        return model;
    }

    public Long getInstrumentModelByNames(String vendorName, String instrumentName) {
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

    public InstrumentDetailsTemplate instrumentDetails() {
        return new InstrumentDetailsTemplate(generateString(), generateString(), generateString());
    }

    public long anyInstrumentModelByVendor(long vendor) {
        return randElement(instrumentCreationHelper.models(vendor)).id;
    }

    public long anyAvailableInstrumentModel(long user) {
        return randElement(experimentCreationHelper.availableInstrumentModels(user, null)).id;
    }

    public long anotherInstrumentModel(long vendor, final long model) {
        return Iterables.find(instrumentCreationHelper.models(vendor), new Predicate<DictionaryItem>() {
            @Override
            public boolean apply(DictionaryItem input) {
                return input.id != model;
            }
        }).id;
    }

    public <T> T randElement(Collection<T> all) {
        final Random random = new Random();
        return Iterables.get(all, random.nextInt(all.size()));
    }

    public String generateString() {
        return UUID.randomUUID().toString();
    }

    public ImmutableList<ExperimentManagementTemplate.FileItemTemplate> noFactoredFile(long file) {
        return of(factoredFile(file, Data.NO_FACTOR_VALUES));
    }

    public ImmutableList<ExperimentManagementTemplate.FileItemTemplate> noFactoredFiles(List<Long> files) {
        return from(files).transformAndConcat(new Function<Long, ImmutableList<ExperimentManagementTemplate.FileItemTemplate>>() {
            @Override
            public ImmutableList<ExperimentManagementTemplate.FileItemTemplate> apply(Long fileId) {
                return noFactoredFile(fileId);
            }
        }).toList();
    }

    public ImmutableList<ExperimentManagementTemplate.FileItemTemplate> anyFile(long user) {
        final long file = uc.saveFile(user);
        return noFactoredFile(file);
    }

    public long admin() {
        return adminId;
    }

    public long otherAdmin() {
        return secondAdminId;
    }

    public NotifierTemplate notificator() {
        return super.applicationContext.getBean(NotifierTemplate.class);
    }

    public long createInstrumentAndExperiment(long user, long lab, long project, List<MetaFactorTemplate> noFactors, List<ExperimentManagementTemplate.FileItemTemplate> fileItems) {
        final Optional<Long> lab3Instrument = createInstrumentAndApproveIfNeeded(user, lab, anyInstrumentModelByVendor(anyVendor()), instrumentDetails());
        //todo[tymchenko]: refactor later
        if (lab == uc.getLab3()) {
            uc.onLab3InstrumentCreated(lab3Instrument.get());
        }
        final ExperimentInfoTemplateBuilder builder = experimentInfo().project(project).lab(lab)
                .is2Dlc(false).restriction(restriction(user, fileItems.get(0).id)).factors(noFactors).files(fileItems);
        return experimentManagement.createExperiment(user, builder.build());
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
        final ExperimentInfoTemplateBuilder builder = experimentInfo()
                .project(project).lab(lab).is2Dlc(false)
                .restriction(restriction(user, file)).factors(Data.NO_FACTORS)
                .files(noFactoredFile(file));

        return experimentManagement.createExperiment(user, builder.build());
    }

    public long createExperiment(long user, long project, Long lab, List<ExperimentManagementTemplate.FileItemTemplate> fileItems) {

        final ExperimentInfoTemplateBuilder builder = experimentInfo()
                .project(project)
                .lab(lab)
                .is2Dlc(false)
                .restriction(restriction(user))
                .factors(Data.NO_FACTORS).files(fileItems);
        return experimentManagement.createExperiment(user, builder.build());
    }

    public long createExperiment(long user, long project, long model, Long lab, List<ExperimentManagementTemplate.FileItemTemplate> fileItems) {

        final ExperimentInfoTemplateBuilder<MetaFactorTemplate, ExperimentManagementTemplate.FileItemTemplate> builder = experimentInfo()
                .project(project)
                .lab(lab)
                .is2Dlc(false)
                .restriction(newRestriction(model))
                .factors(Data.NO_FACTORS).files(fileItems);

        return experimentManagement.createExperiment(user, builder.build());
    }

    public Restriction newRestriction(long model) {
        return new Restriction(model, Optional.absent());
    }

    public long createExperiment(long user, long project) {
        return createExperiment(user, project, uc.getLab3());
    }

    public long createExperiment(long user, long project, Long lab) {

        final long file = uc.saveFile(user);
        final ExperimentInfoTemplateBuilder builder = experimentInfo()
                .project(project)
                .lab(lab)
                .is2Dlc(false)
                .restriction(restriction(user, file))
                .factors(Data.NO_FACTORS)
                .files(noFactoredFile(file));
        return experimentManagement.createExperiment(user, builder.build());
    }

    public long createExperiment(long user, long project, long file, long lab) {
        final ExperimentInfoTemplateBuilder builder = experimentInfo()
                .project(project)
                .lab(lab)
                .is2Dlc(false)
                .restriction(restriction(user, file)).factors(Data.NO_FACTORS)
                .files(noFactoredFile(file));
        return experimentManagement.createExperiment(user, builder.build());
    }

    public long createExperimentWithName(long user, long project, String name) {
        final long file = uc.saveFile(user);
        final Long lab = uc.getLab3();
        final ExperimentInfoTemplateBuilder builder = experimentInfo(name)
                .project(project)
                .lab(lab)
                .is2Dlc(false)
                .restriction(restriction(user, file))
                .factors(Data.NO_FACTORS)
                .files(noFactoredFile(file));
        return experimentManagement.createExperiment(user, builder.build());
    }

    public Restriction restriction(long user) {
        return newRestriction(anyAvailableInstrumentModel(user));
    }

    public Restriction restriction(long user, long file) {
        final long model = instrumentModel(user, file);
        return newRestriction(model);
    }

    public long instrumentModel(long user, long file) {
        final FileItemTemplate fileItem = detailsReader.readFile(user, file);
        return detailsReader.readInstrument(user, fileItem.instrumentId).modelId;
    }

    public ExperimentInfoTemplateBuilder<MetaFactorTemplate, ExperimentManagementTemplate.FileItemTemplate> experimentInfo() {
        return experimentInfo(unspecified());
    }

    public ExperimentInfoTemplateBuilder<MetaFactorTemplate, ExperimentManagementTemplate.FileItemTemplate> experimentInfo(String name) {
        return experimentInfo(name, unspecified());
    }

    public ExperimentInfoTemplateBuilder<MetaFactorTemplate, ExperimentManagementTemplate.FileItemTemplate> experimentInfo(long species) {
        return experimentInfo(generateString(), species);
    }

    public ExperimentInfoTemplateBuilder<MetaFactorTemplate, ExperimentManagementTemplate.FileItemTemplate> experimentInfo(String name, long species) {
        return new ExperimentInfoTemplateBuilder<>()
                .name(name)
                .description(generateString())
                .experimentType(anyExperimentType())
                .species(species);
    }

    public long anySpecies() {
        Collection<DictionaryItem> filtered = Collections2.filter(experimentCreationHelper.species(), not(DictionaryItem.UNSPECIFIED));
        return randElement(filtered).id;
    }

    public long unspecified() {
        return Iterables.find(experimentCreationHelper.species(), DictionaryItem.UNSPECIFIED).id;
    }

    public long anyExperimentType() {
        return experimentCreationHelper.experimentTypes().first().id;
    }

    public long instrumentFromExperimentFile(long actor, long experiment) {
        return Iterables.getFirst(fileReader.readFilesByExperiment(actor, experiment), null).instrumentId;
    }

    public void updateExperimentFiles(long user, long experiment, long file) {
        final ImmutableList.Builder<ExperimentManagementTemplate.FileItemTemplate> builder = ImmutableList.builder();
        final DetailsReaderTemplate.ExperimentItemTemplate experimentItem = detailsReader.readExperiment(user, experiment);
        builder.addAll(transform(experimentItem.files, new Function<FileItemTemplate, ExperimentManagementTemplate.FileItemTemplate>() {
            @Override
            public ExperimentManagementTemplate.FileItemTemplate apply(FileItemTemplate input) {
                return factoredFile(input.id, Data.NO_FACTOR_VALUES);
            }
        }));
        builder.addAll(noFactoredFile(file));

        final ExperimentInfoTemplateBuilder build = experimentInfo()
                .name(generateString())
                .description(generateString())
                .experimentType(anyExperimentType())
                .species(unspecified())
                .project(experimentItem.project)
                .is2Dlc(false)
                .restriction(restriction(user, file))
                .factors(Data.NO_FACTORS)
                .files(builder.build());

        experimentManagement.updateExperiment(user, experiment, build.build());
    }

    public void addFilesToExperiment(long user, long experiment) {
        final long file = uc.saveFile(user, instrumentFromExperimentFile(user, experiment));
        updateExperimentFiles(user, experiment, file);
    }

    public void addFilesToExperiment(long user, long experiment, final List<MetaFactorTemplate> factors, ImmutableList<ExperimentManagementTemplate.FileItemTemplate> fileItems, final List<String> factorValuesForOldFiles) {
        final ImmutableList.Builder<ExperimentManagementTemplate.FileItemTemplate> builder = ImmutableList.builder();
        final DetailsReaderTemplate.ExperimentItemTemplate experimentItem = detailsReader.readExperiment(user, experiment);
        builder.addAll(fileItems);
        builder.addAll(transform(experimentItem.files, new Function<FileItemTemplate, ExperimentManagementTemplate.FileItemTemplate>() {
            @Override
            public ExperimentManagementTemplate.FileItemTemplate apply(FileItemTemplate input) {
                final long id = input.id;
                return factoredFile(id, factorValuesForOldFiles);
            }
        }));

        final ExperimentInfoTemplateBuilder<?, ?> infoBuilder = experimentInfo()
                .name(generateString())
                .description(generateString())
                .experimentType(anyExperimentType())
                .species(unspecified())
                .project(experimentItem.project)
                .is2Dlc(false)
                .restriction(restriction(user, experimentItem.files.get(0).id))
                .factors(factors)
                .files(builder.build());

        experimentManagement.updateExperiment(user, experiment, infoBuilder.build());
    }

    public ExperimentManagementTemplate.FileItemTemplate factoredFile(long id, List<String> factorValuesForOldFiles) {
        return new ExperimentManagementTemplate.FileItemTemplate(id, factorValuesForOldFiles, ImmutableList.<AnnotationTemplate>of(), false);
    }

    /**
     * Don't do any db writes if db wasn't empty before test started.
     */
    @BeforeMethod
    public void setUp() {
        Mockito.reset(super.applicationContext.getBean(NotifierTemplate.class));
        for (CrudRepository repo : repos.get()) {
            try {
                Preconditions.checkState(repo.count() == 0, repo.findAll());
            } catch (RuntimeException ex) {
                log.error("Failed on deletion {}", repo);
                throw Throwables.propagate(ex);
            }
        }
        adminId = predefinedDataCreator.admin("Mark", "Thomson", Data.ADMIN_EMAIL, "123");
        secondAdminId = predefinedDataCreator.admin("Mark2", "Thomson2", Data.ADMIN_EMAIL_2, "1234");

        final HashSet<FileExtensionItem> extensions = newHashSet(new FileExtensionItem(".raw", "", Collections.<String, AdditionalExtensionImportance>emptyMap()));
        predefinedDataCreator.instrumentModel(Data.vendor1, Data.instrumentType11, Data.studyType1, Data.instrumentModel111, false, false, extensions);
        predefinedDataCreator.instrumentModel(Data.vendor1, Data.instrumentType12, Data.studyType1, Data.instrumentModel121, false, false, extensions);
        predefinedDataCreator.instrumentModel(Data.vendor1, Data.instrumentType12, Data.studyType1, Data.instrumentModel122, false, false, extensions);

        predefinedDataCreator.instrumentModel(Data.vendor2, Data.instrumentType21, Data.studyType1, Data.instrumentModel211, false, false, extensions);
        predefinedDataCreator.instrumentModel(Data.vendor2, Data.instrumentType21, Data.studyType1, Data.instrumentModel212, false, false, extensions);

        final HashSet<FileExtensionItem> extensions3 = newHashSet(new FileExtensionItem(".raw", ".raw", Collections.<String, AdditionalExtensionImportance>emptyMap()));
        predefinedDataCreator.instrumentModel(Data.vendor3, Data.instrumentType21, Data.studyType1, Data.instrumentModel212, true, false, extensions3);

        predefinedDataCreator.experimentType("Unspecified", false, false);

        predefinedDataCreator.species("Man", "Rat", "Horse", "Cat", "Escherichia coli", "Unspecified");

        predefinedDataCreator.allUsersGroup();

        uc = new UseCase(writeServices, readServices, adminId, instrumentCreationHelper, experimentCreationHelper);

    }

    /**
     * Clean up db after each test.
     */
    @AfterMethod
    @SuppressWarnings("unchecked")
    public void tearDown() {
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

    public long createPublicProject(long user, Long lab) {
        final long project = projectManagement.createProject(user, new ProjectInfoTemplate(lab, "public project", "", "area"));
        sharingManagement.makeProjectPublic(user, project);
        return project;
    }

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
    protected UserLabMembershipRequestRepositoryTemplate<UserLabMembershipRequestTemplate> getUserLabMemebershipRequestRepository() {
        return repos.userLabMembershipRequestRepository;
    }

    public static class ExperimentInfoTemplateBuilder<META_FACTOR extends MetaFactorTemplate,
            FILE_ITEM extends ExperimentManagementTemplate.FileItemTemplate> {
        private Long lab;
        private String name;
        private String description;
        private long project;
        private List<META_FACTOR> factors = emptyList();
        private List<FILE_ITEM> files = emptyList();
        private long specie;
        private boolean is2dLc;
        private Restriction restriction;
        private long experimentType;

        public ExperimentInfoTemplateBuilder lab(Long lab) {
            this.lab = lab;
            return this;
        }

        public ExperimentInfoTemplateBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ExperimentInfoTemplateBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ExperimentInfoTemplateBuilder project(long project) {
            this.project = project;
            return this;
        }

        public ExperimentInfoTemplateBuilder factors(List<META_FACTOR> factors) {
            this.factors = factors;
            return this;
        }

        public ExperimentInfoTemplateBuilder files(List<FILE_ITEM> files) {
            this.files = files;
            return this;
        }

        public ExperimentInfoTemplateBuilder species(long specie) {
            this.specie = specie;
            return this;
        }

        public ExperimentInfoTemplateBuilder is2Dlc(boolean is2dLc) {
            this.is2dLc = is2dLc;
            return this;
        }

        public ExperimentInfoTemplateBuilder restriction(Restriction restriction) {
            this.restriction = restriction;
            return this;
        }

        public ExperimentInfoTemplateBuilder experimentType(long experimentType) {
            this.experimentType = experimentType;
            return this;
        }

        public ExperimentManagementTemplate.ExperimentInfoTemplate<META_FACTOR, FILE_ITEM> build() {
            return new ExperimentManagementTemplate.ExperimentInfoTemplate<>(lab, name, description, project, factors, files, specie, is2dLc, restriction, experimentType);
        }
    }
}
