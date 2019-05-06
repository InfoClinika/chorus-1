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
package com.infoclinika.mssharing.search;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.PredefinedDataCreator;
import com.infoclinika.mssharing.model.Searcher;
import com.infoclinika.mssharing.model.helper.*;
import com.infoclinika.mssharing.model.internal.features.FeaturesInitializer;
import com.infoclinika.mssharing.model.read.*;
import com.infoclinika.mssharing.model.read.dto.details.FileItem;
import com.infoclinika.mssharing.model.read.dto.details.InstrumentItem;
import com.infoclinika.mssharing.model.write.*;
import com.infoclinika.mssharing.model.write.billing.BillingManagement;
import com.infoclinika.mssharing.platform.fileserver.StoredObject;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.AdditionalExtensionImportance;
import com.infoclinika.mssharing.platform.model.common.items.FileExtensionItem;
import com.infoclinika.mssharing.platform.model.helper.ExperimentCreationHelperTemplate.ExperimentTypeItem;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate.PotentialOperator;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import com.infoclinika.mssharing.platform.model.write.ExperimentManagementTemplate.Restriction;
import com.infoclinika.mssharing.platform.model.write.LabManagementTemplate;
import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate;
import com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.infoclinika.mssharing.model.helper.ExperimentSampleTypeItem.LIGHT;
import static com.infoclinika.mssharing.services.billing.rest.api.model.BillingChargeType.PER_GB;
import static com.infoclinika.mssharing.services.billing.rest.api.model.BillingFeature.*;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * @author Stanislav Kurilin
 */
@ContextConfiguration(classes = SpringConfig.class)
public class SearcherImplTest extends AbstractTestNGSpringContextTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearcherImplTest.class);
    private static final List<LockMzItem> NO_LOCK_MASSES = Collections.emptyList();
    private static final String LAB_INSTRUMENT_NAME = "fooo000";
    @Inject
    Searcher searcher;
    @Inject
    PredefinedDataCreator predefinedDataCreator;
    @Inject
    LabManagement labManagement;
    @Inject
    UserManagement userManagement;
    @Inject
    StudyManagement studyManagement;
    @Inject
    ExperimentCreationHelper experimentCreationHelper;
    @Inject
    InstrumentManagement instrumentManagement;
    @Inject
    InstrumentCreationHelperTemplate<PotentialOperator> instrumentCreationHelper;
    @Inject
    SharingManagement sharingManagement;
    @Inject
    DashboardReader dashboardReader;
    private long bob;
    private long lab;
    private Optional<Long> labInstrument = Optional.absent();
    private long kate;
    private List<LockMzItem> lockMasses = Collections.emptyList();
    @Inject
    private DetailsReader detailsReader;
    @Inject
    private BillingManagement billingManagement;
    @Inject
    private FeaturesInitializer featuresInitializer;
    private long adminId;

    //    @BeforeClass
    public void create() {
        billingManagement.createChargeableItem(450, ARCHIVE_STORAGE, 1, PER_GB);
        billingManagement.createChargeableItem(450, ANALYSE_STORAGE, 1, PER_GB);
    }

    //    @Test
    public void testSearcherAvailable() {
        assertNotNull(searcher);
    }

    //    @Test(dependsOnMethods = "testSearcherAvailable", timeOut = 3000)
    public void testSearchBecomeEnable() {
        while (true) {
            if (searcher.isSearchEnabled()) {
                return;
            }
        }
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testUserCanSearchHisProjects() {
        final long proj = studyManagement.createProject(bob, new ProjectInfo("foo bar2", anyStr(), anyStr(), lab));
        final ImmutableCollection<ProjectLine> res = searcher.projects(bob, "foo");
        assertTrue(any(res, input -> input.id == proj && input.name.equals("foo bar2")), reflectionToString(res));
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testUserCanSearchHisExperiments() {
        final long proj = studyManagement.createProject(bob, new ProjectInfo("foo bar1", anyStr(), anyStr(), lab));
        final long experiment = experiment(proj, "H study", anyStr());
        ImmutableList<ExperimentLine> experiments = searcher.experiments(bob, "study");
        assertTrue(any(experiments, input -> input.id == experiment && input.name.equals("H study")));
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testUserCanSearchHisInstruments() {
        final long instrument = labInstrument();
        ImmutableList<InstrumentLine> instruments = searcher.instruments(bob, "fooo");
        assertTrue(any(instruments, input -> input.id == instrument));
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testCountItems() {
        createInstrumentAndApproveIfNeeded(bob, lab, anyInstrumentModel(),
            new InstrumentDetails("fooo", anyStr(), anyStr(), anyStr(), lockMasses)
        ).get();
        final Searcher.Count fooo = searcher.getItemsCount(new PagedItemInfo(25, 0, "", true, "fooo"), bob);
        assertTrue(fooo.instruments == 1);
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testUserCanSearchHisFiles() {
        final Long instrument = labInstrument();
        final long file = instrumentManagement.createFile(bob, instrument,
            new FileMetaDataInfo("c15092005_009.RAW", 0, anyStr(), null, anySpecie(), false)
        );
        instrumentManagement.setContent(bob, file, mock(StoredObject.class));
        checkCanFindFileWithQuery(file, "2005");
        checkCanFindFileWithQuery(file, "raw");
        checkCanFindFileWithQuery(file, "RAW");
    }

    private Long labInstrument() {
        if (labInstrument.isPresent()) {
            return labInstrument.get();
        }
        labInstrument = createInstrumentAndApproveIfNeeded(bob, lab, anyInstrumentModel(),
            new InstrumentDetails(LAB_INSTRUMENT_NAME, anyStr(), anyStr(), anyStr(), lockMasses)
        );
        return labInstrument.get();
    }

    private void checkCanFindFileWithQuery(final long file, String query) {
        final ImmutableCollection<FileLine> res = searcher.files(bob, query);
        checkFileIsPresentInResult(file, res);
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testUserCanSearchUsingSubStringOnName() {
        final long proj = studyManagement.createProject(bob, new ProjectInfo("foo bar4", anyStr(), anyStr(), lab));
        final ImmutableCollection<ProjectLine> res = searcher.projects(bob, "oo");
        assertTrue(any(res, input -> input.id == proj), reflectionToString(res));
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testUserCanSearchUsingSubStringOnNotNameFields() {
        final long proj = studyManagement.createProject(bob, new ProjectInfo(anyStr(), "foo bar", anyStr(), lab));
        final ImmutableCollection<ProjectLine> res = searcher.projects(bob, "oo");
        assertTrue(any(res, input -> input.id == proj), reflectionToString(res));
    }

    //
    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testUserCanSearchInUpperCase() {
        final long proj = studyManagement.createProject(bob, new ProjectInfo("Foo", anyStr(), anyStr(), lab));
        final ImmutableCollection<ProjectLine> res = searcher.projects(bob, "foo");
        assertTrue(any(res, input -> input.id == proj), reflectionToString(res));
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testUserCanSearchSeveralSameEntities() {
        final long projBar = studyManagement.createProject(bob, new ProjectInfo("foo bar3", anyStr(), anyStr(), lab));
        final long projFoo = studyManagement.createProject(bob, new ProjectInfo("foo foo1", anyStr(), anyStr(), lab));
        final ImmutableCollection<ProjectLine> res = searcher.projects(bob, "foo");
        assertTrue(any(res, input -> input.id == projBar), reflectionToString(res));
        assertTrue(any(res, input -> input.id == projFoo), reflectionToString(res));
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testNotAllEntitiesGoesToResultSearch() {
        studyManagement.createProject(bob, new ProjectInfo("foo bar", anyStr(), anyStr(), lab));
        final long projFoo = studyManagement.createProject(bob, new ProjectInfo("foo foo", anyStr(), anyStr(), lab));
        final ImmutableCollection<ProjectLine> res = searcher.projects(bob, "bar");
        assertFalse(any(res, input -> input.id == projFoo), reflectionToString(res));
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testUserCanSearchByProjectDescriptions() {
        final long proj = studyManagement.createProject(bob, new ProjectInfo(anyStr(), anyStr(), "desc", lab));
        final ImmutableCollection<ProjectLine> res = searcher.projects(bob, "de");
        assertTrue(any(res, input -> input.id == proj), reflectionToString(res));
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testUserCanSearchBySeveralWords() {
        final long proj = studyManagement.createProject(bob, new ProjectInfo(anyStr(), anyStr(), "my project", lab));
        studyManagement.createProject(bob, new ProjectInfo(anyStr(), anyStr(), "shared project", lab));
        final ImmutableCollection<ProjectLine> res = searcher.projects(bob, "my project");
        assertTrue(res.stream().anyMatch(input -> input.id == proj), reflectionToString(res));
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testUserCanSearchByAreaOfResearch() {
        final long proj = studyManagement.createProject(bob, new ProjectInfo(anyStr(), "story", anyStr(), lab));
        final ImmutableCollection<ProjectLine> res = searcher.projects(bob, "story");
        assertTrue(
            res.stream().anyMatch(input -> input.id == proj && input.areaOfResearch.contains("story")),
            reflectionToString(res)
        );
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testUserCanSearchPagedUsingSpecialSymbolsInTitle() {
        final long proj = studyManagement.createProject(bob, new ProjectInfo("some-project", anyStr(), anyStr(), lab));
        final List<ProjectLine> res = searcher.pagedProjects(bob, getPageItem("some-project", false)).items;
        assertTrue(res.stream().anyMatch(input -> input.id == proj), reflectionToString(res));
    }

    private PagedItemInfo getPageItem(String query, boolean inversed) {
        return new PaginationItems.PagedItemInfo(
            10, 0, "name", !inversed, query, Optional.absent());
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testHibernateSearchPaging() {
        createTestData();
        final List<ProjectLine> res = searcher.pagedProjects(bob, getPageItem("some", false)).items;
        //Ok, true
        assertSame(res.size(), 10);
        //Now search and sort by name inversed
        final List<ProjectLine> res2 = searcher.pagedProjects(kate, getPageItem("some", true)).items;
        //Ok, true - one public project match the search expression
        assertSame(res2.size(), 1);
        //Now search and sort by name not inversed
        final List<ProjectLine> res3 = searcher.pagedProjects(kate, getPageItem("some", false)).items;

        //Assertion failed. Some confusion
        assertSame(res3.size(), 1);
        //Test searching for id
        //We can find with this ID
        boolean haveNeedRes = false;
        final List<ProjectLine> res4 = searcher.pagedProjectsWithId(bob, getPageItem("10", false)).items;
        for (ProjectLine result : res4) {
            if (result.id == 10) {
                haveNeedRes = true;
            }
        }
        assertSame(haveNeedRes, true);
        //We can't find with this ID
        haveNeedRes = false;
        final List<ProjectLine> res5 = searcher.pagedProjectsWithId(kate, getPageItem("12", true)).items;
        for (ProjectLine result : res5) {
            if (result.id == 12) {
                haveNeedRes = true;
            }
        }
        assertSame(haveNeedRes, false);
    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testSearchHisPagedInstruments() {

        final long instrument = createInstrumentAndApproveIfNeeded(bob, lab, anyInstrumentModel(),
            new InstrumentDetails("fooooo", anyStr(), anyStr(), anyStr(), lockMasses)
        ).get();
        PagedItem<InstrumentLine> instruments = searcher.pagedInstruments(bob, getPageItem("foo", false));
        assertTrue(StreamSupport.stream(instruments.spliterator(), false).anyMatch(input -> input.id == instrument));

    }

    //    @Test(dependsOnMethods = "testSearchBecomeEnable")
    public void testSearchHisPagedFiles() {

        final Long instrument = labInstrument();
        final long file = instrumentManagement.createFile(bob, instrument,
            new FileMetaDataInfo("c15092005_007.RAW", 0, anyStr(), null, anySpecie(), false)
        );

        instrumentManagement.setContent(bob, file, mock(StoredObject.class));

        checkCanFindPagedFileWithQuery(file, "2005");
        checkCanFindPagedFileWithQuery(file, "raw");
        checkCanFindPagedFileWithQuery(file, "RAW");

    }

    private void checkCanFindPagedFileWithQuery(final long file, String query) {

        final PagedItem<FileLine> res = searcher.pagedFiles(bob, getPageItem(query, false));
        checkFileIsPresentInResult(file, res);

    }

    private void checkFileIsPresentInResult(final long file, Iterable<FileLine> res) {
        assertTrue(any(res, input -> input.id == file), reflectionToString(res));
    }

    private void createTestData() {
        final long publicProject =
            studyManagement.createProject(bob, new ProjectInfo("some-public-project", anyStr(), anyStr(), lab));
        sharingManagement.makeProjectPublic(bob, publicProject);
        for (int i = 0; i < 10; i++) {
            studyManagement.createProject(bob, new ProjectInfo("some-project-" + i, anyStr(), anyStr(), lab));
        }
        //Now for kate available 1 project
        sharingManagement.makeProjectPublic(bob, publicProject);
    }

    //    @BeforeClass
    public void setUp() {

        predefinedDataCreator.instrumentModel(anyStr(), anyStr(), anyStr(), anyStr(), false,
            false,
            newHashSet(new FileExtensionItem(".raw", "", Collections.<String, AdditionalExtensionImportance>emptyMap()))
        );

        predefinedDataCreator.experimentType("Unspecified", false, false);
        predefinedDataCreator.species("Human", "Rat");
        predefinedDataCreator.allUsersGroup();
        featuresInitializer.initializeFeatures();
        adminId = predefinedDataCreator.admin(anyStr(), anyStr(), anyStr(), anyStr());
        final long chargeableItem =
            billingManagement.createChargeableItem(10, ANALYSE_STORAGE, 1, PER_GB);
        lab = labManagement.createLab(adminId, new LabManagementTemplate.LabInfoTemplate(anyStr(),
            new UserManagementTemplate.PersonInfo(anyStr(), anyStr(), anyStr()), anyStr()
        ), anyStr());
        bob = userManagement
            .createPersonAndApproveMembership(new UserManagement.PersonInfo(anyStr(), anyStr(), anyStr()), anyStr(),
                lab, null
            );
        kate = userManagement
            .createPersonAndApproveMembership(new UserManagement.PersonInfo(anyStr(), anyStr(), anyStr()), anyStr(),
                lab, null
            );
        createInstrumentAndApproveIfNeeded(
            bob, lab, anyInstrumentModel(),
            new InstrumentDetails(anyStr(), anyStr(), anyStr(), anyStr(), lockMasses)
        );
    }

    private long experiment(long proj, String name, String description) {
        final ExperimentTypeItem type = experimentCreationHelper.experimentTypes().iterator().next();
        long specie = anySpecie();
        final long file = saveFile(specie);
        final ExperimentInfo.Builder builder =
            new ExperimentInfo.Builder().name(name).description(description).experimentType(type.id).specie(specie)
                .project(proj).lab(lab).billLab(lab).is2dLc(false).restriction(restriction(bob, file))
                .factors(Collections.emptyList())
                .files(noFactoredFile(file)).bounds(new AnalysisBounds()).lockMasses(NO_LOCK_MASSES)
                .experimentLabels(new ExperimentLabelsInfo()).sampleTypesCount(1);
        return studyManagement.createExperiment(bob, builder.build());
    }

    protected String anyStr() {
        return UUID.randomUUID().toString();
    }

    protected long anyInstrumentModel() {
        return randElement(instrumentCreationHelper.models(anyVendor())).id;
    }

    private long anyVendor() {
        return randElement(instrumentCreationHelper.vendors()).id;
    }

    private long anySpecie() {
        return randElement(experimentCreationHelper.species()).id;
    }

    private <T> T randElement(Set<T> all) {
        final Random random = new Random();
        return Iterables.get(all, random.nextInt(all.size()));
    }

    public long saveFile(long species) {
        if (!labInstrument.isPresent()) {
            labInstrument = createInstrumentAndApproveIfNeeded(bob, lab, anyInstrumentModel(),
                new InstrumentDetails(anyStr(), anyStr(), anyStr(), anyStr(), lockMasses)
            );
        }
        return saveFile(labInstrument.get(), species);
    }

    public long saveFile(long instrument, long species) {
        final long file = instrumentManagement.createFile(bob, instrument,
            new FileMetaDataInfo(UUID.randomUUID().toString(), 0, "", null, species, false)
        );
        instrumentManagement.setContent(bob, file, mock(StoredObject.class));
        return file;
    }

    private ImmutableList<com.infoclinika.mssharing.model.write.FileItem> noFactoredFile(long file) {
        final ImmutableSet<ExperimentSampleItem> bioSamples =
            ImmutableSet.of(new ExperimentSampleItem(String.valueOf(file), LIGHT, newArrayList()));
        return ImmutableList.of(new com.infoclinika.mssharing.model.write.FileItem(file, false, 0,
            new ExperimentPreparedSampleItem(String.valueOf(file), bioSamples)
        ));
    }

    protected Restriction restriction(long user, long file) {
        final FileItem fileItem = detailsReader.readFile(user, file);
        final InstrumentItem instrument = detailsReader.readInstrument(user, fileItem.instrumentId);
        return new Restriction(instrument.modelId, Optional.absent());
    }

    protected long admin() {
        return adminId;
    }

    public Optional<Long> createInstrumentAndApproveIfNeeded(
        long user,
        long lab,
        long model,
        InstrumentDetails details
    ) {
        final boolean labHead = labManagement.isLabHead(user, lab);
        if (labHead) {
            return Optional.of(instrumentManagement.createInstrument(user, lab, model, details));
        } else {
            final Optional<Long> instrumentRequest =
                instrumentManagement.newInstrumentRequest(user, lab, model, details);
            final LabReaderTemplate.LabLineTemplate labLine = dashboardReader.readLab(lab);
            return Optional
                .of(instrumentManagement.approveInstrumentCreation(labLine.labHead, instrumentRequest.get()));
        }
    }
}
